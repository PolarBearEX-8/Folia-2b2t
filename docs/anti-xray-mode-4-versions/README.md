# AntiXray Mode 4 versions

These files are historical snapshots for comparison and restoration. They are not applied by the build.

[Detailed legacy chunk-system guide (Thai)](../legacy-chunk-system-th.md) covers both the Folia/Moonrise lifecycle and the previous occluded-air mode 4 implementation.

- `v1-skyfix.patch` — historical heightmap/skylight terrain filtering.
- `v2-chunk-boundary-fix.patch` — historical variant that treats unloaded neighbors as unknown.
- `v3-player-sight.patch` — historical combined Mode 4 and Player Sight implementation.
- `v3-player-sight-folia.patch` — historical per-player chunk refresh companion.
- `0009-AntiXray-mode-4-player-sight.patch` — exact incremental Player Sight patch removed from the active stack.
- `0018-AntiXray-per-player-chunk-refresh.patch` — exact refresh patch removed from the active stack.

- `v4-occluded-air.patch` — previous active implementation, hiding enclosed opaque blocks as air without the underground exemption.

The active build uses `folia-server/paper-patches/features/0008-Add-AntiXray-engine-mode-4-cave-concealment.patch` together with `folia-server/minecraft-patches/features/0018-Add-per-player-cave-visibility.patch`. It conceals undiscovered caves with client-only solid terrain and reveals nearby connected space per player. `folia-server/minecraft-patches/features/0019-Hide-entities-and-effects-in-unrevealed-caves.patch` adds native entity tracking and effect filtering to the same mask. See `../anti-xray-mode-4.md` for the current behavior and limits.
