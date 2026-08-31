package cn.erindax.betterclue.client.panel;

import net.minecraft.client.gui.GuiGraphics;

public final class ScrollBar {
	private static final int WIDTH = 3;
	private static final int MIN_THUMB = 10;

	private ScrollBar() {
	}

	public record Click(int scroll, double grab) {
	}

	public static void draw(GuiGraphics graphics, int x, int y, int h, int scroll, int maxScroll, boolean hot) {
		if (maxScroll <= 0 || h <= 0) {
			return;
		}
		int thumbY = thumbTop(y, h, scroll, maxScroll);
		graphics.fill(x, y, x + WIDTH, y + h, 0xFF1E1E1E);
		graphics.fill(x, thumbY, x + WIDTH, thumbY + thumbHeight(h, maxScroll), hot ? 0xFFC8C8C8 : 0xFF8A8A8A);
	}

	public static boolean hit(int x, int y, int h, double mouseX, double mouseY) {
		return mouseX >= x - 2 && mouseX < x + WIDTH + 5 && mouseY >= y && mouseY < y + h;
	}

	public static Click click(int x, int y, int h, int scroll, int maxScroll, double mouseX, double mouseY) {
		if (maxScroll <= 0 || !hit(x, y, h, mouseX, mouseY)) {
			return null;
		}
		if (hitThumb(x, y, h, scroll, maxScroll, mouseX, mouseY)) {
			return new Click(scroll, grabOffset(y, h, scroll, maxScroll, mouseY));
		}
		double halfThumb = thumbHeight(h, maxScroll) / 2.0;
		return new Click(scrollAtGrab(y, h, maxScroll, mouseY, halfThumb), halfThumb);
	}

	public static int scrollAtGrab(int y, int h, int maxScroll, double mouseY, double grabFromThumbTop) {
		if (maxScroll <= 0 || h <= 1) {
			return 0;
		}
		int travel = h - thumbHeight(h, maxScroll);
		if (travel <= 0) {
			return 0;
		}
		double ratio = (mouseY - y - grabFromThumbTop) / travel;
		return Math.round((float) Math.max(0, Math.min(1, ratio)) * maxScroll);
	}

	private static int thumbHeight(int h, int maxScroll) {
		if (maxScroll <= 0 || h <= 0) {
			return Math.max(h, MIN_THUMB);
		}
		return Math.max(MIN_THUMB, (int) (h * ((float) h / (h + maxScroll))));
	}

	private static int thumbTop(int y, int h, int scroll, int maxScroll) {
		if (maxScroll <= 0) {
			return y;
		}
		return y + (int) ((h - thumbHeight(h, maxScroll)) * ((float) scroll / maxScroll));
	}

	private static boolean hitThumb(int x, int y, int h, int scroll, int maxScroll, double mouseX, double mouseY) {
		if (!hit(x, y, h, mouseX, mouseY) || maxScroll <= 0) {
			return false;
		}
		int thumbY = thumbTop(y, h, scroll, maxScroll);
		return mouseY >= thumbY && mouseY < thumbY + thumbHeight(h, maxScroll);
	}

	private static double grabOffset(int y, int h, int scroll, int maxScroll, double mouseY) {
		double off = mouseY - thumbTop(y, h, scroll, maxScroll);
		return Math.max(0, Math.min(thumbHeight(h, maxScroll), off));
	}
}
