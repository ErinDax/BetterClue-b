package cn.erindax.betterclue.common.network;

import cn.erindax.betterclue.BetterClue;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ShareOfferS2C(String fromName, BookData book) implements CustomPacketPayload {
	public static final Type<ShareOfferS2C> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BetterClue.MOD_ID, "share_offer"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ShareOfferS2C> CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, ShareOfferS2C::fromName,
		BookData.STREAM_CODEC, ShareOfferS2C::book,
		ShareOfferS2C::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
