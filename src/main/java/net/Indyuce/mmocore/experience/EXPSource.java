package net.Indyuce.mmocore.experience;

public enum EXPSource {

    /**
     * When using a profession/class experience source
     */
    SOURCE,

    /**
     * When using the /mmocore admin exp command
     */
    COMMAND,

    /**
     * When converting vanilla exp into MMOCore exp
     */
    VANILLA,

    /**
     * When party members share exp
     */
    PARTY_SHARING,

    /**
     * When using the experience trigger. Keep in mind the
     * experience trigger can also use another experience source
     * when using the right parameter
     */
    QUEST,

    /**
     * When gaining experience from fishing
     */
    FISHING,

    /**
     * Anything else
     */
    OTHER
}
