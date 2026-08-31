package cn.erindax.betterclue.client;

import cn.erindax.betterclue.client.book.Book;
import cn.erindax.betterclue.client.gui.BookReadScreen;
import cn.erindax.betterclue.client.gui.RenamePromptScreen;
import cn.erindax.betterclue.PlatformNetwork;
import cn.erindax.betterclue.common.network.ShareDispatcher;
import cn.erindax.betterclue.common.network.ShareNearbyC2S;
import cn.erindax.betterclue.common.network.ShareViewC2S;
import cn.erindax.betterclue.common.network.BookData;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.phys.AABB;
import org.lwjgl.glfw.GLFW;

public final class ShareHandler {
	private static final Queue<Offer> OFFERS = new ArrayDeque<>();
	private static final ItemStack BOOK_ICON = new ItemStack(Items.WRITTEN_BOOK);
	private static boolean wasY;
	private static boolean wasN;

	private ShareHandler() {
	}

	public static void offer(String fromName, BookData book) {
		OFFERS.add(new Offer(fromName, book));
	}

	public static void openDirect(String ignoredFromName, BookData book) {
		Minecraft.getInstance().setScreen(new BookReadScreen(book.pages(), () -> {
			CollectHandler.collectShared(book);
			Minecraft.getInstance().setScreen(null);
		}));
	}

	public static void shareCollected(Book book) {
		if (book == null) {
			return;
		}
		shareData(new BookData(book.title(), book.author(), book.pages()));
	}

	public static void shareCollected(List<Book> books) {
		if (books == null || books.isEmpty()) {
			CollectHandler.showNotice("该分类暂无书籍");
			return;
		}
		int nearby = shareableNearbyCount(ShareNearbyC2S.TYPE);
		if (nearby < 0) {
			return;
		}
		for (Book book : books) {
			PlatformNetwork.sendToServer(new ShareNearbyC2S(new BookData(book.title(), book.author(), book.pages())));
		}
		CollectHandler.showNotice("已向附近 " + nearby + " 名玩家分享 " + books.size() + " 本书");
	}

	private static void shareData(BookData book) {
		int nearby = shareableNearbyCount(ShareNearbyC2S.TYPE);
		if (nearby < 0) {
			return;
		}
		PlatformNetwork.sendToServer(new ShareNearbyC2S(book));
		CollectHandler.showNotice("已向附近 " + nearby + " 名玩家分享《" + book.displayTitle() + "》");
	}

	private static int shareableNearbyCount(CustomPacketPayload.Type<?> type) {
		if (!PlatformNetwork.canSendToServer(type)) {
			CollectHandler.showNotice("当前服务器无法分享");
			return -1;
		}
		int nearby = nearbyPlayerCount();
		if (nearby <= 0) {
			CollectHandler.showNotice("附近5格内没有其他玩家");
			return -1;
		}
		return nearby;
	}

	public static void shareHeldToLookedPlayer() {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null) {
			return;
		}
		if (!PlatformNetwork.canSendToServer(ShareViewC2S.TYPE)) {
			CollectHandler.showNotice("当前服务器无法分享");
			return;
		}
		BookData book = fromHeldBook(player.getMainHandItem());
		if (book == null) {
			CollectHandler.showNotice("请手持成书后再分享");
			return;
		}
		Entity hit = minecraft.crosshairPickEntity;
		if (!(hit instanceof Player target) || target.getUUID().equals(player.getUUID()) || player.distanceTo(target) > ShareDispatcher.SHARE_RANGE) {
			CollectHandler.showNotice("请准星对准5格内的玩家");
			return;
		}
		PlatformNetwork.sendToServer(new ShareViewC2S(target.getUUID(), book));
		CollectHandler.showNotice("已将《" + book.displayTitle() + "》分享给 " + target.getGameProfile().getName());
	}

	private static BookData fromHeldBook(ItemStack stack) {
		WrittenBookContent content = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
		if (content == null) {
			return null;
		}
		List<String> pages = content.getPages(false).stream().map(Component::getString).toList();
		return new BookData(content.title().raw(), content.author(), pages);
	}

	private static int nearbyPlayerCount() {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null || minecraft.level == null) {
			return 0;
		}
		double r = ShareDispatcher.SHARE_RANGE;
		AABB box = player.getBoundingBox().inflate(r);
		return minecraft.level.getEntitiesOfClass(Player.class, box, other -> other != player && player.distanceTo(other) <= r).size();
	}

	public static void render(GuiGraphics graphics, int screenWidth) {
		Offer offer = OFFERS.peek();
		if (offer == null) {
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		String title = offer.fromName + "想向你分享书籍";
		String message = "《" + offer.book.displayTitle() + "》  Y同意  N拒绝";
		int width = Math.min(280, Math.max(170, 40 + Math.max(font.width(title), font.width(message))));
		int x = screenWidth - width - 4;
		int y = 4;
		graphics.fill(x, y, x + width, y + 32, 0xA0101010);
		graphics.fill(x, y, x + width, y + 1, 0x80FFFFFF);
		graphics.fill(x, y + 31, x + width, y + 32, 0x80000000);
		graphics.renderFakeItem(BOOK_ICON, x + 8, y + 8);
		graphics.drawString(font, font.plainSubstrByWidth(title, width - 38), x + 30, y + 7, 0xFFFF55, false);
		graphics.drawString(font, font.plainSubstrByWidth(message, width - 38), x + 30, y + 18, 0xFFFFFF, false);
	}

	public static void tick() {
		Minecraft minecraft = Minecraft.getInstance();
		long window = minecraft.getWindow().getWindow();
		boolean y = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_Y);
		boolean n = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_N);
		if (!OFFERS.isEmpty() && canUseAnswerKeys(minecraft.screen)) {
			if (y && !wasY) {
				accept();
			} else if (n && !wasN) {
				OFFERS.poll();
			}
		}
		wasY = y;
		wasN = n;
		while (ClientSetup.SHARE_KEY.consumeClick()) {
			if (minecraft.screen == null) {
				shareHeldToLookedPlayer();
			}
		}
	}

	private static boolean canUseAnswerKeys(Screen screen) {
		if (screen instanceof ChatScreen || screen instanceof RenamePromptScreen) {
			return false;
		}
		return screen == null || !(screen.getFocused() instanceof EditBox);
	}

	private static void accept() {
		Offer offer = OFFERS.poll();
		if (offer == null) {
			return;
		}
		CollectHandler.collectShared(offer.book);
	}

	private record Offer(String fromName, BookData book) {
	}
}
