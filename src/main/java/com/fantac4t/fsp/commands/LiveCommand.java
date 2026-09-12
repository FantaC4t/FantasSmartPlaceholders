package com.fantac4t.fsp.commands;

import com.fantac4t.fsp.config.PlayerDataConfig;
import com.fantac4t.fsp.player.LiveManager;
import com.fantac4t.fsp.util.TextUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class LiveCommand {

    /** Matches the caps on /lore and /tag — a stream link has no business being longer than this. */
    private static final int MAX_LINK_LENGTH = 256;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("live")
            .executes(ctx -> {
                LiveManager.toggleLive(ctx.getSource().getPlayerOrException());
                return 1;
            })
            .then(Commands.literal("list")
                .executes(ctx -> {
                    List<ServerPlayer> live = ctx.getSource().getServer()
                        .getPlayerList().getPlayers().stream()
                        .filter(p -> PlayerDataConfig.isLive(p.getUUID()))
                        .toList();

                    if (live.isEmpty()) {
                        send(ctx.getSource(), "<gray>No players are currently live.</gray>");
                        return 1;
                    }

                    send(ctx.getSource(), "<gray>── Live Now (" + live.size() + ") ──</gray>");

                    for (ServerPlayer p : live) {
                        String url = TextUtil.normalizeUrl(PlayerDataConfig.getLink(p.getUUID()));
                        String name = p.getName().getString();

                        StringBuilder mini = new StringBuilder(" <red>●</red> <white>").append(name).append("</white>");
                        if (url != null) {
                            String u = esc(url);
                            mini.append("<gray> — </gray>")
                                .append("<hover:show_text:'<gray>Click to open: ").append(u).append("</gray>'>")
                                .append("<click:open_url:'").append(u).append("'>")
                                .append("<aqua><underlined>").append(u).append("</underlined></aqua>")
                                .append("</click></hover>");
                        }
                        send(ctx.getSource(), mini.toString());
                    }
                    return 1;
                }))
            .then(Commands.literal("persist")
                .executes(ctx -> {
                    LiveManager.togglePersist(ctx.getSource().getPlayerOrException());
                    return 1;
                }))
            .then(Commands.literal("link")
                .executes(ctx -> {
                    var player = ctx.getSource().getPlayerOrException();
                    String rawLink = PlayerDataConfig.getLink(player.getUUID());
                    String url = TextUtil.normalizeUrl(rawLink);

                    if (url != null) {
                        String u = esc(url);
                        send(ctx.getSource(), "<gray>Your stream link: </gray>"
                            + "<hover:show_text:'<gray>Click to open: " + u + "</gray>'>"
                            + "<click:open_url:'" + u + "'><aqua><underlined>" + u + "</underlined></aqua></click></hover>");
                    } else {
                        send(ctx.getSource(), "<gray>You have no stream link set. Use </gray><white>/live link <url></white><gray> to set one.</gray>");
                    }
                    return 1;
                })
                .then(Commands.literal("clear")
                    .executes(ctx -> {
                        var player = ctx.getSource().getPlayerOrException();
                        LiveManager.clearLink(player);
                        send(ctx.getSource(), "<yellow>Stream link cleared.</yellow>");
                        return 1;
                    }))
                .then(Commands.argument("url", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        String link = StringArgumentType.getString(ctx, "url").trim();
                        if (link.length() > MAX_LINK_LENGTH) {
                            ctx.getSource().sendFailure(Component.literal(
                                "That link is too long (max " + MAX_LINK_LENGTH + " characters)."));
                            return 0;
                        }
                        if (!TextUtil.looksLikeUrl(link)) {
                            ctx.getSource().sendFailure(Component.literal(
                                "That doesn't look like a valid URL. Example: /live link https://twitch.tv/yourname"));
                            return 0;
                        }
                        LiveManager.setLink(ctx.getSource().getPlayerOrException(), link);
                        return 1;
                    })))
        );
    }

    private static void send(CommandSourceStack src, String mini) {
        Component c = TextUtil.parseMini(mini);
        src.sendSuccess(() -> c, false);
    }

    private static String esc(String s) { return TextUtil.escapeMini(s); }
}
