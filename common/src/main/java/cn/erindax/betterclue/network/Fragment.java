package cn.erindax.betterclue.network;

import net.minecraft.network.FriendlyByteBuf;

public record Fragment(int transferId, int index, int count, byte[] data) {
	public static final int MAX_DATA_LENGTH = 30000;

	public void write(FriendlyByteBuf buf) {
		buf.writeVarInt(this.transferId);
		buf.writeVarInt(this.index);
		buf.writeVarInt(this.count);
		buf.writeByteArray(this.data);
	}

	public static Fragment read(FriendlyByteBuf buf) {
		return new Fragment(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readByteArray(MAX_DATA_LENGTH));
	}
}
