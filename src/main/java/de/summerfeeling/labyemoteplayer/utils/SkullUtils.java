package de.summerfeeling.labyemoteplayer.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import de.summerfeeling.labyemoteplayer.utils.reflect.FieldAccessor;
import de.summerfeeling.labyemoteplayer.utils.reflect.Reflect;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Iterator;
import java.util.UUID;

public class SkullUtils {
	
	private static final FieldAccessor META_FIELD = Reflect.getField(Reflect.getCoreMCClass("obc.inventory.CraftMetaSkull"), "profile");

	public static ItemStack getPlayerSkull(Player player) {
		return returnPlayerSkull(((CraftPlayer) player).getProfile());
	}
	
	public static ItemStack returnPlayerSkull(String playerName, UUID uuid, String textures) {
		GameProfile profile = new GameProfile(uuid, playerName);
		profile.getProperties().put("textures", new Property("textures", textures));
		return returnPlayerSkull(profile);
	}
	
	public static ItemStack returnPlayerSkull(GameProfile profile) {
		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
		ItemMeta meta = skull.getItemMeta();
		
		META_FIELD.set(meta, profile);
		
		meta.setDisplayName(profile.getName());
		skull.setItemMeta(meta);
		
		return skull;
	}
	
	public static ItemStack returnSkullFromTextureId(String name, String textureId) {
		return returnSkullFromTextureId(UUID.randomUUID(), name, textureId);
	}
	
	public static ItemStack returnSkullFromTextureId(UUID uuid, String name, String textureId) {
		GameProfile petProfile = GameProfileBuilder.getProfile(uuid, name, String.format("http://textures.minecraft.net/texture/%s", textureId));
		
		return new ItemStackBuilder(returnPlayerSkull(petProfile)).build();
	}
	
	public static SkullMeta setGameProfile(SkullMeta meta, GameProfile gameProfile) {
		GameProfile profile = new GameProfile(gameProfile.getId(), gameProfile.getName());
		Iterator<Property> propertyIterator = gameProfile.getProperties().get("textures").iterator();
		
		if (propertyIterator.hasNext()) {
			profile.getProperties().put("textures", new Property("textures", propertyIterator.next().getValue()));
			META_FIELD.set(meta, gameProfile);
		}
		
		return meta;
	}
}