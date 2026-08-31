package cn.erindax.betterclue.common.network;

import net.minecraft.network.FriendlyByteBuf;

public record OpenSharedBookS2C(String fromName, BookData book) {
	public static void encode(OpenSharedBookS2C msg, FriendlyByteBuf buf) {
		buf.writeUtf(msg.fromName, 16);
		BookData.write(buf, msg.book);
	}

	public static OpenSharedBookS2C decode(FriendlyByteBuf buf) {
		return new OpenSharedBookS2C(buf.readUtf(16), BookData.read(buf));
	}
}
