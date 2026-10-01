package cn.erindax.betterclue.network;

import cn.erindax.betterclue.BetterClue;
import cn.erindax.betterclue.PlatformNetwork;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class ShareDispatcher {
	public static final double SHARE_RANGE = 5.0;
	private static final long COOLDOWN_MS = 2000L;
	private static final long SESSION_TIMEOUT_MS = 60000L;
	private static final Map<UUID, Session> SESSIONS = new HashMap<>();
	private static int nextTransferId;

	private ShareDispatcher() {
	}

	public static void receive(ServerPlayer sender, Fragment fragment) {
		long now = Util.getMillis();
		SESSIONS.values().removeIf(session -> now - session.lastActivity > SESSION_TIMEOUT_MS);
		Session session = SESSIONS.computeIfAbsent(sender.getUUID(), id -> new Session());
		session.lastActivity = now;
		if (fragment.index() == 0 && now < session.cooldownUntil) {
			session.assembler.reset();
			return;
		}
		byte[] payload = session.assembler.accept(fragment);
		if (payload == null) {
			return;
		}
		session.cooldownUntil = now + COOLDOWN_MS;
		ShareRequest request;
		try {
			request = ShareRequest.decode(payload);
		} catch (RuntimeException e) {
			BetterClue.LOGGER.debug("Rejected malformed share request from {}", sender.getGameProfile().getName(), e);
			return;
		}
		dispatch(sender, request);
	}

	private static void dispatch(ServerPlayer sender, ShareRequest request) {
		String name = sender.getGameProfile().getName();
		if (request.target() == null) {
			List<ServerPlayer> targets = sender.serverLevel().getEntitiesOfClass(
				ServerPlayer.class,
				sender.getBoundingBox().inflate(SHARE_RANGE),
				player -> player != sender && sender.distanceTo(player) <= SHARE_RANGE
			);
			deliver(targets, new ShareDelivery(false, name, request.books()));
			return;
		}
		Player target = sender.serverLevel().getPlayerByUUID(request.target());
		if (target instanceof ServerPlayer player && player != sender && sender.distanceTo(player) <= SHARE_RANGE) {
			deliver(List.of(player), new ShareDelivery(true, name, request.books().subList(0, 1)));
		}
	}

	private static void deliver(List<ServerPlayer> targets, ShareDelivery delivery) {
		List<Fragment> fragments = null;
		for (ServerPlayer target : targets) {
			if (!PlatformNetwork.canSendToPlayer(target)) {
				continue;
			}
			if (fragments == null) {
				fragments = Transfer.split(nextTransferId++, delivery.encode());
			}
			for (Fragment fragment : fragments) {
				PlatformNetwork.sendToPlayer(target, fragment);
			}
		}
	}

	private static final class Session {
		private final Transfer.Assembler assembler = new Transfer.Assembler();
		private long lastActivity;
		private long cooldownUntil;
	}
}
