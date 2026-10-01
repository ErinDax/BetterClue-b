package cn.erindax.betterclue.client.book;

import cn.erindax.betterclue.BetterClue;
import cn.erindax.betterclue.client.Texts;
import cn.erindax.betterclue.network.BookData;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

public final class Library {
	public static final int MAX_CATEGORIES = 16;
	public static final int MAX_BOOKS_PER_CATEGORY = 64;
	public static final int MAX_CATEGORY_NAME_LENGTH = 16;
	public static final int MAX_REMARK_LENGTH = 64;
	private static final String FILE_NAME = "books.nbt";
	private static final int VERSION = 1;
	private static final Library INSTANCE = new Library();

	private final List<Category> categories = new ArrayList<>();
	private String preferredCategory = "";
	private boolean loaded;
	private boolean writable = true;

	private Library() {
	}

	public static Library get() {
		INSTANCE.ensureLoaded();
		return INSTANCE;
	}

	public enum AddResult {
		ADDED,
		ALREADY_COLLECTED,
		CATEGORY_FULL,
		NO_CATEGORY
	}

	public List<Category> categories() {
		return Collections.unmodifiableList(this.categories);
	}

	public Category category(int index) {
		return index >= 0 && index < this.categories.size() ? this.categories.get(index) : null;
	}

	public int preferredCategoryIndex() {
		if (this.categories.isEmpty()) {
			return -1;
		}
		for (int i = 0; i < this.categories.size(); i++) {
			if (this.categories.get(i).name.equals(this.preferredCategory)) {
				return i;
			}
		}
		return 0;
	}

	public void setPreferredCategory(String name) {
		if (name != null && !name.isEmpty() && !name.equals(this.preferredCategory)) {
			this.preferredCategory = name;
			this.save();
		}
	}

	public AddResult addBook(BookData book) {
		AddResult result = this.insert(book);
		if (result == AddResult.ADDED) {
			this.save();
		}
		return result;
	}

	public List<AddResult> addBooks(List<BookData> books) {
		List<AddResult> results = new ArrayList<>(books.size());
		for (BookData book : books) {
			results.add(this.insert(book));
		}
		if (results.contains(AddResult.ADDED)) {
			this.save();
		}
		return results;
	}

	public boolean createCategory(String name) {
		String trimmed = trimName(name);
		if (trimmed == null || this.categories.size() >= MAX_CATEGORIES || this.indexOf(trimmed) >= 0) {
			return false;
		}
		this.categories.add(new Category(trimmed));
		this.save();
		return true;
	}

	public boolean renameCategory(int index, String name) {
		Category category = this.category(index);
		String trimmed = trimName(name);
		if (category == null || trimmed == null) {
			return false;
		}
		int existing = this.indexOf(trimmed);
		if (existing >= 0 && existing != index) {
			return false;
		}
		if (category.name.equals(this.preferredCategory)) {
			this.preferredCategory = trimmed;
		}
		category.name = trimmed;
		this.save();
		return true;
	}

	public void deleteCategory(int index) {
		if (this.category(index) == null) {
			return;
		}
		this.categories.remove(index);
		this.ensureCategory();
		if (this.indexOf(this.preferredCategory) < 0) {
			this.preferredCategory = this.categories.get(0).name;
		}
		this.save();
	}

	public void moveCategory(int from, int to) {
		if (from == to || this.category(from) == null || this.category(to) == null) {
			return;
		}
		this.categories.add(to, this.categories.remove(from));
		this.save();
	}

	public void setRemark(int categoryIndex, int bookIndex, String remark) {
		Category category = this.category(categoryIndex);
		if (category == null || bookIndex < 0 || bookIndex >= category.books.size()) {
			return;
		}
		String trimmed = remark == null ? "" : remark.trim();
		if (trimmed.length() > MAX_REMARK_LENGTH) {
			trimmed = trimmed.substring(0, MAX_REMARK_LENGTH);
		}
		category.books.set(bookIndex, category.books.get(bookIndex).withRemark(trimmed));
		this.save();
	}

	public void deleteBook(int categoryIndex, int bookIndex) {
		Category category = this.category(categoryIndex);
		if (category == null || bookIndex < 0 || bookIndex >= category.books.size()) {
			return;
		}
		category.books.remove(bookIndex);
		this.save();
	}

	public void moveBook(int categoryIndex, int from, int to) {
		Category category = this.category(categoryIndex);
		if (category == null || from < 0 || from >= category.books.size()) {
			return;
		}
		int target = Math.max(0, Math.min(to, category.books.size() - 1));
		if (from == target) {
			return;
		}
		category.books.add(target, category.books.remove(from));
		this.save();
	}

	public void moveBookToCategory(int sourceIndex, int bookIndex, int targetIndex) {
		Category source = this.category(sourceIndex);
		Category target = this.category(targetIndex);
		if (source == null || target == null || sourceIndex == targetIndex || bookIndex < 0 || bookIndex >= source.books.size() || target.books.size() >= MAX_BOOKS_PER_CATEGORY) {
			return;
		}
		target.books.add(source.books.remove(bookIndex));
		this.save();
	}

	private AddResult insert(BookData book) {
		String key = keyOf(book);
		for (Category category : this.categories) {
			for (Book existing : category.books) {
				if (existing.key().equals(key)) {
					return AddResult.ALREADY_COLLECTED;
				}
			}
		}
		Category target = this.category(this.preferredCategoryIndex());
		if (target == null) {
			return AddResult.NO_CATEGORY;
		}
		if (target.books.size() >= MAX_BOOKS_PER_CATEGORY) {
			return AddResult.CATEGORY_FULL;
		}
		target.books.add(new Book(key, book.title(), book.author(), book.pages(), System.currentTimeMillis(), ""));
		return AddResult.ADDED;
	}

	private int indexOf(String name) {
		for (int i = 0; i < this.categories.size(); i++) {
			if (this.categories.get(i).name.equals(name)) {
				return i;
			}
		}
		return -1;
	}

	private void ensureCategory() {
		if (this.categories.isEmpty()) {
			this.categories.add(new Category(I18n.get("betterclue.category.default")));
		}
	}

	private void ensureLoaded() {
		if (this.loaded) {
			return;
		}
		this.loaded = true;
		Path path = savePath();
		if (Files.exists(path)) {
			try {
				this.read(NbtIo.readCompressed(new ByteArrayInputStream(Files.readAllBytes(path)), NbtAccounter.unlimitedHeap()));
			} catch (IOException | RuntimeException e) {
				BetterClue.LOGGER.error("Failed to read {}", path, e);
				this.categories.clear();
				this.preferredCategory = "";
				this.writable = backup(path);
			}
		}
		this.ensureCategory();
	}

	private void read(CompoundTag root) throws IOException {
		int version = root.getInt("Version");
		if (version != VERSION) {
			throw new IOException("Unsupported library version " + version);
		}
		this.preferredCategory = root.getString("PreferredCategory");
		ListTag list = root.getList("Categories", Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			this.categories.add(Category.fromTag(list.getCompound(i)));
		}
	}

	private CompoundTag write() {
		CompoundTag root = new CompoundTag();
		root.putInt("Version", VERSION);
		root.putString("PreferredCategory", this.preferredCategory);
		ListTag list = new ListTag();
		for (Category category : this.categories) {
			list.add(category.toTag());
		}
		root.put("Categories", list);
		return root;
	}

	private void save() {
		if (!this.writable) {
			return;
		}
		Path path = savePath();
		Path temp = path.resolveSibling(FILE_NAME + ".tmp");
		try {
			Files.createDirectories(path.getParent());
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			NbtIo.writeCompressed(this.write(), buffer);
			Files.write(temp, buffer.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE, StandardOpenOption.SYNC);
			try {
				Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			BetterClue.LOGGER.error("Failed to save {}", path, e);
		}
	}

	private static boolean backup(Path path) {
		Path target = path.resolveSibling(FILE_NAME + ".broken-" + System.currentTimeMillis());
		try {
			Files.move(path, target);
			BetterClue.LOGGER.warn("Moved unreadable library to {}", target);
			Texts.toast("betterclue.library.backed_up");
			return true;
		} catch (IOException e) {
			BetterClue.LOGGER.error("Failed to back up {}, changes will not be saved", path, e);
			Texts.toast("betterclue.library.read_only");
			return false;
		}
	}

	private static Path savePath() {
		return Minecraft.getInstance().gameDirectory.toPath().resolve(BetterClue.MOD_ID).resolve(FILE_NAME);
	}

	private static String trimName(String name) {
		if (name == null) {
			return null;
		}
		String trimmed = name.trim();
		return trimmed.isEmpty() || trimmed.length() > MAX_CATEGORY_NAME_LENGTH ? null : trimmed;
	}

	private static String keyOf(BookData book) {
		StringBuilder builder = new StringBuilder();
		builder.append(book.title()).append('\u0001').append(book.author()).append('\u0001');
		for (String page : book.pages()) {
			builder.append(page).append('\u0002');
		}
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(builder.toString().getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}
}
