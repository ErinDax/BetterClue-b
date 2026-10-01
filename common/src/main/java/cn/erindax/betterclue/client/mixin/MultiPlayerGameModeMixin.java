package cn.erindax.betterclue.client.mixin;

import cn.erindax.betterclue.client.CollectHandler;
import cn.erindax.betterclue.client.book.BookReader;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
	@Inject(method = "useItem", at = @At("RETURN"))
	private void betterclue$collectUsedNote(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		ItemStack stack = player.getItemInHand(hand);
		if (cir.getReturnValue().consumesAction() && BookReader.isNote(stack)) {
			CollectHandler.collect(stack);
		}
	}
}
