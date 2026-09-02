# Folia 2b2t 1.21.11-1

## Client Chunk Cache Distance

This build adds `client-chunk-cache-distance`. It lets a client retain chunks it has already received beyond the server's active view distance, giving players a longer visible horizon without increasing simulation distance.

## Configuration

Add the following settings to `server.properties`:

```properties
view-distance=6
simulation-distance=6
client-chunk-cache-distance=12
```

`client-chunk-cache-distance` defaults to `-1`, which disables the feature and preserves the normal vanilla/Paper behaviour. Restart the server after changing this setting.

## Behaviour

- Newly generated and sent chunks still follow `view-distance`.
- Chunks outside the active view distance remain visible only if the client has received them previously, and are removed once they exceed the cache distance.
- The feature does not increase simulation distance and does not keep cache-only chunks loaded or ticketed on the server.
- When a player returns to the active range, the chunk is sent again through the normal chunk pipeline, including Anti-Xray processing.

## Download

Use `folia-paperclip-1.21.11-R0.1-SNAPSHOT-mojmap.jar` from the GitHub release assets.
