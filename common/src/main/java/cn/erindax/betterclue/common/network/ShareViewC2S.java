package cn.erindax.betterclue.common.network;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;

public record ShareViewC2S(UUID targetId, BookData book) {
	public static void encode(ShareViewC2S msg, FriendlyByteBuf buf) {
		buf.writeUUID(msg.targetId);
		BookData.write(buf, msg.book);
	}

	public static ShareViewC2S decode(FriendlyByteBuf buf) {
		return new ShareViewC2S(buf.readUUID(), BookData.read(buf));
	}
}
