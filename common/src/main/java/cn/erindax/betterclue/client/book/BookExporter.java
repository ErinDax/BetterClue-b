package cn.erindax.betterclue.client.book;

import cn.erindax.betterclue.BetterClue;
import cn.erindax.betterclue.client.Texts;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;

public final class BookExporter {
	private BookExporter() {
	}

	public static String directory() {
		return gameDirectory().relativize(exportDirectory()).toString();
	}

	public static String export(Book book) {
		Path directory = exportDirectory();
		try {
			Files.createDirectories(directory);
			String base = sanitize(Texts.title(book.title()));
			Path file = directory.resolve(base + ".txt");
			for (int i = 1; Files.exists(file); i++) {
				file = directory.resolve(base + "-" + i + ".txt");
			}
			Files.writeString(file, render(book), StandardCharsets.UTF_8);
			return gameDirectory().relativize(file).toString();
		} catch (IOException e) {
			BetterClue.LOGGER.error("Failed to export book {}", book.title(), e);
			return null;
		}
	}

	private static String render(Book book) {
		StringBuilder text = new StringBuilder();
		text.append(I18n.get("betterclue.book.quoted", Texts.title(book.title()))).append('\n');
		if (!book.remark().isEmpty()) {
			text.append(I18n.get("betterclue.export.remark", book.remark())).append('\n');
		}
		if (!book.author().isEmpty()) {
			text.append(I18n.get("betterclue.export.author", book.author())).append('\n');
		}
		text.append("================\n\n");
		for (int i = 0; i < book.pages().size(); i++) {
			text.append(I18n.get("betterclue.export.page", i + 1)).append('\n').append(book.pages().get(i)).append("\n\n");
		}
		return text.toString();
	}

	private static Path gameDirectory() {
		return Minecraft.getInstance().gameDirectory.toPath();
	}

	private static Path exportDirectory() {
		return gameDirectory().resolve(BetterClue.MOD_ID).resolve("export");
	}

	private static String sanitize(String name) {
		String cleaned = name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
		return cleaned.isEmpty() ? "untitled" : cleaned;
	}
}
