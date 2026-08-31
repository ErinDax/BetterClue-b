package cn.erindax.betterclue.common.network;

import net.minecraft.network.FriendlyByteBuf;

public record ShareNearbyC2S(BookData book) {
	public static void encode(ShareNearbyC2S msg, FriendlyByteBuf buf) {
		BookData.write(buf, msg.book);
	}

	public static ShareNearbyC2S decode(FriendlyByteBuf buf) {
		return new ShareNearbyC2S(BookData.read(buf));
	}
}
