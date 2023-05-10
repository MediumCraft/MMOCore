package net.Indyuce.mmocore.comp.vault;

import net.Indyuce.mmocore.MMOCore;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public class VaultEconomy {

    @Nullable
    private final Economy economy;

    public VaultEconomy() {
        Economy eco;
        try {
            eco = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
            MMOCore.log(Level.INFO, "Hooked onto Vault");
        } catch (Exception exception) {
            MMOCore.plugin.getLogger().log(Level.WARNING, "Vault was found but MMOCore was unable to successfully find/load an economy plugin.");
            eco = null;
        }
        this.economy = eco;
    }

    /**
     * @return If an economy plugin was found
     */
    public boolean isValid() {
        return economy != null;
    }

    public Economy getEconomy() {
        return economy;
    }
}
