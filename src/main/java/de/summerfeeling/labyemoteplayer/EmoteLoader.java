package de.summerfeeling.labyemoteplayer;

import de.summerfeeling.labyemoteplayer.inventory.EmoteSelectorInventory;
import de.summerfeeling.labyemoteplayer.utils.GZIPCompression;
import org.bukkit.Bukkit;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;

public class EmoteLoader extends Thread {
	
	private Map<Short, String> nameTranslations = new HashMap<>();
	
	public EmoteLoader() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/emotes.txt")));
			String text = "";
			
			while ((text = reader.readLine()) != null) {
				String[] parts = text.split(":");
				this.nameTranslations.put(Short.valueOf(parts[0]), parts[1].trim());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		try {
			HttpURLConnection connection = (HttpURLConnection)(new URL("http://dl.labymod.net/emotes/emotedata")).openConnection();
			connection.setRequestProperty("User-Agent", "LabyMod v3.3.0 on mc1.8.9");
			connection.setReadTimeout(5000);
			connection.setConnectTimeout(2000);
			connection.connect();
			
			int responseCode = connection.getResponseCode();
			
			if (responseCode / 100 == 2) {
				BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
				ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
				DataOutputStream dataOutput = new DataOutputStream(byteOutput);
				byte[] data = new byte[1024];
				
				int length;
				while((length = inputStream.read(data, 0, 1024)) != -1) {
					dataOutput.write(data, 0, length);
				}
				
				ByteArrayInputStream byteInput = new ByteArrayInputStream(byteOutput.toByteArray());
				DataInputStream dataInput = new DataInputStream(byteInput);
				byte[] compressed = new byte[dataInput.readInt()];
				dataInput.read(compressed);
				
				this.decompile(GZIPCompression.decompress(compressed));
			} else {
				Bukkit.getConsoleSender().sendMessage(I18n.t("EMOTE_FILE_ERROR", LabyEmotePlayer.PREFIX));
				Bukkit.getPluginManager().disablePlugin(LabyEmotePlayer.getInstance());
			}
		} catch (Exception var12) {
			var12.printStackTrace();
		}
		
	}
	
	private void decompile(byte[] decompressed) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(decompressed);
		DataInputStream dis = new DataInputStream(bais);
		int count = dis.readInt();
		
		for(int i = 0; i < count; ++i) {
			short id = dis.readShort();
			byte[] nameInBytes = new byte[dis.readInt()];
			dis.read(nameInBytes);
			String name = new String(nameInBytes);
			byte[] jsonInBytes = new byte[dis.readInt()];
			dis.read(jsonInBytes);
			String json = new String(jsonInBytes);
			
			EmoteSelectorInventory.EMOTES.add(new SimpleEntry<>(id, nameTranslations.getOrDefault(id, name)));
			EmoteSelectorInventory.MAX_PAGES = (int) Math.ceil((double) EmoteSelectorInventory.EMOTES.size() / 21.0D);
		}
		
		Bukkit.getConsoleSender().sendMessage(I18n.t("EMOTE_FILE_LOADED", LabyEmotePlayer.PREFIX, count));
	}
	
}