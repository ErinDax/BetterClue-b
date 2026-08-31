package cn.erindax.betterclue.client.panel;

import cn.erindax.betterclue.BetterClue;
import cn.erindax.betterclue.client.book.Category;
import cn.erindax.betterclue.client.book.Library;
import cn.erindax.betterclue.client.book.Book;
import cn.erindax.betterclue.client.CollectHandler;
import cn.erindax.betterclue.client.ShareHandler;
import cn.erindax.betterclue.client.gui.BookReadScreen;
import cn.erindax.betterclue.client.gui.RenamePromptScreen;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class BookPanel extends AbstractWidget {
	private static final int COLLAPSED_STRIP_WIDTH = 16;
	private static final int COLLAPSED_STRIP_HEIGHT = 60;
	private static final int HEADER_HEIGHT = 18;
	private static final int CAT_ROW_HEIGHT = 13;
	private static final int BOOK_ROW_HEIGHT = 14;
	private static final int NEW_CAT_HEIGHT = 14;
	private static final int BOOKS_HEADER_HEIGHT = 12;
	private static final int PANEL_EDGE = 3;
	private static final int ACTION_WIDTH = 12;
	private static final int CAT_SHARE_X = 45;
	private static final int CAT_RENAME_X = 32;
	private static final int CAT_DELETE_X = 19;
	private static final int BOOK_SHARE_X = 69;
	private static final int BOOK_RENAME_X = 56;
	private static final int BOOK_EXPORT_X = 43;
	private static final int BOOK_DELETE_X = 30;

	private final int panelHeight;
	private final int catAreaHeight;
	private final Font font;
	private boolean expanded = true;
	private int selectedCategoryIndex = -1;
	private int categoryScroll = 0;
	private int bookScroll = 0;

	private boolean pressArmed = false;
	private boolean dragging = false;
	private DragKind dragKind;
	private int dragSourceIndex;
	private double dragStartY;
	private double dragCurrentY;
	private boolean barDragging = false;
	private boolean barOnCategories = false;
	private double barGrab;

	private enum DragKind { CATEGORY, BOOK }

	public BookPanel(int screenWidth, int screenHeight, int containerWidth) {
		super(0, 0, computePanelWidth(screenWidth, containerWidth), screenHeight, Component.literal(BetterClue.DISPLAY_NAME));
		this.font = Minecraft.getInstance().font;
		this.panelHeight = screenHeight;
		int available = this.panelHeight - HEADER_HEIGHT - NEW_CAT_HEIGHT - BOOKS_HEADER_HEIGHT - PANEL_EDGE - 6;
		this.catAreaHeight = Math.max(18, (int) (available * 0.34));
		this.selectedCategoryIndex = Library.get().preferredCategoryIndex();
		this.ensureSelectedCategoryVisible();
	}

	public static boolean alreadyPresent(Screen screen) {
		return find(screen) != null;
	}

	public static BookPanel find(Screen screen) {
		for (GuiEventListener child : screen.children()) {
			if (child instanceof BookPanel panel) {
				return panel;
			}
		}
		return null;
	}

	private static int computePanelWidth(int screenWidth, int containerWidth) {
		int leftPos = (screenWidth - containerWidth) / 2;
		int maxWidth = leftPos - 10;
		return Math.max(64, Math.min(150, maxWidth));
	}

	private int catAreaTop() {
		return this.getY() + HEADER_HEIGHT;
	}

	private int newCatTop() {
		return this.catAreaTop() + this.catAreaHeight + 2;
	}

	private int booksHeaderTop() {
		return this.newCatTop() + NEW_CAT_HEIGHT + 4;
	}

	private int booksAreaTop() {
		return this.booksHeaderTop() + BOOKS_HEADER_HEIGHT;
	}

	private int booksAreaBottom() {
		return this.getY() + this.panelHeight - PANEL_EDGE;
	}

	private boolean inCollapsedStrip(double mx, double my) {
		int stripY = this.getY() + this.panelHeight / 2 - COLLAPSED_STRIP_HEIGHT / 2;
		return mx >= 0 && mx < COLLAPSED_STRIP_WIDTH && my >= stripY && my < stripY + COLLAPSED_STRIP_HEIGHT;
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.expanded ? super.isMouseOver(mouseX, mouseY) : this.inCollapsedStrip(mouseX, mouseY);
	}

	@Override
	protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float ignored) {
		if (!this.expanded) {
			this.renderCollapsed(guiGraphics, mouseX, mouseY);
		} else {
			this.renderExpanded(guiGraphics, mouseX, mouseY);
		}
	}

	private void renderCollapsed(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		int stripY = this.getY() + this.panelHeight / 2 - COLLAPSED_STRIP_HEIGHT / 2;
		boolean hover = this.inCollapsedStrip(mouseX, mouseY);
		guiGraphics.fill(0, stripY, COLLAPSED_STRIP_WIDTH, stripY + COLLAPSED_STRIP_HEIGHT, hover ? 0xE0303030 : 0xC0181818);
		guiGraphics.hLine(0, COLLAPSED_STRIP_WIDTH - 1, stripY, 0xFF666666);
		guiGraphics.hLine(0, COLLAPSED_STRIP_WIDTH - 1, stripY + COLLAPSED_STRIP_HEIGHT - 1, 0xFF666666);
		guiGraphics.drawCenteredString(this.font, "»", COLLAPSED_STRIP_WIDTH / 2, stripY + COLLAPSED_STRIP_HEIGHT / 2 - 4, 0xFFFFFF);
	}

	private void renderExpanded(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		List<Category> categories = Library.get().categories();
		this.clampSelection(categories);
		int x = this.getX();
		int w = this.getWidth();

		guiGraphics.fill(x, this.getY(), x + w, this.getY() + this.panelHeight, 0xC8121212);
		guiGraphics.vLine(x + w - 1, this.getY(), this.getY() + this.panelHeight - 1, 0xFF3A3A3A);
		guiGraphics.hLine(x, x + w - 1, this.getY() + this.panelHeight - 1, 0xFF3A3A3A);

		boolean collapseHover = inRect(mouseX, mouseY, x + w - 14, this.getY() + 2, 12, HEADER_HEIGHT - 2);
		guiGraphics.drawString(this.font, "—", x + w - 11, this.getY() + 3, collapseHover ? 0xFFFFFF : 0xAAAAAA);

		int catTop = this.catAreaTop();
		this.categoryScroll = Math.max(0, Math.min(this.categoryScroll, this.categoryMaxScrollPx(categories.size())));
		guiGraphics.enableScissor(x + 1, catTop, x + w - 6, catTop + this.catAreaHeight);
		for (int i = 0; i < categories.size(); i++) {
			int rowY = catTop + i * CAT_ROW_HEIGHT - this.categoryScroll;
			if (rowY + CAT_ROW_HEIGHT < catTop || rowY > catTop + this.catAreaHeight) {
				continue;
			}
			boolean selected = i == this.selectedCategoryIndex;
			boolean hover = inRect(mouseX, mouseY, x + 1, rowY, w - 8, CAT_ROW_HEIGHT);
			if (selected) {
				guiGraphics.fill(x + 1, rowY, x + w - 6, rowY + CAT_ROW_HEIGHT, 0x804A6A8A);
			} else if (hover) {
				guiGraphics.fill(x + 1, rowY, x + w - 6, rowY + CAT_ROW_HEIGHT, 0x40333333);
			}
			Category category = categories.get(i);
			String label = category.name + " (" + category.books.size() + ")";
			guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(label, w - 69), x + 4, rowY + 2, selected ? 0xFFFFFF : 0xD0D0D0);
			if (hover) {
				this.renderCategoryActions(guiGraphics, rowY, mouseX, mouseY);
			}
		}
		guiGraphics.disableScissor();
		int catMaxPx = this.categoryMaxScrollPx(categories.size());
		int catBarX = this.scrollBarX();
		boolean catBarHot = this.barDragging && this.barOnCategories || ScrollBar.hit(catBarX, catTop, this.catAreaHeight, mouseX, mouseY);
		ScrollBar.draw(guiGraphics, catBarX, catTop, this.catAreaHeight, this.categoryScroll, catMaxPx, catBarHot);

		int ncTop = this.newCatTop();
		boolean ncHover = inRect(mouseX, mouseY, x + 2, ncTop, w - 4, NEW_CAT_HEIGHT);
		guiGraphics.fill(x + 2, ncTop, x + w - 2, ncTop + NEW_CAT_HEIGHT, ncHover ? 0x603A5A3A : 0x40202020);
		guiGraphics.drawString(this.font, "＋ 新建分类", x + 6, ncTop + 3, ncHover ? 0x90FF90 : 0xA0C0A0);

		int bhTop = this.booksHeaderTop();
		guiGraphics.drawString(this.font, this.bookHeaderLabel(), x + 4, bhTop + 2, 0xFFFFFF);
		boolean exportAllHover = inRect(mouseX, mouseY, x + w - 48, bhTop, 46, BOOKS_HEADER_HEIGHT);
		guiGraphics.drawString(this.font, "全导出", x + w - 44, bhTop + 2, exportAllHover ? 0xFFFFFF : 0xAAAAAA);

		List<Book> books = this.currentBooks();
		int bTop = this.booksAreaTop();
		int bBottom = this.booksAreaBottom();
		int bookAreaHeight = bBottom - bTop;
		this.bookScroll = Math.max(0, Math.min(this.bookScroll, this.bookMaxScrollPx(books.size())));
		guiGraphics.enableScissor(x + 1, bTop, x + w - 6, bBottom);
		if (books.isEmpty()) {
			guiGraphics.drawString(this.font, this.selectedCategoryIndex < 0 ? "当前无分类" : "该分类暂无书籍", x + 6, bTop + 4, 0x808080);
		}
		Book hoveredBook = null;
		for (int i = 0; i < books.size(); i++) {
			int rowY = bTop + i * BOOK_ROW_HEIGHT - this.bookScroll;
			if (rowY + BOOK_ROW_HEIGHT < bTop || rowY > bBottom) {
				continue;
			}
			boolean hover = inRect(mouseX, mouseY, x + 1, rowY, w - 8, BOOK_ROW_HEIGHT);
			if (hover) {
				guiGraphics.fill(x + 1, rowY, x + w - 6, rowY + BOOK_ROW_HEIGHT, 0x40333333);
			}
			Book book = books.get(i);
			guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(book.displayTitle(), w - 77), x + 4, rowY + 3, hover ? 0xFFFFFF : 0xD0D0D0);
			if (hover) {
				this.renderBookActions(guiGraphics, rowY, mouseX, mouseY);
				if (!this.dragging && mouseX < this.actionX(BOOK_SHARE_X)) {
					hoveredBook = book;
				}
			}
		}
		guiGraphics.disableScissor();
		int bookMaxPx = this.bookMaxScrollPx(books.size());
		int bookBarX = this.scrollBarX();
		boolean bookBarHot = this.barDragging && !this.barOnCategories || ScrollBar.hit(bookBarX, bTop, bookAreaHeight, mouseX, mouseY);
		ScrollBar.draw(guiGraphics, bookBarX, bTop, bookAreaHeight, this.bookScroll, bookMaxPx, bookBarHot);
		if (hoveredBook != null) {
			this.renderBookTooltip(guiGraphics, hoveredBook, mouseX, mouseY);
		}

		if (this.dragging) {
			int ghostHeight = this.dragKind == DragKind.BOOK ? BOOK_ROW_HEIGHT : CAT_ROW_HEIGHT;
			int ghostY = (int) this.dragCurrentY - ghostHeight / 2;
			guiGraphics.fill(x + 1, ghostY, x + w - 1, ghostY + ghostHeight, 0xA0FFFFFF);
			guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(this.dragGhostLabel(), w - 10), x + 4, ghostY + 2, 0x202020);
		}
	}

	private void renderCategoryActions(GuiGraphics guiGraphics, int rowY, int mouseX, int mouseY) {
		this.drawSmallAction(guiGraphics, "享", rowY, CAT_SHARE_X, mouseX, mouseY);
		this.drawSmallAction(guiGraphics, "改", rowY, CAT_RENAME_X, mouseX, mouseY);
		this.drawSmallAction(guiGraphics, "删", rowY, CAT_DELETE_X, mouseX, mouseY);
	}

	private void renderBookTooltip(GuiGraphics guiGraphics, Book book, int mouseX, int mouseY) {
		List<Component> lines = new ArrayList<>();
		lines.add(Component.literal(book.title().isEmpty() ? "无标题" : book.title()));
		if (!book.remark().isEmpty()) {
			lines.add(Component.literal("备注：" + book.remark()).withStyle(ChatFormatting.GRAY));
		}
		guiGraphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
	}

	private void renderBookActions(GuiGraphics guiGraphics, int rowY, int mouseX, int mouseY) {
		this.drawSmallAction(guiGraphics, "享", rowY, BOOK_SHARE_X, mouseX, mouseY);
		this.drawSmallAction(guiGraphics, "改", rowY, BOOK_RENAME_X, mouseX, mouseY);
		this.drawSmallAction(guiGraphics, "导", rowY, BOOK_EXPORT_X, mouseX, mouseY);
		this.drawSmallAction(guiGraphics, "删", rowY, BOOK_DELETE_X, mouseX, mouseY);
	}

	private void drawSmallAction(GuiGraphics guiGraphics, String text, int rowY, int fromRight, int mouseX, int mouseY) {
		int rectX = this.actionX(fromRight);
		boolean hover = inRect(mouseX, mouseY, rectX, rowY, ACTION_WIDTH, CAT_ROW_HEIGHT);
		guiGraphics.drawString(this.font, text, rectX + 2, rowY + 2, hover ? 0xFFFFFF : 0x999999);
	}

	private int actionX(int fromRight) {
		return this.getX() + this.getWidth() - fromRight;
	}

	private boolean hitAction(double mouseX, int fromRight) {
		int rectX = this.actionX(fromRight);
		return mouseX >= rectX && mouseX < rectX + ACTION_WIDTH;
	}

	private String bookHeaderLabel() {
		Category category = this.selectedCategoryIndex < 0 ? null : Library.get().category(this.selectedCategoryIndex);
		if (category == null) {
			return "请选择分类";
		}
		return category.name + " 书籍 " + category.books.size() + "/" + Library.MAX_BOOKS_PER_CATEGORY;
	}

	private String dragGhostLabel() {
		if (this.dragKind == DragKind.CATEGORY) {
			Category category = Library.get().category(this.dragSourceIndex);
			return category == null ? "移动分类" : "移动: " + category.name;
		}
		Book book = this.currentBookAt(this.dragSourceIndex);
		return "移动: " + (book == null ? "无标题" : book.displayTitle());
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!this.active || !this.visible || button != 0) {
			return false;
		}
		if (!this.expanded) {
			if (this.inCollapsedStrip(mouseX, mouseY)) {
				this.expanded = true;
				return true;
			}
			return false;
		}
		int x = this.getX();
		int w = this.getWidth();
		List<Category> categories = Library.get().categories();
		this.clampSelection(categories);

		if (inRect(mouseX, mouseY, x + w - 14, this.getY() + 2, 12, HEADER_HEIGHT - 2)) {
			this.expanded = false;
			return true;
		}

		if (this.clickScrollBar(mouseX, mouseY)) {
			return true;
		}

		int catTop = this.catAreaTop();
		for (int i = 0; i < categories.size(); i++) {
			int rowY = catTop + i * CAT_ROW_HEIGHT - this.categoryScroll;
			if (rowY + CAT_ROW_HEIGHT < catTop || rowY > catTop + this.catAreaHeight) {
				continue;
			}
			if (mouseX >= x && mouseX < x + w - 6 && mouseY >= rowY && mouseY < rowY + CAT_ROW_HEIGHT) {
				if (this.hitAction(mouseX, CAT_SHARE_X)) {
					ShareHandler.shareCollected(categories.get(i).books);
					return true;
				}
				if (this.hitAction(mouseX, CAT_RENAME_X)) {
					this.requestRenameCategory(i);
					return true;
				}
				if (this.hitAction(mouseX, CAT_DELETE_X)) {
					Library.get().deleteCategory(i);
					return true;
				}
				this.selectedCategoryIndex = i;
				Library.get().setPreferredCategoryName(categories.get(i).name);
				this.armPress(DragKind.CATEGORY, i, mouseY);
				return true;
			}
		}

		int ncTop = this.newCatTop();
		if (mouseX >= x && mouseX < x + w && mouseY >= ncTop && mouseY < ncTop + NEW_CAT_HEIGHT) {
			this.openCreateCategory();
			return true;
		}

		int bhTop = this.booksHeaderTop();
		if (mouseX >= x + w - 48 && mouseX < x + w && mouseY >= bhTop && mouseY < bhTop + BOOKS_HEADER_HEIGHT) {
			this.exportAllBooks();
			return true;
		}

		List<Book> books = this.currentBooks();
		int bTop = this.booksAreaTop();
		int bBottom = this.booksAreaBottom();
		for (int i = 0; i < books.size(); i++) {
			int rowY = bTop + i * BOOK_ROW_HEIGHT - this.bookScroll;
			if (rowY + BOOK_ROW_HEIGHT < bTop || rowY > bBottom) {
				continue;
			}
			if (mouseX >= x && mouseX < x + w - 6 && mouseY >= rowY && mouseY < rowY + BOOK_ROW_HEIGHT) {
				if (this.hitAction(mouseX, BOOK_SHARE_X)) {
					ShareHandler.shareCollected(books.get(i));
					return true;
				}
				if (this.hitAction(mouseX, BOOK_RENAME_X)) {
					this.requestRenameBook(i);
					return true;
				}
				if (this.hitAction(mouseX, BOOK_EXPORT_X)) {
					this.exportBook(i);
					return true;
				}
				if (this.hitAction(mouseX, BOOK_DELETE_X)) {
					Library.get().deleteBook(this.selectedCategoryIndex, i);
					return true;
				}
				this.armPress(DragKind.BOOK, i, mouseY);
				return true;
			}
		}

		return mouseX >= x && mouseX < x + w && mouseY >= this.getY() && mouseY < this.getY() + this.panelHeight;
	}

	@Override
	public boolean mouseDragged(double ignoredX, double mouseY, int button, double ignoredDeltaX, double ignoredDeltaY) {
		if (button != 0) {
			return false;
		}
		if (this.barDragging) {
			if (this.barOnCategories) {
				this.categoryScroll = ScrollBar.scrollAtGrab(
					this.catAreaTop(), this.catAreaHeight, this.categoryMaxScrollPx(Library.get().categories().size()), mouseY, this.barGrab);
			} else {
				int bTop = this.booksAreaTop();
				int bookH = this.booksAreaBottom() - bTop;
				this.bookScroll = ScrollBar.scrollAtGrab(bTop, bookH, this.bookMaxScrollPx(this.currentBooks().size()), mouseY, this.barGrab);
			}
			return true;
		}
		if (this.pressArmed && !this.dragging && Math.abs(mouseY - this.dragStartY) > 4) {
			this.dragging = true;
		}
		if (this.dragging) {
			this.dragCurrentY = mouseY;
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button != 0) {
			return false;
		}
		if (this.barDragging) {
			this.barDragging = false;
			return true;
		}
		if (this.pressArmed || this.dragging) {
			boolean wasDragging = this.dragging;
			DragKind kind = this.dragKind;
			int index = this.dragSourceIndex;
			this.pressArmed = false;
			this.dragging = false;
			this.dragKind = null;
			if (wasDragging) {
				this.performDrop(mouseX, mouseY, kind, index);
			} else if (kind == DragKind.BOOK) {
				this.openBookAt(index);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double deltaAmount) {
		if (!this.expanded || !this.isMouseOver(mouseX, mouseY)) {
			return false;
		}
		int delta = (int) Math.round(-deltaAmount);
		if (delta == 0) {
			delta = deltaAmount > 0 ? -1 : 1;
		}
		int catTop = this.catAreaTop();
		if (mouseY >= catTop && mouseY < catTop + this.catAreaHeight) {
			int max = this.categoryMaxScrollPx(Library.get().categories().size());
			this.categoryScroll = Math.max(0, Math.min(max, this.categoryScroll + delta * CAT_ROW_HEIGHT));
			return true;
		}
		if (mouseY >= this.booksAreaTop() && mouseY < this.booksAreaBottom()) {
			int max = this.bookMaxScrollPx(this.currentBooks().size());
			this.bookScroll = Math.max(0, Math.min(max, this.bookScroll + delta * BOOK_ROW_HEIGHT));
			return true;
		}
		return false;
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		narrationElementOutput.add(NarratedElementType.TITLE, Component.literal(BetterClue.DISPLAY_NAME));
	}

	private void armPress(DragKind kind, int index, double mouseY) {
		this.pressArmed = true;
		this.dragging = false;
		this.dragKind = kind;
		this.dragSourceIndex = index;
		this.dragStartY = mouseY;
		this.dragCurrentY = mouseY;
	}

	private void performDrop(double mouseX, double mouseY, DragKind kind, int sourceIndex) {
		Library collector = Library.get();
		int x = this.getX();
		if (mouseX < x || mouseX >= x + this.getWidth()) {
			return;
		}
		int targetCategory = this.categoryIndexAt(mouseY);
		if (kind == DragKind.CATEGORY) {
			if (targetCategory >= 0 && targetCategory != sourceIndex) {
				collector.moveCategory(sourceIndex, targetCategory);
			}
			return;
		}
		if (targetCategory >= 0 && targetCategory != this.selectedCategoryIndex) {
			collector.moveBookToCategory(this.selectedCategoryIndex, sourceIndex, targetCategory);
		} else {
			int targetRow = this.bookIndexAt(mouseY);
			if (targetRow >= 0 && targetRow != sourceIndex) {
				collector.moveBook(this.selectedCategoryIndex, sourceIndex, targetRow);
			}
		}
	}

	private int categoryIndexAt(double mouseY) {
		List<Category> categories = Library.get().categories();
		int catTop = this.catAreaTop();
		if (mouseY < catTop || mouseY >= catTop + this.catAreaHeight || categories.isEmpty()) {
			return -1;
		}
		int index = (int) Math.floor((mouseY - catTop + this.categoryScroll) / (double) CAT_ROW_HEIGHT);
		return Math.max(0, Math.min(categories.size() - 1, index));
	}

	private int bookIndexAt(double mouseY) {
		List<Book> books = this.currentBooks();
		if (books.isEmpty() || mouseY < this.booksAreaTop() || mouseY >= this.booksAreaBottom()) {
			return -1;
		}
		int index = (int) Math.floor((mouseY - this.booksAreaTop() + this.bookScroll) / (double) BOOK_ROW_HEIGHT);
		return Math.max(0, Math.min(books.size() - 1, index));
	}

	private List<Book> currentBooks() {
		if (this.selectedCategoryIndex < 0) {
			return List.of();
		}
		Category category = Library.get().category(this.selectedCategoryIndex);
		return category == null ? List.of() : category.books;
	}

	private Book currentBookAt(int index) {
		List<Book> books = this.currentBooks();
		return index >= 0 && index < books.size() ? books.get(index) : null;
	}

	private void clampSelection(List<Category> categories) {
		if (categories.isEmpty()) {
			this.selectedCategoryIndex = -1;
			return;
		}
		if (this.selectedCategoryIndex < 0 || this.selectedCategoryIndex >= categories.size()) {
			this.selectedCategoryIndex = Library.get().preferredCategoryIndex();
			if (this.selectedCategoryIndex < 0) {
				this.selectedCategoryIndex = 0;
			}
			this.ensureSelectedCategoryVisible();
		}
	}

	private void ensureSelectedCategoryVisible() {
		if (this.selectedCategoryIndex < 0) {
			return;
		}
		int rowTop = this.selectedCategoryIndex * CAT_ROW_HEIGHT;
		int rowBottom = rowTop + CAT_ROW_HEIGHT;
		if (rowTop < this.categoryScroll) {
			this.categoryScroll = rowTop;
		} else if (rowBottom > this.categoryScroll + this.catAreaHeight) {
			this.categoryScroll = Math.max(0, rowBottom - this.catAreaHeight);
		}
	}

	private void openBookAt(int index) {
		Book book = this.currentBookAt(index);
		if (book == null) {
			return;
		}
		Minecraft.getInstance().setScreen(new BookReadScreen(book.pages(), CollectHandler::openPlayerInventory));
	}

	private void requestRenameCategory(int index) {
		Category category = Library.get().category(index);
		if (category == null) {
			return;
		}
		Minecraft.getInstance().setScreen(new RenamePromptScreen(
			"重命名分类", category.name, Library.MAX_CATEGORY_NAME_LENGTH,
			newName -> {
				if (!Library.get().renameCategory(index, newName)) {
					CollectHandler.showNotice("重命名失败：名称无效或重复");
				}
			}
		));
	}

	private void openCreateCategory() {
		Minecraft.getInstance().setScreen(new RenamePromptScreen(
			"新建分类", "", Library.MAX_CATEGORY_NAME_LENGTH,
			newName -> {
				if (!Library.get().createCategory(newName)) {
					CollectHandler.showNotice("创建失败：名称无效、重复或已达上限(" + Library.MAX_CATEGORIES + ")");
				}
			}
		));
	}

	private void requestRenameBook(int index) {
		Book book = this.currentBookAt(index);
		if (book == null) {
			return;
		}
		Minecraft.getInstance().setScreen(new RenamePromptScreen(
			"书籍备注", book.remark(), 64,
			remark -> Library.get().renameBook(this.selectedCategoryIndex, index, remark),
			true
		));
	}

	private void exportBook(int index) {
		Book book = this.currentBookAt(index);
		if (book == null) {
			return;
		}
		String path = CollectHandler.exportBook(book);
		CollectHandler.showNotice(path == null ? "导出失败" : "已导出: " + path);
	}

	private void exportAllBooks() {
		List<Book> books = this.currentBooks();
		if (books.isEmpty()) {
			CollectHandler.showNotice("当前分类没有可导出的书");
			return;
		}
		int ok = 0;
		for (Book book : books) {
			if (CollectHandler.exportBook(book) != null) {
				ok++;
			}
		}
		CollectHandler.showNotice("已导出 " + ok + " 本书到 betterclue/export/");
	}

	private int scrollBarX() {
		return this.getX() + this.getWidth() - 5;
	}

	private int categoryMaxScrollPx(int count) {
		return Math.max(0, count * CAT_ROW_HEIGHT - this.catAreaHeight);
	}

	private int bookMaxScrollPx(int count) {
		int height = this.booksAreaBottom() - this.booksAreaTop();
		return Math.max(0, count * BOOK_ROW_HEIGHT - height);
	}

	private boolean clickScrollBar(double mouseX, double mouseY) {
		int barX = this.scrollBarX();
		int catTop = this.catAreaTop();
		ScrollBar.Click cat = ScrollBar.click(
			barX, catTop, this.catAreaHeight, this.categoryScroll, this.categoryMaxScrollPx(Library.get().categories().size()), mouseX, mouseY);
		if (cat != null) {
			this.categoryScroll = cat.scroll();
			this.barDragging = true;
			this.barOnCategories = true;
			this.barGrab = cat.grab();
			return true;
		}
		int bTop = this.booksAreaTop();
		int bookH = this.booksAreaBottom() - bTop;
		ScrollBar.Click book = ScrollBar.click(
			barX, bTop, bookH, this.bookScroll, this.bookMaxScrollPx(this.currentBooks().size()), mouseX, mouseY);
		if (book != null) {
			this.bookScroll = book.scroll();
			this.barDragging = true;
			this.barOnCategories = false;
			this.barGrab = book.grab();
			return true;
		}
		return false;
	}

	private static boolean inRect(double mouseX, double mouseY, int rx, int ry, int rw, int rh) {
		return mouseX >= rx && mouseX < rx + rw && mouseY >= ry && mouseY < ry + rh;
	}
}
