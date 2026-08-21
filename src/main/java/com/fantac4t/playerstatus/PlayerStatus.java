package com.fantac4t.playerstatus;

import com.fantac4t.playerstatus.commands.ColorCommand;
import com.fantac4t.playerstatus.commands.FeaturesCommand;
import com.fantac4t.playerstatus.commands.FspCommand;
import com.fantac4t.playerstatus.commands.LiveCommand;
import com.fantac4t.playerstatus.commands.LoreCommand;
import com.fantac4t.playerstatus.commands.NoSleepCommand;
import com.fantac4t.playerstatus.commands.TagCommand;
import com.fantac4t.playerstatus.commands.ProfileCommand;
import com.fantac4t.playerstatus.twitch.TwitchManager;
import com.fantac4t.playerstatus.config.ModConfig;
import com.fantac4t.playerstatus.config.PlayerDataConfig;
import com.fantac4t.playerstatus.events.PlayerEvents;
import com.fantac4t.playerstatus.placeholders.ColorPlaceholders;
import com.fantac4t.playerstatus.placeholders.LivePlaceholder;
import com.fantac4t.playerstatus.placeholders.RolePlaceholder;
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

        ServerLifecycleEvents.SERVER_STARTED.register(TwitchManager::start);

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            TwitchManager.stop();
            PlayerDataConfig.save();
        });

        LOGGER.info("Fanta's Smart Placeholders initialized.");
    }
}
