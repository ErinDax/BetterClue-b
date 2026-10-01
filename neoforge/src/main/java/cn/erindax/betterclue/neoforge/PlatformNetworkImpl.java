package cn.erindax.betterclue.neoforge;

import cn.erindax.betterclue.network.Fragment;
import cn.erindax.betterclue.network.ShareDeliveryPayload;
import cn.erindax.betterclue.network.ShareRequestPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class PlatformNetworkImpl {
	private PlatformNetworkImpl() {
	}

	public static boolean canSendToServer() {
		ClientPacketListener connection = Minecraft.getInstance().getConnection();
		return connection != null && connection.hasChannel(ShareRequestPayload.TYPE);
	}

	public static void sendToServer(Fragment fragment) {
		PacketDistributor.sendToServer(new ShareRequestPayload(fragment));
	}

	public static boolean canSendToPlayer(ServerPlayer player) {
		return player.connection.hasChannel(ShareDeliveryPayload.TYPE);
	}

	public static void sendToPlayer(ServerPlayer player, Fragment fragment) {
		PacketDistributor.sendToPlayer(player, new ShareDeliveryPayload(fragment));
	}
}
