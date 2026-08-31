package cn.erindax.betterclue.common.network;

import net.minecraft.network.FriendlyByteBuf;

public record ShareOfferS2C(String fromName, BookData book) {
	public static void encode(ShareOfferS2C msg, FriendlyByteBuf buf) {
		buf.writeUtf(msg.fromName, 16);
		BookData.write(buf, msg.book);
	}

	public static ShareOfferS2C decode(FriendlyByteBuf buf) {
		return new ShareOfferS2C(buf.readUtf(16), BookData.read(buf));
	}
}
