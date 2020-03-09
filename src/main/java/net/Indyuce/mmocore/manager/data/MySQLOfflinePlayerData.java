package net.Indyuce.mmocore.manager.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.OfflinePlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.api.util.MMOSQL;
import net.Indyuce.mmocore.api.util.MMOSQL.Table;

public class MySQLOfflinePlayerData extends OfflinePlayerData {
	int level;
	long lastLogin;
	PlayerClass profess;
	List<UUID> friends;
	
	public MySQLOfflinePlayerData(UUID uuid) {
		super(uuid);
		
		ResultSet result = MMOSQL.getResult("SELECT * FROM mmocore_playerdata WHERE uuid = '" + uuid + "';");

		try {
			if(!result.next()) {
				level = 0;
				lastLogin = 0;
				profess = MMOCore.plugin.classManager.getDefaultClass();
				friends = new ArrayList<UUID>();
			}
			else while(result.next()) {
				level = result.getInt("level");
				lastLogin = result.getLong("last_login");
				profess = result.getString("class").equalsIgnoreCase("null") ? MMOCore.plugin.classManager.getDefaultClass()
						: MMOCore.plugin.classManager.get(result.getString("class"));
				if(!result.getString("friends").equalsIgnoreCase("null"))
					MMOSQL.getJSONArray(result.getString("friends")).forEach(str -> friends.add(UUID.fromString(str)));
				else friends = new ArrayList<UUID>();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void removeFriend(UUID uuid) {
		friends.remove(uuid);
		new MMOSQL(Table.PLAYERDATA, uuid).updateData("friends",
			friends.stream().map(friend -> friend.toString()).collect(Collectors.toList()));
	}

	@Override
	public boolean hasFriend(UUID uuid) {
		return friends.contains(uuid);
	}

	@Override
	public PlayerClass getProfess() {
		return profess; 
	}

	@Override
	public int getLevel() {
		return level;
	}

	@Override
	public long getLastLogin() {
		return lastLogin;
	}
}
