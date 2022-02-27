package net.Indyuce.mmocore.waypoint;

public enum CostType {

    /**
     * When teleporting to this waypoint
     */
    NORMAL_USE,

    /**
     * When dynamically teleporting to this waypoint
     */
    DYNAMIC_USE,

    /**
     * When setting your spawn point to this waypoint.
     */
    SET_SPAWNPOINT;

    private final String path;

    CostType() {
        this.path = name().toLowerCase().replace("_", "-");
    }

    public String getPath() {
        return path;
    }
}
