package cn.erindax.betterclue.fabric;

import cn.erindax.betterclue.network.Fragment;
import cn.erindax.betterclue.network.ShareDeliveryPayload;
import cn.erindax.betterclue.network.ShareRequestPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public final class PlatformNetworkImpl {
	private PlatformNetworkImpl() {
	}

	public static boolean canSendToServer() {
		return ClientPlayNetworking.canSend(ShareRequestPayload.ID);
	}

	public static void sendToServer(Fragment fragment) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		new ShareRequestPayload(fragment).write(buf);
		ClientPlayNetworking.send(ShareRequestPayload.ID, buf);
	}

	public static boolean canSendToPlayer(ServerPlayer player) {
		return ServerPlayNetworking.canSend(player, ShareDeliveryPayload.ID);
	}

	public static void sendToPlayer(ServerPlayer player, Fragment fragment) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		new ShareDeliveryPayload(fragment).write(buf);
		ServerPlayNetworking.send(player, ShareDeliveryPayload.ID, buf);
	}
}
