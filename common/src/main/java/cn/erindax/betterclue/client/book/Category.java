package cn.erindax.betterclue.client.book;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class Category {
	public String name;
	public final List<Book> books = new ArrayList<>();

	public Category(String name) {
		this.name = name;
	}

	public static Category fromTag(CompoundTag tag) {
		Category category = new Category(tag.getString("Name"));
		ListTag bookList = tag.getList("Books", Tag.TAG_COMPOUND);
		for (int i = 0; i < bookList.size(); i++) {
			category.books.add(Book.fromTag(bookList.getCompound(i)));
		}
		return category;
	}

	public CompoundTag toTag() {
		CompoundTag tag = new CompoundTag();
		tag.putString("Name", this.name);
		ListTag bookList = new ListTag();
		for (Book book : this.books) {
			bookList.add(book.toTag());
		}
		tag.put("Books", bookList);
		return tag;
	}
}
