package cn.erindax.betterclue.client.mixin;

import cn.erindax.betterclue.client.CollectHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.LecternMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
	@Inject(method = "handleOpenBook", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V", shift = At.Shift.AFTER))
	private void betterclue$collectOpenedBook(ClientboundOpenBookPacket packet, CallbackInfo ci) {
		Player player = Minecraft.getInstance().player;
		if (player != null) {
			CollectHandler.collect(player.getItemInHand(packet.getHand()));
		}
	}

	@Inject(method = "handleContainerContent", at = @At("RETURN"))
	private void betterclue$collectLecternContent(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
		betterclue$collectLecternBook();
	}

	@Inject(method = "handleContainerSetSlot", at = @At("RETURN"))
	private void betterclue$collectLecternSlot(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
		betterclue$collectLecternBook();
	}

	private static void betterclue$collectLecternBook() {
		Player player = Minecraft.getInstance().player;
		if (player != null && player.containerMenu instanceof LecternMenu menu) {
			CollectHandler.collect(menu.getBook());
		}
	}
}
