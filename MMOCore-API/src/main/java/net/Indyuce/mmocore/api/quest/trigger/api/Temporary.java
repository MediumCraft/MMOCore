package net.Indyuce.mmocore.api.quest.trigger.api;

/**
 * Non-permanent triggers are triggers which are not saved
 * by the player and taken off when the player logs off,
 * for instance temporary player modifiers. They need to
 * be re-applied everytime the player logs back.
 *
 * @author jules
 */
public interface Temporary extends Removable {
}
