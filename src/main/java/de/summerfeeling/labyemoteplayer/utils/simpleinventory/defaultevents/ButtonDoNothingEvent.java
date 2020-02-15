package de.summerfeeling.labyemoteplayer.utils.simpleinventory.defaultevents;

import de.summerfeeling.labyemoteplayer.utils.simpleinventory.ButtonClickEvent;
import de.summerfeeling.labyemoteplayer.utils.simpleinventory.SimpleInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class ButtonDoNothingEvent implements ButtonClickEvent {

	public static final ButtonClickEvent INSTANCE = new ButtonDoNothingEvent();

	@Override
	public void onClick(SimpleInventory inventory, Player player, ItemStack itemStack, ClickType click) { }
	
}