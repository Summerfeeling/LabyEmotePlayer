package de.summerfeeling.labyemoteplayer;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.summerfeeling.labyemoteplayer.commands.LEPCommand;
import de.summerfeeling.labyemoteplayer.npc.EmotePlayer;
import de.summerfeeling.labyemoteplayer.npc.EmotePlayerSerializer;
import de.summerfeeling.labyemoteplayer.npc.NPCManager;
import de.summerfeeling.labyemoteplayer.utils.LMCProtocol;
import de.summerfeeling.labyemoteplayer.utils.LocationTypeAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LabyEmotePlayer extends JavaPlugin {
	
	public static String PREFIX = "§9LabyEmotePlayer §7┃ §f";
	public static final Gson GSON = new GsonBuilder()
			.excludeFieldsWithoutExposeAnnotation()
			.setPrettyPrinting()
			.registerTypeAdapter(Location.class, new LocationTypeAdapter())
			.registerTypeAdapter(EmotePlayer.class, new EmotePlayerSerializer())
			.create();
	
	private static LabyEmotePlayer instance;
	private static Configuration configuration;
	private static I18n i18n;
	
	private NPCManager npcManager;
	
	public void onLoad() {
		LabyEmotePlayer.instance = this;
	}
	
	public void onEnable() {
		// Loading configuration
		this.loadConfig();
		I18n.init();
		
		// Loading emotes
		Bukkit.getConsoleSender().sendMessage(LabyEmotePlayer.PREFIX + "Emote data are being loaded...");
		(new EmoteLoader()).start();
		
		// Registering commands
		new LEPCommand(npcManager = new NPCManager());
	
		// Registering listeners
		Bukkit.getPluginManager().registerEvents(npcManager, this);
	}
	
	public void onDisable() {
		this.npcManager.save();
		
		try {
			File configFile = new File("plugins/LabyEmotePlayer", "config.json");
			if (!configFile.exists()) return;
			
			Files.write(GSON.toJson(configuration), configFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadConfig() {
		File configFile = new File("plugins/LabyEmotePlayer", "config.json");
		
		Bukkit.getConsoleSender().sendMessage(LabyEmotePlayer.PREFIX + "Loading configuration...");
		
		try {
			if (!configFile.exists()) {
				configFile.getParentFile().mkdirs();
				configFile.createNewFile();
				
				LabyEmotePlayer.configuration = new Configuration();
				Bukkit.getConsoleSender().sendMessage(LabyEmotePlayer.PREFIX + "Configuration file created!");
				Bukkit.getConsoleSender().sendMessage(LabyEmotePlayer.PREFIX + "Using standard configuration.");
				
				Files.write(GSON.toJson(configuration), configFile, StandardCharsets.UTF_8);
			} else {
				LabyEmotePlayer.configuration = GSON.fromJson(Files.toString(configFile, StandardCharsets.UTF_8), Configuration.class);
				Bukkit.getConsoleSender().sendMessage(LabyEmotePlayer.PREFIX + "Configuration loaded!");
			}
		} catch (Exception e) {
			Bukkit.getConsoleSender().sendMessage(LabyEmotePlayer.PREFIX + "§cError while reading / creating the configuration file!");
			Bukkit.getConsoleSender().sendMessage(LabyEmotePlayer.PREFIX + "§cUsing standard configuration.");
			LabyEmotePlayer.configuration = new Configuration();
			
			e.printStackTrace();
		}
	}
	
	public static LabyEmotePlayer getInstance() {
		return instance;
	}
	
	public static Configuration getConfiguration() {
		return configuration;
	}
	
	public static I18n getI18n() {
		return i18n;
	}
	
	public NPCManager getNPCManager() {
		return npcManager;
	}
	
	public static void playEmote(EmotePlayer emotePlayer, long emoteId, Player player) {
		JsonArray array = new JsonArray();
		
		JsonObject forcedEmote = new JsonObject();
		forcedEmote.addProperty("uuid", emotePlayer.getUUID().toString());
		forcedEmote.addProperty("emote_id", emoteId);
		array.add(forcedEmote);
		
		LMCProtocol.sendLMCMessage(player, "emote_api", array);
	}

}
