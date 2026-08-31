package cn.erindax.betterclue.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class NoticeToast implements Toast {
	private static final ResourceLocation BACKGROUND = ResourceLocation.withDefaultNamespace("toast/recipe");
	private static final ItemStack BOOK_ICON = new ItemStack(Items.WRITTEN_BOOK);
	private static final long DISPLAY_MS = 4500L;
	private static final int MIN_WIDTH = 160;
	private static final int MAX_WIDTH = 240;
	private static final int HEIGHT = 32;

	private final Component title;
	private final Component message;
	private final int width;

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
		guiGraphics.blitSprite(BACKGROUND, 0, 0, this.width(), this.height());
		guiGraphics.renderFakeItem(BOOK_ICON, 8, 8);
		Font font = toastComponent.getMinecraft().font;
		if (this.message == null) {
			guiGraphics.drawString(font, this.title, 30, 12, 0x500050, false);
		} else {
			guiGraphics.drawString(font, this.title, 30, 7, 0x500050, false);
			guiGraphics.drawString(font, this.message, 30, 18, 0x000000, false);
		}
		return timeSinceLastVisible >= DISPLAY_MS * toastComponent.getNotificationDisplayTimeMultiplier() ? Visibility.HIDE : Visibility.SHOW;
	}
}
