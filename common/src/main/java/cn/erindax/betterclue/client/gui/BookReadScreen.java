package cn.erindax.betterclue.client.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.Component;

public class BookReadScreen extends BookViewScreen {
	private final Runnable closeAction;

	public BookReadScreen(List<String> pages, Runnable closeAction) {
		super(new BookAccess(toComponents(pages)));
		this.closeAction = closeAction;
	}

	@Override
	public void onClose() {
		this.closeAction.run();
	}

	private static List<Component> toComponents(List<String> pages) {
		List<Component> components = new ArrayList<>();
		for (String page : pages) {
			components.add(Component.literal(page));
		}
		return components.isEmpty() ? List.of(Component.empty()) : components;
	}
}
