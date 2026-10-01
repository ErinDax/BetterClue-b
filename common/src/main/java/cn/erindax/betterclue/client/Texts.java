package cn.erindax.betterclue.client;

import cn.erindax.betterclue.client.gui.NoticeToast;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public final class Texts {
	private Texts() {
	}

	public static String title(String title) {
		return title.isEmpty() ? I18n.get("betterclue.book.untitled") : title;
	}

	public static Component quoted(String title) {
		return Component.translatable("betterclue.book.quoted", title(title));
	}

	public static void toast(String key, Object... args) {
		NoticeToast.show(Component.translatable("betterclue.name"), Component.translatable(key, args));
	}
}
