package cn.erindax.betterclue.client;

import cn.erindax.betterclue.PlatformNetwork;
import cn.erindax.betterclue.client.book.Book;
import cn.erindax.betterclue.client.book.BookReader;
import cn.erindax.betterclue.client.gui.BookReadScreen;
import cn.erindax.betterclue.network.BookData;
import cn.erindax.betterclue.network.Fragment;
import cn.erindax.betterclue.network.ShareDelivery;
import cn.erindax.betterclue.network.ShareDispatcher;
import cn.erindax.betterclue.network.ShareRequest;
import cn.erindax.betterclue.network.Transfer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ShareHandler {
	private static final int RANGE = (int) ShareDispatcher.SHARE_RANGE;
	private static final int MAX_OFFERS = 8;
	private static final long COOLDOWN_MS = 3000L;
	private static final int HEADROOM = 1024;
	private static final ItemStack ICON = new ItemStack(Items.WRITTEN_BOOK);
	private static final Deque<ShareDelivery> OFFERS = new ArrayDeque<>();
	private static final Transfer.Assembler ASSEMBLER = new Transfer.Assembler();
	private static int nextTransferId;
	private static long cooldownUntil;

	private ShareHandler() {
	}

	public static void receive(Fragment fragment) {
		byte[] payload = ASSEMBLER.accept(fragment);
		if (payload == null) {
			return;
		}
		ShareDelivery delivery;
		try {
			delivery = ShareDelivery.decode(payload);
		} catch (RuntimeException e) {
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		if (delivery.open() && minecraft.screen == null) {
			BookData book = delivery.books().get(0);
			minecraft.setScreen(new BookReadScreen(book.pages(), () -> {
				CollectHandler.collectShared(List.of(book));
				minecraft.setScreen(null);
			}));
		} else if (OFFERS.size() < MAX_OFFERS) {
			OFFERS.add(delivery);
		}
	}

	public static void shareHeld() {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null || !ready()) {
			return;
		}
		BookData book = BookReader.read(player.getMainHandItem());
		if (book == null) {
			Texts.toast("betterclue.share.not_holding");
			return;
		}
		if (!(minecraft.crosshairPickEntity instanceof Player target) || target == player || player.distanceTo(target) > ShareDispatcher.SHARE_RANGE) {
			Texts.toast("betterclue.share.no_target", RANGE);
			return;
		}
		if (send(new ShareRequest(target.getUUID(), List.of(book)))) {
			Texts.toast("betterclue.share.sent_to", Texts.quoted(book.title()), target.getName());
		}
	}

	public static void shareNearby(List<Book> books) {
		if (books.isEmpty()) {
			Texts.toast("betterclue.share.empty_category");
			return;
		}
		if (!ready()) {
			return;
		}
		int nearby = nearbyPlayers();
		if (nearby == 0) {
			Texts.toast("betterclue.share.no_nearby", RANGE);
			return;
		}
		if (send(new ShareRequest(null, books.stream().map(Book::toData).toList()))) {
			Texts.toast("betterclue.share.sent_nearby", subject(books.size(), books.get(0).title()), nearby);
		}
	}

	public static void tick() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null) {
			OFFERS.clear();
			ASSEMBLER.reset();
		}
		while (BetterClueClient.SHARE_KEY.consumeClick()) {
			if (minecraft.screen == null) {
				shareHeld();
			}
		}
		while (BetterClueClient.ACCEPT_KEY.consumeClick()) {
			ShareDelivery offer = OFFERS.poll();
			if (offer != null) {
				CollectHandler.collectShared(offer.books());
			}
		}
		while (BetterClueClient.DECLINE_KEY.consumeClick()) {
			OFFERS.poll();
		}
	}

	public static void render(GuiGraphics graphics) {
		ShareDelivery offer = OFFERS.peek();
		if (offer == null) {
			return;
		}
		Font font = Minecraft.getInstance().font;
		String title = Component.translatable("betterclue.offer.title", offer.sender()).getString();
		String hint = Component.translatable(
			"betterclue.offer.hint",
			subject(offer.books().size(), offer.books().get(0).title()),
			BetterClueClient.ACCEPT_KEY.getTranslatedKeyMessage(),
			BetterClueClient.DECLINE_KEY.getTranslatedKeyMessage()
		).getString();
		int width = Math.min(280, Math.max(170, 40 + Math.max(font.width(title), font.width(hint))));
		int x = graphics.guiWidth() - width - 4;
		int y = 4;
		graphics.fill(x, y, x + width, y + 32, 0xA0101010);
		graphics.fill(x, y, x + width, y + 1, 0x80FFFFFF);
		graphics.fill(x, y + 31, x + width, y + 32, 0x80000000);
		graphics.renderFakeItem(ICON, x + 8, y + 8);
		graphics.drawString(font, font.plainSubstrByWidth(title, width - 38), x + 30, y + 7, 0xFFFF55, false);
		graphics.drawString(font, font.plainSubstrByWidth(hint, width - 38), x + 30, y + 18, 0xFFFFFF, false);
	}

	private static Component subject(int count, String firstTitle) {
		return count == 1 ? Texts.quoted(firstTitle) : Component.translatable("betterclue.share.books", count);
	}

	private static boolean ready() {
		if (!PlatformNetwork.canSendToServer()) {
			Texts.toast("betterclue.share.unsupported");
			return false;
		}
		if (Util.getMillis() < cooldownUntil) {
			Texts.toast("betterclue.share.cooldown");
			return false;
		}
		return true;
	}

	private static boolean send(ShareRequest request) {
		byte[] payload = request.encode();
		if (request.books().size() > ShareRequest.MAX_BOOKS || payload.length > Transfer.MAX_LENGTH - HEADROOM) {
			Texts.toast("betterclue.share.too_large");
			return false;
		}
		for (Fragment fragment : Transfer.split(nextTransferId++, payload)) {
			PlatformNetwork.sendToServer(fragment);
		}
		cooldownUntil = Util.getMillis() + COOLDOWN_MS;
		return true;
	}

	private static int nearbyPlayers() {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null || minecraft.level == null) {
			return 0;
		}
		double range = ShareDispatcher.SHARE_RANGE;
		return minecraft.level.getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(range), other -> other != player && player.distanceTo(other) <= range).size();
	}
}
