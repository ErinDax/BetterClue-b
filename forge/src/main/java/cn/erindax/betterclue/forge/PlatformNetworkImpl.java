package cn.erindax.betterclue.forge;

import cn.erindax.betterclue.BetterClue;
import cn.erindax.betterclue.common.network.OpenSharedBookS2C;
import cn.erindax.betterclue.common.network.ShareNearbyC2S;
import cn.erindax.betterclue.common.network.ShareOfferS2C;
import cn.erindax.betterclue.common.network.ShareViewC2S;
import cn.erindax.betterclue.common.network.BookData;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class PlatformNetworkImpl {
	private static final String PROTOCOL = "1";

	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
		new ResourceLocation(BetterClue.MOD_ID, "main"),
		() -> PROTOCOL,
		PROTOCOL::equals,
		PROTOCOL::equals
	);

	private PlatformNetworkImpl() {
	}

	public static boolean canSendToServer() {
		ClientPacketListener connection = Minecraft.getInstance().getConnection();
		return connection != null && CHANNEL.isRemotePresent(connection.getConnection());
	}

	public static void shareNearby(BookData book) {
		CHANNEL.sendToServer(new ShareNearbyC2S(book));
	}

	public static void shareView(UUID targetId, BookData book) {
		CHANNEL.sendToServer(new ShareViewC2S(targetId, book));
	}

	public static boolean canSendToPlayer(ServerPlayer player) {
		return CHANNEL.isRemotePresent(player.connection.connection);
	}

	public static void sendOffer(ServerPlayer player, String fromName, BookData book) {
		CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ShareOfferS2C(fromName, book));
	}

	public static void sendOpen(ServerPlayer player, String fromName, BookData book) {
		CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenSharedBookS2C(fromName, book));
	}
}
