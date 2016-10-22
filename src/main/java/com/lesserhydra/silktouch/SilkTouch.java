package com.lesserhydra.silktouch;

import com.lesserhydra.bukkitutil.TileEntityUtil;
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

import java.util.EnumSet;

public class SilkTouch extends JavaPlugin implements Listener {
	
	private EnumSet<Material> enabledTypes = EnumSet.of(Material.MONSTER_EGGS, Material.MOB_SPAWNER);
	
	@Override
	public void onEnable() {
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
		TileEntityUtil.setTileEntity(event.getBlockPlaced(), itemMeta);
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
		
		meta.setDisplayName(ChatColor.BOLD.toString() + ChatColor.GRAY.toString() + spawner.getCreatureTypeName() + " Spawner");
		TileEntityUtil.setTileEntity((BlockStateMeta) meta, block);
		spawnerItem.setItemMeta(meta);
		
		return spawnerItem;
	}

}
