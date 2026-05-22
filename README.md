# Fanta's Smart Placeholders

A Fabric server-side mod that adds live streaming status, custom name colors, player roles, no-sleep toggling, and real-time voice chat indicators via [Placeholder API](https://placeholders.pb4.eu/).

Built for [TSNSMP](https://tsnsmp.online) and designed to work alongside any chat/tablist mod that supports Placeholder API.

> [!IMPORTANT]
> To use this mod's full features, you must have a compatible mod that supports Placeholder API — such as [Styled Chat](https://modrinth.com/mod/styled-chat) or [Styled Player List](https://modrinth.com/mod/styledplayerlist). The placeholders this mod provides will not appear anywhere without one.

---

## Features

### 🔴 Live Status
Players who stream can mark themselves as live directly in-game. A configurable `LIVE` tag appears via placeholder in chat, the tablist, or anywhere your chat mod supports placeholders. A server-wide broadcast announces the stream with a clickable link.

- Toggle with `/live`
- Set your stream URL with `/live link <url>`
- Optionally keep live status across reconnects with `/live autoLiveOnDisconnect`

### 🎨 Custom Name Colors
Players can set a personal RGB hex color for their name, rendered wherever the color placeholder is used.

- Set with `/color <#hex>` — e.g. `/color #ff6b6b` or `/color ff6b6b`
- Remove with `/color clear`

### 🏷️ Player Roles & Suffixes
Assign configurable role symbols to players (supporter, owner, member, etc.). Symbols are fully customizable in the config file and exposed as placeholders. Operators can also assign freeform suffix text to any player.

### 💤 No-Sleep
Players can opt out of night-skipping. When a no-sleep player is online, anyone who tries to sleep sees a warning title/subtitle and a sound cue. The no-sleep player's placeholder shows a ☠ skull indicator.

- Toggle with `/nosleep`

### 🎙️ Voice Chat Integration *(optional)*
When [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat) is installed, real-time voice status icons appear automatically via placeholder:

| State | Icon |
|---|---|
| Speaking | ⌬ |
| Muted | ⌭ *(currently not working)* |
| Deafened | ⌮ |
| Disconnected | ⌯ |
| In group | ⌰ *(currently not working)* |

Icons map to a resource pack font — include the bundled resource pack for correct rendering.

### 📝 Suffix System *(operator only)*
Operators can assign custom text suffixes to any player.

- `/suffix <player> <text>` — set a suffix (max 48 characters)
- `/suffix get <player>` — view a player's current suffix
- `/suffix clear <player>` — remove a suffix

---

## Commands

| Command | Permission | Description |
|---|---|---|
| `/live` | Everyone | Toggle your live status |
| `/live link <url>` | Everyone | Set your stream URL |
| `/live autoLiveOnDisconnect` | Everyone | Toggle auto-off on disconnect |
| `/color <hex>` | Everyone | Set your name color |
| `/color clear` | Everyone | Remove your name color |
| `/nosleep` | Everyone | Toggle no-sleep status |
| `/suffix <player> <text>` | Operator | Set a player's suffix |
| `/suffix get <player>` | Operator | Get a player's suffix |
| `/suffix clear <player>` | Operator | Clear a player's suffix |

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
| `%playerstatus:role%` | Player's role symbol (also `%playerstatus:suffix%`) |
| `%playerstatus:vc_status%` | Current voice chat status icon |
| `%playerstatus:nosleep%` | ☠ skull icon if no-sleep is active, empty otherwise |

---

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) ≥ 0.18.4
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Install [Placeholder API](https://modrinth.com/mod/placeholder-api)
4. Drop `FantasSmartPlaceholders-1.5.0.jar` into your server's `mods/` folder
5. *(Optional)* Install [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat) for voice status icons
6. Start the server — a default config is generated at `config/playerstatus/config.json`

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
  "roles": {
    "owner": "^",
    "supporter": "$",
    "member": ""
  },
  "vcSpeakingIcon": "⌬",
  "vcMutedIcon": "⌭",
  "vcDeafenedIcon": "⌮",
  "vcDisconnectedIcon": "⌯",
  "vcGroupIcon": "⌰",
  "noSleepPlaceholder": "<red>☠</red>",
  "noSleepOnMessage": "<red>You have toggled no-sleep on.</red>",
  "noSleepOffMessage": "<green>You have toggled no-sleep off.</green>",
  "noSleepBroadcastOnMessage": "<red>{player} doesn't want to skip the night!</red>",
  "noSleepBroadcastOffMessage": "<green>{player} is now okay with skipping the night.</green>",
  "noSleepBedTitle": "<red>Can't skip the night!</red>",
  "noSleepBedSubtitle": "<yellow>{players} don't want to sleep!</yellow>"
}
```

> **Template tokens:** `{player}` = player's name, `{link}` = stream URL, `{players}` = comma-separated list of no-sleep players.

---

## Requirements

| Dependency | Type | Version |
|---|---|---|
| Minecraft | Required | 1.21.11 |
| Fabric Loader | Required | ≥ 0.18.4 |
| Fabric API | Required | Any |
| Placeholder API | Required | Any |
| Simple Voice Chat | Optional | Any |

---

## License

MIT — see [LICENSE](LICENSE) for details.

---

*Made by [fantac4t](https://fantacat.net) for [TSNSMP](https://www.tsnsmp.online)*
