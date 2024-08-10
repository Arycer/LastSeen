package me.arycer.lastseen.mixin;

import me.arycer.lastseen.io.LastSeenIO;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class MixinPlayerManager {
    @Shadow @Final private MinecraftServer server;

    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    private void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        LastSeenIO.INSTANCE.setLastSeen(player);
    }

    @Inject(method = "saveAllPlayerData", at = @At("TAIL"))
    private void saveAllPlayerData(CallbackInfo ci) {
        LastSeenIO.INSTANCE.updateLastSeen(this.server);
    }
}
