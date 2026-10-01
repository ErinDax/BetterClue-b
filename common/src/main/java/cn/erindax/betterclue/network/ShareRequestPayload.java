package cn.erindax.betterclue.network;

import cn.erindax.betterclue.BetterClue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record ShareRequestPayload(Fragment fragment) {
	public static final ResourceLocation ID = new ResourceLocation(BetterClue.MOD_ID, "share_request");

	public void write(FriendlyByteBuf buf) {
		this.fragment.write(buf);
	}

	public static ShareRequestPayload read(FriendlyByteBuf buf) {
		return new ShareRequestPayload(Fragment.read(buf));
	}
}
