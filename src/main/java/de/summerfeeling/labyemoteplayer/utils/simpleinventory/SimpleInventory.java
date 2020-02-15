package de.summerfeeling.labyemoteplayer.utils.simpleinventory;

import de.summerfeeling.labyemoteplayer.LabyEmotePlayer;
import de.summerfeeling.labyemoteplayer.utils.simpleinventory.defaultevents.ButtonDoNothingEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class SimpleInventory implements Listener {
	private ButtonClickEvent[] events;
	private ItemStack fillPattern;
	private Inventory inventory;
	private String name;
	private boolean disposeOnClose = false;

	public SimpleInventory(String name) {
		this(name, new ItemStack(Material.AIR));
	}

	public SimpleInventory(String name, ItemStack fillPattern) {
		this.fillPattern = fillPattern;
		this.name = name;
		this.events = new ButtonClickEvent[5];

		this.createInventory();
		Bukkit.getPluginManager().registerEvents(this, LabyEmotePlayer.getInstance());
	}

	public SimpleInventory(String name, int rows) {
		this(name, rows, new ItemStack(Material.AIR));
	}

	public SimpleInventory(String name, int rows, ItemStack fillPattern) {
		this.fillPattern = fillPattern;
		this.name = name;
		this.events = new ButtonClickEvent[9 * rows];

		this.createInventory();
		Bukkit.getPluginManager().registerEvents(this, LabyEmotePlayer.getInstance());
	}

	public void resize(int rows) {
		if (rows == 5) this.events = new ButtonClickEvent[5];
		else this.events = new ButtonClickEvent[rows * 9];
		
		this.createInventory();
	}
	
	private void createInventory() {
		if (events.length == 5) {
			this.inventory = Bukkit.createInventory(null, InventoryType.HOPPER, name);
		} else {
			this.inventory = Bukkit.createInventory(null, events.length, name);
		}

		for (int i = 0; i < inventory.getSize(); i++) {
			this.addItem(fillPattern, i, ButtonDoNothingEvent.INSTANCE);
		}
	}

	public SimpleInventory addItem(ItemStack itemStack, int x, int y, ButtonClickEvent event) {
		return addItem(itemStack, x + y * 9, event);
	}

	public SimpleInventory addItem(ItemStack itemStack, int position, ButtonClickEvent event) {
		inventory.setItem(position, itemStack);
		events[position] = event == null ? ButtonDoNothingEvent.INSTANCE : event;
		return this;
	}
	
	public SimpleInventory setItems(ItemStack[] items) {
		inventory.setContents(items);
		return this;
	}

	public void clearInventory() {
		Arrays.fill(events, null);

		for (int i = 0; i < inventory.getSize(); i++) {
			inventory.setItem(i, new ItemStack(Material.AIR));
		}
	}

	public void open(Player player) {
		player.openInventory(inventory);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onClose(InventoryCloseEvent e) {
		if (e.getInventory() == null || this.inventory == null) return;
		if (!e.getInventory().equals(this.inventory)) return;

		if (!disposeOnClose) return;

		dispose();
	}

	public void dispose() {
		HandlerList.unregisterAll(this);
		this.events = null;
		this.fillPattern = null;
		this.inventory.clear();
		this.inventory = null;
		this.name = null;
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (this.inventory == null) return;
		Player player = (Player) e.getWhoClicked();
		if (e.getClickedInventory() == null) return;
		if (e.getClickedInventory().equals(this.inventory)) {
//			e.setCancelled(true);
			e.setResult(Result.DENY);
			player.updateInventory();
			if (e.getCurrentItem() != null) {
				ItemStack stack = e.getCurrentItem();
				if (e.getSlot() == e.getRawSlot()) {
					if (e.getSlot() >= this.events.length) {
						return;
					}
					ButtonClickEvent be = this.events[e.getSlot()];
					if (be != null) {
						try {
							be.onClick(this, player, stack, e.getClick());
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		}
	}

	public ButtonClickEvent[] getEvents() {
		return this.events;
	}

	public void setEvent(int position, ButtonClickEvent event) {
		this.events[position] = event;
	}

	public ItemStack getFillPattern() {
		return this.fillPattern;
	}

	public Inventory getInventory() {
		return this.inventory;
	}

	public String getName() {
		return this.name;
	}

	public void setDisposeOnClose(boolean disposeOnClose) {
		this.disposeOnClose = disposeOnClose;
	}

	public boolean isDisposingOnClose() {
		return disposeOnClose;
	}
}