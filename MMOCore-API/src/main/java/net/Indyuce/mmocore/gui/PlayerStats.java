package net.Indyuce.mmocore.gui;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.stat.modifier.StatModifier;
import io.lumine.mythic.lib.manager.StatManager;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.format.DelayFormat;
import net.Indyuce.mmocore.experience.Booster;
import net.Indyuce.mmocore.experience.Profession;
import net.Indyuce.mmocore.gui.api.EditableInventory;
import net.Indyuce.mmocore.gui.api.GeneratedInventory;
import net.Indyuce.mmocore.gui.api.InventoryClickContext;
import net.Indyuce.mmocore.gui.api.item.InventoryItem;
import net.Indyuce.mmocore.gui.api.item.Placeholders;
import net.Indyuce.mmocore.gui.api.item.SimplePlaceholderItem;
import net.Indyuce.mmocore.party.AbstractParty;
import net.Indyuce.mmocore.player.stats.StatInfo;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Objects;

public class PlayerStats extends EditableInventory {
    public PlayerStats() {
        super("player-stats");
    }

    @Override
    public InventoryItem load(String function, ConfigurationSection config) {

        if (function.equals("boost"))
            return new BoostItem(config);

        if (function.equals("boost-next"))
            return new SimplePlaceholderItem<PlayerStatsInventory>(config) {

                @Override
                public boolean hasDifferentDisplay() {
                    return true;
                }

                @Override
                public boolean canDisplay(PlayerStatsInventory inv) {
                    InventoryItem boost = inv.getByFunction("boost");
                    return boost != null && inv.boostOffset + boost.getSlots().size() < MMOCore.plugin.boosterManager.getActive().size();
                }
            };

        if (function.equals("boost-previous"))
            return new SimplePlaceholderItem<PlayerStatsInventory>(config) {

                @Override
                public boolean canDisplay(PlayerStatsInventory inv) {
                    return inv.boostOffset > 0;
                }
            };

        if (function.equals("party"))
            return new PartyMoraleItem(config);

        if (function.startsWith("profession_")) {
            String id = function.substring("profession_".length()).toLowerCase();
            Validate.isTrue(MMOCore.plugin.professionManager.has(id));
            Profession profession = MMOCore.plugin.professionManager.get(id);

            return new InventoryItem<PlayerStatsInventory>(config) {

                @Override
                public boolean hasDifferentDisplay() {
                    return true;
                }

                @Override
                public Placeholders getPlaceholders(PlayerStatsInventory inv, int n) {

                    Placeholders holders = new Placeholders();
                    net.Indyuce.mmocore.api.player.stats.PlayerStats stats = inv.target.getStats();

                    double ratio = inv.target.getCollectionSkills().getExperience(profession)
                            / (double) inv.target.getCollectionSkills().getLevelUpExperience(profession);

                    String bar = "" + ChatColor.BOLD;
                    int chars = (int) (ratio * 20);
                    for (int j = 0; j < 20; j++)
                        bar += (j == chars ? "" + ChatColor.WHITE + ChatColor.BOLD : "") + "|";

                    // holders.register("profession", type.getName());
                    holders.register("progress", bar);
                    holders.register("level", "" + inv.target.getCollectionSkills().getLevel(profession));
                    holders.register("xp", inv.target.getCollectionSkills().getExperience(profession));
                    holders.register("percent", decimal.format(ratio * 100));
                    for (StatInfo stat : MMOCore.plugin.statManager.getLoaded())
                        if (Objects.equals(stat.profession, profession))
                            holders.register(stat.name.toLowerCase(), StatManager.format(stat.name, stats.getStat(stat.name)));

                    return holders;
                }
            };
        }

        if (function.equals("profile"))
            return new PlayerProfileItem(config);

        if (function.equals("stats"))
            return new InventoryItem<PlayerStatsInventory>(config) {

                @Override
                public boolean hasDifferentDisplay() {
                    return true;
                }

                @Override
                public Placeholders getPlaceholders(PlayerStatsInventory inv, int n) {
                    return new Placeholders() {
                        final net.Indyuce.mmocore.api.player.stats.PlayerStats stats = inv.target.getStats();

                        public String apply(Player player, String str) {
                            while (str.contains("{") && str.substring(str.indexOf("{")).contains("}")) {
                                final String holder = str.substring(str.indexOf("{") + 1, str.indexOf("}"));
                                String replaced;
                                if (holder.endsWith("_base")) {
                                    final String stat = UtilityMethods.enumName(holder.substring(0, holder.length() - 5));
                                    replaced = StatManager.format(stat, stats.getBase(stat));
                                } else if (holder.endsWith("_extra")) {
                                    final String stat = UtilityMethods.enumName(holder.substring(0, holder.length() - 6));
                                    replaced = StatManager.format(stat, MythicLib.plugin.getStats().getTotalValue(stat, stats.getMap()) - stats.getBase(stat));
                                } else if (holder.startsWith("attribute_")) {
                                    final PlayerAttribute attr = MMOCore.plugin.attributeManager.get(holder.substring(10).replace("_", "-").toLowerCase());
                                    replaced = String.valueOf(inv.target.getAttributes().getAttribute(attr));
                                } else {
                                    final String stat = UtilityMethods.enumName(holder);
                                    replaced = StatManager.format(stat, MythicLib.plugin.getStats().getTotalValue(stat, stats.getMap()));
                                }

                                str = str.replace("{" + holder + "}", replaced);
                            }

                            // External placeholders
                            return MMOCore.plugin.placeholderParser.parse(player, str);
                        }
                    };
                }
            };

        return new SimplePlaceholderItem(config);
    }

    public PlayerStatsInventory newInventory(PlayerData invTarget, PlayerData opening) {
        return new PlayerStatsInventory(invTarget, opening, this);
    }

    public PlayerStatsInventory newInventory(PlayerData player) {
        return new PlayerStatsInventory(player, player, this);
    }

    public class PlayerStatsInventory extends GeneratedInventory {
        private final PlayerData target;

        private int boostOffset;

        /**
         * @param invTarget Target player
         * @param opening   Player opening the inventory
         * @param inv       Corresponding editable inventory
         */
        public PlayerStatsInventory(PlayerData invTarget, PlayerData opening, EditableInventory inv) {
            super(opening, inv);

            this.target = invTarget;
        }

        @Override
        public String calculateName() {
            return getName();
        }

        @Override
        public void whenClicked(InventoryClickContext event, InventoryItem item) {
            if (item.hasFunction())
                if (item.getFunction().equals("boost-next")) {
                    boostOffset++;
                    open();

                } else if (item.getFunction().equals("boost-previous")) {
                    boostOffset--;
                    open();
                }
        }
    }

    private ItemStack amount(ItemStack item, int amount) {
        item.setAmount(amount);
        return item;
    }

    public static class PartyMoraleItem extends InventoryItem<PlayerStatsInventory> {
        public PartyMoraleItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public Placeholders getPlaceholders(PlayerStatsInventory inv, int n) {
            Placeholders holders = new Placeholders();

            int count = inv.target.getParty().getOnlineMembers().size();
            holders.register("count", "" + count);
            for (StatModifier buff : MMOCore.plugin.partyManager.getBonuses())
                holders.register("buff_" + buff.getStat().toLowerCase(), buff.multiply(count - 1).toString());

            return holders;
        }

        @Override
        public boolean canDisplay(PlayerStatsInventory inv) {
            AbstractParty party = inv.target.getParty();
            return party != null && party.getOnlineMembers().size() > 1;
        }
    }

    public static class PlayerProfileItem extends InventoryItem<PlayerStatsInventory> {
        public PlayerProfileItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public ItemStack display(PlayerStatsInventory inv, int n) {
            ItemStack disp = super.display(inv, n);
            if (disp.getType() == VersionMaterial.PLAYER_HEAD.toMaterial()) {
                SkullMeta meta = (SkullMeta) disp.getItemMeta();
                inv.dynamicallyUpdateItem(this, n, disp, current -> {
                    meta.setOwningPlayer(inv.target.getPlayer());
                    current.setItemMeta(meta);
                });
            }
            return disp;
        }

        @Override
        public Placeholders getPlaceholders(PlayerStatsInventory inv, int n) {
            PlayerData data = inv.target;
            Placeholders holders = new Placeholders();

            int nextLevelExp = inv.target.getLevelUpExperience();
            double ratio = (double) data.getExperience() / (double) nextLevelExp;

            StringBuilder bar = new StringBuilder("" + ChatColor.BOLD);
            int chars = (int) (ratio * 20);
            for (int j = 0; j < 20; j++)
                bar.append(j == chars ? "" + ChatColor.WHITE + ChatColor.BOLD : "").append("|");

            holders.register("percent", decimal.format(ratio * 100));
            holders.register("exp", MythicLib.plugin.getMMOConfig().decimal.format(data.getExperience()));
            holders.register("level", "" + data.getLevel());
            holders.register("class_points", "" + data.getClassPoints());
            holders.register("skill_points", "" + data.getSkillPoints());
            holders.register("attribute_points", "" + data.getAttributePoints());
            holders.register("progress", bar.toString());
            holders.register("next_level", "" + nextLevelExp);
            if (data.isOnline())
                holders.register("player", "" + data.getPlayer().getName());
            holders.register("class", "" + data.getProfess().getName());

            return holders;
        }
    }

    public class BoostItem extends SimplePlaceholderItem<PlayerStatsInventory> {
        private final InventoryItem noBoost, mainLevel, profession;

        public BoostItem(ConfigurationSection config) {
            super(config);

            ConfigurationSection noBoost = config.getConfigurationSection("no-boost");
            Validate.notNull(noBoost, "Could not load 'no-boost' config");
            this.noBoost = new SimplePlaceholderItem(noBoost);

            ConfigurationSection mainLevel = config.getConfigurationSection("main-level");
            Validate.notNull(mainLevel, "Could not load 'main-level' config");
            this.mainLevel = new InventoryItem<PlayerStatsInventory>(mainLevel) {

                @Override
                public boolean hasDifferentDisplay() {
                    return true;
                }

                @Override
                public Placeholders getPlaceholders(PlayerStatsInventory inv, int n) {
                    Placeholders holders = new Placeholders();
                    Booster boost = MMOCore.plugin.boosterManager.get(inv.boostOffset + n);

                    holders.register("author", boost.hasAuthor() ? boost.getAuthor() : "Server");
                    holders.register("value", (int) (boost.getExtra() * 100));
                    holders.register("left", boost.isTimedOut() ?
                            MMOCore.plugin.configManager.getSimpleMessage("booster-expired").message()
                            : new DelayFormat(2).format(boost.getLeft()));

                    return holders;
                }
            };

            ConfigurationSection profession = config.getConfigurationSection("profession");
            Validate.notNull(profession, "Could not load 'profession' config");
            this.profession = new InventoryItem<PlayerStatsInventory>(profession) {

                @Override
                public boolean hasDifferentDisplay() {
                    return true;
                }

                @Override
                public Placeholders getPlaceholders(PlayerStatsInventory inv, int n) {
                    Placeholders holders = new Placeholders();
                    Booster boost = MMOCore.plugin.boosterManager.get(inv.boostOffset + n);

                    holders.register("author", boost.hasAuthor() ? boost.getAuthor() : "Server");
                    holders.register("profession", boost.getProfession().getName());
                    holders.register("value", (int) (boost.getExtra() * 100));
                    holders.register("left", boost.isTimedOut() ?
                            MMOCore.plugin.configManager.getSimpleMessage("booster-expired").message()
                            : new DelayFormat(2).format(boost.getLeft()));

                    return holders;
                }
            };
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public ItemStack display(PlayerStatsInventory inv, int n) {
            int offset = inv.boostOffset;
            if (n + offset >= MMOCore.plugin.boosterManager.getActive().size())
                return noBoost.display(inv, n);

            Booster boost = MMOCore.plugin.boosterManager.get(inv.boostOffset + n);
            return amount(boost.hasProfession() ? profession.display(inv, n) : mainLevel.display(inv, n), n + offset + 1);
        }
    }
}
