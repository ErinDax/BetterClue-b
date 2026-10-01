package cn.erindax.betterclue.network;

import cn.erindax.betterclue.BetterClue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ShareRequestPayload(Fragment fragment) implements CustomPacketPayload {
	public static final Type<ShareRequestPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BetterClue.MOD_ID, "share_request"));
	public static final StreamCodec<FriendlyByteBuf, ShareRequestPayload> CODEC = StreamCodec.of(
		(buf, payload) -> payload.fragment.write(buf),
		buf -> new ShareRequestPayload(Fragment.read(buf))
	);

	@Override
	public Type<ShareRequestPayload> type() {
		return TYPE;
	}
}
