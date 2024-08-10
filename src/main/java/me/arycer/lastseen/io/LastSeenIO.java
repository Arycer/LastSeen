package me.arycer.lastseen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

public class LastSeenIO {
    public static final LastSeenIO INSTANCE = new LastSeenIO();
    private static final Path FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve("lastseen.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private HashMap<UUID, Long> lastSeen = new HashMap<>();

    public void load() {
        if (!FILE_PATH.toFile().exists()) {
            return;
        }

        try {
            LastSeenIO config = GSON.fromJson(Files.readString(FILE_PATH), this.getClass());
            this.lastSeen = config.lastSeen;
        } catch (IOException e) {
            LastSeenMod.LOGGER.error("Failed to load lastseen.json", e);
        }
    }

    public void save() {
        try {
            Files.writeString(FILE_PATH, GSON.toJson(this));
        } catch (IOException e) {
            LastSeenMod.LOGGER.error("Error saving lastseen.json", e);
        }
    }
}
