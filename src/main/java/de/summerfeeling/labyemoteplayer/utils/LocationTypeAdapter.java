package de.summerfeeling.labyemoteplayer.utils;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Type;

public class LocationTypeAdapter implements JsonDeserializer<Location>, JsonSerializer<Location> {
	
	@Override
	public Location deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject object = (JsonObject) json;
		
		String worldName = object.get("world").getAsString();
		World world = Bukkit.getWorld(worldName);
		double x = object.get("x").getAsDouble();
		double y = object.get("y").getAsDouble();
		double z = object.get("z").getAsDouble();
		float yaw = object.get("yaw").getAsFloat();
		float pitch = object.get("pitch").getAsFloat();
		
		return new Location(
				world,
				x,
				y,
				z,
				yaw,
				pitch
		);
	}
	
	@Override
	public JsonElement serialize(Location location, Type type, JsonSerializationContext context) {
		JsonObject object = new JsonObject();
		object.add("world", new JsonPrimitive(location.getWorld().getName()));
		object.add("x", new JsonPrimitive(location.getX()));
		object.add("y", new JsonPrimitive(location.getY()));
		object.add("z", new JsonPrimitive(location.getZ()));
		object.add("yaw", new JsonPrimitive(location.getYaw()));
		object.add("pitch", new JsonPrimitive(location.getPitch()));
		return object;
	}
	
}
