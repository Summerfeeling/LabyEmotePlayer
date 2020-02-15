package de.summerfeeling.labyemoteplayer.inventory;

import de.summerfeeling.labyemoteplayer.I18n;
import de.summerfeeling.labyemoteplayer.LabyEmotePlayer;
import de.summerfeeling.labyemoteplayer.npc.EmotePlayer;
import de.summerfeeling.labyemoteplayer.utils.simpleinventory.ButtonClickEvent;
import de.summerfeeling.labyemoteplayer.utils.simpleinventory.SimpleInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class PlayEmoteAction implements ButtonClickEvent {
	
	private EmotePlayer emotePlayer;
	private String emoteName;
	private short emoteId;
	
	public PlayEmoteAction(String emoteName, short emoteId, EmotePlayer emotePlayer) {
		this.emotePlayer = emotePlayer;
		this.emoteName = emoteName;
		this.emoteId = emoteId;
	}
	
	@Override
	public void onClick(SimpleInventory inventory, Player player, ItemStack itemStack, ClickType click) {
		if (click.isLeftClick()) {
			LabyEmotePlayer.playEmote(emotePlayer, emoteId, player);
			
			if (emoteId > 0) {
				player.sendMessage(I18n.t("INVENTORY_PLAY_EMOTE", LabyEmotePlayer.PREFIX, emoteName));
			}
			
			player.closeInventory();
		}
	}
}
