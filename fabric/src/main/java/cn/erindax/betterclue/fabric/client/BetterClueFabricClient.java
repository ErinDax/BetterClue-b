package cn.erindax.betterclue.fabric.client;

import cn.erindax.betterclue.BetterClue;
import cn.erindax.betterclue.client.BetterClueClient;
import cn.erindax.betterclue.client.ShareHandler;
import cn.erindax.betterclue.network.ShareDeliveryPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.resources.ResourceLocation;

public final class BetterClueFabricClient implements ClientModInitializer {
	private static final ResourceLocation OFFER_LAYER = ResourceLocation.fromNamespaceAndPath(BetterClue.MOD_ID, "share_offer");

	@Override
	public void onInitializeClient() {
		BetterClueClient.KEY_MAPPINGS.forEach(KeyBindingHelper::registerKeyBinding);
		ClientPlayNetworking.registerGlobalReceiver(ShareDeliveryPayload.TYPE, (payload, context) ->
			context.client().execute(() -> ShareHandler.receive(payload.fragment())));
		HudLayerRegistrationCallback.EVENT.register(layers ->
			layers.addLayer(IdentifiedLayer.of(OFFER_LAYER, (graphics, deltaTracker) -> ShareHandler.render(graphics))));
		ClientTickEvents.END_CLIENT_TICK.register(client -> ShareHandler.tick());
	}
}
