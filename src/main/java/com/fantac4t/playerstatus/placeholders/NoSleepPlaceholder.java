package com.fantac4t.playerstatus.placeholders;

import com.fantac4t.playerstatus.PlayerStatus;
import com.fantac4t.playerstatus.player.NoSleepManager;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.parsers.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public final class NoSleepPlaceholder {

    private static final Identifier NOSLEEP_ID = Identifier.fromNamespaceAndPath(PlayerStatus.MOD_ID, "nosleep");

    private NoSleepPlaceholder() {}

    public static void register() {
        Placeholders.register(NOSLEEP_ID, (ctx, arg) -> {
            UUID target = null;

            if (arg != null && !arg.isEmpty()) {
                try { target = UUID.fromString(arg); } catch (IllegalArgumentException ignored) {}
            }

            if (target == null && ctx.hasPlayer()) {
                target = ctx.player().getUUID();
            }

            if (target == null) return PlaceholderResult.value(Component.empty());

            String raw = NoSleepManager.placeholder(target);
            if (raw == null || raw.isEmpty()) return PlaceholderResult.value(Component.empty());

            try { return PlaceholderResult.value(TagParser.DEFAULT.parseText(raw, null)); }
            catch (Throwable t) { return PlaceholderResult.value(Component.literal(raw)); }
        });
    }
}
