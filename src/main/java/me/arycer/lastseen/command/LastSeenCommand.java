package me.arycer.lastseen.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import me.arycer.lastseen.io.LastSeenIO;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.UserCache;

import java.util.*;

public class LastSeenCommand implements Command<ServerCommandSource> {
    public static final DynamicCommandExceptionType UNEXPECTED_ERROR = new DynamicCommandExceptionType((name) ->
            Text.of("An unexpected error ocurred while trying to get the last seen time or timezone of %s".formatted(name)));
    private static final DynamicCommandExceptionType SERVER_NULL = new DynamicCommandExceptionType((ignore) ->
            Text.of("Server is null"));

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("lastseen")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("profiles", GameProfileArgumentType.gameProfile())
                        .executes(new LastSeenCommand())
                )
                .executes(new ListAllCommand())
        );
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((d, r, e) -> register(d));
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Collection<GameProfile> profiles = GameProfileArgumentType.getProfileArgument(context, "profiles");
        MinecraftServer server = context.getSource().getServer();

        if (server != null) {
            LastSeenIO.INSTANCE.updateLastSeen(server);
        }

        for (GameProfile profile : profiles) {
            long time = LastSeenIO.INSTANCE.getLastSeen(profile);
            if (time > 0) {
                Text feedbackText = Text.of("Player %s was last seen %s".formatted(profile.getName(), LastSeenIO.INSTANCE.formatTime(time, profile.getName())));
                context.getSource().sendFeedback(
                        () -> feedbackText,
                        false
                );
            } else {
                context.getSource().sendFeedback(
                        () -> Text.of("Player %s has not played on this server.".formatted(profile.getName())),
                        false
                );
            }
        }

        return 0;
    }

    private static class ListAllCommand implements Command<ServerCommandSource> {
        @Override
        public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            HashMap<UUID, Long> lastSeen = LastSeenIO.INSTANCE.getAll();
            MinecraftServer server = context.getSource().getServer();

            UserCache cache;
            if (server == null || (cache = server.getUserCache()) == null) {
                throw SERVER_NULL.create(null);
            }

            LastSeenIO.INSTANCE.updateLastSeen(server);
            List<Pair<GameProfile, Long>> profiles = new ArrayList<>();
            lastSeen.forEach((uuid, time) -> {
                Optional<GameProfile> profile = cache.getByUuid(uuid);
                profile.ifPresent(gp -> profiles.add(new Pair<>(gp, time)));
            });

            profiles.sort(Comparator.comparingLong(Pair::getRight)); // Sort by last seen time in ascending order
            for (Pair<GameProfile, Long> profile : profiles) {
                Text feedbackText = Text.of("Player %s was last seen %s".formatted(
                        profile.getLeft().getName(),
                        LastSeenIO.INSTANCE.formatTime(
                                profile.getRight(),
                                profile.getLeft().getName()
                        ))
                );
                context.getSource().sendFeedback(() -> feedbackText, false);
            }

            return 0;
        }
    }
}
