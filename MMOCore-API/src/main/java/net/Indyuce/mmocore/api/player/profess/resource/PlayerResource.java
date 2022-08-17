package net.Indyuce.mmocore.api.player.profess.resource;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.command.rpg.admin.ResourceCommandTreeNode;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent;
import net.Indyuce.mmocore.api.quest.trigger.ManaTrigger;
import net.Indyuce.mmocore.api.player.profess.ClassOption;
import org.bukkit.attribute.Attribute;

import java.util.function.BiConsumer;
import java.util.function.Function;

public enum PlayerResource {

    HEALTH(data -> data.getPlayer().getHealth(),
            data -> data.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(),
            (data, amount) -> data.heal(amount, PlayerResourceUpdateEvent.UpdateReason.REGENERATION),
            (data, amount) -> data.heal(amount, PlayerResourceUpdateEvent.UpdateReason.COMMAND),
            (data, amount) -> data.heal(-amount, PlayerResourceUpdateEvent.UpdateReason.COMMAND),
            (data, amount) -> data.getPlayer().setHealth(amount)),

    MANA(PlayerData::getMana,
            data -> data.getStats().getStat("MAX_MANA"),
            (data, amount) -> data.giveMana(amount, PlayerResourceUpdateEvent.UpdateReason.REGENERATION),
            (data, amount) -> data.giveMana(amount, PlayerResourceUpdateEvent.UpdateReason.COMMAND),
            (data, amount) -> data.giveMana(-amount, PlayerResourceUpdateEvent.UpdateReason.COMMAND),
            (data, amount) -> data.setMana(amount)),

    STAMINA(PlayerData::getStamina,
            data -> data.getStats().getStat("MAX_STAMINA"),
            (data, amount) -> data.giveStamina(amount, PlayerResourceUpdateEvent.UpdateReason.REGENERATION),
            (data, amount) -> data.giveStamina(amount, PlayerResourceUpdateEvent.UpdateReason.COMMAND),
            (data, amount) -> data.giveStamina(-amount, PlayerResourceUpdateEvent.UpdateReason.COMMAND),
            (data, amount) -> data.setStamina(amount)),

    STELLIUM(PlayerData::getStellium,
            data -> data.getStats().getStat("MAX_STELLIUM"),
            (data, amount) -> data.giveStellium(amount, PlayerResourceUpdateEvent.UpdateReason.REGENERATION),
            (data, amount) -> data.giveStellium(amount, PlayerResourceUpdateEvent.UpdateReason.COMMAND),
            (data, amount) -> data.giveStellium(-amount, PlayerResourceUpdateEvent.UpdateReason.COMMAND),
            (data, amount) -> data.setStellium(amount));

    private final String regenStat, maxRegenStat;
    private final ClassOption offCombatRegen;
    private final Function<PlayerData, Double> current, max;
    private final BiConsumer<PlayerData, Double> regen;

    // Used for MMOCore commands
    private final BiConsumer<PlayerData, Double> set, give, take;

    PlayerResource(Function<PlayerData, Double> current,
                   Function<PlayerData, Double> max,
                   BiConsumer<PlayerData, Double> regen,
                   BiConsumer<PlayerData, Double> give,
                   BiConsumer<PlayerData, Double> take,
                   BiConsumer<PlayerData, Double> set) {
        this.regenStat = name() + "_REGENERATION";
        this.maxRegenStat = "MAX_" + name() + "_REGENERATION";
        this.offCombatRegen = ClassOption.valueOf("OFF_COMBAT_" + name() + "_REGEN");
        this.current = current;
        this.max = max;
        this.regen = regen;
        this.give = give;
        this.take = take;
        this.set = set;
    }

    /**
     * @return Stat which corresponds to flat resource regeneration
     */
    public String getRegenStat() {
        return regenStat;
    }

    /**
     * @return Stat which corresponds to resource regeneration scaling with the player's max health
     */
    public String getMaxRegenStat() {
        return maxRegenStat;
    }

    /**
     * @return Class option which determines whether or not resource should be
     *         regenerated off combat only
     */
    public ClassOption getOffCombatRegen() {
        return offCombatRegen;
    }

    /**
     * @return Current resource of the given player
     */
    public double getCurrent(PlayerData player) {
        return current.apply(player);
    }

    /**
     * @return Max amount of that resource of the given player
     */
    public double getMax(PlayerData player) {
        return max.apply(player);
    }

    /**
     * Regens a player resource. Whatever resource, a bukkit event is triggered
     *
     * @param player Player to regen
     * @param amount Amount to regen
     */
    public void regen(PlayerData player, double amount) {
        regen.accept(player, amount);
    }

    /**
     * Used by MMOCore admin commands here: {@link ResourceCommandTreeNode}
     */
    public BiConsumer<PlayerData, Double> getConsumer(ManaTrigger.Operation operation) {
        switch (operation) {
            case SET:
                return set;
            case TAKE:
                return take;
            case GIVE:
                return give;
            default:
                throw new IllegalArgumentException("Operation cannot be null");
        }
    }
}
