package cn.erindax.betterclue.client;

import cn.erindax.betterclue.client.book.Library;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class ClientSetup {
	public static final KeyMapping SHARE_KEY = new KeyMapping(
		"key.betterclue.share",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_G,
		"key.categories.betterclue"
	);

	private ClientSetup() {
	}

	public static void init() {
		Library.get().load();
	}
}
