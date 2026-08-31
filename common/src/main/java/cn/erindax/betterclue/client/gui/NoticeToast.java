package cn.erindax.betterclue.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class NoticeToast implements Toast {
	private static final ItemStack BOOK_ICON = new ItemStack(Items.WRITTEN_BOOK);
	private static final long DISPLAY_MS = 4500L;
	private static final int MIN_WIDTH = 160;
	private static final int MAX_WIDTH = 240;
	private static final int HEIGHT = 32;

	private final Component title;
	private final Component message;
	private final int width;
	private long firstSeen = -1L;

	public NoticeToast(Component title, Component message) {
		this.title = title;
		this.message = message;
		Font font = Minecraft.getInstance().font;
		int textWidth = font.width(title);
		if (message != null) {
			textWidth = Math.max(textWidth, font.width(message));
		}
		this.width = Math.min(MAX_WIDTH, Math.max(MIN_WIDTH, 40 + textWidth));
	}

	@Override
	public int width() {
		return this.width;
	}

	@Override
	public int height() {
		return HEIGHT;
	}

	@Override
	public Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long timeSinceLastVisible) {
		if (this.firstSeen < 0L) {
			this.firstSeen = timeSinceLastVisible;
		}
		int w = this.width();
		int h = this.height();
		guiGraphics.fill(0, 0, w, h, 0xA0101010);
		guiGraphics.fill(0, 0, w, 1, 0x80FFFFFF);
		guiGraphics.fill(0, h - 1, w, h, 0x80000000);
		guiGraphics.renderFakeItem(BOOK_ICON, 8, 8);
		Font font = toastComponent.getMinecraft().font;
		if (this.message == null) {
			guiGraphics.drawString(font, this.title, 30, 12, 0xFFFF55, false);
		} else {
			guiGraphics.drawString(font, this.title, 30, 7, 0xFFFF55, false);
			guiGraphics.drawString(font, this.message, 30, 18, 0xFFFFFF, false);
		}
		return timeSinceLastVisible - this.firstSeen < DISPLAY_MS ? Visibility.SHOW : Visibility.HIDE;
	}
}
