package cn.erindax.betterclue.fabric;

import cn.erindax.betterclue.network.Fragment;
import cn.erindax.betterclue.network.ShareDeliveryPayload;
import cn.erindax.betterclue.network.ShareRequestPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class PlatformNetworkImpl {
	private PlatformNetworkImpl() {
	}

	public static boolean canSendToServer() {
		return ClientPlayNetworking.canSend(ShareRequestPayload.TYPE);
	}

	public static void sendToServer(Fragment fragment) {
		ClientPlayNetworking.send(new ShareRequestPayload(fragment));
	}

	public static boolean canSendToPlayer(ServerPlayer player) {
		return ServerPlayNetworking.canSend(player, ShareDeliveryPayload.TYPE);
	}

	public static void sendToPlayer(ServerPlayer player, Fragment fragment) {
		ServerPlayNetworking.send(player, new ShareDeliveryPayload(fragment));
	}
}
