package net.Indyuce.mmocore.api.player.profess;

public enum ClassOption {

	/**
	 * If the class should be applied to newcomers
	 */
	DEFAULT,

	/**
	 * If the class should in the /class GUI
	 */
	DISPLAY(true),

	/**
	 * Health only regens when out of combat
	 */
	OFF_COMBAT_HEALTH_REGEN,

	/**
	 * Mana only regens when out of combat
	 */
	OFF_COMBAT_MANA_REGEN,

	/**
	 * Stamina only regens when out of combat
	 */
	OFF_COMBAT_STAMINA_REGEN,

	/**
	 * Stellium only regens when out of combat
	 */
	OFF_COMBAT_STELLIUM_REGEN;

	private final boolean def;

	ClassOption() {
		this(false);
	}

	ClassOption(boolean def) {
		this.def = def;
	}

	public boolean getDefault() {
		return def;
	}

	public String getPath() {
		return name().toLowerCase().replace("_", "-");
	}
}