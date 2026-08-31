package cn.erindax.betterclue.common.network;

import cn.erindax.betterclue.BetterClue;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ShareViewC2S(UUID targetId, BookData book) implements CustomPacketPayload {
	public static final Type<ShareViewC2S> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BetterClue.MOD_ID, "share_view"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ShareViewC2S> CODEC = StreamCodec.composite(
		UUIDUtil.STREAM_CODEC, ShareViewC2S::targetId,
		BookData.STREAM_CODEC, ShareViewC2S::book,
		ShareViewC2S::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
