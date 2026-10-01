package cn.erindax.betterclue.network;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;

public record BookData(String title, String author, List<String> pages) {
	public static final int MAX_TITLE_LENGTH = 32;
	public static final int MAX_AUTHOR_LENGTH = 16;
	public static final int MAX_PAGES = 100;
	public static final int MAX_PAGE_LENGTH = 1024;

	public BookData {
		title = title == null ? "" : title;
		author = author == null ? "" : author;
		pages = pages == null ? List.of() : List.copyOf(pages);
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeUtf(clip(this.title, MAX_TITLE_LENGTH), MAX_TITLE_LENGTH);
		buf.writeUtf(clip(this.author, MAX_AUTHOR_LENGTH), MAX_AUTHOR_LENGTH);
		int count = Math.min(this.pages.size(), MAX_PAGES);
		buf.writeVarInt(count);
		for (int i = 0; i < count; i++) {
			buf.writeUtf(clip(this.pages.get(i), MAX_PAGE_LENGTH), MAX_PAGE_LENGTH);
		}
	}

	public static BookData read(FriendlyByteBuf buf) {
		String title = buf.readUtf(MAX_TITLE_LENGTH);
		String author = buf.readUtf(MAX_AUTHOR_LENGTH);
		int count = buf.readVarInt();
		if (count < 0 || count > MAX_PAGES) {
			throw new IllegalArgumentException("Invalid page count: " + count);
		}
		List<String> pages = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			pages.add(buf.readUtf(MAX_PAGE_LENGTH));
		}
		return new BookData(title, author, pages);
	}

	public static void writeList(FriendlyByteBuf buf, List<BookData> books) {
		buf.writeVarInt(books.size());
		for (BookData book : books) {
			book.write(buf);
		}
	}

	public static List<BookData> readList(FriendlyByteBuf buf, int maxBooks) {
		int count = buf.readVarInt();
		if (count < 1 || count > maxBooks) {
			throw new IllegalArgumentException("Invalid book count: " + count);
		}
		List<BookData> books = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			books.add(read(buf));
		}
		return books;
	}

	private static String clip(String value, int max) {
		if (value.length() <= max) {
			return value;
		}
		return value.substring(0, Character.isHighSurrogate(value.charAt(max - 1)) ? max - 1 : max);
	}
}
