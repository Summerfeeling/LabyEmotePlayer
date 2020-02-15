/*
* Copyright (c) 2016 by Timo Janz (Summerfeeling).
* LPmitKev, and his team, is authorized to use and edit any code for an unlimited amount of time.
 */

package de.summerfeeling.labyemoteplayer.utils.commands;

import com.google.common.collect.ImmutableList;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public abstract class PlayerCommand<P extends JavaPlugin> extends Command<P> {

    public PlayerCommand(String name) {
        super(name);
    }

    public PlayerCommand(String name, boolean async) {
        super(name, async);
    }

    public PlayerCommand(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    public PlayerCommand(String name, String permission, boolean async, String... aliases) {
        super(name, permission, async, aliases);
    }

    private PlayerCommand(String name, String description, String usage, String permission, String... aliases) {
        super(name, description, usage, permission, false, aliases);
    }

    private PlayerCommand(String name, String description, String usage, String permission, boolean async, String... aliases) {
        super(name, description, usage, permission, async, aliases);
    }

    @Override
    public boolean onCommand(CommandSender cs, String label, String[] args) {
        if (cs instanceof Player) {
            return onCommand((Player) cs, label, args);
        } else {
            cs.sendMessage("§cYou must be a player to execute that command");
        }
        return true;
    }

    public abstract boolean onCommand(Player player, String label, String[] args);

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args) {
        if (sender instanceof Player) {
            return tabComplete((Player) sender, label, args);
        }
        return ImmutableList.of();
    }

    public List<String> tabComplete(Player player, String label, String[] args) {
        return super.tabComplete(player, label, args);
    }

}
