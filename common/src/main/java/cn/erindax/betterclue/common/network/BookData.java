package cn.erindax.betterclue.common.network;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record BookData(String title, String author, List<String> pages) {
	public static final int MAX_TITLE = 32;
	public static final int MAX_AUTHOR = 16;
	public static final int MAX_PAGES = 100;
	public static final int MAX_PAGE_LEN = 1024;

	public static final StreamCodec<RegistryFriendlyByteBuf, BookData> STREAM_CODEC = StreamCodec.of(
		(buf, data) -> write(buf, data),
		BookData::read
	);

	public BookData {
		title = clip(title, MAX_TITLE);
		author = clip(author, MAX_AUTHOR);
		List<String> clipped = new ArrayList<>();
		if (pages != null) {
			int n = Math.min(pages.size(), MAX_PAGES);
			for (int i = 0; i < n; i++) {
				clipped.add(clip(pages.get(i), MAX_PAGE_LEN));
			}
		}
		pages = List.copyOf(clipped);
	}

	public String displayTitle() {
		return this.title.isEmpty() ? "无标题" : this.title;
	}

	private static void write(FriendlyByteBuf buf, BookData data) {
		buf.writeUtf(data.title, MAX_TITLE);
		buf.writeUtf(data.author, MAX_AUTHOR);
		buf.writeVarInt(data.pages.size());
		for (String page : data.pages) {
			buf.writeUtf(page, MAX_PAGE_LEN);
		}
	}

	private static BookData read(FriendlyByteBuf buf) {
		String title = buf.readUtf(MAX_TITLE);
		String author = buf.readUtf(MAX_AUTHOR);
		int count = Math.min(buf.readVarInt(), MAX_PAGES);
		List<String> pages = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			pages.add(buf.readUtf(MAX_PAGE_LEN));
		}
		return new BookData(title, author, pages);
	}

	private static String clip(String value, int max) {
		if (value == null) {
			return "";
		}
		return value.length() <= max ? value : value.substring(0, max);
	}
}
