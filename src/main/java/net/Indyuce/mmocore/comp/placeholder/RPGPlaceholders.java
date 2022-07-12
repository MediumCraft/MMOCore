package net.Indyuce.mmocore.comp.placeholder;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.util.AltChar;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.quest.PlayerQuests;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.player.stats.StatInfo;
import net.Indyuce.mmocore.experience.PlayerProfessions;
import net.Indyuce.mmocore.experience.Profession;
import net.Indyuce.mmocore.party.AbstractParty;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;

import java.util.Objects;


public class RPGPlaceholders extends PlaceholderExpansion {
	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public boolean canRegister() {
		return true;
	}

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
		return MMOCore.plugin.getDescription().getVersion();
	}

	@SuppressWarnings("DuplicateExpressions")
	@Override
	public String onRequest(OfflinePlayer player, String identifier) {
		if (!PlayerData.has(player.getUniqueId()))
			return null;

		PlayerData playerData = PlayerData.get(player);
		if (identifier.equals("mana_icon"))
			return playerData.getProfess().getManaDisplay().getIcon();
		if (identifier.equals("mana_name"))
			return playerData.getProfess().getManaDisplay().getName();

		if (identifier.equals("level"))
			return "" + playerData.getLevel();

		else if (identifier.startsWith("skill_level_")) {
			String id = identifier.substring(12);
			RegisteredSkill skill = Objects.requireNonNull(MMOCore.plugin.skillManager.getSkill(id), "Could not find skill with ID '" + id + "'");
			return String.valueOf(playerData.getSkillLevel(skill));
		}

		else if (identifier.equals("level_percent")) {
			double current = playerData.getExperience(), next = playerData.getLevelUpExperience();
			return MythicLib.plugin.getMMOConfig().decimal.format(current / next * 100);
		}

		else if (identifier.equals("health") && player.isOnline()) {
			return StatInfo.valueOf("MAX_HEALTH").format(player.getPlayer().getHealth());
		}

		else if (identifier.equals("max_health") && player.isOnline()) {
			return StatInfo.valueOf("MAX_HEALTH").format(player.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
		}

		else if (identifier.equals("health_bar") && player.isOnline()) {
			StringBuilder format = new StringBuilder();
			double ratio = 20 * player.getPlayer().getHealth() / player.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
			for (double j = 1; j < 20; j++)
				format.append(ratio >= j ? ChatColor.RED : ratio >= j - .5 ? ChatColor.DARK_RED : ChatColor.DARK_GRAY).append(AltChar.listSquare);
			return format.toString();
		}

		else if (identifier.equals("class"))
			return playerData.getProfess().getName();

		else if (identifier.startsWith("profession_percent_")) {
			PlayerProfessions professions = playerData.getCollectionSkills();
			String name = identifier.substring(19).replace(" ", "-").replace("_", "-").toLowerCase();
			Profession profession = MMOCore.plugin.professionManager.get(name);
			double current = professions.getExperience(profession), next = professions.getLevelUpExperience(profession);
			return MythicLib.plugin.getMMOConfig().decimal.format(current / next * 100);
		}

		else if (identifier.startsWith("is_casting")) {
			return String.valueOf(playerData.isCasting());
		} else if (identifier.startsWith("in_combat")) {
			return String.valueOf(playerData.isInCombat());
		}

		else if (identifier.startsWith("bound_")) {
			int slot = Math.max(0, Integer.parseInt(identifier.substring(6)) - 1);
			return playerData.hasSkillBound(slot) ? playerData.getBoundSkill(slot).getSkill().getName()
					: MMOCore.plugin.configManager.noSkillBoundPlaceholder;
		}

		else if (identifier.startsWith("profession_experience_"))
			return String.valueOf(
					playerData.getCollectionSkills().getExperience(identifier.substring(22).replace(" ", "-").replace("_", "-").toLowerCase()));

		else if (identifier.startsWith("profession_next_level_"))
			return "" + PlayerData.get(player).getCollectionSkills()
					.getLevelUpExperience(identifier.substring(22).replace(" ", "-").replace("_", "-").toLowerCase());

		else if (identifier.startsWith("party_count")) {
			AbstractParty party = playerData.getParty();
			return party == null ? "0" : String.valueOf(party.countMembers());
		}

		else if (identifier.startsWith("profession_"))
			return String
					.valueOf(playerData.getCollectionSkills().getLevel(identifier.substring(11).replace(" ", "-").replace("_", "-").toLowerCase()));

		else if (identifier.equals("experience"))
			return MythicLib.plugin.getMMOConfig().decimal.format(playerData.getExperience());

		else if (identifier.equals("next_level"))
			return String.valueOf(playerData.getLevelUpExperience());

		else if (identifier.equals("class_points"))
			return String.valueOf(playerData.getClassPoints());

		else if (identifier.equals("skill_points"))
			return String.valueOf(playerData.getSkillPoints());

		else if (identifier.equals("attribute_points"))
			return String.valueOf(playerData.getAttributePoints());

		else if (identifier.equals("attribute_reallocation_points"))
			return String.valueOf(playerData.getAttributeReallocationPoints());

		else if (identifier.startsWith("attribute_"))
			return String.valueOf(playerData.getAttributes()
					.getAttribute(MMOCore.plugin.attributeManager.get(identifier.substring(10).toLowerCase().replace("_", "-"))));

		else if (identifier.equals("mana"))
			return MythicLib.plugin.getMMOConfig().decimal.format(playerData.getMana());

		else if (identifier.equals("mana_bar")) {
			return playerData.getProfess().getManaDisplay().generateBar(playerData.getMana(), playerData.getStats().getStat("MAX_MANA"));
		}

		else if (identifier.startsWith("exp_multiplier_")) {
			String format = identifier.substring(15).toLowerCase().replace("_", "-").replace(" ", "-");
			Profession profession = format.equals("main") ? null : MMOCore.plugin.professionManager.get(format);
			return MythicLib.plugin.getMMOConfig().decimal.format(MMOCore.plugin.boosterManager.getMultiplier(profession) * 100);
		}

		else if (identifier.startsWith("exp_boost_")) {
			String format = identifier.substring(10).toLowerCase().replace("_", "-").replace(" ", "-");
			Profession profession = format.equals("main") ? null : MMOCore.plugin.professionManager.get(format);
			return MythicLib.plugin.getMMOConfig().decimal.format((MMOCore.plugin.boosterManager.getMultiplier(profession) - 1) * 100);
		}

		else if (identifier.equals("stamina"))
			return MythicLib.plugin.getMMOConfig().decimal.format(playerData.getStamina());

		else if (identifier.equals("stamina_bar")) {
			StringBuilder format = new StringBuilder();
			double ratio = 20 * playerData.getStamina() / playerData.getStats().getStat("MAX_STAMINA");
			for (double j = 1; j < 20; j++)
				format.append(ratio >= j ? MMOCore.plugin.configManager.staminaFull
						: ratio >= j - .5 ? MMOCore.plugin.configManager.staminaHalf : MMOCore.plugin.configManager.staminaEmpty)
						.append(AltChar.listSquare);
			return format.toString();
		}

		else if (identifier.startsWith("stat_")) {
			StatInfo info = StatInfo.valueOf(identifier.substring(5).toUpperCase());
			return info.format(playerData.getStats().getStat(info.name));
		}

		else if (identifier.equals("stellium"))
			return MythicLib.plugin.getMMOConfig().decimal.format(playerData.getStellium());

		else if (identifier.equals("stellium_bar")) {
			StringBuilder format = new StringBuilder();
			double ratio = 20 * playerData.getStellium() / playerData.getStats().getStat("MAX_STELLIUM");
			for (double j = 1; j < 20; j++)
				format.append(ratio >= j ? ChatColor.BLUE : ratio >= j - .5 ? ChatColor.AQUA : ChatColor.WHITE).append(AltChar.listSquare);
			return format.toString();
		}

		else if (identifier.equals("quest")) {
			PlayerQuests data = playerData.getQuestData();
			return data.hasCurrent() ? data.getCurrent().getQuest().getName() : "None";
		}

		else if (identifier.equals("quest_progress")) {
			PlayerQuests data = playerData.getQuestData();

			return data.hasCurrent() ? MythicLib.plugin.getMMOConfig().decimal
					.format( (double) data.getCurrent().getObjectiveNumber() / data.getCurrent().getQuest().getObjectives().size() * 100L) : "0";
		}

		else if (identifier.equals("quest_objective")) {
			PlayerQuests data = playerData.getQuestData();
			return data.hasCurrent() ? data.getCurrent().getFormattedLore() : "None";
		}

		else if (identifier.startsWith("guild_")) {
			String placeholder = identifier.substring(6);
			if (playerData.getGuild() == null)
				return "";

			if (placeholder.equalsIgnoreCase("name"))
				return playerData.getGuild().getName();
			else if (placeholder.equalsIgnoreCase("tag"))
				return playerData.getGuild().getTag();
			else if (placeholder.equalsIgnoreCase("leader"))
				return Bukkit.getOfflinePlayer(playerData.getGuild().getOwner()).getName();
			else if (placeholder.equalsIgnoreCase("members"))
				return String.valueOf(playerData.getGuild().countMembers());
			else if (placeholder.equalsIgnoreCase("online_members"))
				return String.valueOf(playerData.getGuild().countOnlineMembers());
		}

		return null;
	}
}
