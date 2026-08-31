package cn.erindax.betterclue.client.book;

import cn.erindax.betterclue.BetterClue;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.Tag;
import org.slf4j.Logger;

public final class Library {
	public static final int MAX_CATEGORIES = 16;
	public static final int MAX_BOOKS_PER_CATEGORY = 64;
	private static final String DEFAULT_CATEGORY_NAME = "默认分类";
	public static final int MAX_CATEGORY_NAME_LENGTH = 16;

	private static final Logger LOGGER = BetterClue.LOGGER;
	private static final String SAVE_FILE_NAME = "books.nbt";

	private static final Library INSTANCE = new Library();

	private final List<Category> categories = new ArrayList<>();
	private String preferredCategoryName = DEFAULT_CATEGORY_NAME;
	private boolean loaded = false;

	private Library() {
	}

	public static Library get() {
		return INSTANCE;
	}

	public enum AddResult {
		ADDED,
		ALREADY_COLLECTED,
		CATEGORY_FULL,
		NO_CATEGORY
	}

	public synchronized void load() {
		if (this.loaded) {
			return;
		}
		this.loaded = true;
		try {
			Path path = getSavePath();
			CompoundTag root = Files.exists(path) ? NbtIo.readCompressed(path.toFile()) : null;
			if (root == null) {
				ensureDefaultCategory();
				return;
			}
			if (root.getInt("Version") >= 1) {
				String preferred = root.getString("PreferredCategory");
				this.preferredCategoryName = preferred.isEmpty() ? DEFAULT_CATEGORY_NAME : preferred;
				ListTag catList = root.getList("Categories", Tag.TAG_COMPOUND);
				for (int i = 0; i < catList.size(); i++) {
					this.categories.add(Category.fromTag(catList.getCompound(i)));
				}
			}
			ensureDefaultCategory();
		} catch (IOException e) {
			LOGGER.warn("无法读取书籍收集存档，将使用空存档: {}", e.toString());
			ensureDefaultCategory();
		}
	}

	private void ensureLoaded() {
		if (!this.loaded) {
			this.load();
		}
	}

	private synchronized void save() {
		try {
			Path path = getSavePath();
			Files.createDirectories(path.getParent());
			CompoundTag root = new CompoundTag();
			root.putInt("Version", 1);
			root.putString("PreferredCategory", this.preferredCategoryName);
			ListTag catList = new ListTag();
			for (Category category : this.categories) {
				catList.add(category.toTag());
			}
			root.put("Categories", catList);
			NbtIo.writeCompressed(root, path.toFile());
		} catch (IOException e) {
			LOGGER.warn("保存书籍收集存档失败: {}", e.toString());
		}
	}

	private static Path getSavePath() {
		return Minecraft.getInstance().gameDirectory.toPath().resolve(BetterClue.MOD_ID).resolve(SAVE_FILE_NAME);
	}

	private void ensureDefaultCategory() {
		if (this.categories.isEmpty()) {
			this.categories.add(new Category(DEFAULT_CATEGORY_NAME));
		}
	}

	public synchronized List<Category> categories() {
		ensureLoaded();
		return Collections.unmodifiableList(this.categories);
	}

	public synchronized Category category(int index) {
		ensureLoaded();
		if (index < 0 || index >= this.categories.size()) {
			return null;
		}
		return this.categories.get(index);
	}

	public synchronized void setPreferredCategoryName(String name) {
		ensureLoaded();
		if (name == null || name.isEmpty()) {
			return;
		}
		this.preferredCategoryName = name;
		this.save();
	}

	public synchronized int preferredCategoryIndex() {
		ensureLoaded();
		if (this.categories.isEmpty()) {
			return -1;
		}
		for (int i = 0; i < this.categories.size(); i++) {
			if (this.categories.get(i).name.equals(this.preferredCategoryName)) {
				return i;
			}
		}
		return 0;
	}

	public synchronized AddResult addBook(String title, String author, List<String> pages) {
		ensureLoaded();
		String safeTitle = title == null ? "" : title;
		String safeAuthor = author == null ? "" : author;
		List<String> safePages = pages == null ? List.of() : new ArrayList<>(pages);
		String key = keyOf(safeTitle, safeAuthor, safePages);
		for (Category category : this.categories) {
			for (Book book : category.books) {
				if (book.key().equals(key)) {
					return AddResult.ALREADY_COLLECTED;
				}
			}
		}
		Category target = findPreferredCategory();
		if (target == null) {
			return AddResult.NO_CATEGORY;
		}
		if (target.books.size() >= MAX_BOOKS_PER_CATEGORY) {
			return AddResult.CATEGORY_FULL;
		}
		target.books.add(new Book(key, safeTitle, safeAuthor, safePages, System.currentTimeMillis(), ""));
		this.save();
		return AddResult.ADDED;
	}

	private static String keyOf(String title, String author, List<String> pages) {
		StringBuilder sb = new StringBuilder();
		sb.append(title).append('\u0001');
		sb.append(author).append('\u0001');
		for (String page : pages) {
			sb.append(page).append('\u0002');
		}
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] bytes = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder();
			for (byte b : bytes) {
				hex.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
			}
			return hex.toString();
		} catch (NoSuchAlgorithmException e) {
			return Integer.toHexString(sb.toString().hashCode());
		}
	}

	private Category findPreferredCategory() {
		for (Category category : this.categories) {
			if (category.name.equals(this.preferredCategoryName)) {
				return category;
			}
		}
		return this.categories.isEmpty() ? null : this.categories.get(0);
	}

	public synchronized boolean createCategory(String name) {
		ensureLoaded();
		String trimmed = trimName(name);
		if (trimmed == null) {
			return false;
		}
		if (this.categories.size() >= MAX_CATEGORIES) {
			return false;
		}
		for (Category category : this.categories) {
			if (category.name.equals(trimmed)) {
				return false;
			}
		}
		this.categories.add(new Category(trimmed));
		this.save();
		return true;
	}

	public synchronized boolean renameCategory(int index, String newName) {
		ensureLoaded();
		String trimmed = trimName(newName);
		Category category = index >= 0 && index < this.categories.size() ? this.categories.get(index) : null;
		if (trimmed == null || category == null) {
			return false;
		}
		for (int i = 0; i < this.categories.size(); i++) {
			if (i != index && this.categories.get(i).name.equals(trimmed)) {
				return false;
			}
		}
		String oldName = category.name;
		category.name = trimmed;
		if (oldName.equals(this.preferredCategoryName)) {
			this.preferredCategoryName = trimmed;
		}
		this.save();
		return true;
	}

	public synchronized void deleteCategory(int index) {
		ensureLoaded();
		if (index < 0 || index >= this.categories.size()) {
			return;
		}
		this.categories.remove(index);
		if (this.categories.isEmpty()) {
			this.categories.add(new Category(DEFAULT_CATEGORY_NAME));
		}
		if (this.categories.stream().noneMatch(c -> c.name.equals(this.preferredCategoryName))) {
			this.preferredCategoryName = this.categories.get(0).name;
		}
		this.save();
	}

	public synchronized boolean moveCategory(int fromIndex, int toIndex) {
		ensureLoaded();
		if (fromIndex < 0 || fromIndex >= this.categories.size() || toIndex < 0 || toIndex >= this.categories.size() || fromIndex == toIndex) {
			return false;
		}
		Category category = this.categories.remove(fromIndex);
		this.categories.add(toIndex, category);
		this.save();
		return true;
	}

	public synchronized boolean renameBook(int categoryIndex, int bookIndex, String remark) {
		ensureLoaded();
		Category category = category(categoryIndex);
		if (category == null || bookIndex < 0 || bookIndex >= category.books.size()) {
			return false;
		}
		String trimmed = remark == null ? "" : remark.trim();
		if (trimmed.length() > 64) {
			trimmed = trimmed.substring(0, 64);
		}
		Book book = category.books.get(bookIndex);
		category.books.set(bookIndex, new Book(book.key(), book.title(), book.author(), book.pages(), book.collectedAt(), trimmed));
		this.save();
		return true;
	}

	public synchronized boolean deleteBook(int categoryIndex, int bookIndex) {
		ensureLoaded();
		Category category = category(categoryIndex);
		if (category == null || bookIndex < 0 || bookIndex >= category.books.size()) {
			return false;
		}
		category.books.remove(bookIndex);
		this.save();
		return true;
	}

	public synchronized boolean moveBook(int categoryIndex, int fromIndex, int toIndex) {
		ensureLoaded();
		Category category = category(categoryIndex);
		if (category == null || fromIndex < 0 || fromIndex >= category.books.size()) {
			return false;
		}
		toIndex = Math.max(0, Math.min(toIndex, category.books.size() - 1));
		if (fromIndex == toIndex) {
			return false;
		}
		Book book = category.books.remove(fromIndex);
		category.books.add(toIndex, book);
		this.save();
		return true;
	}

	public synchronized boolean moveBookToCategory(int sourceCategoryIndex, int bookIndex, int targetCategoryIndex) {
		ensureLoaded();
		Category source = category(sourceCategoryIndex);
		Category target = category(targetCategoryIndex);
		if (source == null || target == null || bookIndex < 0 || bookIndex >= source.books.size()) {
			return false;
		}
		if (sourceCategoryIndex == targetCategoryIndex) {
			return moveBook(sourceCategoryIndex, bookIndex, target.books.size() - 1);
		}
		if (target.books.size() >= MAX_BOOKS_PER_CATEGORY) {
			return false;
		}
		Book book = source.books.remove(bookIndex);
		target.books.add(book);
		this.save();
		return true;
	}

	private static String trimName(String name) {
		if (name == null) {
			return null;
		}
		String trimmed = name.trim();
		if (trimmed.isEmpty() || trimmed.length() > MAX_CATEGORY_NAME_LENGTH) {
			return null;
		}
		return trimmed;
	}
}
