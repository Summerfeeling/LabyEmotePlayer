package de.summerfeeling.labyemoteplayer.npc;

import com.google.common.io.Files;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import de.summerfeeling.labyemoteplayer.I18n;
import de.summerfeeling.labyemoteplayer.LabyEmotePlayer;
import io.netty.channel.ChannelPipeline;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class NPCManager implements Listener {
	
	private static final String DECODER_NAME = (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib") ? "protocol_lib_decoder" : "decoder");
	private static final Type TYPE = new TypeToken<Map<String, EmotePlayer>>() { }.getType();
	
	private File playersFile = new File("plugins/LabyEmotePlayer", "players.json");
	
	private Map<Integer, EmotePlayer> emotePlayerIds = new HashMap<>();
	@Expose private Map<String, EmotePlayer> emotePlayers;
	
	public NPCManager() {
		try {
			if (!playersFile.exists()) {
				this.playersFile.getParentFile().mkdirs();
				this.playersFile.createNewFile();
			} else {
				this.emotePlayers = LabyEmotePlayer.GSON.fromJson(Files.toString(playersFile, StandardCharsets.UTF_8), TYPE);
			}
			
			if (emotePlayers == null) emotePlayers = new HashMap<>();
			this.emotePlayers.values().forEach(emotePlayer -> emotePlayerIds.put(emotePlayer.getEntityID(), emotePlayer));
			
			Bukkit.getConsoleSender().sendMessage(I18n.t("EMOTE_PLAYER_LOADED", LabyEmotePlayer.PREFIX, emotePlayers.size()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void save() {
		this.emotePlayers.values().forEach(emotePlayer -> emotePlayer.despawn(null));
		
		try {
			Files.write(LabyEmotePlayer.GSON.toJson(emotePlayers), playersFile, StandardCharsets.UTF_8);
			Bukkit.getConsoleSender().sendMessage(I18n.t("EMOTE_PLAYER_SAVED", LabyEmotePlayer.PREFIX, emotePlayers.size()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void registerEmotePlayer(String registerName, EmotePlayer emotePlayer) {
		this.emotePlayers.put(registerName.toLowerCase(), emotePlayer);
		this.emotePlayerIds.put(emotePlayer.getEntityID(), emotePlayer);
	}
	
	public boolean removeEmotePlayer(String registerName) {
		EmotePlayer emotePlayer = emotePlayers.remove(registerName.toLowerCase());
		
		if (emotePlayer != null) {
			this.emotePlayerIds.remove(emotePlayer.getEntityID());
			emotePlayer.despawn(null);
			return true;
		}
	
		return false;
	}
	
	public boolean isEmotePlayer(String registerName) {
		return emotePlayers.containsKey(registerName.toLowerCase());
	}
	
	public boolean isEmotePlayer(int entityId) {
		return emotePlayerIds.containsKey(entityId);
	}
	
	public EmotePlayer getEmotePlayer(String registerName) {
		return emotePlayers.get(registerName.toLowerCase());
	}
	
	public EmotePlayer getEmotePlayer(int entityId) {
		return emotePlayerIds.get(entityId);
	}
	
	public Set<String> getEmotePlayerNames() {
		return emotePlayers.keySet();
	}

	public List<Entry<String, Location>> getEmotePlayerLocations() {
		return emotePlayers.entrySet().stream().map(entry -> new SimpleEntry<>(entry.getKey(), entry.getValue().getLocation())).collect(Collectors.toList());
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		((CraftPlayer) event.getPlayer()).getHandle().playerConnection.networkManager.channel.pipeline().addAfter(DECODER_NAME, "labyemoteplayer", new NPCInteractListener(this, event.getPlayer()));
		
		this.emotePlayers.values().forEach(emotePlayer -> emotePlayer.checkMovement(event.getPlayer()));
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		ChannelPipeline pipeline = ((CraftPlayer) event.getPlayer()).getHandle().playerConnection.networkManager.channel.pipeline();
		
		this.emotePlayers.values().forEach(emotePlayer -> {
			emotePlayer.removeVisible(event.getPlayer());
			emotePlayer.despawn(event.getPlayer());
		});
		
		
		if (pipeline.get("labyemoteplayer") != null) {
			pipeline.remove("labyemoteplayer");
		}
	}
	
	@EventHandler
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
		this.emotePlayers.values().forEach(emotePlayer -> emotePlayer.checkMovement(event.getPlayer()));
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		this.emotePlayers.values().forEach(emotePlayer -> emotePlayer.checkMovement(event.getPlayer()));
	}
	
}
