package cn.erindax.betterclue.fabric;

import cn.erindax.betterclue.network.ShareDeliveryPayload;
import cn.erindax.betterclue.network.ShareDispatcher;
import cn.erindax.betterclue.network.ShareRequestPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class BetterClueFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playC2S().register(ShareRequestPayload.TYPE, ShareRequestPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ShareDeliveryPayload.TYPE, ShareDeliveryPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ShareRequestPayload.TYPE, (payload, context) ->
			context.server().execute(() -> ShareDispatcher.receive(context.player(), payload.fragment())));
	}
}
