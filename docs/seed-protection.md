# Structure seed protection

Folia 2b2t can derive structure-placement salts from a private server key. This makes a structure set use a different, stable placement salt for each world seed without changing Minecraft's terrain-generation seed.

## Configuration

```yaml
seed-protection:
  enabled: true
```

The server creates `config/2b2tfolia-seed.key` on first startup with protection enabled. The file contains a 256-bit secret. Back it up securely, keep it private, and copy the same file to every server instance that must generate or inspect the same world.

If the key is lost, the next startup creates a different key. The resulting structure placement will differ for chunks generated afterward. Do not rotate or replace the key on a live world unless you intend to change placement for unexplored chunks. Existing saved chunks are not moved.

Set `seed-protection.enabled: false` to use the ordinary Minecraft placement salts for chunks generated afterward. If a world has already been generated with protection enabled, disabling it can make new chunks use a different placement layout from unexplored areas. Keep the option and key consistent for that world.

## Coverage and limits

- Random-spread structure sets use a keyed salt for candidate locations, including registry-backed datapack structure sets.
- Concentric-ring placements, including Strongholds, use a keyed ring seed.
- Frequency checks use the protected salt where the placement method supports a salt, so structure discovery checks follow the same protected layout.
- The existing world seed and terrain, biome, and cave generation are unchanged.
- Structures already saved in chunks stay where they are. The feature does not regenerate or move existing chunks.
- On an existing world, structure lookup such as `/locate` may not find older structures that were generated with the previous placement salts. Newly generated chunks use the protected layout.
- This raises the cost of using known vanilla structure-placement salts to recover a world seed. It is not a guarantee against every seed-recovery method; terrain, biome, plugin, or other server information may still reveal seed information.

Do not publish the key file in a repository, support archive, or public backup. Keep a restricted backup and use the same key when restoring the world.
