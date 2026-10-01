package cn.erindax.betterclue.network;

import java.util.List;

public record ShareDelivery(boolean open, String sender, List<BookData> books) {
	private static final int MAX_SENDER_LENGTH = 64;

	public ShareDelivery {
		sender = sender.length() > MAX_SENDER_LENGTH ? sender.substring(0, MAX_SENDER_LENGTH) : sender;
		books = List.copyOf(books);
	}

	public byte[] encode() {
		return Transfer.encode(buf -> {
			buf.writeBoolean(this.open);
			buf.writeUtf(this.sender, MAX_SENDER_LENGTH);
			BookData.writeList(buf, this.books);
		});
	}

	public static ShareDelivery decode(byte[] bytes) {
		return Transfer.decode(bytes, buf -> new ShareDelivery(buf.readBoolean(), buf.readUtf(MAX_SENDER_LENGTH), BookData.readList(buf, ShareRequest.MAX_BOOKS)));
	}
}
