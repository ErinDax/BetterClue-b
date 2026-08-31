package cn.erindax.betterclue.client.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

public class BookReadScreen extends BookViewScreen {
	private final Runnable closeAction;

	public BookReadScreen(List<String> pages, Runnable closeAction) {
		super(access(pages));
		this.closeAction = closeAction;
	}

	@Override
	public void onClose() {
		this.closeAction.run();
	}

	private static BookAccess access(List<String> pages) {
		List<Component> components = new ArrayList<>();
		for (String page : pages) {
			components.add(Component.literal(page));
		}
		List<Component> safePages = components.isEmpty() ? List.of(Component.empty()) : List.copyOf(components);
		return new BookAccess() {
			@Override
			public int getPageCount() {
				return safePages.size();
			}

			@Override
			public FormattedText getPageRaw(int page) {
				return page >= 0 && page < safePages.size() ? safePages.get(page) : FormattedText.EMPTY;
			}
		};
	}
}
