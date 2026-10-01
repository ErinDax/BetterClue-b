package cn.erindax.betterclue.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class BetterClueClient {
	private static final String CATEGORY = "key.categories.betterclue";
	public static final KeyMapping SHARE_KEY = key("share", GLFW.GLFW_KEY_G);
	public static final KeyMapping ACCEPT_KEY = key("accept", GLFW.GLFW_KEY_Y);
	public static final KeyMapping DECLINE_KEY = key("decline", GLFW.GLFW_KEY_N);
	public static final List<KeyMapping> KEY_MAPPINGS = List.of(SHARE_KEY, ACCEPT_KEY, DECLINE_KEY);

	private BetterClueClient() {
	}

	private static KeyMapping key(String name, int defaultKey) {
		return new KeyMapping("key.betterclue." + name, InputConstants.Type.KEYSYM, defaultKey, CATEGORY);
	}
}
