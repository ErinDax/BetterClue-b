package cn.erindax.betterclue.forge;

import cn.erindax.betterclue.BetterClue;
import cn.erindax.betterclue.client.ShareHandler;
import cn.erindax.betterclue.forge.client.BetterClueForgeClient;
import cn.erindax.betterclue.common.network.ShareDispatcher;
import cn.erindax.betterclue.common.network.OpenSharedBookS2C;
import cn.erindax.betterclue.common.network.ShareNearbyC2S;
import cn.erindax.betterclue.common.network.ShareOfferS2C;
import cn.erindax.betterclue.common.network.ShareViewC2S;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod(BetterClue.MOD_ID)
public final class BetterClueForge {
	public BetterClueForge() {
		BetterClue.init();
		SimpleChannel channel = PlatformNetworkImpl.CHANNEL;
		int id = 0;
		channel.registerMessage(id++, ShareNearbyC2S.class, ShareNearbyC2S::encode, ShareNearbyC2S::decode, (msg, ctx) -> {
			ctx.get().enqueueWork(() -> {
				ServerPlayer sender = ctx.get().getSender();
				if (sender != null) {
					ShareDispatcher.broadcastOffer(sender, msg.book());
				}
			});
			ctx.get().setPacketHandled(true);
		});
		channel.registerMessage(id++, ShareViewC2S.class, ShareViewC2S::encode, ShareViewC2S::decode, (msg, ctx) -> {
			ctx.get().enqueueWork(() -> {
				ServerPlayer sender = ctx.get().getSender();
				if (sender != null) {
					ShareDispatcher.sendOpen(sender, msg.targetId(), msg.book());
				}
			});
			ctx.get().setPacketHandled(true);
		});
		channel.registerMessage(id++, ShareOfferS2C.class, ShareOfferS2C::encode, ShareOfferS2C::decode, (msg, ctx) -> {
			ctx.get().enqueueWork(() -> ShareHandler.offer(msg.fromName(), msg.book()));
			ctx.get().setPacketHandled(true);
		});
		channel.registerMessage(id, OpenSharedBookS2C.class, OpenSharedBookS2C::encode, OpenSharedBookS2C::decode, (msg, ctx) -> {
			ctx.get().enqueueWork(() -> ShareHandler.openDirect(msg.fromName(), msg.book()));
			ctx.get().setPacketHandled(true);
		});
		if (FMLEnvironment.dist == Dist.CLIENT) {
			BetterClueForgeClient.init();
		}
	}
}
