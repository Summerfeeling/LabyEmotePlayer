package de.summerfeeling.labyemoteplayer;

import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.*;

public class I18n {
	
	private static final List<String> STANDARD_LANGUAGES = Arrays.asList("lep_de.properties", "lep_en.properties");
	private static final File LANGUAGE_DIR = new File("plugins/LabyEmotePlayer/languages");
	private static final Map<String, Map<String, String>> BUNDLES = new HashMap<>();
	
	private static String currentLanguage = "en";
	
	public static void init() {
		if (!LANGUAGE_DIR.exists()) I18n.LANGUAGE_DIR.mkdirs();
		
		for (String fileName : STANDARD_LANGUAGES) {
			File langFile = new File(LANGUAGE_DIR, fileName.replace("lep_", ""));
			
			if (!langFile.exists()) {
				try {
					System.out.println(langFile.getAbsolutePath());
					Files.copy(I18n.class.getResourceAsStream("/languages/" + fileName), Paths.get(URI.create("file:///" + langFile.getAbsolutePath())), StandardCopyOption.REPLACE_EXISTING);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		for (File langFile : Objects.requireNonNull(LANGUAGE_DIR.listFiles())) {
			try {
				Properties properties = new Properties();
				properties.load(new InputStreamReader(new FileInputStream(langFile), StandardCharsets.UTF_8));
				
				I18n.BUNDLES.put(langFile.getName().replace(".properties", ""), new HashMap(properties));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Bukkit.getConsoleSender().sendMessage(LabyEmotePlayer.PREFIX + "Loaded " + BUNDLES.size() + " languages: " + BUNDLES.keySet().toString().replace("[", "").replace("]", ""));
		
		if (BUNDLES.containsKey(LabyEmotePlayer.getConfiguration().getLanguage())) {
			Bukkit.getConsoleSender().sendMessage(LabyEmotePlayer.PREFIX + "§aUsing language " + (currentLanguage = LabyEmotePlayer.getConfiguration().getLanguage()));
			LabyEmotePlayer.PREFIX = t("PREFIX");
		} else {
			Bukkit.getConsoleSender().sendMessage(LabyEmotePlayer.PREFIX + "§cLanguage " + LabyEmotePlayer.getConfiguration().getLanguage() + " not found. Using english.");
			LabyEmotePlayer.getConfiguration().setLanguage("en");
		}
	}
	
	public static String t(String key, Object... objs) {
		return MessageFormat.format(BUNDLES.get(currentLanguage).getOrDefault(key, key), objs).trim();
	}
}
