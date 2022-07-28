package net.Indyuce.mmocore.api.player;

import net.Indyuce.mmocore.api.player.attribute.PlayerAttributes;
import net.Indyuce.mmocore.api.quest.PlayerQuests;
import net.Indyuce.mmocore.experience.PlayerProfessions;
import net.Indyuce.mmocore.skill.ClassSkill;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Just a container holding the basic playerData information that are needed to save it in a database.
 */
public record SavingPlayerData(
        UUID uuid,
        int classPoints,
        int skillPoints,
        int attributePoints,
        int attributeReallocationPoints,
        int level,
        double experience,
        String classId,
        long lastLogin,
        String guildId,
        Set<String> waypoints,
        List<UUID> friends,
        List<String> boundSkills,
        Map<String,Integer> skills,
        Map<String,Integer> itemClaims,
        String attributes,
        String collectionsSkills,
        String questData,
        String classInfoData)
{





}
