package cn.erindax.betterclue.fabric.client;

import cn.erindax.betterclue.client.ClientSetup;
import cn.erindax.betterclue.client.ShareHandler;
import cn.erindax.betterclue.fabric.PlatformNetworkImpl;
import cn.erindax.betterclue.common.network.OpenSharedBookS2C;
import cn.erindax.betterclue.common.network.ShareOfferS2C;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;

public final class BetterClueFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientSetup.init();
		KeyBindingHelper.registerKeyBinding(ClientSetup.SHARE_KEY);
		ClientPlayNetworking.registerGlobalReceiver(PlatformNetworkImpl.SHARE_OFFER, (client, handler, buf, responseSender) -> {
			ShareOfferS2C msg = ShareOfferS2C.decode(buf);
			client.execute(() -> ShareHandler.offer(msg.fromName(), msg.book()));
		});
		ClientPlayNetworking.registerGlobalReceiver(PlatformNetworkImpl.SHARE_OPEN, (client, handler, buf, responseSender) -> {
			OpenSharedBookS2C msg = OpenSharedBookS2C.decode(buf);
			client.execute(() -> ShareHandler.openDirect(msg.fromName(), msg.book()));
		});
		HudRenderCallback.EVENT.register((graphics, tickDelta) ->
			ShareHandler.render(graphics, Minecraft.getInstance().getWindow().getGuiScaledWidth()));
		ClientTickEvents.END_CLIENT_TICK.register(client -> ShareHandler.tick());
	}
}
