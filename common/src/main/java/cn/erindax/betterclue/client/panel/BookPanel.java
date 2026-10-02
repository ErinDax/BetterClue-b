package cn.erindax.betterclue.client.panel;

import cn.erindax.betterclue.client.ShareHandler;
import cn.erindax.betterclue.client.Texts;
import cn.erindax.betterclue.client.book.Book;
import cn.erindax.betterclue.client.book.BookExporter;
import cn.erindax.betterclue.client.book.Category;
import cn.erindax.betterclue.client.book.Library;
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
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
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
	private static final Action[] CATEGORY_ACTIONS = {Action.SHARE_CATEGORY, Action.RENAME_CATEGORY, Action.DELETE_CATEGORY};
	private static final Action[] BOOK_ACTIONS = {Action.SHARE_BOOK, Action.REMARK_BOOK, Action.EXPORT_BOOK, Action.DELETE_BOOK};

	private final int panelHeight;
	private final int catAreaHeight;
	private final Font font;
	private int selectedCategoryIndex;
	private int categoryScroll;
	private int bookScroll;
	private Component hoveredTooltip;

	private boolean pressArmed;
	private boolean dragging;
	private DragKind dragKind;
	private int dragSourceIndex;
	private double dragStartY;
	private double dragCurrentY;
	private boolean barDragging;
	private boolean barOnCategories;
	private double barGrab;

	private enum DragKind {
		CATEGORY,
		BOOK
	}

	private enum Action {
		SHARE_CATEGORY("share", "share_category", 45),
		RENAME_CATEGORY("rename", "rename_category", 32),
		DELETE_CATEGORY("delete", "delete_category", 19),
		SHARE_BOOK("share", "share_book", 69),
		REMARK_BOOK("rename", "remark_book", 56),
		EXPORT_BOOK("export", "export_book", 43),
		DELETE_BOOK("delete", "delete_book", 30);

		private final String label;
		private final Component tooltip;
		private final int fromRight;

		Action(String label, String tooltip, int fromRight) {
			this.label = "betterclue.panel.action." + label;
			this.tooltip = Component.translatable("betterclue.panel.tooltip." + tooltip);
			this.fromRight = fromRight;
		}
	}

	public BookPanel(int screenWidth, int screenHeight, int containerWidth) {
		super(0, 0, computePanelWidth(screenWidth, containerWidth), screenHeight, Component.translatable("betterclue.name"));
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
		return Math.max(64, Math.min(150, leftPos - 10));
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

	private int exportAllWidth() {
		return this.font.width(I18n.get("betterclue.panel.export_all")) + 4;
	}

	private boolean inCollapsedStrip(double mx, double my) {
		int stripY = this.getY() + this.panelHeight / 2 - COLLAPSED_STRIP_HEIGHT / 2;
		return mx >= 0 && mx < COLLAPSED_STRIP_WIDTH && my >= stripY && my < stripY + COLLAPSED_STRIP_HEIGHT;
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return Library.get().panelExpanded() ? super.isMouseOver(mouseX, mouseY) : this.inCollapsedStrip(mouseX, mouseY);
	}

	@Override
	protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		if (Library.get().panelExpanded()) {
			this.renderExpanded(guiGraphics, mouseX, mouseY);
		} else {
			this.renderCollapsed(guiGraphics, mouseX, mouseY);
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
		this.hoveredTooltip = null;
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
			boolean hover = inRect(mouseX, mouseY, x + 1, rowY, w - 8, CAT_ROW_HEIGHT) && mouseY >= catTop && mouseY < catTop + this.catAreaHeight;
			if (selected) {
				guiGraphics.fill(x + 1, rowY, x + w - 6, rowY + CAT_ROW_HEIGHT, 0x804A6A8A);
			} else if (hover) {
				guiGraphics.fill(x + 1, rowY, x + w - 6, rowY + CAT_ROW_HEIGHT, 0x40333333);
			}
			Category category = categories.get(i);
			String label = category.name + " (" + category.books.size() + ")";
			guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(label, w - 69), x + 4, rowY + 2, selected ? 0xFFFFFF : 0xD0D0D0);
			if (hover) {
				this.drawActions(guiGraphics, CATEGORY_ACTIONS, rowY, mouseX, mouseY);
			}
		}
		guiGraphics.disableScissor();
		int barX = this.scrollBarX();
		boolean catBarHot = this.barDragging && this.barOnCategories || ScrollBar.hit(barX, catTop, this.catAreaHeight, mouseX, mouseY);
		ScrollBar.draw(guiGraphics, barX, catTop, this.catAreaHeight, this.categoryScroll, this.categoryMaxScrollPx(categories.size()), catBarHot);

		int ncTop = this.newCatTop();
		boolean ncHover = inRect(mouseX, mouseY, x + 2, ncTop, w - 4, NEW_CAT_HEIGHT);
		guiGraphics.fill(x + 2, ncTop, x + w - 2, ncTop + NEW_CAT_HEIGHT, ncHover ? 0x603A5A3A : 0x40202020);
		guiGraphics.drawString(this.font, I18n.get("betterclue.panel.new_category"), x + 6, ncTop + 3, ncHover ? 0x90FF90 : 0xA0C0A0);

		int bhTop = this.booksHeaderTop();
		int exportWidth = this.exportAllWidth();
		guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(this.bookHeaderLabel(), w - exportWidth - 8), x + 4, bhTop + 2, 0xFFFFFF);
		boolean exportAllHover = inRect(mouseX, mouseY, x + w - exportWidth - 2, bhTop, exportWidth, BOOKS_HEADER_HEIGHT);
		guiGraphics.drawString(this.font, I18n.get("betterclue.panel.export_all"), x + w - exportWidth, bhTop + 2, exportAllHover ? 0xFFFFFF : 0xAAAAAA);

		List<Book> books = this.currentBooks();
		int bTop = this.booksAreaTop();
		int bBottom = this.booksAreaBottom();
		this.bookScroll = Math.max(0, Math.min(this.bookScroll, this.bookMaxScrollPx(books.size())));
		guiGraphics.enableScissor(x + 1, bTop, x + w - 6, bBottom);
		if (books.isEmpty()) {
			String empty = I18n.get(this.selectedCategoryIndex < 0 ? "betterclue.panel.no_category" : "betterclue.panel.empty");
			guiGraphics.drawString(this.font, empty, x + 6, bTop + 4, 0x808080);
		}
		Book hoveredBook = null;
		for (int i = 0; i < books.size(); i++) {
			int rowY = bTop + i * BOOK_ROW_HEIGHT - this.bookScroll;
			if (rowY + BOOK_ROW_HEIGHT < bTop || rowY > bBottom) {
				continue;
			}
			boolean hover = inRect(mouseX, mouseY, x + 1, rowY, w - 8, BOOK_ROW_HEIGHT) && mouseY >= bTop && mouseY < bBottom;
			if (hover) {
				guiGraphics.fill(x + 1, rowY, x + w - 6, rowY + BOOK_ROW_HEIGHT, 0x40333333);
			}
			Book book = books.get(i);
			guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(book.displayTitle(), w - 77), x + 4, rowY + 3, hover ? 0xFFFFFF : 0xD0D0D0);
			if (hover) {
				this.drawActions(guiGraphics, BOOK_ACTIONS, rowY, mouseX, mouseY);
				if (!this.dragging && mouseX < this.actionX(Action.SHARE_BOOK)) {
					hoveredBook = book;
				}
			}
		}
		guiGraphics.disableScissor();
		int bookAreaHeight = bBottom - bTop;
		boolean bookBarHot = this.barDragging && !this.barOnCategories || ScrollBar.hit(barX, bTop, bookAreaHeight, mouseX, mouseY);
		ScrollBar.draw(guiGraphics, barX, bTop, bookAreaHeight, this.bookScroll, this.bookMaxScrollPx(books.size()), bookBarHot);

		if (this.hoveredTooltip != null) {
			guiGraphics.renderTooltip(this.font, this.hoveredTooltip, mouseX, mouseY);
		} else if (hoveredBook != null) {
			this.renderBookTooltip(guiGraphics, hoveredBook, mouseX, mouseY);
		}

		if (this.dragging) {
			int ghostHeight = this.dragKind == DragKind.BOOK ? BOOK_ROW_HEIGHT : CAT_ROW_HEIGHT;
			int ghostY = (int) this.dragCurrentY - ghostHeight / 2;
			guiGraphics.fill(x + 1, ghostY, x + w - 1, ghostY + ghostHeight, 0xA0FFFFFF);
			guiGraphics.drawString(this.font, this.font.plainSubstrByWidth(this.dragGhostLabel(), w - 10), x + 4, ghostY + 2, 0x202020);
		}
	}

	private void drawActions(GuiGraphics guiGraphics, Action[] actions, int rowY, int mouseX, int mouseY) {
		for (Action action : actions) {
			int rectX = this.actionX(action);
			boolean hover = inRect(mouseX, mouseY, rectX, rowY, ACTION_WIDTH, CAT_ROW_HEIGHT);
			guiGraphics.drawString(this.font, I18n.get(action.label), rectX + 2, rowY + 2, hover ? 0xFFFFFF : 0x999999);
			if (hover && !this.dragging) {
				this.hoveredTooltip = action.tooltip;
			}
		}
	}

	private void renderBookTooltip(GuiGraphics guiGraphics, Book book, int mouseX, int mouseY) {
		List<Component> lines = new ArrayList<>();
		lines.add(Component.literal(Texts.title(book.title())));
		if (!book.remark().isEmpty()) {
			lines.add(Component.translatable("betterclue.book.remark", book.remark()).withStyle(ChatFormatting.GRAY));
		}
		guiGraphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
	}

	private int actionX(Action action) {
		return this.getX() + this.getWidth() - action.fromRight;
	}

	private Action actionAt(Action[] actions, double mouseX) {
		for (Action action : actions) {
			int rectX = this.actionX(action);
			if (mouseX >= rectX && mouseX < rectX + ACTION_WIDTH) {
				return action;
			}
		}
		return null;
	}

	private String bookHeaderLabel() {
		Category category = Library.get().category(this.selectedCategoryIndex);
		if (category == null) {
			return I18n.get("betterclue.panel.select_category");
		}
		return I18n.get("betterclue.panel.books", category.name, category.books.size(), Library.MAX_BOOKS_PER_CATEGORY);
	}

	private String dragGhostLabel() {
		if (this.dragKind == DragKind.CATEGORY) {
			Category category = Library.get().category(this.dragSourceIndex);
			return I18n.get("betterclue.panel.move", category == null ? "" : category.name);
		}
		Book book = this.currentBookAt(this.dragSourceIndex);
		return I18n.get("betterclue.panel.move", book == null ? Texts.title("") : book.displayTitle());
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!this.active || !this.visible || button != 0) {
			return false;
		}
		if (!Library.get().panelExpanded()) {
			if (this.inCollapsedStrip(mouseX, mouseY)) {
				Library.get().setPanelExpanded(true);
				return true;
			}
			return false;
		}
		int x = this.getX();
		int w = this.getWidth();
		Library library = Library.get();
		List<Category> categories = library.categories();
		this.clampSelection(categories);

		if (inRect(mouseX, mouseY, x + w - 14, this.getY() + 2, 12, HEADER_HEIGHT - 2)) {
			Library.get().setPanelExpanded(false);
			return true;
		}
		if (this.clickScrollBar(mouseX, mouseY)) {
			return true;
		}

		int catTop = this.catAreaTop();
		if (mouseY >= catTop && mouseY < catTop + this.catAreaHeight) {
			for (int i = 0; i < categories.size(); i++) {
				int rowY = catTop + i * CAT_ROW_HEIGHT - this.categoryScroll;
				if (mouseX >= x && mouseX < x + w - 6 && mouseY >= rowY && mouseY < rowY + CAT_ROW_HEIGHT) {
					Action action = this.actionAt(CATEGORY_ACTIONS, mouseX);
					if (action != null) {
						this.perform(action, i);
						return true;
					}
					this.selectedCategoryIndex = i;
					library.setPreferredCategory(categories.get(i).name);
					this.armPress(DragKind.CATEGORY, i, mouseY);
					return true;
				}
			}
		}

		int ncTop = this.newCatTop();
		if (mouseX >= x && mouseX < x + w && mouseY >= ncTop && mouseY < ncTop + NEW_CAT_HEIGHT) {
			this.createCategory();
			return true;
		}

		int bhTop = this.booksHeaderTop();
		int exportWidth = this.exportAllWidth();
		if (inRect(mouseX, mouseY, x + w - exportWidth - 2, bhTop, exportWidth, BOOKS_HEADER_HEIGHT)) {
			this.exportAll();
			return true;
		}

		List<Book> books = this.currentBooks();
		int bTop = this.booksAreaTop();
		int bBottom = this.booksAreaBottom();
		if (mouseY >= bTop && mouseY < bBottom) {
			for (int i = 0; i < books.size(); i++) {
				int rowY = bTop + i * BOOK_ROW_HEIGHT - this.bookScroll;
				if (mouseX >= x && mouseX < x + w - 6 && mouseY >= rowY && mouseY < rowY + BOOK_ROW_HEIGHT) {
					Action action = this.actionAt(BOOK_ACTIONS, mouseX);
					if (action != null) {
						this.perform(action, i);
						return true;
					}
					this.armPress(DragKind.BOOK, i, mouseY);
					return true;
				}
			}
		}

		return mouseX >= x && mouseX < x + w && mouseY >= this.getY() && mouseY < this.getY() + this.panelHeight;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (button != 0) {
			return false;
		}
		if (this.barDragging) {
			if (this.barOnCategories) {
				this.categoryScroll = ScrollBar.scrollAtGrab(this.catAreaTop(), this.catAreaHeight, this.categoryMaxScrollPx(Library.get().categories().size()), mouseY, this.barGrab);
			} else {
				int bTop = this.booksAreaTop();
				this.bookScroll = ScrollBar.scrollAtGrab(bTop, this.booksAreaBottom() - bTop, this.bookMaxScrollPx(this.currentBooks().size()), mouseY, this.barGrab);
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
				this.openBook(index);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		return this.scroll(mouseX, mouseY, amount);
	}

	public boolean scroll(double mouseX, double mouseY, double amount) {
		if (!Library.get().panelExpanded() || amount == 0 || !this.isMouseOver(mouseX, mouseY)) {
			return false;
		}
		int delta = (int) Math.round(-amount);
		if (delta == 0) {
			delta = amount > 0 ? -1 : 1;
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
		narrationElementOutput.add(NarratedElementType.TITLE, this.getMessage());
	}

	private void perform(Action action, int index) {
		Library library = Library.get();
		switch (action) {
			case SHARE_CATEGORY -> {
				Category category = library.category(index);
				if (category != null) {
					ShareHandler.shareNearby(category.books);
				}
			}
			case RENAME_CATEGORY -> this.renameCategory(index);
			case DELETE_CATEGORY -> this.deleteCategory(index);
			case SHARE_BOOK -> {
				Book book = this.currentBookAt(index);
				if (book != null) {
					ShareHandler.shareNearby(List.of(book));
				}
			}
			case REMARK_BOOK -> this.editRemark(index);
			case EXPORT_BOOK -> this.exportBook(index);
			case DELETE_BOOK -> this.deleteBook(index);
		}
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
		if (mouseX < this.getX() || mouseX >= this.getX() + this.getWidth()) {
			return;
		}
		Library library = Library.get();
		int targetCategory = this.categoryIndexAt(mouseY);
		if (kind == DragKind.CATEGORY) {
			if (targetCategory >= 0 && targetCategory != sourceIndex) {
				library.moveCategory(sourceIndex, targetCategory);
			}
			return;
		}
		if (targetCategory >= 0 && targetCategory != this.selectedCategoryIndex) {
			library.moveBookToCategory(this.selectedCategoryIndex, sourceIndex, targetCategory);
			return;
		}
		int targetRow = this.bookIndexAt(mouseY);
		if (targetRow >= 0 && targetRow != sourceIndex) {
			library.moveBook(this.selectedCategoryIndex, sourceIndex, targetRow);
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
			this.selectedCategoryIndex = Math.max(0, Library.get().preferredCategoryIndex());
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

	private void openBook(int index) {
		Book book = this.currentBookAt(index);
		if (book != null) {
			Minecraft.getInstance().setScreen(new BookReadScreen(book.pages(), BookPanel::openInventory));
		}
	}

	private void createCategory() {
		Minecraft.getInstance().setScreen(new RenamePromptScreen(
			Component.translatable("betterclue.prompt.new_category"), "", Library.MAX_CATEGORY_NAME_LENGTH, false,
			name -> {
				if (!Library.get().createCategory(name)) {
					Texts.toast("betterclue.category.create_failed", Library.MAX_CATEGORIES);
				}
			}
		));
	}

	private void renameCategory(int index) {
		Category category = Library.get().category(index);
		if (category == null) {
			return;
		}
		Minecraft.getInstance().setScreen(new RenamePromptScreen(
			Component.translatable("betterclue.prompt.rename_category"), category.name, Library.MAX_CATEGORY_NAME_LENGTH, false,
			name -> {
				if (!Library.get().renameCategory(index, name)) {
					Texts.toast("betterclue.category.rename_failed");
				}
			}
		));
	}

	private void deleteCategory(int index) {
		Category category = Library.get().category(index);
		if (category == null) {
			return;
		}
		if (category.books.isEmpty()) {
			Library.get().deleteCategory(index);
			return;
		}
		confirm(
			Component.translatable("betterclue.confirm.delete_category"),
			Component.translatable("betterclue.confirm.delete_category.detail", category.name, category.books.size()),
			() -> Library.get().deleteCategory(index)
		);
	}

	private void editRemark(int index) {
		Book book = this.currentBookAt(index);
		if (book == null) {
			return;
		}
		int categoryIndex = this.selectedCategoryIndex;
		Minecraft.getInstance().setScreen(new RenamePromptScreen(
			Component.translatable("betterclue.prompt.remark"), book.remark(), Library.MAX_REMARK_LENGTH, true,
			remark -> Library.get().setRemark(categoryIndex, index, remark)
		));
	}

	private void deleteBook(int index) {
		Book book = this.currentBookAt(index);
		if (book == null) {
			return;
		}
		int categoryIndex = this.selectedCategoryIndex;
		confirm(
			Component.translatable("betterclue.confirm.delete_book"),
			Component.translatable("betterclue.confirm.delete_book.detail", Texts.quoted(book.title())),
			() -> Library.get().deleteBook(categoryIndex, index)
		);
	}

	private void exportBook(int index) {
		Book book = this.currentBookAt(index);
		if (book == null) {
			return;
		}
		String path = BookExporter.export(book);
		if (path == null) {
			Texts.toast("betterclue.export.failed");
		} else {
			Texts.toast("betterclue.export.done", path);
		}
	}

	private void exportAll() {
		List<Book> books = this.currentBooks();
		if (books.isEmpty()) {
			Texts.toast("betterclue.export.empty");
			return;
		}
		int exported = 0;
		for (Book book : books) {
			if (BookExporter.export(book) != null) {
				exported++;
			}
		}
		Texts.toast("betterclue.export.done_many", exported, BookExporter.directory());
	}

	private static void confirm(Component title, Component detail, Runnable action) {
		Minecraft minecraft = Minecraft.getInstance();
		Screen previous = minecraft.screen;
		minecraft.setScreen(new ConfirmScreen(confirmed -> {
			if (confirmed) {
				action.run();
			}
			minecraft.setScreen(previous);
		}, title, detail));
	}

	private static void openInventory() {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null) {
			minecraft.setScreen(null);
		} else if (player.isCreative()) {
			minecraft.setScreen(new CreativeModeInventoryScreen(player, player.connection.enabledFeatures(), minecraft.options.operatorItemsTab().get()));
		} else {
			minecraft.setScreen(new InventoryScreen(player));
		}
	}

	private int scrollBarX() {
		return this.getX() + this.getWidth() - 5;
	}

	private int categoryMaxScrollPx(int count) {
		return Math.max(0, count * CAT_ROW_HEIGHT - this.catAreaHeight);
	}

	private int bookMaxScrollPx(int count) {
		return Math.max(0, count * BOOK_ROW_HEIGHT - (this.booksAreaBottom() - this.booksAreaTop()));
	}

	private boolean clickScrollBar(double mouseX, double mouseY) {
		int barX = this.scrollBarX();
		ScrollBar.Click category = ScrollBar.click(barX, this.catAreaTop(), this.catAreaHeight, this.categoryScroll, this.categoryMaxScrollPx(Library.get().categories().size()), mouseX, mouseY);
		if (category != null) {
			this.categoryScroll = category.scroll();
			this.barDragging = true;
			this.barOnCategories = true;
			this.barGrab = category.grab();
			return true;
		}
		int bTop = this.booksAreaTop();
		ScrollBar.Click book = ScrollBar.click(barX, bTop, this.booksAreaBottom() - bTop, this.bookScroll, this.bookMaxScrollPx(this.currentBooks().size()), mouseX, mouseY);
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
