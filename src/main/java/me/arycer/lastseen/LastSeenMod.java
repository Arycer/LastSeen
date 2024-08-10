package me.arycer.lastseen;

import me.arycer.lastseen.command.LastSeenCommand;
import me.arycer.lastseen.io.LastSeenIO;
import net.fabricmc.api.DedicatedServerModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LastSeenMod implements DedicatedServerModInitializer {
    public static final String MOD_ID = "lastseen";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitializeServer() {
        LastSeenIO.INSTANCE.load();
        LastSeenCommand.register();
    }
}
