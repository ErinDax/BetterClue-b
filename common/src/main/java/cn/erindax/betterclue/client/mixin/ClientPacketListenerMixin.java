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
	@Inject(method = "handleOpenBook", at = @At("HEAD"))
	private void betterclue$onOpenBook(ClientboundOpenBookPacket packet, CallbackInfo ignored) {
		Player player = Minecraft.getInstance().player;
		if (player != null) {
			CollectHandler.onBookOpened(player.getItemInHand(packet.getHand()));
		}
	}
}
