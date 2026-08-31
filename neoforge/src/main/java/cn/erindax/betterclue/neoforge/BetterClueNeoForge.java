package cn.erindax.betterclue.neoforge;

import cn.erindax.betterclue.BetterClue;
import cn.erindax.betterclue.client.ShareHandler;
import cn.erindax.betterclue.common.network.ShareDispatcher;
import cn.erindax.betterclue.common.network.OpenSharedBookS2C;
import cn.erindax.betterclue.common.network.ShareNearbyC2S;
import cn.erindax.betterclue.common.network.ShareOfferS2C;
import cn.erindax.betterclue.common.network.ShareViewC2S;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(BetterClue.MOD_ID)
public final class BetterClueNeoForge {
	public BetterClueNeoForge(IEventBus modBus) {
		BetterClue.init();
		modBus.addListener((RegisterPayloadHandlersEvent event) -> {
			PayloadRegistrar registrar = event.registrar("1");
			registrar.playToServer(ShareNearbyC2S.TYPE, ShareNearbyC2S.CODEC, (payload, context) ->
				context.enqueueWork(() -> {
					if (context.player() instanceof ServerPlayer sender) {
						ShareDispatcher.broadcastOffer(sender, payload.book());
					}
				}));
			registrar.playToServer(ShareViewC2S.TYPE, ShareViewC2S.CODEC, (payload, context) ->
				context.enqueueWork(() -> {
					if (context.player() instanceof ServerPlayer sender) {
						ShareDispatcher.sendOpen(sender, payload.targetId(), payload.book());
					}
				}));
			registrar.playToClient(ShareOfferS2C.TYPE, ShareOfferS2C.CODEC, (payload, context) ->
				context.enqueueWork(() -> ShareHandler.offer(payload.fromName(), payload.book())));
			registrar.playToClient(OpenSharedBookS2C.TYPE, OpenSharedBookS2C.CODEC, (payload, context) ->
				context.enqueueWork(() -> ShareHandler.openDirect(payload.fromName(), payload.book())));
		});
	}
}
