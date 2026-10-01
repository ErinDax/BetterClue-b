package cn.erindax.betterclue.forge;

import cn.erindax.betterclue.BetterClue;
import cn.erindax.betterclue.forge.client.BetterClueForgeClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(BetterClue.MOD_ID)
public final class BetterClueForge {
	public BetterClueForge(FMLJavaModLoadingContext context) {
		PlatformNetworkImpl.register();
		if (FMLEnvironment.dist == Dist.CLIENT) {
			BetterClueForgeClient.init(context.getModEventBus());
		}
	}
}
