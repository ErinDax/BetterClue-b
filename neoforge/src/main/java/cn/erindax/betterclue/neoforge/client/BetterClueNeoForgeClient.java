package cn.erindax.betterclue.neoforge.client;

import cn.erindax.betterclue.BetterClue;
import cn.erindax.betterclue.client.BetterClueClient;
import cn.erindax.betterclue.client.ShareHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = BetterClue.MOD_ID, dist = Dist.CLIENT)
public final class BetterClueNeoForgeClient {
	public BetterClueNeoForgeClient(IEventBus modBus) {
		modBus.addListener((RegisterKeyMappingsEvent event) -> BetterClueClient.KEY_MAPPINGS.forEach(event::register));
		NeoForge.EVENT_BUS.addListener((RenderGuiEvent.Post event) -> ShareHandler.render(event.getGuiGraphics()));
		NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> ShareHandler.tick());
	}
}
