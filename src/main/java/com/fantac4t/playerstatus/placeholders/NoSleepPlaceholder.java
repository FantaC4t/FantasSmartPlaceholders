package com.fantac4t.playerstatus.placeholders;

import com.fantac4t.playerstatus.PlayerStatus;
import com.fantac4t.playerstatus.config.PlayerDataConfig;
import com.fantac4t.playerstatus.player.NoSleepManager;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import net.minecraft.resources.Identifier;

import java.util.Optional;
import java.util.UUID;

public final class NoSleepPlaceholder {

    private static final Identifier NOSLEEP_ID = Identifier.fromNamespaceAndPath(PlayerStatus.MOD_ID, "nosleep");

    private NoSleepPlaceholder() {}

    public static void register() {
        // %playerstatus:nosleep% — shows icon when active, empty when inactive
        Placeholders.register(NOSLEEP_ID, (ctx, arg) -> {
            UUID target = null;

            // If an argument is provided, try to parse it as a UUID
            if (arg != null && !arg.isEmpty()) {
                try {
                    target = UUID.fromString(arg);
                } catch (IllegalArgumentException ignored) {}
            }

            // Otherwise use the context player
            if (target == null && ctx.hasPlayer()) {
                target = ctx.player().getUUID();
            }

            if (target == null) {
                return PlaceholderResult.value(net.minecraft.network.chat.Component.empty());
            }

            String raw = NoSleepManager.placeholder(target);
            if (raw == null || raw.isEmpty()) {
                return PlaceholderResult.value(net.minecraft.network.chat.Component.empty());
            }
            return PlaceholderResult.value(TextParserUtils.formatText(raw));
        });
    }
}
