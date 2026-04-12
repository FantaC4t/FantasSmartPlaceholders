package com.fantac4t.playerstatus;

import com.fantac4t.playerstatus.commands.ColorCommand;
import com.fantac4t.playerstatus.commands.LiveCommand;
import com.fantac4t.playerstatus.commands.NoSleepCommand;
import com.fantac4t.playerstatus.commands.SuffixCommand;
import com.fantac4t.playerstatus.config.ModConfig;
import com.fantac4t.playerstatus.config.PlayerDataConfig;
import com.fantac4t.playerstatus.events.PlayerEvents;
import com.fantac4t.playerstatus.placeholders.ColorPlaceholders;
import com.fantac4t.playerstatus.placeholders.LivePlaceholder;
import com.fantac4t.playerstatus.placeholders.RolePlaceholder;
import com.fantac4t.playerstatus.placeholders.PlaceholderManager;
import com.fantac4t.playerstatus.placeholders.NoSleepPlaceholder;
import com.fantac4t.playerstatus.placeholders.VoicechatPlaceholder;
import com.fantac4t.playerstatus.player.NoSleepManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerStatus implements ModInitializer {
    public static final String MOD_ID = "playerstatus";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    public static ModConfig CONFIG;

    @Override
    public void onInitialize() {
        LOGGER.info("[PlayerStatus] Init");
        
        CONFIG = ModConfig.load();
        
        // Load player data on startup
        PlayerDataConfig.load();
        
        // Register placeholders
        PlaceholderManager.register();
        LivePlaceholder.register();
        ColorPlaceholders.register();
        RolePlaceholder.register();
        VoicechatPlaceholder.register();
        NoSleepPlaceholder.register();
        
        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiveCommand.register(dispatcher);
            ColorCommand.register(dispatcher);
            SuffixCommand.register(dispatcher);
            NoSleepCommand.register(dispatcher);
        });
        
        // Register events
        PlayerEvents.register();
        NoSleepManager.registerBedEvent();
        
        // ADD THIS: Save player data when server stops
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info("[PlayerStatus] Server stopping, saving player data...");
            PlayerDataConfig.save();
            LOGGER.info("[PlayerStatus] Player data saved successfully");
        });
        
        LOGGER.info("[PlayerStatus] Initialized successfully");
    }
}