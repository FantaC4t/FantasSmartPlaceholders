package com.fantac4t.playerstatus.commands;

import com.fantac4t.playerstatus.util.TextUtil;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class FeaturesCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("features")
            .executes(ctx -> { sendCommands(ctx.getSource()); return 1; })
            .then(Commands.literal("placeholders")
                .executes(ctx -> { sendPlaceholders(ctx.getSource()); return 1; }))
        );
    }

    private static void sendCommands(CommandSourceStack src) {
        send(src, header());
        send(src, section("Live"));
        send(src, cmd("/live",                         "toggle your live status on/off — shows the live badge and fires the chat broadcast"));
        send(src, cmd("/live list",                    "see everyone currently live, with a clickable link to their stream if they set one"));
        send(src, cmd("/live persist",                 "keep your live status across reconnects instead of it resetting when you log out"));
        send(src, cmd("/live link <url>",              "set your stream link — a twitch.tv URL is auto-detected for Twitch auto-live"));
        send(src, cmd("/live link",                    "show your current stream link"));
        send(src, cmd("/live link clear",              "remove your stream link"));
        send(src, note("Twitch auto-live (going live on Twitch flips your in-game status automatically) only works if the server admin has set twitchClientId/twitchClientSecret in config.json. Without it, use /live to toggle manually."));
        send(src, section("Color"));
        send(src, cmd("/color",                        "show your current name color"));
        send(src, cmd("/color <hex>",                  "set a solid name color, e.g. /color #FF6200"));
        send(src, cmd("/color gradient <hex1> <hex2>", "set a two-stop gradient across your name"));
        send(src, cmd("/color clear",                  "remove name color, back to default"));
        send(src, section("No-Sleep"));
        send(src, cmd("/nosleep",                      "toggle a status that warns others when they try to sleep while you're not ready"));
        send(src, section("Lore"));
        send(src, cmd("/lore",                         "show your current lore text"));
        send(src, cmd("/lore <text>",                  "set your profile lore — supports MiniMessage color/gradient tags"));
        send(src, cmd("/lore clear",                   "remove your lore"));
        send(src, section("Profile"));
        send(src, cmd("/profile",                      "view your own profile card (name, pronouns, role, live status, lore, etc.)"));
        send(src, cmd("/profile <player>",             "view another player's profile — works for offline players too"));
        send(src, section("Admin"));
        send(src, cmd("/tag set <player> <text>",      "set a player's nametag suffix (max 48 characters, truncated if longer)"));
        send(src, cmd("/tag get <player>",             "view a player's current nametag suffix"));
        send(src, cmd("/tag remove <player>",          "clear a player's nametag suffix"));
        send(src, cmd("/tag list",                     "list every player who currently has a nametag suffix set"));
        send(src, cmd("/fsp reload",                   "hot-reload config.json without restarting the server"));
        send(src, parse("<gray><italic><click:run_command:'/features placeholders'>View placeholder list → /features placeholders</click></italic></gray>"));
    }

    private static void sendPlaceholders(CommandSourceStack src) {
        send(src, header());
        send(src, section("Live"));
        send(src, ph("%playerstatus:live%",             "the live badge — clickable link to the player's stream if one is set, otherwise plain text"));
        send(src, ph("%playerstatus:stream%",           "the player's raw stream link, empty if not live or no link set"));
        send(src, ph("%playerstatus:live_stream%",      "live badge and stream link combined in one placeholder"));
        send(src, ph("%playerstatus:clickable_stream%", "underlined clickable link with hover text; optional arg sets the label, e.g. %playerstatus:clickable_stream:Watch Now%"));
        send(src, ph("%playerstatus:live_count%",       "number of players currently online and marked live"));
        send(src, section("Color"));
        send(src, ph("%playerstatus:coloredname%",      "the player's name rendered in their set color or gradient"));
        send(src, ph("%playerstatus:color%",            "the player's raw color value (hex or gradient string)"));
        send(src, section("Identity"));
        send(src, ph("%playerstatus:role%",             "the player's LuckPerms primary group mapped to a configured symbol"));
        send(src, ph("%playerstatus:suffix%",           "the player's custom nametag suffix text, if any"));
        send(src, section("Misc"));
        send(src, ph("%playerstatus:nosleep%",          "the no-sleep icon, shown only while the player has it toggled on"));
        send(src, ph("%playerstatus:vc_status%",        "the player's current voice chat state icon (speaking, muted, deafened, etc.)"));
        send(src, parse("<gray><italic><click:run_command:'/features'>← Back to commands → /features</click></italic></gray>"));
    }

    // ── Builders ────────────────────────────────────────────────────

    private static Component header() {
        return parse("<gray>━━ </gray><gold><bold>Fanta's Smart Placeholders</bold></gold><gray> ━━</gray>");
    }

    private static Component section(String name) {
        return parse("<yellow> " + name + "</yellow>");
    }

    private static Component cmd(String command, String desc) {
        String base = command.contains("<") ? command.substring(0, command.indexOf('<')).trim() : command;
        String display = mmEscape(command);
        return parse("  <aqua><click:suggest_command:'" + esc(base) + "'>"
            + "<hover:show_text:'<gray>Click to suggest</gray>'>"
            + display + "</hover></click></aqua>"
            + "<gray> — " + desc + "</gray>");
    }

    private static Component ph(String placeholder, String desc) {
        return parse("  <green><click:copy_to_clipboard:'" + esc(placeholder) + "'>"
            + "<hover:show_text:'<gray>Click to copy</gray>'>"
            + placeholder + "</hover></click></green>"
            + "<gray> — " + desc + "</gray>");
    }

    private static Component note(String text) {
        return parse("  <gray><italic>" + text + "</italic></gray>");
    }

    private static Component parse(String mini) { return TextUtil.parseMini(mini); }

    private static void send(CommandSourceStack src, Component c) {
        src.sendSuccess(() -> c, false);
    }

    private static String esc(String s)     { return s.replace("'", "\\'"); }
    private static String mmEscape(String s) { return s.replace("<", "\\<").replace(">", "\\>"); }
}
