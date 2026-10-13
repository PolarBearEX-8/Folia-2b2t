# Folia 2b2t 26.2

## Ender Pearl Stasis

This release adds an idempotent Ender Pearl ticket reapply after a pearl returns to the loaded state. It also loads `config/2b2tfolia.yml` automatically during server startup.

The following keys control the fork patch:

```yaml
ender-pearl-stasis:
  enabled: true
  reapply-ticket-on-loaded-transition: true
```

The release includes a regression-test procedure for remote stasis chambers, multiple pearls in one chunk, and cross-region cases.

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
