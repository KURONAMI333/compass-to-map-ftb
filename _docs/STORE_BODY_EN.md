<!-- Summary (CF/Modrinth の一行。本文には含めない) -->
<!-- Turn Explorer's Compass and Nature's Compass finds into FTB Chunks waypoints the moment you find them. -->

Every structure and biome you find with Explorer's Compass or Nature's Compass lands on your FTB Chunks map as a colour-coded waypoint, the instant the search succeeds.

The compasses give you coordinates; FTB Chunks stores waypoints. Nothing carries one to the other, so you end up reading numbers off the compass HUD and typing them into the waypoint screen. This addon closes that gap and does nothing else.

**Features**

- Waypoints structures (Explorer's Compass) and biomes (Nature's Compass). Either mod on its own is enough — the other half simply stays quiet
- Coloured by category, so a full map still reads at a glance: villages gold, strongholds purple, temples and monuments cyan, nether fortresses orange, ancient cities aqua. Biomes follow their own terrain palette
- Named from the target, not a generic label — `minecraft:village_plains` becomes **Village Plains**
- Once placed, it is an ordinary FTB Chunks waypoint. Rename it, recolour it, hide it, delete it. This addon never touches a waypoint it has already placed
- Sensible about repeats. Searching the same biome again will not scatter near-duplicates, and finding a *different* village still gets its own pin
- **Client-side only.** The server does not need this mod. Tested against a dedicated server running only FTB Chunks and the compasses

**Config** (`config/compasstomapftb-client.toml`, or the Mods screen) — three switches, and the defaults are the product: `structures`, `biomes`, `chatNotification`.

**Dependencies**

- [FTB Chunks](https://www.curseforge.com/minecraft/mc-mods/ftb-chunks-forge) — required. It is the map the waypoints go to, so install it the way you normally would (on the server too, if you play on one)
- At least one of [Explorer's Compass](https://www.curseforge.com/minecraft/mc-mods/explorers-compass) or [Nature's Compass](https://www.curseforge.com/minecraft/mc-mods/natures-compass)
- JourneyMap or Xaero's can be installed alongside; this addon only ever writes to FTB Chunks

Using a different map? Sister mods: **Compass to Map** for JourneyMap, **Compass to Map: Xaero's** for Xaero's Minimap.

Bugs and questions: comment on the CurseForge page, or DM @kuronami333 on X.

All Rights Reserved. Free to put in any modpack, on any platform, monetised or not - no permission needed, no credit required. Source is published so you can read exactly what it does: https://github.com/KURONAMI333/compass-to-map-ftb
