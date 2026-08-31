package cn.erindax.betterclue.common.network;

import cn.erindax.betterclue.BetterClue;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenSharedBookS2C(String fromName, BookData book) implements CustomPacketPayload {
	public static final Type<OpenSharedBookS2C> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BetterClue.MOD_ID, "share_open"));
	public static final StreamCodec<RegistryFriendlyByteBuf, OpenSharedBookS2C> CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, OpenSharedBookS2C::fromName,
		BookData.STREAM_CODEC, OpenSharedBookS2C::book,
		OpenSharedBookS2C::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
