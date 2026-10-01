package cn.erindax.betterclue.fabric;

import cn.erindax.betterclue.network.Fragment;
import cn.erindax.betterclue.network.ShareDispatcher;
import cn.erindax.betterclue.network.ShareRequestPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class BetterClueFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		ServerPlayNetworking.registerGlobalReceiver(ShareRequestPayload.ID, (server, player, handler, buf, responseSender) -> {
			Fragment fragment = ShareRequestPayload.read(buf).fragment();
			server.execute(() -> ShareDispatcher.receive(player, fragment));
		});
	}
}
