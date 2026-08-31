package cn.erindax.betterclue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BetterClue {
	public static final String MOD_ID = "betterclue";
	public static final String DISPLAY_NAME = "更好的线索";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private BetterClue() {
	}

	public static void init() {
		LOGGER.info("{} 已加载", DISPLAY_NAME);
	}
}
