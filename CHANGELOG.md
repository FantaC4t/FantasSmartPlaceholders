# Changelog

## [1.6.0] — 2026-06-06

### Added
- **`/nametag` command** — replaces `/suffix` with a cleaner `set/get/remove` subcommand structure. Text is capped at 48 characters with a warning if truncated. Operator-only.
- **LuckPerms role integration** — `%playerstatus:role%` now reads each player's primary group from LuckPerms and maps it to a configurable symbol. LuckPerms is optional; the placeholder returns empty if it isn't installed.
- **Config-driven messages** for `/live persist` toggle and `/live link` — three new keys: `livePersistOnMessage`, `livePersistOffMessage`, `liveLinkSetMessage`.
- **MIT License**

### Changed
- `/live autoLiveOnDisconnect` renamed to `/live persist` — shorter and clearer.
- `%playerstatus:suffix%` placeholder identifier kept for backward compatibility, but now backed by the renamed nametag system.
- Role config defaults updated with proper MiniMessage formatting (`owner`, `admin`, `moderator`, `vip`, `default`).
- Player data storage is now thread-safe (`ConcurrentHashMap`, `volatile` dirty flag).
- Player data JSON is backward-compatible — existing `suffix` entries are read correctly after the internal rename.

### Fixed
- `/color clear` was sending a duplicate confirmation message — now sends once.
- `/live` and `/nosleep` commands now always return success code `1` regardless of the toggled state.

### Removed
- `/suffix` command — replaced by `/nametag`.

