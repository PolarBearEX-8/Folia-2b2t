# Folia 2b2t 1.21.11-5

## Ender Pearl Despawn

Fixes Ender Pearls incorrectly despawning while their owner transfers between dimensions or regions. Folia temporarily removes a player from the source world during an asynchronous transfer. That temporary removal must not be treated as player death by the Ender Pearl despawn check.

The death check now uses the owner's actual dying state rather than general entity liveness. The existing Ender Pearl death gamerule and End-credit exemption remain in effect. Genuine death is not exempted, and pearl physics, collisions and teleportation are unchanged.

### Configuration cleanup

The fix is automatic: there is no custom chamber mode or new configuration switch. The obsolete `config/2b2tfolia.yml` loader and its old fork-specific settings have been removed. Existing copies of that file are ignored by build 5 and can be archived or removed manually. They are not deleted at runtime.

Modern Ender Pearl tickets and their loaded-state reapplication remain enabled automatically when modern Paper pearl behavior is in use. Existing Paper settings, gamerules, performance commands and client chunk cache settings remain available.

See [Ender Pearl Despawn regression checks](docs/ender-pearl-despawn-regression.md) for dimension transfer, actual death, multiple pearls and chunk lifecycle scenarios.

## Performance Boss Bars

Players can enable their own real-time status bars:

- `/ftpsbar [on|off]` shows median TPS, MSPT, and worst MSPT.
- `/frambar [on|off]` shows JVM memory usage.

Both bars refresh once per second and can be toggled independently.

## Client Chunk Cache Distance

This build adds `client-chunk-cache-distance`. It lets a client retain chunks it has already received beyond the server's active view distance, giving players a longer visible horizon without increasing simulation distance.

## Configuration

Add the following settings to `server.properties`:

```properties
view-distance=6
simulation-distance=6
client-chunk-cache-distance=12
```

`client-chunk-cache-distance` accepts values from `-1` to `128`; `-1` is the default and disables the feature, preserving normal vanilla/Paper behaviour. Restart the server after changing this setting.

## Behaviour

- Newly generated and sent chunks still follow `view-distance`.
- Chunks outside the active view distance remain visible only if the client has received them previously, and are removed once they exceed the cache distance.
- The feature does not increase simulation distance and does not keep cache-only chunks loaded or ticketed on the server.
- When a player returns to the active range, the chunk is sent again through the normal chunk pipeline, including Anti-Xray processing.

## Download

Use the attached Paperclip JAR from the GitHub release assets.
