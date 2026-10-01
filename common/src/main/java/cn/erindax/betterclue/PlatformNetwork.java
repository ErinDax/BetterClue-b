package cn.erindax.betterclue;

import cn.erindax.betterclue.network.Fragment;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.server.level.ServerPlayer;

public final class PlatformNetwork {
	private PlatformNetwork() {
	}

	@ExpectPlatform
	public static boolean canSendToServer() {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static void sendToServer(Fragment fragment) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static boolean canSendToPlayer(ServerPlayer player) {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static void sendToPlayer(ServerPlayer player, Fragment fragment) {
		throw new AssertionError();
	}
}
