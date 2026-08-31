package cn.erindax.betterclue.forge.client;

import cn.erindax.betterclue.client.ClientSetup;
import cn.erindax.betterclue.client.ShareHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class BetterClueForgeClient {
	private BetterClueForgeClient() {
	}

	public static void init() {
		ClientSetup.init();
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		modBus.addListener((RegisterKeyMappingsEvent event) -> event.register(ClientSetup.SHARE_KEY));
		MinecraftForge.EVENT_BUS.addListener((RenderGuiEvent.Post event) ->
			ShareHandler.render(event.getGuiGraphics(), Minecraft.getInstance().getWindow().getGuiScaledWidth()));
		MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
			if (event.phase == TickEvent.Phase.END) {
				ShareHandler.tick();
			}
		});
	}
}
