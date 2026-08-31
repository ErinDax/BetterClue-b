package cn.erindax.betterclue.client.book;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public record Book(String key, String title, String author, List<String> pages, long collectedAt, String remark) {

	public Book {
		key = key == null ? "" : key;
		title = title == null ? "" : title;
		author = author == null ? "" : author;
		pages = pages == null ? List.of() : List.copyOf(pages);
		remark = remark == null ? "" : remark;
	}

	public static Book fromTag(CompoundTag tag) {
		ListTag pageList = tag.getList("Pages", Tag.TAG_STRING);
		List<String> pages = new ArrayList<>(pageList.size());
		for (int i = 0; i < pageList.size(); i++) {
			pages.add(pageList.getString(i));
		}
		return new Book(
			tag.getString("Key"),
			tag.getString("Title"),
			tag.getString("Author"),
			pages,
			tag.getLong("CollectedAt"),
			tag.getString("Remark")
		);
	}

	public CompoundTag toTag() {
		CompoundTag tag = new CompoundTag();
		tag.putString("Key", this.key);
		tag.putString("Title", this.title);
		tag.putString("Author", this.author);
		tag.putLong("CollectedAt", this.collectedAt);
		if (!this.remark.isEmpty()) {
			tag.putString("Remark", this.remark);
		}
		ListTag pageList = new ListTag();
		for (String page : this.pages) {
			pageList.add(StringTag.valueOf(page));
		}
		tag.put("Pages", pageList);
		return tag;
	}

	public String displayTitle() {
		String original = this.title.isEmpty() ? "无标题" : this.title;
		if (this.remark.isEmpty()) {
			return original;
		}
		return original + "（" + this.remark + "）";
	}
}
