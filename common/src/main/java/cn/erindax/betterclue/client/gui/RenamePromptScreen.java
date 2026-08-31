package cn.erindax.betterclue.client.gui;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class RenamePromptScreen extends Screen {
	private final String promptTitle;
	private final String initialValue;
	private final int maxLength;
	private final Consumer<String> onConfirm;
	private final boolean allowEmpty;
	private final Screen previousScreen;
	private EditBox editBox;

	public RenamePromptScreen(String promptTitle, String initialValue, int maxLength, Consumer<String> onConfirm) {
		this(promptTitle, initialValue, maxLength, onConfirm, false);
	}

	public RenamePromptScreen(String promptTitle, String initialValue, int maxLength, Consumer<String> onConfirm, boolean allowEmpty) {
		super(Component.literal(promptTitle));
		this.promptTitle = promptTitle;
		this.initialValue = initialValue;
		this.maxLength = maxLength;
		this.onConfirm = onConfirm;
		this.allowEmpty = allowEmpty;
		this.previousScreen = Minecraft.getInstance().screen;
	}

	@Override
	protected void init() {
		int centerX = this.width / 2;
		int centerY = this.height / 2;
		this.editBox = new EditBox(this.font, centerX - 100, centerY - 24, 200, 20, Component.literal(this.promptTitle));
		this.editBox.setMaxLength(this.maxLength);
		this.editBox.setValue(this.initialValue);
		this.setInitialFocus(this.editBox);
		this.addRenderableWidget(this.editBox);
		this.addRenderableWidget(Button.builder(Component.literal("确认"), button -> this.confirm()).bounds(centerX - 100, centerY + 2, 98, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("取消"), button -> this.onClose()).bounds(centerX + 2, centerY + 2, 98, 20).build());
	}

	private void confirm() {
		String value = this.editBox.getValue().trim();
		if (this.allowEmpty || !value.isEmpty()) {
			this.onConfirm.accept(value);
		}
		this.onClose();
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			this.confirm();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		guiGraphics.drawCenteredString(this.font, this.promptTitle, this.width / 2, this.height / 2 - 44, 0xFFFFFF);
	}

	@Override
	public void onClose() {
		Minecraft.getInstance().setScreen(this.previousScreen);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
