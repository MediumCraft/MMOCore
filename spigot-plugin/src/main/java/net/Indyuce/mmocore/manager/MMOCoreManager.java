package net.Indyuce.mmocore.manager;

public interface MMOCoreManager {

    /**
     * Called either when the server starts when initializing the manager for
     * the first time, or when issuing a plugin reload; in that case, stuff
     * like listeners must all be cleared before
     *
     * @param clearBefore True when issuing a plugin reload
     */
    void initialize(boolean clearBefore);
}
