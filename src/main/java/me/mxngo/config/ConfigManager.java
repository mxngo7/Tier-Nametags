package me.mxngo.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import me.mxngo.TierNametags;
import net.fabricmc.loader.api.FabricLoader;

public class ConfigManager {
	private static ConfigManager instance = new ConfigManager();
	private final Logger logger = LoggerFactory.getLogger(TierNametags.MODID + "/config");
	
	private TierNametagsConfig config;
	
	private Path path = FabricLoader.getInstance().getConfigDir();
	private File file = path.resolve(TierNametags.MODID.concat(".json")).toFile();
	
	public ConfigManager() {
		instance = this;
		
		if (!file.exists()) {
			logger.info("Config file does not exist. Generating new config file.");
			saveDefaultConfig();
		} else {
			config = loadConfig();
		}
	}
	
	public TierNametagsConfig getConfig() {
		return config;
	}
	
	private TierNametagsConfig saveDefaultConfig() {
		config = new TierNametagsConfig();
		saveConfig();
		return config;
	}
	
	public void saveConfig() {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		
		Gson gson = builder.create();
		
		try (FileWriter writer = new FileWriter(file.getAbsolutePath())) {
			gson.toJson(config, writer);
		} catch (IOException exception) {
			logger.trace(exception.getLocalizedMessage());
		}
	}
	
	public void saveConfig(TierNametagsConfig updatedConfig) {
		config = updatedConfig;
		saveConfig();
	}
	
	public TierNametagsConfig loadConfig() {
		Gson gson = new Gson();
		
		try (BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
			config = gson.fromJson(reader, TierNametagsConfig.class);
			return config;
		} catch (FileNotFoundException exception) {
			logger.trace(exception.getLocalizedMessage());
		} catch (IOException exception) {
			logger.trace(exception.getLocalizedMessage());
		}
		
		return saveDefaultConfig();
	}
	
	public static ConfigManager getInstance() {
		return instance;
	}
}
