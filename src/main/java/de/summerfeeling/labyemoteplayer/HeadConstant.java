package de.summerfeeling.labyemoteplayer;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import de.summerfeeling.labyemoteplayer.utils.SkullUtils;
import de.summerfeeling.labyemoteplayer.utils.reflect.FieldAccessor;
import de.summerfeeling.labyemoteplayer.utils.reflect.Reflect;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public enum HeadConstant {
	
	IRON_ARROW_LEFT("a185c97dbb8353de652698d24b64327b793a3f32a98be67b719fbedab35e"),
	IRON_ARROW_RIGHT("31c0ededd7115fc1b23d51ce966358b27195daf26ebb6e45a66c34c69c34091"),
	QUESTION_MARK("5163dafac1d91a8c91db576caac784336791a6e18d8f7f62778fc47bf146b6"),
	;
	
	private static final Class CRAFT_META_SKULL = ((Supplier<Class>) () -> { try { return Class.forName("org.bukkit.craftbukkit.v1_8_R3.inventory.CraftMetaSkull"); } catch (ClassNotFoundException e) { e.printStackTrace(); } return null; }).get();
	private static final FieldAccessor gameProfileField = Reflect.getField(CRAFT_META_SKULL, "profile");
	private static final Map<String, HeadConstant> idmap;
	
	static {
		ImmutableMap.Builder<String, HeadConstant> idMapBuilder = ImmutableMap.builder();
		
		for (HeadConstant constant : HeadConstant.values()) {
			idMapBuilder.put(constant.textureId, constant);
		}
		
		idmap = idMapBuilder.build();
	}
	
	public static HeadConstant getFromTextureId(String textureId) {
		if (textureId.startsWith("http://textures.minecraft.net/texture/")) {
			textureId = textureId.substring("http://textures.minecraft.net/texture/".length());
		}
		HeadConstant constant = idmap.get(textureId);
		return constant == null ? QUESTION_MARK : constant;
	}
	
	public static HeadConstant getFromItemStack(ItemStack itemStack) {
		SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
		GameProfile profile = gameProfileField.get(meta);
		try {
			
			String textures = profile.getProperties().get("textures").iterator().next().getValue();
			
			JsonObject jsonObject = LabyEmotePlayer.GSON.fromJson(new String(Base64.getDecoder().decode(textures.getBytes())), JsonObject.class);
			textures = jsonObject.get("textures").getAsJsonObject().get("SKIN").getAsJsonObject().get("url").getAsString();
			return getFromTextureId(textures);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private String textureId;
	private UUID uuid;
	
	HeadConstant(String texture) {
		this.textureId = texture;
		this.uuid = UUID.randomUUID();
	}
	
	public ItemStack getItemStack() {
		return SkullUtils.returnSkullFromTextureId(uuid, this.toString(), this.textureId);
	}
	
	public String getTextureId() {
		return this.textureId;
	}
	
}
