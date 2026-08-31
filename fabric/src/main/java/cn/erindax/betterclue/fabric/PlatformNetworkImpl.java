package cn.erindax.betterclue.fabric;

import cn.erindax.betterclue.BetterClue;
import cn.erindax.betterclue.common.network.OpenSharedBookS2C;
import cn.erindax.betterclue.common.network.ShareNearbyC2S;
import cn.erindax.betterclue.common.network.ShareOfferS2C;
import cn.erindax.betterclue.common.network.ShareViewC2S;
import cn.erindax.betterclue.common.network.BookData;
import java.util.UUID;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class PlatformNetworkImpl {
	public static final ResourceLocation SHARE_NEARBY = new ResourceLocation(BetterClue.MOD_ID, "share_nearby");
	public static final ResourceLocation SHARE_VIEW = new ResourceLocation(BetterClue.MOD_ID, "share_view");
	public static final ResourceLocation SHARE_OFFER = new ResourceLocation(BetterClue.MOD_ID, "share_offer");
	public static final ResourceLocation SHARE_OPEN = new ResourceLocation(BetterClue.MOD_ID, "share_open");

	private PlatformNetworkImpl() {
	}

	public static boolean canSendToServer() {
		return ClientPlayNetworking.canSend(SHARE_NEARBY);
	}

	public static void shareNearby(BookData book) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		ShareNearbyC2S.encode(new ShareNearbyC2S(book), buf);
		ClientPlayNetworking.send(SHARE_NEARBY, buf);
	}

	public static void shareView(UUID targetId, BookData book) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		ShareViewC2S.encode(new ShareViewC2S(targetId, book), buf);
		ClientPlayNetworking.send(SHARE_VIEW, buf);
	}

	public static boolean canSendToPlayer(ServerPlayer player) {
		return ServerPlayNetworking.canSend(player, SHARE_OFFER);
	}

	public static void sendOffer(ServerPlayer player, String fromName, BookData book) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		ShareOfferS2C.encode(new ShareOfferS2C(fromName, book), buf);
		ServerPlayNetworking.send(player, SHARE_OFFER, buf);
	}

	public static void sendOpen(ServerPlayer player, String fromName, BookData book) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		OpenSharedBookS2C.encode(new OpenSharedBookS2C(fromName, book), buf);
		ServerPlayNetworking.send(player, SHARE_OPEN, buf);
	}
}
