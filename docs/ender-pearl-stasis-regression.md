# Ender Pearl Stasis Regression Test

Use a fresh test world with Ender Pearl stasis enabled in `config/2b2tfolia.yml`.

1. Create a bubble-column stasis chamber at least 13 chunks from the test player. Throw one Ender Pearl into it and record its entity UUID with `/data get entity` while standing nearby.
2. Leave until the chamber is outside the 12-chunk view distance and wait at least 90 seconds. Do not keep the chamber loaded with a player, portal, or plugin ticket.
3. Throw three additional Ender Pearls at the player's new location. After each throw, wait five seconds.
4. Return to the chamber. The original pearl must still be present and have the recorded UUID. Trigger the chamber and confirm that it teleports its owner.
5. Repeat the test with the chamber across a Folia region boundary and with two pearls in the same chamber chunk. Both pearls must remain present; triggering one must not remove the other.

For a failure report, record the server build, all Paper world settings, AnarchyExploitFixes configuration, the original pearl UUID, and logs around `EntityRemoveEvent`. A pearl removed with a cause other than `HIT`, `DESPAWN`, `KILLED`, or an explicit plugin action is a regression.
