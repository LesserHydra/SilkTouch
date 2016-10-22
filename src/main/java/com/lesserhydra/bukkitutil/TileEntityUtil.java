package com.lesserhydra.bukkitutil;

import net.minecraft.server.v1_10_R1.BlockPosition;
import net.minecraft.server.v1_10_R1.NBTTagCompound;
import net.minecraft.server.v1_10_R1.TileEntity;
import net.minecraft.server.v1_10_R1.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftMetaBlockState;

import java.lang.reflect.Field;

public class TileEntityUtil {
	
	private static Field BLOCK_ENTITY_TAG;
	
	static {
		try {
			BLOCK_ENTITY_TAG = CraftMetaBlockState.class.getDeclaredField("blockEntityTag");
			BLOCK_ENTITY_TAG.setAccessible(true);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
	}
	
	public static void setTileEntity(org.bukkit.block.Block to, org.bukkit.block.Block from) {
		setTileEntity(to, getTileEntity(from));
	}
	
	public static void setTileEntity(org.bukkit.block.Block to, org.bukkit.inventory.meta.BlockStateMeta from) {
		setTileEntity(to, getTileEntity(from));
	}
	
	public static void setTileEntity(org.bukkit.inventory.meta.BlockStateMeta to, org.bukkit.block.Block from) {
		setTileEntity(to, getTileEntity(from));
	}
	
	
	static TileEntity getTileEntity(org.bukkit.inventory.meta.BlockStateMeta meta) {
		NBTTagCompound blockEntityTag = null;
		try {
			blockEntityTag = (NBTTagCompound) BLOCK_ENTITY_TAG.get((CraftMetaBlockState) meta);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return (blockEntityTag == null) ? null : TileEntity.a(null, blockEntityTag);
	}
	
	static TileEntity getTileEntity(org.bukkit.block.Block block) {
		return ((CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
	}
	
	static void setTileEntity(Block block, TileEntity tile) {
		assert tile != null;
		
		World nmsWorld = ((CraftWorld) block.getWorld()).getHandle();
		BlockPosition position = new BlockPosition(block.getX(), block.getY(), block.getZ());
		
		NBTTagCompound compound = tile.save(new NBTTagCompound());
		compound.setInt("x", position.getX());
		compound.setInt("y", position.getY());
		compound.setInt("z", position.getZ());
		TileEntity destTile = nmsWorld.getTileEntity(position);
		destTile.a(compound);
		destTile.update();
	}
	
	static void setTileEntity(org.bukkit.inventory.meta.BlockStateMeta blockMeta, TileEntity tile) {
		assert(tile != null);
		
		NBTTagCompound blockEntityTag = tile.save(new NBTTagCompound());
		blockEntityTag.remove("x");
		blockEntityTag.remove("y");
		blockEntityTag.remove("z");
		try {
			BLOCK_ENTITY_TAG.set(blockMeta, blockEntityTag);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
}
