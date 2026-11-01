# AntiXray engine mode 4: cave concealment

`engine-mode: 4` sends undiscovered underground terrain as solid stone (deepslate below Y=0, netherrack in the Nether, end stone in the End). It covers ordinary air, cave air, fluids, ores, and block entities in hidden cells. The saved world and server collision state are unchanged.

The current mode 4 also reuses the legacy occluded-solid test as a second packet layer. It evaluates the original server blocks against the six neighboring states in that player's outgoing view. If a candidate solid is completely surrounded by solid states—including the new system's concealed filler wall—it is sent as the dimension's solid filler. The new per-player concealment mask takes precedence, so the legacy pass cannot punch holes in fake walls. This preserves the old mode's candidate rules (redstone-conductor solids, excluding logs and leaves, with the configured stationary-lava option) while adapting its replacement to cave concealment.

## Visibility

- World-facing open space is preserved, including the Nether roof and the void above and below End islands. A six-connected flood extends 12 blocks through entrances and beneath overhangs. The calculation includes the eight loaded neighboring chunks, so chunk borders are not artificial walls. Missing chunks are closed and are never loaded by this feature.
- A second six-connected flood starts around the real server-side player position, travels at most 24 steps inside a 49-block-wide volume, and visits at most 32,768 blocks. Reaching a large connected cave does not reveal the entire cave system.
- Visibility is stored in 4x4x4 cells inside each chunk section. A one-cell wall margin and an eight-block safety shell prevent ordinary movement and mining from hitting fake terrain. The safety shell updates every tick and includes a short prediction of player motion. The connected flood updates every five ticks when needed.
- Revealed cells remain revealed until that player unloads the chunk. Login, respawn, dimension changes, and disconnect create or clear connection-owned state. Previously sent terrain cannot be made secret again from a modified client.
- Each player processes at most two chunk visibility updates per tick. Ordinary reveals send only newly visible cells as section block updates, affected light sections and newly visible block entities. Full chunk packets are reserved for missing initial state or permission bypass changes. Initial chunk delivery keeps the normal chunk-loader limits. Shared topology caches are bounded to 256 chunks. Only actual terrain edits advance visibility revisions; cache fills and evictions never mark player chunks dirty.
- The adapted legacy occlusion pass runs inline with chunk serialization or packet filtering; it does not launch one task per player. Neighbor solidity comes from bounded snapshots copied under the section palette lock, and the pass never mutates another region's world. The earlier standalone projection timings do not benchmark this combined pass, so production movement and multi-player load still need measurement.

## Packet coverage

Chunk sections are copied and reserialized, including correct block/fluid counts, palettes and biome data. Hidden block entities and their oversized extra packets are omitted. Sky and block light nibbles are masked in both initial chunks and live light updates. Single-block updates, section updates, block-entity updates, block events, destruction progress, level events and bundled packets use the same mask that was actually sent to that player. Refresh packets are filtered per recipient even when the original packet is shared.

Native entity tracking now also checks the sent mask before pairing an entity with a player. This includes dropped items, XP orbs, mobs, projectiles and other players. The normal tracker removes entities entering concealed cells and sends their normal spawn/metadata bundle again after revelation. It uses the existing nearby-player tracker loop; there is no extra world entity scan, packet replay queue, ray test or visibility flood per entity.

The packet filter records IDs from allowed spawn packets and forgets them on removal or connection reset. Metadata, equipment, movement, animation, entity effects, entity sounds and pickup animations referencing unknown IDs are suppressed. Passenger lists omit hidden IDs; own damage notifications retain their gameplay effect while hidden attacker references and source positions are removed. Particles and positional sounds use their origin cell, including when sent in bundles. Hidden explosions without player knockback are suppressed; explosion packets carrying knockback are preserved for client physics. The player's own entity, root vehicle and spectator camera remain available.

Filtering uses the existing revealed-cell mask, not exact line of sight. Particle spreads are judged by the emitter origin rather than clipped particle by particle. Player-list entries, locator/waypoint data and arbitrary plugin payloads are outside this filter; plugin-managed virtual entities require their own lifecycle handling. The safety/wall margins intentionally disclose nearby terrain, and distant parts of large caves can show a temporary solid boundary. Extreme travel speeds and busy-server bandwidth require in-game testing; the bounded work is not a production-load benchmark. There is no yaw/pitch cone or exact line-of-sight ray test in this implementation.

## Configuration

```yaml
anticheat:
  anti-xray:
    enabled: true
    engine-mode: 4
    max-block-height: 320
    use-permission: false
```

`max-block-height` is rounded down to a section boundary and limits concealment and the legacy occlusion pass. Modes 1-3 retain their existing algorithms. Mode 4 ignores `hidden-blocks` and `replacement-blocks`; it uses `lava-obscures` for the adapted legacy solid lookup and `update-radius` to refresh nearby blocks when mining opens a face. Revealing still uses the per-player system. When `use-permission` is true, `paper.antixray.bypass` receives real data. There is no blanket exemption for underground players.

After installing a new build, reconnect to discard previously received client chunks.

## Sources and checks

[เอกสารระบบ chunk เก่าแบบละเอียด (Folia/Moonrise + occluded-air)](legacy-chunk-system-th.md)

- Paper-side implementation and tests: `folia-server/paper-patches/features/0008-Add-AntiXray-engine-mode-4-cave-concealment.patch`.
- Minecraft packet and tick hooks: `folia-server/minecraft-patches/features/0018-Add-per-player-cave-visibility.patch`.
- Native entity-tracker gate and packet ID accessors: `folia-server/minecraft-patches/features/0019-Hide-entities-and-effects-in-unrevealed-caves.patch`.
- Historical implementations: `docs/anti-xray-mode-4-versions/`; these are not applied by the build.
- Focused tests: `gradlew :folia-server:test --tests io.papermc.paper.antixray.CaveVisibilityTestSuite`.

## Validation (2026-11-01)

The focused suite passed all 17 tests (7 topology tests and 10 packet/tick tests), with zero failures, errors or skipped tests. `createPaperclipJar` and `scanJarForBadCalls` passed. The Paper patch was checked against the edited sources, and the Minecraft patch was applied to clean baseline copies and reproduced all seven integration files. The packaged JAR reached `Done` in the isolated localhost smoke runtime.

These checks cover packet contents, counts, biome preservation, masking, player isolation, bypass changes, refresh limits, Nether roof air and End void. They do not constitute a visual freecam test or a production-load test.

## Performance regression and mitigation (2026-11-01)

The first cave-concealment build caused severe movement lag. A 20-second JFR recording of the live test server contained 1,791 execution samples; 1,592 (88.9%) were inside mask projection (`addCell` or `toCells`). A region-thread dump also caught `toCells` running inside a full-chunk refresh on the player tick thread. The server was gracefully rolled back to `folia-paperclip-mode4-underground-fix-26.2.jar`, and the player confirmed movement was smooth again. Cave concealment is not active in that rollback build.

The candidate fix replaces unbounded `BitSet.nextSetBit` row searches with constant-size word queries, projects exterior masks only into the recipient chunk without creating nine maps of boxed coordinates, separates geometry invalidation from neighboring visibility invalidation, and prevents cache insertion/eviction from manufacturing terrain-change revisions. Movement reveals use section deltas instead of resending complete chunk data.

A standalone comparison using a 48x384x48 voxel grid measured the old and fixed projection paths with identical output masks: surface case 84.647 ms -> 0.414 ms per chunk; sparse case 837.113 ms -> 0.203 ms. These are local component timings, not server TPS or a multiplayer load benchmark. The candidate must not be described as live-validated solely from these timings.

Candidate validation: all 22 focused tests passed (9 topology/projection and 13 packet/tick cases), including 270 chunk loads to exceed the 256-entry topology caches, 100 idle ticks without resending, and section-only movement reveals. `createPaperclipJar` and `scanJarForBadCalls` passed. The candidate artifact is `run/artifacts/folia-paperclip-mode4-cave-perf-fix-26.2.jar`. Before the subsequent switch, the test server was kept on the rollback artifact: after rollback its `/tps` report showed 20.00 TPS and 2.31 MSPT with one player, and the player confirmed smooth movement.

The performance-fix candidate also reached `Done` in the isolated localhost smoke runtime on port 25568, which was then stopped. It was initially left off the live runtime pending the user-requested switch.

Deployment update (2026-11-01 22:54 Asia/Bangkok): at the user's request, the previous server was stopped gracefully and its world/player data saved. The runtime on `127.0.0.1:25567` now uses `folia-paperclip-mode4-cave-perf-fix-26.2.jar` (PID 17868). It reached `Done` at 22:54:36. Movement performance on the newly switched runtime has not yet been measured with a player.

## Entity/effect patch validation (2026-11-01)

All 35 focused tests passed: 9 topology/projection, 13 chunk/tick and 13 entity/effect cases. The new cases cover hidden/visible spawn bundles, metadata and movement, re-pairing after removal, item pickups, particle/sound origins, passenger references, own damage/knockback, vehicle/camera exceptions, bypass and entity-ID cleanup. The existing cache-churn and no-full-chunk-refresh regressions still pass. `createPaperclipJar` and `scanJarForBadCalls` passed. The 0019 Minecraft patch was applied to clean baseline copies and reproduced all five integration files.

The entity/effect build is `run/artifacts/folia-paperclip-mode4-entities-effects-26.2.jar`. It is separate from the currently running performance-fix build; no live switch is implied by updating these patches.

The entity/effect artifact reached `Done` at 23:18:35 in the isolated localhost runtime on port 25568. That runtime was then stopped; port 25567 was left running the previous performance-fix artifact. No live entity/particle visual test has been claimed.

Deployment update (2026-11-01 23:24 Asia/Bangkok): at the user's restart request, the live runtime was gracefully stopped and switched to `folia-paperclip-mode4-entities-effects-26.2.jar` on `127.0.0.1:25567` (PID 18596). It reached `Done` at 23:24:45. World data and the existing OP list were retained.

Deployment update (2026-11-02 00:38 Asia/Bangkok): at the user's request, the live runtime was gracefully stopped, saving all worlds and player data, then switched to `folia-paperclip-mode4-legacy-layer-26.2.jar` on `127.0.0.1:25567` (PID 12592). It reached `Done` at 00:38:28. No player-side visual or movement test has been run since the switch.

Deployment update (2026-11-02 00:52 Asia/Bangkok): after correcting the legacy occlusion replacement to air for revealed cells while keeping concealed cells as filler, the server was gracefully stopped and restarted with `folia-paperclip-mode4-legacy-air-fix-26.2.jar` on `127.0.0.1:25567` (PID 5608). All worlds and player data were saved; the new process reached `Done` at 00:52:51.

Rollback update (2026-11-02 00:55 Asia/Bangkok): the air replacement produced excessive air, so the source and patch stack were restored to the solid-filler behavior and the server was switched back to `folia-paperclip-mode4-legacy-layer-26.2.jar` after a graceful save. It reached `Done` at 00:55:37 on PID 9552 and is listening on `127.0.0.1:25567`.
