# Fanta's Smart Placeholders

[![Modrinth Downloads](https://img.shields.io/modrinth/dt/fantas-smart-placeholders?logo=modrinth&color=1bd96a&label=downloads)](https://modrinth.com/mod/fantas-smart-placeholders)
[![Modrinth Version](https://img.shields.io/modrinth/v/fantas-smart-placeholders?logo=modrinth&color=1bd96a&label=version)](https://modrinth.com/mod/fantas-smart-placeholders)
[![Modrinth Game Versions](https://img.shields.io/modrinth/game-versions/fantas-smart-placeholders?logo=modrinth&color=1bd96a&label=minecraft)](https://modrinth.com/mod/fantas-smart-placeholders)

A server-side Fabric mod that gives players a "going live" status, a `/profile` card, custom name colors, and a couple of other small conveniences — all exposed as placeholders so your chat/tablist mod can display them. 
One catch: none of this shows up anywhere on its own. You need a chat or tablist mod that actually reads placeholders, like [Styled Chat](https://modrinth.com/mod/styled-chat) or [Styled Player List](https://modrinth.com/mod/styledplayerlist).

---

## What's in it

**Going live.** `/live` flips your status on and drops a broadcast in chat. Give it a link with `/live link <url>` and it'll show up as a clickable badge wherever the placeholder is used — `/live list` shows everyone who's currently live. Drop a `twitch.tv/...` link and the mod picks up the channel name on its own; if the admin has Twitch API keys configured, going live on Twitch flips your in-game status automatically, no command needed.

**`/profile [player]`.** One card with name, color, pronouns (if [Player Pronouns](https://modrinth.com/mod/playerpronouns) is around), role, live status, lore, and no-sleep state. Works for players who are offline too, using whatever they last had set.

**`/color`.** Pick a hex color for your name (`/color ff6b6b` or with the `#`, doesn't matter), or go two-tone with `/color gradient <hex1> <hex2>`. `/color` alone shows what you've got, `/color clear` wipes it.

**`/lore <text>`.** Free text for your profile card. MiniMessage tags work if you want color or a gradient in there.

**`/nosleep`.** Opt out of night-skipping — anyone who tries to sleep while you're toggled on gets a warning and then they can leave their bed.

Voice chat status icons show up automatically if [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat) is installed (speaking/muted/deafened/etc.) hoever,it doesnt use any textures UNLESS YOU TELL IT TO, you will see ugly text! and role tags pull from [LuckPerms](https://luckperms.net) if that's installed — neither is required, they just quietly do nothing if the mod isn't there.

Operators get `/tag` for pinning a custom suffix on someone's name, and `/fsp reload` to reload the config without a restart.

Forget all of the above and just run `/features` in-game — it lists every command (click to autofill) and every placeholder (click to copy).

---

## Commands

| Command | Who | What it does |
|---|---|---|
| `/live` | everyone | toggle live status |
| `/live list` | everyone | see who's live right now |
| `/live link [url]` | everyone | view or set your stream link |
| `/live link clear` | everyone | remove your stream link |
| `/live persist` | everyone | keep live status across a reconnect |
| `/color [hex]` | everyone | view or set your name color |
| `/color gradient <hex1> <hex2>` | everyone | gradient name color |
| `/color clear` | everyone | remove name color |
| `/lore [text]` | everyone | view or set your lore |
| `/lore clear` | everyone | remove lore |
| `/profile [player]` | everyone | view a profile card |
| `/nosleep` | everyone | toggle no-sleep |
| `/features [placeholders]` | everyone | list commands or placeholders |
| `/tag set/get/remove <player> [text]` | op | manage a player's nametag suffix |
| `/tag list` | op | list everyone with a nametag set |
| `/fsp reload` | op | reload config.json live |

## Placeholders

| Placeholder | Value |
|---|---|
| `%fsp:live%` | LIVE badge, clickable if a link is set |
| `%fsp:stream%` | raw stream URL, empty if not live |
| `%fsp:live_stream%` | badge + URL together |
| `%fsp:clickable_stream%` | underlined clickable link with hover text, optional label arg |
| `%fsp:live_count%` | how many online players are live |
| `%fsp:coloredname%` | name in the player's set color/gradient |
| `%fsp:color%` | raw color value |
| `%fsp:role%` | LuckPerms role symbol |
| `%fsp:suffix%` | nametag suffix text |
| `%fsp:vc_status%` | voice chat state icon |
| `%fsp:nosleep%` | skull icon while no-sleep is active |

---

## Setting it up

1. Fabric Loader ≥ 0.18.4 (or ≥ 0.19.3 if you're on Minecraft 26.x), plus [Fabric API](https://modrinth.com/mod/fabric-api) and [Placeholder API](https://modrinth.com/mod/placeholder-api).
2. Grab the jar for your Minecraft version — 1.21.11, or 26.x (covers 26.1.x and 26.2.x) — and drop it in `mods/`. (26.x needs Java 25 on the server; 1.21.11 needs Java 21.)
3. Start the server once to generate `config/Fanta's Placeholders/config.json`.

That's the whole thing for basic usage — colors, profiles, lore, manual `/live` all work out of the box. Everything past this point is optional, for admins who want to go further.

<details>
<summary><strong>Server-side extras: Twitch auto-detect, full config</strong></summary>

### Twitch auto-detect

Grab a client ID and secret from the [Twitch Developer Console](https://dev.twitch.tv/console/apps) and set them in the config:

```json
"twitchClientId": "...",
"twitchClientSecret": "...",
"twitchPollIntervalSeconds": 60
```

Leave them blank and nothing breaks — players just toggle `/live` manually instead, and the mod tells them so when they set a Twitch link without the poller running.

### The rest of config.json

Every message field takes [MiniMessage-style tags](https://placeholders.pb4.eu/user/text-format/) — colors, gradients, click/hover events, all of it. `{player}`, `{link}`, and `{players}` get substituted where relevant.

```json
{
  "livePlaceholder": "<red><bold>LIVE</bold></red>",
  "notLivePlaceholder": "",
  "liveOnMessage": "<green>You are now live!</green>",
  "liveOffMessage": "<yellow>You are no longer live.</yellow>",
  "liveBroadcastMessage": "<gold>{player}</gold> is now live: <aqua><underline><click:open_url:'{link}'>{link}</click></underline></aqua>",
  "livePersistOnMessage": "<green>Auto live on reconnect: <bold>ENABLED</bold></green>",
  "livePersistOffMessage": "<yellow>Auto live on reconnect: <bold>DISABLED</bold></yellow>",
  "liveLinkSetMessage": "<green>Stream link set to: <white>{link}</white></green>",
  "liveLinkTwitchNotConfiguredMessage": "<yellow>Note: this server hasn't set up Twitch auto-detection...</yellow>",
  "twitchClientId": "",
  "twitchClientSecret": "",
  "twitchPollIntervalSeconds": 60,
  "roles": {
    "owner":     "<gold>[Owner]</gold>",
    "admin":     "<red>[Admin]</red>",
    "moderator": "<blue>[Mod]</blue>",
    "vip":       "<yellow>[VIP]</yellow>",
    "default":   ""
  },
  "vcSpeakingIcon": "⌬",
  "vcMutedIcon": "⌭",
  "vcDeafenedIcon": "⌮",
  "vcDisconnectedIcon": "⌯",
  "vcGroupIcon": "⌰",
  "noSleepPlaceholder": "<red>☠</red>",
  "noSleepNotPlaceholder": "",
  "noSleepOnMessage": "<red>You have toggled no-sleep on. Others will be warned when they try to sleep.</red>",
  "noSleepOffMessage": "<green>You have toggled no-sleep off. Others can sleep peacefully.</green>",
  "noSleepBroadcastOnMessage": "<red>{player} doesn't want to skip the night!</red>",
  "noSleepBroadcastOffMessage": "<green>{player} is now okay with skipping the night.</green>",
  "noSleepBedTitle": "<red>Can't skip the night!</red>",
  "noSleepBedSubtitle": "<yellow>{players} doesn't want to sleep!</yellow>"
}
```

`roles` keys need to match your LuckPerms group names exactly. The voice chat icons (`vc*Icon`) map to a font in a resource pack — if the raw glyphs look wrong to players, that's why; you'll want a pack that defines them.

</details>

---

## License

MIT — see [LICENSE](LICENSE).

---

Made for [TSNSMP](https://tsnsmp.com), a whitelisted 18+ SMP. By [fantac4t](https://fantacat.net).
