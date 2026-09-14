# Wow More Enchants

A Minecraft 1.21.1 mod that adds new enchantments. Built for both **Fabric**
and **NeoForge** (NeoForge is the actively maintained continuation of Forge
for 1.21.1).

## Enchantments

| Enchantment | Item        | Max level | Where the book is found                    | Effect                                                       |
|-------------|-------------|-----------|--------------------------------------------|--------------------------------------------------------------|
| Swiftness   | Boots       | 4         | Village librarian trades (master)          | +6% movement speed per level (attribute-based, no code)      |
| Leech       | Sword/Axe   | 3         | Bastion remnant chests                     | Heal 2 HP per level when you kill a mob                       |
| Zealot      | Sword/Axe   | 3         | Bastion remnant chests                     | Gain Regeneration for 3 seconds per level when you kill a mob |
| Vein Miner  | Pickaxe/Axe | 1         | Mineshaft & dungeon chests                 | Mines an entire connected ore vein (works with Fortune)      |
| Timber      | Axe         | 1         | Mineshaft & dungeon chests                 | Fells a whole tree (works with Fortune/Silk Touch)            |
| Planter     | Hoe         | 3         | Enchanting table                           | Freshly planted crops start at a later growth stage           |
| Heavy       | Chestplate  | 5         | Rare iron golem drop                       | +10% knockback resistance per level (attribute-based)         |
| Spikes      | Shield      | 3         | Outpost / mansion chests                   | Damages attackers your shield blocks                          |
| Berserker   | Sword/Axe   | 3         | Outpost / mansion chests                   | Below 35% health: +15/25/40% damage and +10/15/25% attack speed |
| Executioner | Sword/Axe   | 3         | Enchanting table                           | Bonus damage vs weakened enemies (by remaining health %)      |
| Momentum    | Boots       | 3         | Enchanting table                           | Movement speed builds while you keep moving (resets on damage)|
| Overcharge  | Pickaxe     | 3         | Archaeology (suspicious sand & gravel)     | Breaking the same block type chains into more mining speed    |
| Thornsoul   | Chestplate  | 3         | Ancient city chests                        | Melee hits give a temporary +15/30/45% attack damage          |

Leech and Zealot are mutually exclusive; Vein Miner and Timber are mutually
exclusive.

Swiftness and Heavy are defined entirely in datapack JSON (vanilla
`minecraft:attributes` effect). Leech and Zealot use a small on-kill event
handler. Vein Miner uses a block-break event handler: it expands through
connected ore blocks (using the vanilla ore tags, so `iron_ore` connects to
`deepslate_iron_ore`), and routes drops through the vanilla loot path so
Fortune and Silk Touch apply normally. Timber expands through connected logs
(`#minecraft:logs`) and only triggers on a real tree (a cluster of at least 3
logs, at least 3 blocks tall, with a trunk column). Planter boosts a freshly
placed crop's vanilla `age` property to the stage matching its level. Spikes
hurts the attacker via the thorns damage path whenever your shield actually
blocks a hit.

Berserker adds transient attack damage and attack speed attribute modifiers
while you are below 35% max health (checked every server tick). Executioner grants bonus damage based on the
enemy's health before your hit: up to x4 level below 35% health, x2.5 level
below 50%, and x1 level below 70%. Momentum ramps a movement speed bonus up
to 5/9/14% per level while you keep moving and drops back to zero the moment
you stop or take damage. Overcharge adds a stacking mining-speed bonus (a
temporary modifier on the player block-break-speed attribute, not a Haste
effect) while you consecutively break the same block type: +20% per stack, up
to 2/4/6 stacks per level, fading 5 seconds after you stop.
Thornsoul turns melee hits you take into a temporary attack damage boost
(80/120/160 ticks per level).

A server tick handler drives the per-player timed effects (Berserker, Momentum,
Thornsoul, Overcharge); the damage events feed Executioner and Thornsoul.

## Project layout

```
EnchantExtras/
  fabric/     Self-contained Fabric build (Loom + Mojang mappings)
  neoforge/   Self-contained NeoForge build (ModDevGradle + Parchment)
  LICENSE
  README.md
```

The enchantment definitions (`data/enchantextras/enchantment/*.json`), the
`EEAbilities` behavior class, and the datapack tags are kept identical in both
projects.

## Building

Gradle wrapper per project, so no Gradle install is needed (Java 21 required):

```
cd fabric
.\gradlew.bat build

cd neoforge
.\gradlew.bat build
```

The built JAR is in `build/libs/` and goes into your mods folder.

To run a test client instead of building:

```
.\gradlew.bat runClient      # Fabric
.\gradlew.bat runClient      # NeoForge
```

## Finding the enchantments in game

Enchanted books and sources:

- **Planter, Momentum, Executioner** – available on the vanilla enchanting table
  (added to the `minecraft:in_enchanting_table` enchantment tag).
- **Leech, Zealot** – enchanted books in bastion remnants chests (bridge, hoglin
  stable, other, treasure).
- **Vein Miner, Timber** – enchanted books in mineshaft and dungeon chests.
- **Spikes, Berserker** – enchanted books in pillager outpost and woodland
  mansion chests.
- **Thornsoul** – enchanted books in ancient city chests.
- **Overcharge** – enchanted books from archaeology: suspicious sand in desert
  pyramids and suspicious gravel in trail ruins.
- **Heavy** – a rare drop from iron golems killed by a player (this is the only
  source).
- **Swiftness** – sold by a village librarian (master-tier trade, 32 emeralds).

Every loot table above is an appended rare pool on top of the vanilla table, so
nothing vanilla is removed. None of the enchantments outside the enchanting
table are otherwise random-enchantable from vanilla loot.

For quick testing, use a command:

```
/enchant @p enchantextras:swiftness 4
/enchant @p enchantextras:leech 3
/enchant @p enchantextras:zealot 3
/enchant @p enchantextras:vein_miner 1
/enchant @p enchantextras:timber 1
/enchant @p enchantextras:planter 3
/enchant @p enchantextras:heavy 5
/enchant @p enchantextras:spikes 3
/enchant @p enchantextras:berserker 3
/enchant @p enchantextras:executioner 3
/enchant @p enchantextras:momentum 3
/enchant @p enchantextras:overcharge 3
/enchant @p enchantextras:thornsoul 3
```

Planter works per-block (stage is applied to crops placed while a Planter hoe
is held in either hand). Timber works with Fortune and Silk Touch. Spikes
levels deal 2, 4 and 7 damage respectively for levels I, II and III.

## Notes for adding your own enchantments

1. Add a JSON file under `data/enchantextras/enchantment/<name>.json`
   (see the existing files for the schema).
2. If the enchantment needs custom behavior, add the logic to `EEAbilities`
   and hook it up in the platform event handler:
   - NeoForge: `EEEvents` (`LivingDeathEvent`, `LivingDamageEvent.Post`,
     `BlockEvent.BreakEvent`, `BlockEvent.EntityPlaceEvent`, `ServerTickEvent.Post`,
     on the game bus)
   - Fabric: `EEMod` (`ServerLivingEntityEvents.AFTER_DEATH`,
     `ServerLivingEntityEvents.AFTER_DAMAGE`, `PlayerBlockBreakEvents.AFTER`,
     `UseBlockCallback`, `ServerTickEvents.END_SERVER_TICK`)
3. Add a display name in `assets/enchantextras/lang/en_us.json`.