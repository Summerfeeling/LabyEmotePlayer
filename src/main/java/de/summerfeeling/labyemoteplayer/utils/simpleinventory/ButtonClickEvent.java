package de.summerfeeling.labyemoteplayer.utils.simpleinventory;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("rawtypes")
public interface ButtonClickEvent {
	void onClick(SimpleInventory inventory, Player player, ItemStack itemStack, ClickType click);
}
