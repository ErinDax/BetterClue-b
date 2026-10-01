package cn.erindax.betterclue.client;

import cn.erindax.betterclue.client.book.BookReader;
import cn.erindax.betterclue.client.book.Library;
import cn.erindax.betterclue.client.gui.NoticeToast;
import cn.erindax.betterclue.network.BookData;
import java.util.Collections;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class CollectHandler {
	private CollectHandler() {
	}

	public static void collect(ItemStack stack) {
		BookData book = BookReader.read(stack);
		if (book != null) {
			report(Library.get().addBook(book), book);
		}
	}

	public static void collectShared(List<BookData> books) {
		List<Library.AddResult> results = Library.get().addBooks(books);
		if (results.size() == 1) {
			report(results.get(0), books.get(0));
			return;
		}
		int added = Collections.frequency(results, Library.AddResult.ADDED);
		if (added > 0) {
			NoticeToast.show(Component.translatable("betterclue.toast.collected"), Component.translatable("betterclue.toast.collected_count", added));
		}
		if (results.contains(Library.AddResult.CATEGORY_FULL)) {
			reportFailure(Library.AddResult.CATEGORY_FULL);
		} else if (results.contains(Library.AddResult.NO_CATEGORY)) {
			reportFailure(Library.AddResult.NO_CATEGORY);
		}
	}

	private static void report(Library.AddResult result, BookData book) {
		if (result == Library.AddResult.ADDED) {
			NoticeToast.show(Component.translatable("betterclue.toast.collected"), Texts.quoted(book.title()));
		} else {
			reportFailure(result);
		}
	}

	private static void reportFailure(Library.AddResult result) {
		Component title = Component.translatable("betterclue.toast.collect_failed");
		if (result == Library.AddResult.CATEGORY_FULL) {
			NoticeToast.show(title, Component.translatable("betterclue.toast.category_full", Library.MAX_BOOKS_PER_CATEGORY));
		} else if (result == Library.AddResult.NO_CATEGORY) {
			NoticeToast.show(title, Component.translatable("betterclue.toast.no_category"));
		}
	}
}
