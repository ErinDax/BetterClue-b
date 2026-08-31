package cn.erindax.betterclue.client.mixin;

import cn.erindax.betterclue.client.CollectHandler;
import net.minecraft.client.gui.screens.inventory.LecternScreen;
import net.minecraft.world.inventory.LecternMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LecternScreen.class)
public abstract class LecternScreenMixin {
	@Shadow
	@Final
	private LecternMenu menu;

	@Inject(method = "bookChanged", at = @At("HEAD"))
	private void betterclue$onLecternBookChanged(CallbackInfo ignored) {
		CollectHandler.onBookOpened(this.menu.getBook());
	}
}
