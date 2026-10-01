package cn.erindax.betterclue.forge;

import cn.erindax.betterclue.BetterClue;
import cn.erindax.betterclue.client.ShareHandler;
import cn.erindax.betterclue.network.Fragment;
import cn.erindax.betterclue.network.ShareDeliveryPayload;
import cn.erindax.betterclue.network.ShareDispatcher;
import cn.erindax.betterclue.network.ShareRequestPayload;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class PlatformNetworkImpl {
	private static final String PROTOCOL = "2";
	private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
		ResourceLocation.fromNamespaceAndPath(BetterClue.MOD_ID, "main"),
		() -> PROTOCOL,
		NetworkRegistry.acceptMissingOr(PROTOCOL),
		NetworkRegistry.acceptMissingOr(PROTOCOL)
	);

	private PlatformNetworkImpl() {
	}

	static void register() {
		CHANNEL.registerMessage(0, ShareRequestPayload.class, ShareRequestPayload::write, ShareRequestPayload::read, (payload, context) -> {
			NetworkEvent.Context ctx = context.get();
			ServerPlayer sender = ctx.getSender();
			if (sender != null) {
				ctx.enqueueWork(() -> ShareDispatcher.receive(sender, payload.fragment()));
			}
			ctx.setPacketHandled(true);
		}, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		CHANNEL.registerMessage(1, ShareDeliveryPayload.class, ShareDeliveryPayload::write, ShareDeliveryPayload::read, (payload, context) -> {
			NetworkEvent.Context ctx = context.get();
			ctx.enqueueWork(() -> ShareHandler.receive(payload.fragment()));
			ctx.setPacketHandled(true);
		}, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
	}

	public static boolean canSendToServer() {
		ClientPacketListener connection = Minecraft.getInstance().getConnection();
		return connection != null && CHANNEL.isRemotePresent(connection.getConnection());
	}

	public static void sendToServer(Fragment fragment) {
		CHANNEL.sendToServer(new ShareRequestPayload(fragment));
	}

	public static boolean canSendToPlayer(ServerPlayer player) {
		return CHANNEL.isRemotePresent(player.connection.connection);
	}

	public static void sendToPlayer(ServerPlayer player, Fragment fragment) {
		CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ShareDeliveryPayload(fragment));
	}
}
