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

    private static ModConfig INSTANCE;
    private static ModConfig ACTIVE;
    private static boolean loaded = false;

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
    public boolean curseStone = true;

    public static ModConfig get() {
        if (!loaded) {
            load();
        }
        return INSTANCE;
    }

    public static ModConfig active() {
        if (!loaded) {
            load();
        }
        return ACTIVE;
    }

    public static void load() {
        loaded = true;
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                INSTANCE = GSON.fromJson(json, ModConfig.class);
            } catch (Exception e) {
                CrdtrdsMod.LOGGER.error("Failed to load config", e);
            }
        }
        if (INSTANCE == null) {
            INSTANCE = new ModConfig();
        }
        ACTIVE = GSON.fromJson(GSON.toJson(INSTANCE), ModConfig.class);
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
