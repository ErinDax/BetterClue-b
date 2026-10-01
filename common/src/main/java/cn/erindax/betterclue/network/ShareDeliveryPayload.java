package cn.erindax.betterclue.network;

import cn.erindax.betterclue.BetterClue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record ShareDeliveryPayload(Fragment fragment) {
	public static final ResourceLocation ID = new ResourceLocation(BetterClue.MOD_ID, "share_delivery");

	public void write(FriendlyByteBuf buf) {
		this.fragment.write(buf);
	}

	public static ShareDeliveryPayload read(FriendlyByteBuf buf) {
		return new ShareDeliveryPayload(Fragment.read(buf));
	}
}
