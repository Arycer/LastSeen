package me.arycer.lastseen.mixin;

import me.arycer.lastseen.io.LastSeenIO;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class MixinServerPlayerEntity {
    @Unique
    private final ServerPlayerEntity serverPlayer = (ServerPlayerEntity) (Object) this;

    @Inject(method = "onDisconnect", at = @At("HEAD"))
    private void onDisconnect(CallbackInfo ci) {
        LastSeenIO.INSTANCE.setLastSeen(serverPlayer);
    }
}
