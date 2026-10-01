package cn.erindax.betterclue.network;

import java.util.List;
import java.util.UUID;

public record ShareRequest(UUID target, List<BookData> books) {
	public static final int MAX_BOOKS = 64;

	public ShareRequest {
		books = List.copyOf(books);
	}

	public byte[] encode() {
		return Transfer.encode(buf -> {
			buf.writeBoolean(this.target != null);
			if (this.target != null) {
				buf.writeUUID(this.target);
			}
			BookData.writeList(buf, this.books);
		});
	}

	public static ShareRequest decode(byte[] bytes) {
		return Transfer.decode(bytes, buf -> new ShareRequest(buf.readBoolean() ? buf.readUUID() : null, BookData.readList(buf, MAX_BOOKS)));
	}
}
