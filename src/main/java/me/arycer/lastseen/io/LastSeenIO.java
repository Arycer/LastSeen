package me.arycer.lastseen.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.arycer.lastseen.LastSeenMod;
import me.arycer.lastseen.command.LastSeenCommand;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.UUID;

public class LastSeenIO {
    public static final LastSeenIO INSTANCE = new LastSeenIO();
    private static final Path FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve("lastseen.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private HashMap<UUID, Long> lastSeen = new HashMap<>();
    private String timeZone = "Europe/Madrid";

    public String formatTime(long time, String name) throws CommandSyntaxException {
        try {
            ZoneId zone = ZoneId.of(this.timeZone);
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), zone);

            return String.format(
                    "%02d/%02d/%d - %02d:%02d",
                    dateTime.getDayOfMonth(),
                    dateTime.getMonthValue(),
                    dateTime.getYear(),
                    dateTime.getHour(),
                    dateTime.getMinute()
            );
        } catch (Exception e) {
            throw LastSeenCommand.UNEXPECTED_ERROR.create(name);
        }
    }

    public void load() {
        if (!FILE_PATH.toFile().exists()) {
            return;
        }

        try {
            LastSeenIO config = GSON.fromJson(Files.readString(FILE_PATH), LastSeenIO.class);
            this.lastSeen = config.lastSeen;
            this.timeZone = config.timeZone;
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

    public void setLastSeen(ServerPlayerEntity player) {
        this.lastSeen.put(player.getUuid(), System.currentTimeMillis());
        this.save();
    }

    public void updateLastSeen(MinecraftServer server) {
        server.getPlayerManager().getPlayerList().forEach(this::setLastSeen);
    }

    public long getLastSeen(GameProfile profile) {
        return this.lastSeen.getOrDefault(profile.getId(), -1L);
    }

    public HashMap<UUID, Long> getAll() {
        return this.lastSeen;
    }
}
