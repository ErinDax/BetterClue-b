package cn.erindax.betterclue;

import cn.erindax.betterclue.common.network.BookData;
import dev.architectury.injectables.annotations.ExpectPlatform;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

public final class PlatformNetwork {
	private PlatformNetwork() {
	}

	@ExpectPlatform
	public static boolean canSendToServer() {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static void shareNearby(BookData book) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static void shareView(UUID targetId, BookData book) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static boolean canSendToPlayer(ServerPlayer player) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static void sendOffer(ServerPlayer player, String fromName, BookData book) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static void sendOpen(ServerPlayer player, String fromName, BookData book) {
		throw new AssertionError();
	}
}
