package de.summerfeeling.labyemoteplayer.inventory;

import de.summerfeeling.labyemoteplayer.HeadConstant;
import de.summerfeeling.labyemoteplayer.I18n;
import de.summerfeeling.labyemoteplayer.LabyEmotePlayer;
import de.summerfeeling.labyemoteplayer.npc.EmotePlayer;
import de.summerfeeling.labyemoteplayer.utils.ItemStackBuilder;
import de.summerfeeling.labyemoteplayer.utils.simpleinventory.ButtonClickEvent;
import de.summerfeeling.labyemoteplayer.utils.simpleinventory.SimpleInventory;
import de.summerfeeling.labyemoteplayer.utils.simpleinventory.defaultevents.ButtonDoNothingEvent;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class EmoteSelectorInventory extends SimpleInventory {
	
	public static List<Entry<Short, String>> EMOTES = new ArrayList<>();
	public static int MAX_PAGES = 0;
	
	private EmotePlayer emotePlayer;
	
	public EmoteSelectorInventory(Player player, EmotePlayer emotePlayer, int page) {
		super("§9Emotes", 6);
		
		if (EMOTES.isEmpty()) {
			player.sendMessage(I18n.t("INVENTORY_EMOTES_LOADING", LabyEmotePlayer.PREFIX));
			player.closeInventory();
			return;
		}
		
		this.emotePlayer = emotePlayer;
		super.setDisposeOnClose(true);
		this.fillInventory(page);
	}
	
	private void fillInventory(int page) {
		this.clearInventory();

		// Emotes
		int currentX = 1, currentY = 1;
		int from = page * 21;
		int to = from + 21;

		List<Entry<Short, String>> toDisplay = new ArrayList<>(EMOTES);
		toDisplay = toDisplay.subList(from, Math.min(to, toDisplay.size()));

		for (int i = 0; i < toDisplay.size(); i++) {
			Entry<Short, String> entry = toDisplay.get(i);

			super.addItem(new ItemStackBuilder(Material.BOOK).name(I18n.t("INVENTORY_EMOTE_NAME", entry.getValue())).build(), currentX++, currentY, new PlayEmoteAction(entry.getValue(), entry.getKey(), emotePlayer));

			if (i == 6 || i == 13 || i == 27) {
				currentX = 1;
				currentY++;
			}
		}

		// Default items
		super.addItem(new ItemStackBuilder(Material.EMERALD).name(I18n.t("INVENTORY_PAGE", (page + 1), MAX_PAGES)).build(), 4, 0, ButtonDoNothingEvent.INSTANCE);
		super.addItem(new ItemStackBuilder(Material.BARRIER).name(I18n.t("INVENTORY_STOP_EMOTE")).build(), 4, 5, new PlayEmoteAction("<Stop emote>", (short) -1, emotePlayer));

		if (page != 0) {
			super.addItem(new ItemStackBuilder(HeadConstant.IRON_ARROW_LEFT.getItemStack()).name(I18n.t("INVENTORY_PREVIOUS_PAGE")).build(), 3, 5, new SwitchInventorySite(page - 1));
		}

		if (to < EMOTES.size()) {
			super.addItem(new ItemStackBuilder(HeadConstant.IRON_ARROW_RIGHT.getItemStack()).name(I18n.t("INVENTORY_NEXT_PAGE")).build(), 5, 5, new SwitchInventorySite(page + 1));
		}
	}
	
	private class SwitchInventorySite implements ButtonClickEvent {
		private int page;
		
		public SwitchInventorySite(int page) {
			this.page = page;
		}
		
		public void onClick(SimpleInventory simpleInventory, Player player, ItemStack itemStack, ClickType clickType) {
			player.playSound(player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(0.5D)), Sound.SUCCESSFUL_HIT, 1.0F, 1.0F);
			fillInventory(page);
		}
	}
	
}
