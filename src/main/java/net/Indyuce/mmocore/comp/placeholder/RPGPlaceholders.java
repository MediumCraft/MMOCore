package net.Indyuce.mmocore.comp.placeholder;

import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.AltChar;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.PlayerQuests;
import net.Indyuce.mmocore.api.player.Professions;
import net.Indyuce.mmocore.api.player.stats.StatType;

public class RPGPlaceholders extends PlaceholderExpansion {

	@Override
	public String getAuthor() {
		return "Indyuce";
	}

	@Override
	public String getIdentifier() {
		return "mmocore";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String onPlaceholderRequest(Player player, String identifier) {

		if (identifier.equals("level"))
			return "" + PlayerData.get(player).getLevel();

		else if (identifier.equals("level_percent")) {
			PlayerData playerData = PlayerData.get(player);
			double current = playerData.getExperience(), next = MMOCore.plugin.configManager.getNeededExperience(playerData.getLevel() + 1);
			return MMOCore.plugin.configManager.decimal.format(current / next * 100);
		}

		else if (identifier.equals("combat"))
			return String.valueOf(PlayerData.get(player).isInCombat());

		else if (identifier.equals("health"))
			return MMOCore.plugin.configManager.decimals.format(player.getHealth());

		else if (identifier.startsWith("attribute_"))
			return String.valueOf(PlayerData.get(player).getAttributes().getAttribute(MMOCore.plugin.attributeManager.get(identifier.substring(10).toLowerCase().replace("_", "-"))));

		else if (identifier.equals("class"))
			return PlayerData.get(player).getProfess().getName();

		else if (identifier.startsWith("profession_percent_")) {
			Professions professions = PlayerData.get(player).getCollectionSkills();
			String profession = identifier.substring(19).replace(" ", "-").replace("_", "-").toLowerCase();
			double current = professions.getExperience(profession), next = MMOCore.plugin.configManager.getNeededExperience(professions.getLevel(profession) + 1);
			return MMOCore.plugin.configManager.decimal.format(current / next * 100);
		}

		else if (identifier.startsWith("profession_"))
			return "" + PlayerData.get(player).getCollectionSkills().getLevel(identifier.substring(11).replace(" ", "-").replace("_", "-").toLowerCase());

		else if (identifier.equals("max_health"))
			return MMOCore.plugin.configManager.decimals.format(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());

		else if (identifier.equals("experience"))
			return "" + PlayerData.get(player).getExperience();

		else if (identifier.equals("next_level"))
			return "" + MMOCore.plugin.configManager.getNeededExperience(PlayerData.get(player).getLevel() + 1);

		else if (identifier.equals("class_points"))
			return "" + PlayerData.get(player).getClassPoints();

		else if (identifier.equals("skill_points"))
			return "" + PlayerData.get(player).getSkillPoints();

		else if (identifier.equals("attribute_points"))
			return "" + PlayerData.get(player).getAttributePoints();

		else if (identifier.equals("attribute_reallocation_points"))
			return "" + PlayerData.get(player).getAttributeReallocationPoints();

		else if (identifier.equals("mana"))
			return MMOCore.plugin.configManager.decimal.format(PlayerData.get(player).getMana());

		else if (identifier.equals("max_mana"))
			return MMOCore.plugin.configManager.decimal.format(PlayerData.get(player).getStats().getStat(StatType.MAX_MANA));

		else if (identifier.equals("mana_bar")) {
			String format = "";
			PlayerData data = PlayerData.get(player);
			double ratio = 20 * data.getMana() / data.getStats().getStat(StatType.MAX_MANA);
			for (double j = 1; j < 20; j++)
				format += (ratio >= j ? MMOCore.plugin.configManager.manaFull : ratio >= j - .5 ? MMOCore.plugin.configManager.manaHalf : MMOCore.plugin.configManager.manaEmpty) + AltChar.listSquare;
			return format;
		}

		else if (identifier.equals("stamina"))
			return MMOCore.plugin.configManager.decimal.format(PlayerData.get(player).getStamina());

		else if (identifier.equals("max_stamina"))
			return MMOCore.plugin.configManager.decimal.format(PlayerData.get(player).getStats().getStat(StatType.MAX_STAMINA));

		else if (identifier.equals("stamina_bar")) {
			String format = "";
			PlayerData data = PlayerData.get(player);
			double ratio = 20 * data.getStamina() / data.getStats().getStat(StatType.MAX_STAMINA);
			for (double j = 1; j < 20; j++)
				format += (ratio >= j ? MMOCore.plugin.configManager.staminaFull : ratio >= j - .5 ? MMOCore.plugin.configManager.staminaHalf : MMOCore.plugin.configManager.staminaEmpty) + AltChar.listSquare;
			return format;
		}

		else if (identifier.equals("stellium"))
			return MMOCore.plugin.configManager.decimal.format(PlayerData.get(player).getStellium());

		else if (identifier.equals("max_stellium"))
			return MMOCore.plugin.configManager.decimal.format(PlayerData.get(player).getStats().getStat(StatType.MAX_STELLIUM));

		else if (identifier.equals("stellium_bar")) {
			String format = "";
			PlayerData data = PlayerData.get(player);
			double ratio = 20 * data.getStellium() / data.getStats().getStat(StatType.MAX_STELLIUM);
			for (double j = 1; j < 20; j++)
				format += (ratio >= j ? ChatColor.BLUE : ratio >= j - .5 ? ChatColor.AQUA : ChatColor.WHITE) + AltChar.listSquare;
			return format;
		}

		else if (identifier.equals("quest")) {
			PlayerQuests data = PlayerData.get(player).getQuestData();
			return data.hasCurrent() ? data.getCurrent().getQuest().getName() : "None";
		}

		else if (identifier.equals("quest_progress")) {
			PlayerQuests data = PlayerData.get(player).getQuestData();
			return data.hasCurrent() ? MMOCore.plugin.configManager.decimal.format((int) (double) data.getCurrent().getObjectiveNumber() / data.getCurrent().getQuest().getObjectives().size() * 100) : "0";
		}

		else if (identifier.equals("quest_objective")) {
			PlayerQuests data = PlayerData.get(player).getQuestData();
			return data.hasCurrent() ? data.getCurrent().getFormattedLore() : "None";
		}

		else if (identifier.startsWith("stat_"))
			return StatType.valueOf(identifier.substring(5).toUpperCase()) != null ? "" + PlayerData.get(player).getStats().getStat(StatType.valueOf(identifier.substring(5).toUpperCase())) : "Invalid stat";
		else if (identifier.startsWith("formatted_stat_"))
			return StatType.valueOf(identifier.substring(5).toUpperCase()) != null ? "" + StatType.valueOf(identifier.substring(5).toUpperCase()).format(PlayerData.get(player).getStats().getStat(StatType.valueOf(identifier.substring(5).toUpperCase()))) : "Invalid stat";
			
		return null;
	}
}
