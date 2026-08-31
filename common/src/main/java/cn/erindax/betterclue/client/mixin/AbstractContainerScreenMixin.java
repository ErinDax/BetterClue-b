package cn.erindax.betterclue.client.mixin;

import cn.erindax.betterclue.client.panel.BookPanel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen {
	protected AbstractContainerScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
	private void betterclue$dragPanel(double mouseX, double mouseY, int button, double dragX, double dragY, CallbackInfoReturnable<Boolean> cir) {
		BookPanel panel = BookPanel.find(this);
		if (panel != null && panel.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
	private void betterclue$releasePanel(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		BookPanel panel = BookPanel.find(this);
		if (panel != null && panel.mouseReleased(mouseX, mouseY, button)) {
			cir.setReturnValue(true);
		}
	}
}
