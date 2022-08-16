package net.Indyuce.mmocore.api.player;

public class TemporaryPlayerData {
    public final double mana, stamina, stellium;

    public TemporaryPlayerData(PlayerData player) {
        this.mana = player.getMana();
        this.stamina = player.getStamina();
        this.stellium = player.getStellium();
    }
}
