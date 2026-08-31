package cn.erindax.betterclue.client.mixin;

import cn.erindax.betterclue.client.panel.BookPanel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends Screen {
	protected InventoryScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void betterclue$addPanel(CallbackInfo ignored) {
		if (BookPanel.alreadyPresent(this)) {
			return;
		}
		this.addRenderableWidget(new BookPanel(this.width, this.height, 176));
	}
}
