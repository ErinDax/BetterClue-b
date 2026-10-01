package cn.erindax.betterclue.forge.client;

import cn.erindax.betterclue.client.BetterClueClient;
import cn.erindax.betterclue.client.ShareHandler;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public final class BetterClueForgeClient {
	private BetterClueForgeClient() {
	}

	public static void init(IEventBus modBus) {
		modBus.addListener((RegisterKeyMappingsEvent event) -> BetterClueClient.KEY_MAPPINGS.forEach(event::register));
		MinecraftForge.EVENT_BUS.addListener((RenderGuiEvent.Post event) -> ShareHandler.render(event.getGuiGraphics()));
		MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
			if (event.phase == TickEvent.Phase.END) {
				ShareHandler.tick();
			}
		});
	}
}
