package cn.erindax.betterclue.client.book;

import cn.erindax.betterclue.network.BookData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.WrittenBookContent;

public final class BookReader {
	private static final ResourceLocation CANDLELIGHT_NOTE = ResourceLocation.fromNamespaceAndPath("candlelight", "note_paper_written");

	private BookReader() {
	}

	public static BookData read(ItemStack stack) {
		WrittenBookContent content = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
		if (content != null) {
			return new BookData(content.title().raw(), content.author(), content.getPages(false).stream().map(Component::getString).toList());
		}
		return isNote(stack) ? readNote(stack) : null;
	}

	public static boolean isNote(ItemStack stack) {
		return !stack.isEmpty() && CANDLELIGHT_NOTE.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
	}

	private static BookData readNote(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		if (data == null) {
			return null;
		}
		CompoundTag tag = data.copyTag();
		ListTag list = tag.getList("text", Tag.TAG_STRING);
		List<String> pages = new ArrayList<>(list.size());
		for (int i = 0; i < list.size(); i++) {
			pages.add(plainText(list.getString(i)));
		}
		return new BookData(tag.getString("title"), tag.getString("author"), pages);
	}

	private static String plainText(String raw) {
		ClientLevel level = Minecraft.getInstance().level;
		if (raw.isEmpty() || level == null) {
			return raw;
		}
		try {
			Component component = Component.Serializer.fromJson(raw, level.registryAccess());
			return component == null ? raw : component.getString();
		} catch (RuntimeException e) {
			return raw;
		}
	}
}
