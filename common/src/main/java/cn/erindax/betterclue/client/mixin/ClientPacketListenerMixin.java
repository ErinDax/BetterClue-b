package cn.erindax.betterclue.client.mixin;

import cn.erindax.betterclue.client.CollectHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
	@Inject(method = "handleOpenBook", at = @At("RETURN"))
	private void betterclue$collectOpenedBook(ClientboundOpenBookPacket packet, CallbackInfo ci) {
		Player player = Minecraft.getInstance().player;
		if (player != null) {
			CollectHandler.collect(player.getItemInHand(packet.getHand()));
		}
	}
}
