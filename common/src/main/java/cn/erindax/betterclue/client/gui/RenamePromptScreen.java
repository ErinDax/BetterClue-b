package cn.erindax.betterclue.client.gui;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class RenamePromptScreen extends Screen {
	private final String initialValue;
	private final int maxLength;
	private final boolean allowEmpty;
	private final Consumer<String> onConfirm;
	private final Screen previous;
	private EditBox editBox;

	public RenamePromptScreen(Component title, String initialValue, int maxLength, boolean allowEmpty, Consumer<String> onConfirm) {
		super(title);
		this.initialValue = initialValue;
		this.maxLength = maxLength;
		this.allowEmpty = allowEmpty;
		this.onConfirm = onConfirm;
		this.previous = Minecraft.getInstance().screen;
	}

	@Override
	protected void init() {
		int centerX = this.width / 2;
		int centerY = this.height / 2;
		this.editBox = new EditBox(this.font, centerX - 100, centerY - 24, 200, 20, this.title);
		this.editBox.setMaxLength(this.maxLength);
		this.editBox.setValue(this.initialValue);
		this.addRenderableWidget(this.editBox);
		this.setInitialFocus(this.editBox);
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.confirm()).bounds(centerX - 100, centerY + 2, 98, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).bounds(centerX + 2, centerY + 2, 98, 20).build());
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
		this.renderBackground(guiGraphics);
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 44, 0xFFFFFF);
	}

	@Override
	public void onClose() {
		Minecraft.getInstance().setScreen(this.previous);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void confirm() {
		String value = this.editBox.getValue().trim();
		if (this.allowEmpty || !value.isEmpty()) {
			this.onConfirm.accept(value);
		}
		this.onClose();
	}
}
