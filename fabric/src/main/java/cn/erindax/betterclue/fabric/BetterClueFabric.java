package cn.erindax.betterclue.fabric;

import cn.erindax.betterclue.BetterClue;
import cn.erindax.betterclue.common.network.ShareDispatcher;
import cn.erindax.betterclue.common.network.ShareNearbyC2S;
import cn.erindax.betterclue.common.network.ShareViewC2S;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class BetterClueFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		BetterClue.init();
		ServerPlayNetworking.registerGlobalReceiver(PlatformNetworkImpl.SHARE_NEARBY, (server, player, handler, buf, responseSender) -> {
			ShareNearbyC2S msg = ShareNearbyC2S.decode(buf);
			server.execute(() -> ShareDispatcher.broadcastOffer(player, msg.book()));
		});
		ServerPlayNetworking.registerGlobalReceiver(PlatformNetworkImpl.SHARE_VIEW, (server, player, handler, buf, responseSender) -> {
			ShareViewC2S msg = ShareViewC2S.decode(buf);
			server.execute(() -> ShareDispatcher.sendOpen(player, msg.targetId(), msg.book()));
		});
	}
}
