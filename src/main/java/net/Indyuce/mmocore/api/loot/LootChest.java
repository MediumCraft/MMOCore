package net.Indyuce.mmocore.api.loot;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class LootChest {
	private final LootChestRegion region;

	/*
	 * saves data of block replaced
	 */
	private final ReplacedBlock block;

	public LootChest(LootChestRegion region, Block block) {
		this.region = region;
		this.block = new ReplacedBlock(block);
	}

	public ReplacedBlock getBlock() {
		return block;
	}

	public LootChestRegion getRegion() {
		return region;
	}

	public class ReplacedBlock {
		private final Material material;
		private final BlockData data;
		private final Location loc;

		public ReplacedBlock(Block block) {
			this.material = block.getType();
			this.data = block.getBlockData();
			loc = block.getLocation();
		}

		public void restore() {
			loc.getBlock().setType(material);
			loc.getBlock().setBlockData(data);
		}
	}
}
