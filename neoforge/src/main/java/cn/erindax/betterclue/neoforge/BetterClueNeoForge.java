package cn.erindax.betterclue.neoforge;

import cn.erindax.betterclue.BetterClue;
import cn.erindax.betterclue.client.ShareHandler;
import cn.erindax.betterclue.network.ShareDeliveryPayload;
import cn.erindax.betterclue.network.ShareDispatcher;
import cn.erindax.betterclue.network.ShareRequestPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;

@Mod(BetterClue.MOD_ID)
public final class BetterClueNeoForge {
	private static final String PROTOCOL = "2";

	public BetterClueNeoForge(IEventBus modBus) {
		modBus.addListener(BetterClueNeoForge::registerPayloads);
	}

	private static void registerPayloads(RegisterPayloadHandlersEvent event) {
		event.registrar(PROTOCOL)
			.optional()
			.executesOn(HandlerThread.MAIN)
			.playToServer(ShareRequestPayload.TYPE, ShareRequestPayload.CODEC, (payload, context) -> {
				if (context.player() instanceof ServerPlayer player) {
					ShareDispatcher.receive(player, payload.fragment());
				}
			})
			.playToClient(ShareDeliveryPayload.TYPE, ShareDeliveryPayload.CODEC, (payload, context) -> ShareHandler.receive(payload.fragment()));
	}
}
