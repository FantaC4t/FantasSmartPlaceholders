package com.fantac4t.fsp;

import com.fantac4t.fsp.commands.ColorCommand;
import com.fantac4t.fsp.commands.FeaturesCommand;
import com.fantac4t.fsp.commands.FspCommand;
import com.fantac4t.fsp.commands.LiveCommand;
import com.fantac4t.fsp.commands.LoreCommand;
import com.fantac4t.fsp.commands.NoSleepCommand;
import com.fantac4t.fsp.commands.TagCommand;
import com.fantac4t.fsp.commands.ProfileCommand;
import com.fantac4t.fsp.player.TwitchManager;
import com.fantac4t.fsp.config.ModConfig;
import com.fantac4t.fsp.config.PlayerDataConfig;
import com.fantac4t.fsp.player.PlayerEvents;
import com.fantac4t.fsp.placeholders.ColorPlaceholders;
import com.fantac4t.fsp.placeholders.LivePlaceholder;
import com.fantac4t.fsp.placeholders.RolePlaceholder;
import com.fantac4t.fsp.placeholders.NoSleepPlaceholder;
import com.fantac4t.fsp.placeholders.VoicechatPlaceholder;
import com.fantac4t.fsp.player.NoSleepManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FSP implements ModInitializer {
    public static final String MOD_ID = "fanta-smart-placeholders";
    public static final String PLACEHOLDER_NS = "fsp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ModConfig CONFIG;

    @Override
    public void onInitialize() {
        CONFIG = ModConfig.load();

        PlayerDataConfig.load();

        LivePlaceholder.register();
        ColorPlaceholders.register();
        RolePlaceholder.register();
        VoicechatPlaceholder.register();
        NoSleepPlaceholder.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiveCommand.register(dispatcher);
            ColorCommand.register(dispatcher);
            TagCommand.register(dispatcher);
            NoSleepCommand.register(dispatcher);
            FspCommand.register(dispatcher);
            LoreCommand.register(dispatcher);
            ProfileCommand.register(dispatcher);
            FeaturesCommand.register(dispatcher);
        });

        PlayerEvents.register();
        NoSleepManager.registerBedEvent();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            TwitchManager.start(server);
            PlayerDataConfig.startAutosave();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            TwitchManager.stop();
            PlayerDataConfig.stopAutosave();
            PlayerDataConfig.save();
        });

        LOGGER.info("Fanta's Smart Placeholders initialized.");
    }
}
