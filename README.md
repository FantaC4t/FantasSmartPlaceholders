# Fanta's Smart Placeholders

[![Modrinth Downloads](https://img.shields.io/modrinth/dt/fantas-smart-placeholders?logo=modrinth&color=1bd96a&label=downloads)](https://modrinth.com/mod/fantas-smart-placeholders)
[![Modrinth Version](https://img.shields.io/modrinth/v/fantas-smart-placeholders?logo=modrinth&color=1bd96a&label=version)](https://modrinth.com/mod/fantas-smart-placeholders)
[![Modrinth Game Versions](https://img.shields.io/modrinth/game-versions/fantas-smart-placeholders?logo=modrinth&color=1bd96a&label=minecraft)](https://modrinth.com/mod/fantas-smart-placeholders)

A Fabric server-side mod that adds live streaming status, custom name colors, LuckPerms-driven player roles, no-sleep toggling, and real-time voice chat indicators via [Placeholder API](https://placeholders.pb4.eu/).

Built for [TSNSMP](https://tsnsmp.online) and designed to work alongside any chat/tablist mod that supports Placeholder API.

> [!IMPORTANT]
> To use this mod's full features, you must have a compatible mod that supports Placeholder API — such as [Styled Chat](https://modrinth.com/mod/styled-chat) or [Styled Player List](https://modrinth.com/mod/styledplayerlist). The placeholders this mod provides will not appear anywhere without one.

---

## Features

### 🔴 Live Status
Players who stream can mark themselves as live directly in-game. A configurable `LIVE` tag appears via placeholder in chat, the tablist, or anywhere your chat mod supports placeholders. A server-wide broadcast announces the stream with a clickable link.

- Toggle with `/live`
- Set your stream URL with `/live link <url>`
- Keep live status across reconnects with `/live persist`

### 🎨 Custom Name Colors
Players can set a personal RGB hex color for their name, rendered wherever the color placeholder is used.

- Set with `/color <#hex>` — e.g. `/color #ff6b6b` or `/color ff6b6b`
- Remove with `/color clear`

### 🏷️ Player Roles
Role symbols are driven by [LuckPerms](https://luckperms.net) — the mod reads each player's primary group and maps it to a configurable symbol via the `roles` section in config. Symbols support full MiniMessage formatting.

LuckPerms is optional: if it isn't installed, `%playerstatus:role%` simply returns empty.

### 📝 Nametag *(operator only)*
Operators can assign freeform text to appear as a suffix in a player's name.

- `/nametag set <player> <text>` — assign a nametag (max 48 characters)
- `/nametag get <player>` — view a player's current nametag
- `/nametag remove <player>` — remove a nametag

### 💤 No-Sleep
Players can opt out of night-skipping. When a no-sleep player is online, anyone who tries to sleep sees a warning title/subtitle and a sound cue. The no-sleep player's placeholder shows a ☠ skull indicator.

- Toggle with `/nosleep`

### 🎙️ Voice Chat Integration *(optional)*
When [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat) is installed, real-time voice status icons appear automatically via placeholder:

| State | Icon |
|---|---|
| Speaking | ⌬ |
| Muted | ⌭ |
| Deafened | ⌮ |
| Disconnected | ⌯ |
| In group | ⌰ |

Icons map to a resource pack font — include the bundled resource pack for correct rendering.

---

## Commands

| Command | Permission | Description |
|---|---|---|
| `/live` | Everyone | Toggle your live status |
| `/live link <url>` | Everyone | Set your stream URL |
| `/live persist` | Everyone | Toggle auto-off live status on disconnect |
| `/color <hex>` | Everyone | Set your name color |
| `/color clear` | Everyone | Remove your name color |
| `/nosleep` | Everyone | Toggle no-sleep status |
| `/nametag set <player> <text>` | Operator | Assign a nametag to a player |
| `/nametag get <player>` | Operator | View a player's current nametag |
| `/nametag remove <player>` | Operator | Remove a player's nametag |

---

## Placeholders

All placeholders are registered with [Placeholder API](https://placeholders.pb4.eu/) and work in any compatible chat/tablist mod.

| Placeholder | Description |
|---|---|
| `%playerstatus:live%` | LIVE tag if the player is live, empty otherwise |
| `%playerstatus:stream%` | Raw stream URL if the player is live, empty otherwise |
| `%playerstatus:live_stream%` | LIVE tag + stream URL combined |
| `%playerstatus:clickable_stream%` | Clickable, hoverable stream link (optional label as argument) |
| `%playerstatus:coloredname%` | Player's name rendered in their chosen color |
| `%playerstatus:color%` | Player's chosen hex color value |
| `%playerstatus:role%` | Role symbol from LuckPerms primary group (requires LuckPerms) |
| `%playerstatus:suffix%` | Manually assigned nametag text (set via `/nametag`) |
| `%playerstatus:vc_status%` | Current voice chat status icon |
| `%playerstatus:nosleep%` | ☠ skull icon if no-sleep is active, empty otherwise |

---

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) ≥ 0.18.4
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Install [Placeholder API](https://modrinth.com/mod/placeholder-api)
4. Drop `FantasSmartPlaceholders-1.6.0.jar` into your server's `mods/` folder
5. *(Optional)* Install [LuckPerms](https://luckperms.net) for role placeholder support
6. *(Optional)* Install [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat) for voice status icons
7. Start the server — a default config is generated at `config/playerstatus/config.json`

---

## Configuration

Config is generated at `config/playerstatus/config.json` on first launch. All message fields support [QuickText / MiniMessage](https://placeholders.pb4.eu/user/quicktext/) formatting.

```json
{
  "livePlaceholder": "<red><bold>LIVE</bold></red>",
  "notLivePlaceholder": "",
  "liveOnMessage": "<green>You are now live!</green>",
  "liveOffMessage": "<yellow>You are no longer live.</yellow>",
  "liveBroadcastMessage": "<gold>{player}</gold> is now live: <aqua><underline><click:open_url:{link}>{link}</click></underline></aqua>",
  "livePersistOnMessage": "<green>Auto live on reconnect: <bold>ENABLED</bold></green>",
  "livePersistOffMessage": "<yellow>Auto live on reconnect: <bold>DISABLED</bold></yellow>",
  "liveLinkSetMessage": "<green>Stream link set to: <white>{link}</white></green>",
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

> **Template tokens:** `{player}` = player's name, `{link}` = stream URL, `{players}` = comma-separated list of no-sleep players.

> **Roles:** Keys must match your LuckPerms group names exactly. Values can be plain text or MiniMessage tags.

---

## Requirements

| Dependency | Type | Version |
|---|---|---|
| Minecraft | Required | 1.21.11 |
| Fabric Loader | Required | ≥ 0.18.4 |
| Fabric API | Required | Any |
| Placeholder API | Required | Any |
| LuckPerms | Optional | Any |
| Simple Voice Chat | Optional | Any |

---

## License

MIT — see [LICENSE](LICENSE) for details.

---

## About TSNSMP

This mod was built for **[TSNSMP](https://www.tsnsmp.online)** — a whitelisted, community-driven Minecraft SMP.

> **18+ server.** Minors are not permitted to join unless thoroughly vouched for by existing members.

If you're interested in joining or just want to see the mod in action, check us out at [tsnsmp.online](https://www.tsnsmp.online).

---

*Made by [fantac4t](https://fantacat.net)*
