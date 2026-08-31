package cn.erindax.betterclue.client;

import cn.erindax.betterclue.BetterClue;
import cn.erindax.betterclue.client.book.Library;
import cn.erindax.betterclue.client.book.Book;
import cn.erindax.betterclue.client.gui.NoticeToast;
import cn.erindax.betterclue.common.network.BookData;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;

public final class CollectHandler {
	private static final Logger LOGGER = BetterClue.LOGGER;

	private CollectHandler() {
	}

	public static void openPlayerInventory() {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		if (player == null) {
			minecraft.setScreen(null);
			return;
		}
		minecraft.setScreen(createPlayerInventoryScreen(player));
	}

	private static Screen createPlayerInventoryScreen(Player player) {
		Minecraft minecraft = Minecraft.getInstance();
		if (player.isCreative() && player instanceof LocalPlayer local) {
			return new CreativeModeInventoryScreen(
				local,
				local.connection.enabledFeatures(),
				minecraft.options.operatorItemsTab().get()
			);
		}
		return new InventoryScreen(player);
	}

	public static void onBookOpened(ItemStack stack) {
		if (!stack.is(Items.WRITTEN_BOOK)) {
			return;
		}
		CompoundTag tag = stack.getTag();
		if (tag == null) {
			return;
		}
		String title = tag.getString("title");
		String author = tag.getString("author");
		List<String> pages = new ArrayList<>();
		ListTag pageList = tag.getList("pages", Tag.TAG_STRING);
		for (int i = 0; i < pageList.size(); i++) {
			pages.add(pageToPlainText(pageList.getString(i)));
		}
		notifyAddResult(Library.get().addBook(title, author, pages), title);
	}

	public static void collectShared(BookData book) {
		Library.AddResult result = Library.get().addBook(book.title(), book.author(), book.pages());
		if (result == Library.AddResult.ALREADY_COLLECTED) {
			return;
		}
		notifyAddResult(result, book.title());
	}

	public static String pageToPlainText(String raw) {
		if (raw == null || raw.isEmpty()) {
			return "";
		}
		try {
			Component component = Component.Serializer.fromJson(raw);
			return component == null ? raw : component.getString();
		} catch (Exception ignored) {
			return raw;
		}
	}

	private static void notifyAddResult(Library.AddResult result, String title) {
		String displayTitle = title.isEmpty() ? "无标题" : title;
		if (result == Library.AddResult.ADDED) {
			showNotice("已收录书籍", "《" + displayTitle + "》");
		} else if (result == Library.AddResult.CATEGORY_FULL) {
			showNotice("无法收录", "书籍已满（单分类上限 " + Library.MAX_BOOKS_PER_CATEGORY + " 本）");
		} else if (result == Library.AddResult.NO_CATEGORY) {
			showNotice("无法收录", "没有任何可用分类");
		}
	}

	private static void showNotice(String title, String message) {
		Minecraft.getInstance().getToasts().addToast(new NoticeToast(
			Component.literal(title),
			message.isEmpty() ? null : Component.literal(message)
		));
	}

	public static void showNotice(String message) {
		showNotice(BetterClue.DISPLAY_NAME, message);
	}

	public static String exportBook(Book book) {
		Path gameDir = Minecraft.getInstance().gameDirectory.toPath();
		Path dir = gameDir.resolve(BetterClue.MOD_ID).resolve("export");
		try {
			Files.createDirectories(dir);
			String base = sanitizeFileName(book.title().isEmpty() ? "无标题" : book.title());
			Path file = dir.resolve(base + ".txt");
			int suffix = 1;
			while (Files.exists(file)) {
				file = dir.resolve(base + "-" + suffix + ".txt");
				suffix++;
			}
			StringBuilder sb = new StringBuilder();
			sb.append("《").append(book.title()).append("》\n");
			if (!book.remark().isEmpty()) {
				sb.append("备注：").append(book.remark()).append('\n');
			}
			if (!book.author().isEmpty()) {
				sb.append("作者：").append(book.author()).append('\n');
			}
			sb.append("================\n\n");
			for (int i = 0; i < book.pages().size(); i++) {
				sb.append("【第 ").append(i + 1).append(" 页】\n");
				sb.append(book.pages().get(i)).append("\n\n");
			}
			Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
			return gameDir.relativize(file).toString();
		} catch (IOException e) {
			LOGGER.warn("导出书籍失败: {}", e.toString());
			return null;
		}
	}

	private static String sanitizeFileName(String name) {
		String cleaned = name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
		return cleaned.isEmpty() ? "untitled" : cleaned;
	}
}
