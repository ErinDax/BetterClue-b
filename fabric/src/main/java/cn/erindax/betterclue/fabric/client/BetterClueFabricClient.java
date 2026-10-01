package cn.erindax.betterclue.fabric.client;

import cn.erindax.betterclue.client.BetterClueClient;
import cn.erindax.betterclue.client.ShareHandler;
import cn.erindax.betterclue.network.Fragment;
import cn.erindax.betterclue.network.ShareDeliveryPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public final class BetterClueFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BetterClueClient.KEY_MAPPINGS.forEach(KeyBindingHelper::registerKeyBinding);
		ClientPlayNetworking.registerGlobalReceiver(ShareDeliveryPayload.ID, (client, handler, buf, responseSender) -> {
			Fragment fragment = ShareDeliveryPayload.read(buf).fragment();
			client.execute(() -> ShareHandler.receive(fragment));
		});
		HudRenderCallback.EVENT.register((graphics, tickDelta) -> ShareHandler.render(graphics));
		ClientTickEvents.END_CLIENT_TICK.register(client -> ShareHandler.tick());
	}
}
