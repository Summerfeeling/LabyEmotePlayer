package de.summerfeeling.labyemoteplayer.npc;

import com.mojang.authlib.GameProfile;
import de.summerfeeling.labyemoteplayer.LabyEmotePlayer;
import de.summerfeeling.labyemoteplayer.utils.GameProfileBuilder;
import de.summerfeeling.labyemoteplayer.utils.LocationUtils;
import de.summerfeeling.labyemoteplayer.utils.reflect.FieldAccessor;
import de.summerfeeling.labyemoteplayer.utils.reflect.Reflect;
import net.minecraft.server.v1_8_R3.*;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity.PacketPlayOutRelEntityMove;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.PlayerInfoData;
import net.minecraft.server.v1_8_R3.ScoreboardTeamBase.EnumNameTagVisibility;
import net.minecraft.server.v1_8_R3.WorldSettings.EnumGamemode;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftChatMessage;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.util.*;

public class EmotePlayer {
	
	/* Reflection cache */
	private static final double FORCEFIELD_RADIUS = Math.pow(LabyEmotePlayer.getConfiguration().getForcefieldRadius(), 2);
	private static final double VIEW_DISTANCE = Math.pow(LabyEmotePlayer.getConfiguration().getViewDistance(), 2);
	private static boolean FORCEFIELD_ENABLED = LabyEmotePlayer.getConfiguration().isForcefieldEnabled();
	
	private static final Random RANDOM = new Random();
	
	private static final FieldAccessor SPAWN_ID = Reflect.getField(PacketPlayOutNamedEntitySpawn.class, "a");
	private static final FieldAccessor SPAWN_UUID = Reflect.getField(PacketPlayOutNamedEntitySpawn.class, "b");
	private static final FieldAccessor SPAWN_X = Reflect.getField(PacketPlayOutNamedEntitySpawn.class, "c");
	private static final FieldAccessor SPAWN_Y = Reflect.getField(PacketPlayOutNamedEntitySpawn.class, "d");
	private static final FieldAccessor SPAWN_Z = Reflect.getField(PacketPlayOutNamedEntitySpawn.class, "e");
	private static final FieldAccessor SPAWN_YAW = Reflect.getField(PacketPlayOutNamedEntitySpawn.class, "f");
	private static final FieldAccessor SPAWN_PITCH = Reflect.getField(PacketPlayOutNamedEntitySpawn.class, "g");
	private static final FieldAccessor SPAWN_WATCHER = Reflect.getField(PacketPlayOutNamedEntitySpawn.class, "i");
	
	private static final FieldAccessor HEAD_ID = Reflect.getField(PacketPlayOutEntityHeadRotation.class, "a");
	private static final FieldAccessor HEAD_YAW = Reflect.getField(PacketPlayOutEntityHeadRotation.class, "b");
	
	private static final FieldAccessor REL_X = Reflect.getField(PacketPlayOutRelEntityMove.class, "b");
	private static final FieldAccessor REL_Z = Reflect.getField(PacketPlayOutRelEntityMove.class, "d");
	
	private static final FieldAccessor PROFILE_ID = Reflect.getField(GameProfile.class, "id");
	private static final FieldAccessor PROFILE_NAME = Reflect.getField(GameProfile.class, "name");
	
	private static final FieldAccessor INFO_PLAYERS = Reflect.getField(PacketPlayOutPlayerInfo.class, "b");
	
	private static final FieldAccessor TEAM_NAME = Reflect.getField(PacketPlayOutScoreboardTeam.class, "a");
	private static final FieldAccessor TEAM_DISPLAY_NAME = Reflect.getField(PacketPlayOutScoreboardTeam.class, "b");
	private static final FieldAccessor TEAM_PREFIX = Reflect.getField(PacketPlayOutScoreboardTeam.class, "c");
	private static final FieldAccessor TEAM_SUFFIX = Reflect.getField(PacketPlayOutScoreboardTeam.class, "d");
	private static final FieldAccessor TEAM_NAMETAG_VISIBILITY = Reflect.getField(PacketPlayOutScoreboardTeam.class, "e");
	private static final FieldAccessor TEAM_ACTION = Reflect.getField(PacketPlayOutScoreboardTeam.class, "h");
	private static final FieldAccessor TEAM_PLAYERS = Reflect.getField(PacketPlayOutScoreboardTeam.class, "g");

	/* Local variables */
	
	private List<UUID> visibleTo = new ArrayList<>();
	
	private GameProfile gameProfile;
	private UUID textureOwner;
	
	private String displayName;
	private String teamPrefix;
	private String teamSuffix;
	private String teamName;
	
	private Location location;
	private int entityId;
	
	public EmotePlayer(String displayName, Location location, UUID textureOwner) {
		if (displayName.length() > 48) displayName.substring(0, 48);
		this.displayName = displayName;
		
		if (displayName.length() > 32) {
			this.teamName = "lep_" + RandomStringUtils.randomAlphanumeric(8);
			this.teamPrefix = displayName.substring(0, 16);
			this.displayName = displayName.substring(16, 32);
			this.teamSuffix = displayName.substring(32);
		} else if (displayName.length() > 16) {
			this.teamName = "lep_" + RandomStringUtils.randomAlphanumeric(8);
			this.teamPrefix = displayName.substring(0, 16);
			this.displayName = displayName.substring(16);
		} else {
			this.displayName = displayName;
		}
		
		this.entityId = -RANDOM.nextInt(10000);
		this.textureOwner = textureOwner;
		this.location = location;
		
		try {
			this.gameProfile = new GameProfile(new UUID(RANDOM.nextLong(), 0), this.displayName);
			this.gameProfile.getProperties().putAll(GameProfileBuilder.fetch(textureOwner).getProperties());
			
			EmotePlayer.PROFILE_NAME.set(gameProfile, this.displayName);
			EmotePlayer.PROFILE_ID.set(gameProfile, new UUID(RANDOM.nextLong(), 0));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void spawn(Player player) {
		PacketPlayOutNamedEntitySpawn spawnPacket = new PacketPlayOutNamedEntitySpawn();
		PacketPlayOutEntityHeadRotation rotationPacket = new PacketPlayOutEntityHeadRotation();
		
		DataWatcher dataWatcher = new DataWatcher(null);
		dataWatcher.a(6, 20.0F);
		dataWatcher.a(10, (byte) 0xFF);
		
		this.addToTablist(player);
		
		EmotePlayer.SPAWN_ID.set(spawnPacket, entityId);
		EmotePlayer.SPAWN_UUID.set(spawnPacket, gameProfile.getId());
		EmotePlayer.SPAWN_X.set(spawnPacket, toFixedPoint(location.getBlockX() + 0.5D));
		EmotePlayer.SPAWN_Y.set(spawnPacket, toFixedPoint(location.getBlockY()));
		EmotePlayer.SPAWN_Z.set(spawnPacket, toFixedPoint(location.getBlockZ() + 0.5D));
		EmotePlayer.SPAWN_YAW.set(spawnPacket, toPackedByte(location.getYaw()));
		EmotePlayer.SPAWN_PITCH.set(spawnPacket, toPackedByte(location.getPitch()));
		EmotePlayer.SPAWN_WATCHER.set(spawnPacket, dataWatcher);
		
		EmotePlayer.HEAD_ID.set(rotationPacket, entityId);
		EmotePlayer.HEAD_YAW.set(rotationPacket, toPackedByte(location.getYaw()));
		
		if (player != null) {
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(spawnPacket);
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(rotationPacket);
		} else {
			for (Entity nearby : location.getWorld().getNearbyEntities(location, 40, 40, 40)) {
				if (!(nearby instanceof Player)) continue;
				
				((CraftPlayer) nearby).getHandle().playerConnection.sendPacket(spawnPacket);
				((CraftPlayer) nearby).getHandle().playerConnection.sendPacket(rotationPacket);
			}
		}
		
		this.uglyFixingOddPosition(player, false);
		this.createTeam(player);
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(LabyEmotePlayer.getInstance(), () -> this.removeFromTablist(player), 24L);
	}
	
	// Body rotation is calculated client-sided. So I'll move the npc two blocks to let the client calculate a body rotation - fixes for n/s/w/e, but not for subcardinal directions (ne/se/sw/nw)
	public void uglyFixingOddPosition(Player player, boolean force) {
		if (!LabyEmotePlayer.getConfiguration().isPositionFixEnabled() && !force) return;
		BlockFace facing = LocationUtils.getSubCardinalDirection(location);
		
		PacketPlayOutRelEntityMove movePacket = new PacketPlayOutRelEntityMove(entityId, (byte) toFixedPoint(facing.getModX() * 2), (byte) toFixedPoint(0), (byte) toFixedPoint(facing.getModZ() * 2), true);
		
		new BukkitRunnable() {
			private int i = 0;
			
			public void run() {
				if (i == 2) {
					EmotePlayer.this.sendPacket(player, movePacket);
				} else if (i == 3) {
					EmotePlayer.REL_X.set(movePacket, (byte) toFixedPoint(facing.getOppositeFace().getModX()));
					EmotePlayer.REL_Z.set(movePacket, (byte) toFixedPoint(facing.getOppositeFace().getModZ()));
					
					EmotePlayer.this.sendPacket(player, movePacket);
				} else if (i == 4) {
					EmotePlayer.this.sendPacket(player, movePacket);
					cancel();
				}
				
				i++;
			}
		}.runTaskTimer(LabyEmotePlayer.getInstance(), 0L, 2L);
	}
	
	private void createTeam(Player player) {
		if (teamPrefix == null) return;
	
		PacketPlayOutScoreboardTeam teamPacket = new PacketPlayOutScoreboardTeam();
		
		EmotePlayer.TEAM_NAME.set(teamPacket, teamName);
		EmotePlayer.TEAM_DISPLAY_NAME.set(teamPacket, teamName);
		EmotePlayer.TEAM_PREFIX.set(teamPacket, teamPrefix);
		if (teamSuffix != null) EmotePlayer.TEAM_SUFFIX.set(teamPacket, teamSuffix);
		EmotePlayer.TEAM_NAMETAG_VISIBILITY.set(teamPacket, EnumNameTagVisibility.ALWAYS.e);
		EmotePlayer.TEAM_ACTION.set(teamPacket, 0);
		EmotePlayer.TEAM_PLAYERS.set(teamPacket, new ArrayList<>(Collections.singletonList(displayName)));
		
		this.sendPacket(player, teamPacket);
	}
	
	public void despawn(Player player) {
		PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(new int[] { entityId });
		this.sendPacket(player, destroyPacket);
		this.removeTeam(player);
	}
	
	private void removeTeam(Player player) {
		if (teamPrefix == null) return;
		
		PacketPlayOutScoreboardTeam teamPacket = new PacketPlayOutScoreboardTeam();
		
		EmotePlayer.TEAM_NAME.set(teamPacket, teamName);
		EmotePlayer.TEAM_ACTION.set(teamPacket, 1);
		
		this.sendPacket(player, teamPacket);
	}
	
	public void checkMovement(Player player) {
		if (!player.getWorld().getName().equals(location.getWorld().getName())) {
			if (visibleTo.contains(player.getUniqueId())) {
				this.visibleTo.remove(player.getUniqueId());
				this.despawn(player);
			}
			
			return;
		}
		
		double distanceSquared = player.getLocation().distanceSquared(location);
		
		if (distanceSquared > VIEW_DISTANCE && visibleTo.contains(player.getUniqueId())) {
			this.visibleTo.remove(player.getUniqueId());
			this.despawn(player);
			// Effect?
		} else if (distanceSquared < VIEW_DISTANCE && !visibleTo.contains(player.getUniqueId())) {
			this.visibleTo.add(player.getUniqueId());
			this.spawn(player);
			// Effect?
		}
		
		if (FORCEFIELD_ENABLED) {
			Block playerBlock = player.getLocation().getBlock();
			Block entityBlock = location.getBlock();
			
			if (distanceSquared < FORCEFIELD_RADIUS || (entityBlock.getX() == playerBlock.getX() && entityBlock.getZ() == playerBlock.getZ() && playerBlock.getY() - entityBlock.getY() < 1.8 && playerBlock.getY() - entityBlock.getY() > -0.5)) {
				Vector vector = null;
				
				if (distanceSquared == 0) {
					vector = new Vector(0.587F, 0.587F, 0.587F);
				} else {
					vector = player.getLocation().subtract(location).toVector().setY(0).normalize().multiply(0.3F).setY(0.587F);
				}
				
				(player.getVehicle() == null ? player : player.getVehicle()).setVelocity(vector);
			}
		}
	}
	
	private void addToTablist(Player player) {
		PacketPlayOutPlayerInfo infoPacket = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER);
		PlayerInfoData infoData = infoPacket.new PlayerInfoData(gameProfile, 1, EnumGamemode.NOT_SET, CraftChatMessage.fromString(displayName)[0]);
		this.manipulateTablist(infoPacket, infoData, player);
	}
	
	private void removeFromTablist(Player player) {
		PacketPlayOutPlayerInfo infoPacket = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER);
		PlayerInfoData infoData = infoPacket.new PlayerInfoData(gameProfile, -1, EnumGamemode.NOT_SET, CraftChatMessage.fromString(displayName)[0]);
		this.manipulateTablist(infoPacket, infoData, player);
	}
	
	private void manipulateTablist(PacketPlayOutPlayerInfo infoPacket, PlayerInfoData infoData, Player player) {
		List<PlayerInfoData> players = EmotePlayer.INFO_PLAYERS.get(infoPacket);
		players.add(infoData);
		
		EmotePlayer.INFO_PLAYERS.set(infoPacket, players);
		this.sendPacket(player, infoPacket);
	}
	
	private void sendPacket(Player player, Packet packet) {
		if (player != null) {
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
		} else {
			MinecraftServer.getServer().getPlayerList().sendAll(packet);
		}
	}
	
	public void removeVisible(Player player) {
		this.visibleTo.remove(player.getUniqueId());
	}
	
	private int toFixedPoint(double d) {
		return (int) (d * 32.0);
	}
	
	private byte toPackedByte(float f) {
		return (byte) ((int) (f * 256.0F / 360.0F));
	}
	
	public String getDisplayName() {
		return (teamPrefix != null ? teamPrefix : "") + displayName + (teamSuffix != null ? teamSuffix : "");
	}
	
	public UUID getTextureOwner() {
		return textureOwner;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public int getEntityID() {
		return entityId;
	}
	
	public UUID getUUID() {
		return gameProfile.getId();
	}
}
