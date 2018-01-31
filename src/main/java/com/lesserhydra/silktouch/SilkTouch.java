package com.lesserhydra.silktouch;

import com.lesserhydra.bukkitutil.TileEntityUtil;
import com.lesserhydra.bukkitutil.TileEntityWrapper;
import com.lesserhydra.hydracore.HydraCore;
import com.lesserhydra.util.Version;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

public class SilkTouch extends JavaPlugin implements Listener {
	
	private static final int CORE_MAJOR = 2;
	private static final int CORE_MINOR = 0;
	
	private EnumSet<Material> enabledTypes = EnumSet.of(Material.MONSTER_EGGS, Material.MOB_SPAWNER);
	
	@Override
	public void onEnable() {
		assert HydraCore.isLoaded();
		Version.Compat coreCompat = HydraCore.expectVersion(CORE_MAJOR, CORE_MINOR);
		if (coreCompat != Version.Compat.MATCH) {
			if (coreCompat.isOutdated()) {
				getLogger().severe("The loaded version of HydraCore is outdated! Please update to "
						+ CORE_MAJOR + "." + CORE_MINOR + "+.");
				//TODO: Link
			}
			else {
				getLogger().severe("The loaded version of HydraCore is incompatible with this " +
						"version of SilkTouch. Please update SilkTouch or downgrade HydraCore to "
						+ CORE_MAJOR + "." + CORE_MINOR + "+.");
				//TODO: Links
			}
			getPluginLoader().disablePlugin(this);
			return;
		}
		
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBroken(BlockBreakEvent event) {
		Block block = event.getBlock();
		
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
		if (!enabledTypes.contains(block.getType())) return;
		if (!isSilktouchPick(event.getPlayer().getInventory().getItemInMainHand())) return;
		
		ItemStack blockItem;
		if (block.getType() == Material.MOB_SPAWNER) blockItem = createSpawnerItem(block);
		else blockItem = createSilkTouchedItem(block);
		
		event.setCancelled(true);
		block.setType(Material.AIR);
		block.getWorld().dropItemNaturally(block.getLocation(), blockItem);
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onSilkItemPlace(BlockPlaceEvent event) {
		ItemStack placedItem = event.getItemInHand();
		if (placedItem.getType() != Material.MOB_SPAWNER) return;
		
		BlockStateMeta itemMeta = (BlockStateMeta) placedItem.getItemMeta();
		TileEntityWrapper tile = TileEntityUtil.getTileEntity(itemMeta);
		assert tile != null;
		CreatureSpawner spawner = (CreatureSpawner) event.getBlockPlaced().getState();
		tile.copyTo(spawner);
		spawner.update();
	}
	
	private static boolean isSilktouchPick(ItemStack item) {
		if (!item.containsEnchantment(Enchantment.SILK_TOUCH)) return false;
		if (item.getType() != Material.WOOD_PICKAXE && item.getType() != Material.STONE_PICKAXE
				&& item.getType() != Material.GOLD_PICKAXE && item.getType() != Material.IRON_PICKAXE
				&& item.getType() != Material.DIAMOND_PICKAXE) return false;
		return true;
	}
	
	@SuppressWarnings("deprecation")
	private static ItemStack createSilkTouchedItem(Block block) {
		ItemStack result = block.getState().getData().toItemStack(1);
		result.setDurability(block.getState().getData().getData());
		return result;
	}
	
	private static ItemStack createSpawnerItem(Block block) {
		ItemStack spawnerItem = new ItemStack(block.getType());
		ItemMeta meta = spawnerItem.getItemMeta();
		
		CreatureSpawner spawner = (CreatureSpawner) block.getState();
		spawner.setDelay(-1);
		TileEntityWrapper tile = TileEntityUtil.getTileEntity(spawner);
		
		String creatureName = determineName(spawner.getSpawnedType().name().toLowerCase());
		meta.setDisplayName(ChatColor.RESET + creatureName + " Spawner");
		
		tile.copyTo((BlockStateMeta) meta);
		spawnerItem.setItemMeta(meta);
		
		return spawnerItem;
	}

	private static String determineName(String identifier) {
		return Arrays.stream(identifier.split("_"))
				.filter(str -> str.length() > 0)
                .map(str -> Character.toUpperCase(str.charAt(0)) + str.substring(1))
				.collect(Collectors.joining(" "));
	}

}
