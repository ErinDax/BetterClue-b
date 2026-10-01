package cn.erindax.betterclue.network;

import cn.erindax.betterclue.BetterClue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ShareDeliveryPayload(Fragment fragment) implements CustomPacketPayload {
	public static final Type<ShareDeliveryPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BetterClue.MOD_ID, "share_delivery"));
	public static final StreamCodec<FriendlyByteBuf, ShareDeliveryPayload> CODEC = StreamCodec.of(
		(buf, payload) -> payload.fragment.write(buf),
		buf -> new ShareDeliveryPayload(Fragment.read(buf))
	);

	@Override
	public Type<ShareDeliveryPayload> type() {
		return TYPE;
	}
}
