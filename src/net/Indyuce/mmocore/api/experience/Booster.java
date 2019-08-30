package net.Indyuce.mmocore.api.experience;

import java.util.UUID;

public class Booster {
	private final UUID uuid = UUID.randomUUID();
	private final long date = System.currentTimeMillis(), length;
	private final Profession profession;
	private final double extra;
	private final String author;

	public Booster(double extra, long length) {
		this(null, null, extra, length);
	}

	public Booster(String author, double extra, long length) {
		this(author, null, extra, length);
	}

	public Booster(String author, Profession profession, double extra, long length) {
		this.author = author;
		this.length = length * 1000;
		this.profession = profession;
		this.extra = extra;
	}

	public boolean isTimedOut() {
		return date + length < System.currentTimeMillis();
	}

	public long getLeft() {
		return Math.max(0, date + length - System.currentTimeMillis());
	}

	public long getCreationDate() {
		return date;
	}

	public long getLength() {
		return length;
	}

	public boolean hasProfession() {
		return profession != null;
	}

	public Profession getProfession() {
		return profession;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public double calculateExp(double exp) {
		return exp * (1 + extra);
	}

	public double getExtra() {
		return extra;
	}

	public boolean hasAuthor() {
		return author != null;
	}

	public String getAuthor() {
		return author;
	}
}
