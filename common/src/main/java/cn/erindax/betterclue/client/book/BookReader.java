package cn.erindax.betterclue.client.book;

import cn.erindax.betterclue.network.BookData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WrittenBookContent;

public final class BookReader {
	private BookReader() {
	}

	public static BookData read(ItemStack stack) {
		WrittenBookContent content = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
		if (content == null) {
			return null;
		}
		return new BookData(content.title().raw(), content.author(), content.getPages(false).stream().map(Component::getString).toList());
	}
}
