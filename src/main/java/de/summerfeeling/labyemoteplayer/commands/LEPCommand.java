package de.summerfeeling.labyemoteplayer.commands;

import de.summerfeeling.labyemoteplayer.EmoteLoader;
import de.summerfeeling.labyemoteplayer.I18n;
import de.summerfeeling.labyemoteplayer.LabyEmotePlayer;
import de.summerfeeling.labyemoteplayer.inventory.EmoteSelectorInventory;
import de.summerfeeling.labyemoteplayer.npc.EmotePlayer;
import de.summerfeeling.labyemoteplayer.npc.NPCManager;
import de.summerfeeling.labyemoteplayer.utils.UUIDFetcher;
import de.summerfeeling.labyemoteplayer.utils.commands.PlayerCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map.Entry;

public class LEPCommand extends PlayerCommand<LabyEmotePlayer> {
	
	private NPCManager manager;
	
	public LEPCommand(NPCManager npcManager) {
		super("labyemoteplayer", "labyemoteplayer.admin", "emoteplayer", "labyplayer", "lep");
		
		this.manager = npcManager;
	}
	
	@Override
	public boolean onCommand(Player player, String label, String[] args) {
		if (args.length >= 4 && (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("add"))) {
			if (!manager.isEmotePlayer(args[1])) {
				player.sendMessage(I18n.t("CREATING_PLAYER", LabyEmotePlayer.PREFIX));
				
				UUIDFetcher.getUUID(args[2], uuid -> {
					if (uuid == null) {
						player.sendMessage(I18n.t("NO_PLAYER_FOUND", LabyEmotePlayer.PREFIX, args[2]));
						return;
					}
					
					StringBuilder builder = new StringBuilder();
					for (int i = 3; i < args.length; i++) builder.append(args[i]).append(' ');
					
					EmotePlayer emotePlayer = new EmotePlayer(ChatColor.translateAlternateColorCodes('&', builder.toString().trim()), player.getLocation(), uuid);
					emotePlayer.spawn(null);
					
					this.manager.registerEmotePlayer(args[1], emotePlayer);
					
					player.sendMessage(I18n.t("EMOTE_PLAYER_CREATED", LabyEmotePlayer.PREFIX, args[1]));
				});
			} else {
				player.sendMessage(I18n.t("EMOTE_PLAYER_ALREADY_EXISTING", LabyEmotePlayer.PREFIX, args[1]));
			}
		} else if (args.length == 2 && (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("delete"))) {
			if (manager.removeEmotePlayer(args[1])) {
				player.sendMessage(I18n.t("EMOTE_PLAYER_DELETED", LabyEmotePlayer.PREFIX, args[1]));
			} else {
				player.sendMessage(I18n.t("EMOTE_PLAYER_NOT_EXISTING", LabyEmotePlayer.PREFIX, args[1]));
			}
		} else if (args.length == 2 && args[0].equalsIgnoreCase("fixrot")) {
			if (manager.isEmotePlayer(args[1])) {
				this.manager.getEmotePlayer(args[1]).uglyFixingOddPosition(null, true);
				player.sendMessage(I18n.t("EMOTE_PLAYER_ROTATION_FIXED", LabyEmotePlayer.PREFIX, args[1]));
			} else {
				player.sendMessage(I18n.t("EMOTE_PLAYER_NOT_EXISTING", LabyEmotePlayer.PREFIX, args[1]));
			}
		} else if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
			List<Entry<String, Location>> entries = manager.getEmotePlayerLocations();
			
			player.sendMessage(I18n.t("EMOTE_PLAYER_LIST_HEAD", LabyEmotePlayer.PREFIX, entries.size()));
			
			entries.forEach(entry -> {
				Location location = entry.getValue();
				player.sendMessage(I18n.t("EMOTE_PLAYER_LIST_ENTRY", LabyEmotePlayer.PREFIX, entry.getKey(), location.getBlockX(), location.getBlockY(), location.getBlockZ()));
			});
		} else if (args.length == 1 && args[0].equalsIgnoreCase("update")) {
			player.sendMessage(I18n.t("EMOTE_UPDATING", LabyEmotePlayer.PREFIX));

			Bukkit.getScheduler().runTaskAsynchronously(LabyEmotePlayer.getInstance(), () -> {
				EmoteSelectorInventory.EMOTES.clear();
				EmoteSelectorInventory.MAX_PAGES = 0;
				
				(new EmoteLoader()).start();
				
				player.sendMessage(I18n.t("EMOTE_UPDATED", LabyEmotePlayer.PREFIX));
			});
		} else {
			player.sendMessage(I18n.t("HELP", LabyEmotePlayer.PREFIX));
		}
		
		return true;
	}
}
