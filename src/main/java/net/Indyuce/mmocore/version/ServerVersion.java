package net.Indyuce.mmocore.version;

import net.Indyuce.mmocore.version.texture.CustomModelDataHandler;
import net.Indyuce.mmocore.version.texture.TextureByDurabilityHandler;
import net.Indyuce.mmocore.version.texture.TextureHandler;
import net.Indyuce.mmocore.version.wrapper.DefaultVersionWrapper;
import net.Indyuce.mmocore.version.wrapper.LegacyVersionWrapper;
import net.Indyuce.mmocore.version.wrapper.VersionWrapper;

public class ServerVersion {
	private final String version;
	private final int[] integers;
	private final TextureHandler textureHandler;
	private final VersionWrapper versionWrapper;

	public ServerVersion(Class<?> clazz) {
		this.version = clazz.getPackage().getName().replace(".", ",").split(",")[3];
		String[] split = version.substring(1).split("\\_");
		this.integers = new int[] { Integer.parseInt(split[0]), Integer.parseInt(split[1]) };

		versionWrapper = isBelowOrEqual(1, 12) ? new LegacyVersionWrapper() : new DefaultVersionWrapper();
		textureHandler = isBelowOrEqual(1, 13) ? new TextureByDurabilityHandler() : new CustomModelDataHandler();
	}

	public boolean isBelowOrEqual(int... version) {
		return version[0] > integers[0] ? true : version[1] >= integers[1];
	}

	public boolean isStrictlyHigher(int... version) {
		return version[0] < integers[0] ? true : version[1] < integers[1];
		// return !isBelowOrEqual(version);
	}

	public int getRevisionNumber() {
		return Integer.parseInt(version.split("\\_")[2].replaceAll("[^0-9]", ""));
	}

	public int[] toNumbers() {
		return integers;
	}

	public boolean isTextureByDurability() {
		return textureHandler instanceof TextureByDurabilityHandler;
	}

	public TextureHandler getTextureHandler() {
		return textureHandler;
	}

	public VersionWrapper getVersionWrapper() {
		return versionWrapper;
	}

	@Override
	public String toString() {
		return version;
	}
}
