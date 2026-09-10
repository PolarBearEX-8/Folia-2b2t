# Ender Pearl Despawn regression checks — 1.21.11-5

## Scope

Build 5 distinguishes actual player death from the temporary removal used by Folia's asynchronous transfers. It does not cancel all pearl removals and does not change physics or invent a replacement pearl entity.

No fork-specific configuration is required. Remove/archive the old `config/2b2tfolia.yml` when convenient; build 5 does not load it. Do not delete unrelated Paper or plugin configuration.

## In-game acceptance checks

Run on an isolated test world first. Record the build, gamerules, world overrides and each pearl UUID. PearlStasisAudit can record lifecycle events, but `HIT` does not by itself prove successful player teleportation.

1. Suspend an owned pearl in a bubble column. Record its UUID.
2. Enter the Nether while still alive. Return to the Overworld. Repeat at least 20 times. The original UUID must remain; no `DESPAWN` should accompany transfer.
3. Repeat with End portals (including End credits) and same-dimension teleports crossing Folia regions.
4. Keep three pearls owned by one player in one chunk, and another pearl owned by a second player. Repeat transfers. No pearl should be overwritten or removed by another throw.
5. With the existing Ender Pearl death gamerule enabled, kill the owner without a concurrent respawn/transfer. Their ticking pearls should still despawn. This must not become a keep-pearls-after-death modification.
6. With that gamerule disabled, repeat actual death. Pearls must not be removed by the death branch.
7. Leave the chamber beyond view/simulation distance, wait 90 seconds, throw three more pearls elsewhere, then return. Inspect the original UUID and trigger the chamber.
8. Restart normally. Verify saved pearls load and resume according to existing Paper behavior. Do not classify `UNLOAD` as destruction.
9. Trigger by the normal chamber mechanism; test TNT displacement separately. `HIT`/out-of-world removal must still occur normally. Verify the owner's teleport separately.
10. Disconnect/reconnect without dying. Collect logs for any unexpected removal.

The automatic tests cover the liveness/actual-death predicate; they do not simulate a live cross-region portal transfer. Report live tests separately, with the build and evidence, rather than claiming 100% compatibility from compilation alone.
