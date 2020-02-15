/*
* Copyright (c) 2016 by Timo Janz (Summerfeeling).
* LPmitKev, and his team, is authorized to use and edit any code for an unlimited amount of time.
 */

package de.summerfeeling.labyemoteplayer.utils.commands;

import com.google.common.collect.ImmutableList;
import de.summerfeeling.labyemoteplayer.I18n;
import de.summerfeeling.labyemoteplayer.LabyEmotePlayer;
import de.summerfeeling.labyemoteplayer.utils.reflect.Reflect;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.help.CommandAliasHelpTopic;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public abstract class Command<P extends JavaPlugin> extends org.bukkit.command.Command {

    private static CommandMap commandMap = Reflect.getField(CraftServer.class, "commandMap").get(Bukkit.getServer());
    protected P plugin;
    protected boolean async;

    public Command(String name, boolean async) {
        this(name, null, async);
    }

    public Command(String name) {
        this(name, null, false);
    }

    public Command(String name, String permission, boolean async, String... aliases) {
        this(name, name, "/<command>", permission, async, aliases);
    }

    public Command(String name, String permission, String... aliases) {
        this(name, permission, false, aliases);
    }

    public Command(String name, String description, String usage, String permission, boolean async, String... aliases) {
        super(name, description, usage, (aliases == null || aliases.length == 0 ? ImmutableList.of() : ImmutableList.copyOf(aliases)));
        this.async = async;

        try {
            Type genericType = getClass().getGenericSuperclass();
            Validate.isTrue(genericType instanceof ParameterizedType, "The command class needs explicit generic type declaration (Command<ProvidingPlugin>");
            this.plugin = (P) JavaPlugin.getPlugin((Class) ((ParameterizedType) genericType).getActualTypeArguments()[0]);
            if (permission != null) super.setPermission(permission.replace("%pluginname%", plugin.getName()));

            setPermissionMessage(I18n.t("NO_PERMISSION", LabyEmotePlayer.PREFIX));
            commandMap.register(plugin.getName(), this);
            
            Bukkit.getHelpMap().addTopic(new GenericCommandHelpTopic(this));
            
            if (aliases != null) {
                for (String  alias : aliases) {
                    Bukkit.getHelpMap().addTopic(new CommandAliasHelpTopic(alias, name, Bukkit.getHelpMap()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static CommandMap getCommandMap() {
        return Command.commandMap;
    }

    public boolean execute(CommandSender cs, String label, String[] args) {
        if (!testPermission(cs)) {
            return true;
        }

        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> runCommand(cs, label, args));
        } else {
            runCommand(cs, label, args);
        }

        return true;
    }

    protected void runCommand(CommandSender cs, String label, String[] args) {
        try {
            if (!onCommand(cs, label, args)) {
                for (String s : usageMessage.replace("<command>", label).split("\n")) {
                    cs.sendMessage(s);
                }
            }
        } catch (Exception e) {
            throw new CommandException("Unhandled exception executing '" + label + "'", e);
        }
    }

    @Override public List<String> tabComplete(CommandSender cs, String label, String[] args) {
        return super.tabComplete(cs, label, args);
    }

    public abstract boolean onCommand(CommandSender cs, String label, String[] args);

    public P getPlugin() {
        return plugin;
    }
}
