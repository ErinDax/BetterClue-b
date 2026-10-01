package cn.erindax.betterclue.network;

import io.netty.buffer.Unpooled;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;

public final class Transfer {
	public static final int MAX_LENGTH = 2 * 1024 * 1024;
	private static final int MAX_FRAGMENTS = (MAX_LENGTH + Fragment.MAX_DATA_LENGTH - 1) / Fragment.MAX_DATA_LENGTH;

	private Transfer() {
	}

	public static byte[] encode(Consumer<FriendlyByteBuf> writer) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		try {
			writer.accept(buf);
			byte[] bytes = new byte[buf.readableBytes()];
			buf.readBytes(bytes);
			return bytes;
		} finally {
			buf.release();
		}
	}

	public static <T> T decode(byte[] bytes, Function<FriendlyByteBuf, T> reader) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));
		try {
			T value = reader.apply(buf);
			if (buf.isReadable()) {
				throw new IllegalArgumentException("Unexpected trailing bytes: " + buf.readableBytes());
			}
			return value;
		} finally {
			buf.release();
		}
	}

	public static List<Fragment> split(int transferId, byte[] payload) {
		int count = Math.max(1, (payload.length + Fragment.MAX_DATA_LENGTH - 1) / Fragment.MAX_DATA_LENGTH);
		List<Fragment> fragments = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			int from = i * Fragment.MAX_DATA_LENGTH;
			int to = Math.min(payload.length, from + Fragment.MAX_DATA_LENGTH);
			fragments.add(new Fragment(transferId, i, count, Arrays.copyOfRange(payload, from, to)));
		}
		return fragments;
	}

	public static final class Assembler {
		private ByteArrayOutputStream buffer;
		private int transferId;
		private int count;
		private int received;

		public byte[] accept(Fragment fragment) {
			if (fragment.index() == 0) {
				this.reset();
				if (fragment.count() < 1 || fragment.count() > MAX_FRAGMENTS) {
					return null;
				}
				this.buffer = new ByteArrayOutputStream();
				this.transferId = fragment.transferId();
				this.count = fragment.count();
			} else if (this.buffer == null || fragment.transferId() != this.transferId || fragment.count() != this.count || fragment.index() != this.received) {
				this.reset();
				return null;
			}
			if (this.buffer.size() + fragment.data().length > MAX_LENGTH) {
				this.reset();
				return null;
			}
			this.buffer.writeBytes(fragment.data());
			this.received++;
			if (this.received < this.count) {
				return null;
			}
			byte[] payload = this.buffer.toByteArray();
			this.reset();
			return payload;
		}

		public void reset() {
			this.buffer = null;
			this.received = 0;
		}
	}
}
