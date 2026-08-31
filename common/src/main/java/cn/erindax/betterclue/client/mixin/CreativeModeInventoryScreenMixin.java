package cn.erindax.betterclue.client.mixin;

import cn.erindax.betterclue.client.panel.BookPanel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends Screen {
	protected CreativeModeInventoryScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void betterclue$addPanel(CallbackInfo ignored) {
		if (BookPanel.alreadyPresent(this)) {
			return;
		}
		this.addRenderableWidget(new BookPanel(this.width, this.height, 195));
	}

	@Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
	private void betterclue$scrollPanel(double mouseX, double mouseY, double delta, CallbackInfoReturnable<Boolean> cir) {
		BookPanel panel = BookPanel.find(this);
		if (panel != null && panel.mouseScrolled(mouseX, mouseY, delta)) {
			cir.setReturnValue(true);
		}
	}
}
