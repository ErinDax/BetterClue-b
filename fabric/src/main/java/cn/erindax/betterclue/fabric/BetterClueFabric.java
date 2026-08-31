package cn.erindax.betterclue.fabric;

import cn.erindax.betterclue.BetterClue;
import cn.erindax.betterclue.common.network.ShareDispatcher;
import cn.erindax.betterclue.common.network.OpenSharedBookS2C;
import cn.erindax.betterclue.common.network.ShareNearbyC2S;
import cn.erindax.betterclue.common.network.ShareOfferS2C;
import cn.erindax.betterclue.common.network.ShareViewC2S;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class BetterClueFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		BetterClue.init();
		PayloadTypeRegistry.playC2S().register(ShareNearbyC2S.TYPE, ShareNearbyC2S.CODEC);
		PayloadTypeRegistry.playC2S().register(ShareViewC2S.TYPE, ShareViewC2S.CODEC);
		PayloadTypeRegistry.playS2C().register(ShareOfferS2C.TYPE, ShareOfferS2C.CODEC);
		PayloadTypeRegistry.playS2C().register(OpenSharedBookS2C.TYPE, OpenSharedBookS2C.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ShareNearbyC2S.TYPE, (payload, context) ->
			context.server().execute(() -> ShareDispatcher.broadcastOffer(context.player(), payload.book())));
		ServerPlayNetworking.registerGlobalReceiver(ShareViewC2S.TYPE, (payload, context) ->
			context.server().execute(() -> ShareDispatcher.sendOpen(context.player(), payload.targetId(), payload.book())));
	}
}
