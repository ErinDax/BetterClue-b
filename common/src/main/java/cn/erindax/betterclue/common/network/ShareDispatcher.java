package cn.erindax.betterclue.common.network;

import cn.erindax.betterclue.PlatformNetwork;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public final class ShareDispatcher {
	public static final double SHARE_RANGE = 5.0;

	private ShareDispatcher() {
	}

	public static void broadcastOffer(ServerPlayer sender, BookData book) {
		String fromName = sender.getGameProfile().getName();
		double r = SHARE_RANGE;
		AABB box = sender.getBoundingBox().inflate(r);
		List<ServerPlayer> nearby = sender.serverLevel().getEntitiesOfClass(ServerPlayer.class, box, other -> other != sender && sender.distanceTo(other) <= r);
		for (ServerPlayer target : nearby) {
			if (PlatformNetwork.canSendToPlayer(target)) {
				PlatformNetwork.sendOffer(target, fromName, book);
			}
		}
	}

	public static void sendOpen(ServerPlayer sender, UUID targetId, BookData book) {
		Player raw = sender.serverLevel().getPlayerByUUID(targetId);
		if (!(raw instanceof ServerPlayer target) || sender.distanceTo(target) > SHARE_RANGE) {
			return;
		}
		if (PlatformNetwork.canSendToPlayer(target)) {
			PlatformNetwork.sendOpen(target, sender.getGameProfile().getName(), book);
		}
	}
}
