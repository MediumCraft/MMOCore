package net.Indyuce.mmocore.waypoint;

public enum WaypointOption {

    DEFAULT(false),

    /**
     * Enable this so make the waypoint auto unlock when you sneak on it.
     */
    UNLOCKABLE(true),

    /**
     * When enabled players can sneak when standing on that waypoint
     * to open its menu. This option can be disabled to create waypoints
     * that you can only teleport to.
     */
    ENABLE_MENU(true),

    /**
     * By defaut, players must stand into
     */
    DYNAMIC(false),

    /**
     * When set to true, players can choose this waypoint as their spawnpoint.
     * This action costs some stellium.
     */
    // SPAWNABLE(false)
    ;

    private final String path;
    private final boolean defaultValue;

    WaypointOption(boolean defaultValue) {
        this.path = name().toLowerCase().replace("_", "-");
        this.defaultValue = defaultValue;
    }

    public String getPath() {
        return path;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }
}
