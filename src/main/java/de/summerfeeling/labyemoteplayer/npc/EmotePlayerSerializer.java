package de.summerfeeling.labyemoteplayer.npc;

import com.google.gson.*;
import org.bukkit.Location;

import java.lang.reflect.Type;
import java.util.UUID;

public class EmotePlayerSerializer implements JsonSerializer<EmotePlayer>, JsonDeserializer<EmotePlayer> {
	
	@Override
	public EmotePlayer deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = jsonElement.getAsJsonObject();
	
		EmotePlayer emotePlayer = new EmotePlayer(object.get("displayName").getAsString(), context.deserialize(object.getAsJsonObject("location"), Location.class), UUID.fromString(object.get("textureOwner").getAsString()));
		emotePlayer.spawn(null);
		
		return emotePlayer;
	}
	
	@Override
	public JsonElement serialize(EmotePlayer emotePlayer, Type type, JsonSerializationContext context) {
		JsonObject object = new JsonObject();
		
		object.add("displayName", new JsonPrimitive(emotePlayer.getDisplayName()));
		object.add("location", context.serialize(emotePlayer.getLocation(), Location.class));
		object.add("textureOwner", new JsonPrimitive(emotePlayer.getTextureOwner().toString()));
		
		return object;
	}
}
