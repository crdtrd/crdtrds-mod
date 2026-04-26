package com.drtdrc.crdtrdsmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("crdtrdsmod.json");

    private static ModConfig INSTANCE = new ModConfig();

    public boolean enchantingEncore = true;
    public boolean flexiblePortals = true;
    public boolean goAfk = true;
    public boolean tickWarpSleep = true;
    public boolean mineableTrials = true;
    public boolean mineableBedrock = true;
    public boolean cocktails = true;
    public boolean cheaperAnvils = true;
    public boolean mineableSpawners = true;
    public boolean spawnEggDrops = true;
    public boolean delimitedAnvils = true;
    public boolean giveMeRecipes = true;

    public static ModConfig get() {
        return INSTANCE;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                INSTANCE = GSON.fromJson(json, ModConfig.class);
                if (INSTANCE == null) {
                    INSTANCE = new ModConfig();
                }
            } catch (IOException e) {
                CrdtrdsMod.LOGGER.error("Failed to load config", e);
                INSTANCE = new ModConfig();
            }
        }
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(INSTANCE));
        } catch (IOException e) {
            CrdtrdsMod.LOGGER.error("Failed to save config", e);
        }
    }
}
