package cn.erindax.betterclue.common.network;

import cn.erindax.betterclue.BetterClue;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ShareNearbyC2S(BookData book) implements CustomPacketPayload {
	public static final Type<ShareNearbyC2S> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BetterClue.MOD_ID, "share_nearby"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ShareNearbyC2S> CODEC = StreamCodec.composite(
		BookData.STREAM_CODEC, ShareNearbyC2S::book, ShareNearbyC2S::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
