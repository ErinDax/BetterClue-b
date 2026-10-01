package cn.erindax.betterclue.client.book;

import cn.erindax.betterclue.network.BookData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class BookReader {
	private static final ResourceLocation CANDLELIGHT_NOTE = new ResourceLocation("candlelight", "note_paper_written");

	private BookReader() {
	}

	public static BookData read(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		if (tag == null) {
			return null;
		}
		if (stack.is(Items.WRITTEN_BOOK)) {
			return fromTag(tag, "pages");
		}
		return isNote(stack) ? fromTag(tag, "text") : null;
	}

	public static boolean isNote(ItemStack stack) {
		return !stack.isEmpty() && CANDLELIGHT_NOTE.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
	}

	private static BookData fromTag(CompoundTag tag, String pagesKey) {
		ListTag list = tag.getList(pagesKey, Tag.TAG_STRING);
		List<String> pages = new ArrayList<>(list.size());
		for (int i = 0; i < list.size(); i++) {
			pages.add(plainText(list.getString(i)));
		}
		return new BookData(tag.getString("title"), tag.getString("author"), pages);
	}

	private static String plainText(String raw) {
		if (raw.isEmpty()) {
			return raw;
		}
		try {
			Component component = Component.Serializer.fromJson(raw);
			return component == null ? raw : component.getString();
		} catch (RuntimeException e) {
			return raw;
		}
	}
}
