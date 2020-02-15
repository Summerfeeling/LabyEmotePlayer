package de.summerfeeling.labyemoteplayer.utils;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class LocationUtils {
	
	public static final BlockFace[] AXIS = { BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST };
	public static final BlockFace[] RADIAL = { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };
	
	public static BlockFace getDirection(Player player) {
		return getDirection(player.getLocation().getYaw());
	}
	
	public static BlockFace getDirection(Location location) {
		return getDirection(location.getYaw());
	}
	
	public static BlockFace getDirection(double yaw) {
		return LocationUtils.AXIS[(int) (Math.round(yaw / 90.0F) & 0x3)];
	}
	
	public static BlockFace getSubCardinalDirection(Player player) {
		return getSubCardinalDirection(player.getLocation().getYaw());
	}
	
	public static BlockFace getSubCardinalDirection(Location location) {
		return getSubCardinalDirection(location.getYaw());
	}
	
	public static BlockFace getSubCardinalDirection(double yaw) {
		return LocationUtils.RADIAL[(int) (Math.round(yaw / 45.0F) & 0x7)];
	}
	
}

