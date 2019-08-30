package net.Indyuce.mmocore.comp.vault;

import java.util.logging.Level;

import org.bukkit.Bukkit;

import net.Indyuce.mmocore.MMOCore;
import net.milkbowl.vault.economy.Economy;

public class VaultEconomy {
	private Economy economy;

	public VaultEconomy() {
		try {
			economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
			MMOCore.log(Level.INFO, "Hooked onto Vault");
		} catch (Exception exception) {
			MMOCore.plugin.getLogger().log(Level.WARNING, "Vault was found but MMOCore was unable to successfully find/load an economy plugin.");
		}
	}

	/*
	 * checks if an economy plugin was found.
	 */
	public boolean isValid() {
		return economy != null;
	}

	public Economy getEconomy() {
		return economy;
	}
}
