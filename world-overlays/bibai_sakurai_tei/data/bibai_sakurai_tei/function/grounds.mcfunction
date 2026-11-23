# Sakurai residence branch of the Bibai City Community Hall: the former Sakurai family residence (main house,
# four-room wing, detached room), the Bibai tondenhei-oku (soldier-settler house) and the bust of Sakurai Shogo.
# After the photos on bibai-sk.net (images 1-3 and the bust photo), the first photo of the tondenhei-oku on
# Bunka Isan Online, and an aerial screenshot supplied by the team (sakurai01). Buildings follow OpenStreetMap.
# Relative origin: (X 1306, Y -55, Z 1453) at ground level. Writes only inside ~0..~80, ~-1..~20, ~-6..~34
# (X 1306-1386, Y -56..-35, Z 1447-1487); tree crowns at the edges may reach a few blocks further.

# Remove the generated placeholder buildings, paving and trees (the street sidewalks stay outside), level the lawn
fill ~ ~1 ~ ~80 ~10 ~34 minecraft:air
fill ~ ~11 ~ ~80 ~20 ~34 minecraft:air
fill ~ ~ ~ ~80 ~ ~34 minecraft:grass_block
fill ~ ~-1 ~ ~80 ~-1 ~34 minecraft:dirt

# North strip (Z 1447-1452, outside the main rectangle): remove only the generated placeholder buildings found there.
# Their columns were listed from the base map; sidewalks and the street are not touched.
fill ~51 ~1 ~-6 ~54 ~20 ~-6 minecraft:air
fill ~51 ~0 ~-6 ~54 ~0 ~-6 minecraft:grass_block
fill ~51 ~-1 ~-6 ~54 ~-1 ~-6 minecraft:dirt
fill ~30 ~1 ~-5 ~40 ~20 ~-5 minecraft:air
fill ~51 ~1 ~-5 ~58 ~20 ~-5 minecraft:air
fill ~51 ~0 ~-5 ~58 ~0 ~-5 minecraft:grass_block
fill ~51 ~-1 ~-5 ~58 ~-1 ~-5 minecraft:dirt
fill ~30 ~1 ~-4 ~40 ~20 ~-4 minecraft:air
fill ~51 ~1 ~-4 ~62 ~20 ~-4 minecraft:air
fill ~51 ~0 ~-4 ~62 ~0 ~-4 minecraft:grass_block
fill ~51 ~-1 ~-4 ~62 ~-1 ~-4 minecraft:dirt
fill ~30 ~1 ~-3 ~40 ~20 ~-3 minecraft:air
fill ~51 ~1 ~-3 ~65 ~20 ~-3 minecraft:air
fill ~51 ~0 ~-3 ~65 ~0 ~-3 minecraft:grass_block
fill ~51 ~-1 ~-3 ~65 ~-1 ~-3 minecraft:dirt
fill ~30 ~1 ~-2 ~39 ~20 ~-2 minecraft:air
fill ~43 ~1 ~-2 ~45 ~20 ~-2 minecraft:air
fill ~50 ~1 ~-2 ~67 ~20 ~-2 minecraft:air
fill ~50 ~0 ~-2 ~67 ~0 ~-2 minecraft:grass_block
fill ~50 ~-1 ~-2 ~67 ~-1 ~-2 minecraft:dirt
fill ~30 ~1 ~-1 ~39 ~20 ~-1 minecraft:air
fill ~43 ~1 ~-1 ~47 ~20 ~-1 minecraft:air
fill ~43 ~0 ~-1 ~47 ~0 ~-1 minecraft:grass_block
fill ~43 ~-1 ~-1 ~47 ~-1 ~-1 minecraft:dirt
fill ~50 ~1 ~-1 ~64 ~20 ~-1 minecraft:air
fill ~50 ~0 ~-1 ~64 ~0 ~-1 minecraft:grass_block
fill ~50 ~-1 ~-1 ~64 ~-1 ~-1 minecraft:dirt

# Gravel forecourt and paths: from the street, to the porch, to the bust and to the tondenhei-oku
fill ~47 ~0 ~2 ~54 ~0 ~14 minecraft:gravel
fill ~50 ~0 ~0 ~52 ~0 ~1 minecraft:gravel
fill ~55 ~0 ~7 ~71 ~0 ~9 minecraft:gravel
fill ~15 ~0 ~16 ~34 ~0 ~17 minecraft:gravel

# --- Main house (1933). After photo 1 (from the east: entrance porch), photo 2 (from the west) and photo 3
#     (from the north-east): a two-storey core with a north-facing gable, the entrance porch on its east face,
#     a ground-floor bay on its north face, and a single-storey west wing with its own red gabled roof ---
# two-storey core (ridge north-south)
fill ~38 ~0 ~4 ~45 ~0 ~11 minecraft:spruce_planks
fill ~38 ~1 ~4 ~45 ~9 ~11 minecraft:dark_oak_planks
fill ~38 ~1 ~4 ~45 ~1 ~11 minecraft:spruce_planks
fill ~39 ~1 ~5 ~44 ~4 ~10 minecraft:air
fill ~39 ~5 ~5 ~44 ~5 ~10 minecraft:spruce_planks
fill ~39 ~6 ~5 ~44 ~9 ~10 minecraft:air
fill ~37 ~10 ~3 ~37 ~10 ~12 minecraft:mangrove_slab[type=bottom]
fill ~38 ~10 ~3 ~38 ~10 ~12 minecraft:mangrove_slab[type=top]
fill ~39 ~11 ~3 ~39 ~11 ~12 minecraft:mangrove_slab[type=bottom]
fill ~40 ~11 ~3 ~40 ~11 ~12 minecraft:mangrove_slab[type=top]
fill ~41 ~12 ~3 ~41 ~12 ~12 minecraft:mangrove_slab[type=bottom]
fill ~42 ~12 ~3 ~42 ~12 ~12 minecraft:mangrove_slab[type=bottom]
fill ~43 ~11 ~3 ~43 ~11 ~12 minecraft:mangrove_slab[type=top]
fill ~44 ~11 ~3 ~44 ~11 ~12 minecraft:mangrove_slab[type=bottom]
fill ~45 ~10 ~3 ~45 ~10 ~12 minecraft:mangrove_slab[type=top]
fill ~46 ~10 ~3 ~46 ~10 ~12 minecraft:mangrove_slab[type=bottom]
fill ~39 ~10 ~4 ~39 ~10 ~4 minecraft:dark_oak_planks
fill ~40 ~10 ~4 ~40 ~10 ~4 minecraft:dark_oak_planks
fill ~41 ~10 ~4 ~41 ~11 ~4 minecraft:dark_oak_planks
fill ~42 ~10 ~4 ~42 ~11 ~4 minecraft:dark_oak_planks
fill ~43 ~10 ~4 ~43 ~10 ~4 minecraft:dark_oak_planks
fill ~44 ~10 ~4 ~44 ~10 ~4 minecraft:dark_oak_planks
fill ~39 ~10 ~11 ~39 ~10 ~11 minecraft:dark_oak_planks
fill ~40 ~10 ~11 ~40 ~10 ~11 minecraft:dark_oak_planks
fill ~41 ~10 ~11 ~41 ~11 ~11 minecraft:dark_oak_planks
fill ~42 ~10 ~11 ~42 ~11 ~11 minecraft:dark_oak_planks
fill ~43 ~10 ~11 ~43 ~10 ~11 minecraft:dark_oak_planks
fill ~44 ~10 ~11 ~44 ~10 ~11 minecraft:dark_oak_planks
# upper-floor windows on the south and west faces; the north gable of the main roof is plain boards (photo 3)
fill ~40 ~6 ~11 ~43 ~8 ~11 minecraft:glass_pane
fill ~38 ~6 ~6 ~38 ~8 ~9 minecraft:glass_pane
# lower north projection with its own gable roof below the main gable (stepped double gable, photo 3):
# small railed window in its gable, and a ground-floor bay window with a little roof under it
fill ~39 ~0 ~2 ~44 ~0 ~3 minecraft:spruce_planks
fill ~39 ~1 ~2 ~44 ~5 ~3 minecraft:dark_oak_planks
fill ~39 ~1 ~2 ~44 ~1 ~3 minecraft:spruce_planks
fill ~40 ~1 ~3 ~43 ~4 ~4 minecraft:air
fill ~38 ~6 ~1 ~38 ~6 ~4 minecraft:mangrove_slab[type=bottom]
fill ~39 ~6 ~1 ~39 ~6 ~4 minecraft:mangrove_slab[type=top]
fill ~40 ~7 ~1 ~40 ~7 ~4 minecraft:mangrove_slab[type=bottom]
fill ~41 ~7 ~1 ~41 ~7 ~4 minecraft:mangrove_slab[type=top]
fill ~42 ~7 ~1 ~42 ~7 ~4 minecraft:mangrove_slab[type=top]
fill ~43 ~7 ~1 ~43 ~7 ~4 minecraft:mangrove_slab[type=bottom]
fill ~44 ~6 ~1 ~44 ~6 ~4 minecraft:mangrove_slab[type=top]
fill ~45 ~6 ~1 ~45 ~6 ~4 minecraft:mangrove_slab[type=bottom]
fill ~40 ~6 ~2 ~40 ~6 ~2 minecraft:dark_oak_planks
fill ~41 ~6 ~2 ~41 ~6 ~2 minecraft:dark_oak_planks
fill ~42 ~6 ~2 ~42 ~6 ~2 minecraft:dark_oak_planks
fill ~43 ~6 ~2 ~43 ~6 ~2 minecraft:dark_oak_planks
setblock ~41 ~6 ~2 minecraft:glass_pane
setblock ~42 ~6 ~2 minecraft:glass_pane
fill ~40 ~5 ~1 ~43 ~5 ~1 minecraft:spruce_slab[type=top]
fill ~40 ~6 ~1 ~43 ~6 ~1 minecraft:spruce_fence
fill ~40 ~1 ~1 ~43 ~1 ~1 minecraft:spruce_planks
fill ~40 ~2 ~1 ~43 ~3 ~1 minecraft:glass_pane
fill ~39 ~4 ~1 ~44 ~4 ~1 minecraft:mangrove_stairs[facing=south]
fill ~40 ~2 ~2 ~43 ~3 ~2 minecraft:air
fill ~46 ~6 ~5 ~46 ~8 ~7 minecraft:glass_pane
fill ~46 ~9 ~4 ~46 ~9 ~8 minecraft:mangrove_slab[type=bottom]
# entrance porch on the east with a small gable, double door and white plastered panel
fill ~46 ~0 ~6 ~48 ~0 ~10 minecraft:smooth_stone
fill ~48 ~1 ~6 ~48 ~3 ~6 minecraft:stripped_dark_oak_log
fill ~48 ~1 ~10 ~48 ~3 ~10 minecraft:stripped_dark_oak_log
fill ~46 ~4 ~5 ~49 ~4 ~5 minecraft:mangrove_slab[type=bottom]
fill ~46 ~4 ~6 ~49 ~4 ~6 minecraft:mangrove_slab[type=top]
fill ~46 ~5 ~7 ~49 ~5 ~7 minecraft:mangrove_slab[type=bottom]
fill ~46 ~5 ~8 ~49 ~5 ~8 minecraft:mangrove_slab[type=top]
fill ~46 ~5 ~9 ~49 ~5 ~9 minecraft:mangrove_slab[type=bottom]
fill ~46 ~4 ~10 ~49 ~4 ~10 minecraft:mangrove_slab[type=top]
fill ~46 ~4 ~11 ~49 ~4 ~11 minecraft:mangrove_slab[type=bottom]
fill ~49 ~4 ~7 ~49 ~4 ~7 minecraft:dark_oak_planks
fill ~49 ~4 ~8 ~49 ~4 ~8 minecraft:dark_oak_planks
fill ~49 ~4 ~9 ~49 ~4 ~9 minecraft:dark_oak_planks
fill ~45 ~1 ~7 ~45 ~2 ~8 minecraft:air
setblock ~45 ~1 ~7 minecraft:spruce_door[facing=east,half=lower,hinge=left]
setblock ~45 ~2 ~7 minecraft:spruce_door[facing=east,half=upper,hinge=left]
setblock ~45 ~1 ~8 minecraft:spruce_door[facing=east,half=lower,hinge=right]
setblock ~45 ~2 ~8 minecraft:spruce_door[facing=east,half=upper,hinge=right]
fill ~45 ~1 ~9 ~45 ~3 ~9 minecraft:white_concrete
fill ~45 ~2 ~5 ~45 ~3 ~5 minecraft:glass_pane
# single-storey west wing (9 m east-west): long roof with its ridge running east-west (photo from the north,
# sakurai03) and a large board gable facing west with a vent window, awninged windows and a grey mortar panel
# at its north end (photo from the west)
fill ~29 ~0 ~3 ~37 ~0 ~11 minecraft:spruce_planks
fill ~29 ~1 ~3 ~37 ~4 ~11 minecraft:dark_oak_planks
fill ~29 ~1 ~3 ~37 ~1 ~11 minecraft:spruce_planks
fill ~30 ~1 ~4 ~37 ~4 ~10 minecraft:air
fill ~28 ~5 ~2 ~37 ~5 ~2 minecraft:mangrove_slab[type=bottom]
fill ~28 ~5 ~3 ~37 ~5 ~3 minecraft:mangrove_slab[type=top]
fill ~28 ~6 ~4 ~37 ~6 ~4 minecraft:mangrove_slab[type=bottom]
fill ~28 ~6 ~5 ~37 ~6 ~5 minecraft:mangrove_slab[type=top]
fill ~28 ~7 ~6 ~37 ~7 ~6 minecraft:mangrove_slab[type=bottom]
fill ~28 ~7 ~7 ~37 ~7 ~7 minecraft:mangrove_slab[type=top]
fill ~28 ~7 ~8 ~37 ~7 ~8 minecraft:mangrove_slab[type=bottom]
fill ~28 ~6 ~9 ~37 ~6 ~9 minecraft:mangrove_slab[type=top]
fill ~28 ~6 ~10 ~37 ~6 ~10 minecraft:mangrove_slab[type=bottom]
fill ~28 ~5 ~11 ~37 ~5 ~11 minecraft:mangrove_slab[type=top]
fill ~28 ~5 ~12 ~37 ~5 ~12 minecraft:mangrove_slab[type=bottom]
fill ~29 ~5 ~4 ~29 ~5 ~4 minecraft:dark_oak_planks
fill ~29 ~5 ~5 ~29 ~5 ~5 minecraft:dark_oak_planks
fill ~29 ~5 ~6 ~29 ~6 ~6 minecraft:dark_oak_planks
fill ~29 ~5 ~7 ~29 ~6 ~7 minecraft:dark_oak_planks
fill ~29 ~5 ~8 ~29 ~6 ~8 minecraft:dark_oak_planks
fill ~29 ~5 ~9 ~29 ~5 ~9 minecraft:dark_oak_planks
fill ~29 ~5 ~10 ~29 ~5 ~10 minecraft:dark_oak_planks
fill ~29 ~6 ~6 ~29 ~6 ~8 minecraft:glass_pane
fill ~29 ~2 ~6 ~29 ~3 ~10 minecraft:glass_pane
fill ~28 ~4 ~6 ~28 ~4 ~10 minecraft:mangrove_slab[type=top]
fill ~29 ~2 ~3 ~30 ~3 ~3 minecraft:glass_pane
fill ~30 ~2 ~11 ~31 ~3 ~11 minecraft:glass_pane
fill ~33 ~2 ~11 ~34 ~3 ~11 minecraft:glass_pane
# grey mortar panel at the north end of the west wall (photo from the west)
fill ~29 ~1 ~3 ~29 ~3 ~5 minecraft:polished_tuff
# North wing at the junction of the west wing and the core (photos sakurai02/05): projects 6 m north,
# gabled roof with its ridge running north-south and its gable facing north; door and window in the gable wall
fill ~31 ~0 ~-3 ~37 ~0 ~3 minecraft:spruce_planks
fill ~31 ~1 ~-3 ~37 ~4 ~3 minecraft:dark_oak_planks
fill ~31 ~1 ~-3 ~37 ~1 ~3 minecraft:spruce_planks
fill ~32 ~1 ~-2 ~36 ~4 ~3 minecraft:air
fill ~30 ~5 ~-4 ~30 ~5 ~3 minecraft:mangrove_slab[type=bottom]
fill ~31 ~5 ~-4 ~31 ~5 ~3 minecraft:mangrove_slab[type=top]
fill ~32 ~6 ~-4 ~32 ~6 ~3 minecraft:mangrove_slab[type=bottom]
fill ~33 ~6 ~-4 ~33 ~6 ~3 minecraft:mangrove_slab[type=top]
fill ~34 ~7 ~-4 ~34 ~7 ~3 minecraft:mangrove_slab[type=bottom]
fill ~35 ~6 ~-4 ~35 ~6 ~3 minecraft:mangrove_slab[type=top]
fill ~36 ~6 ~-4 ~36 ~6 ~3 minecraft:mangrove_slab[type=bottom]
fill ~37 ~5 ~-4 ~37 ~5 ~3 minecraft:mangrove_slab[type=top]
fill ~38 ~5 ~-4 ~38 ~5 ~3 minecraft:mangrove_slab[type=bottom]
fill ~32 ~5 ~-3 ~32 ~5 ~-3 minecraft:dark_oak_planks
fill ~33 ~5 ~-3 ~33 ~5 ~-3 minecraft:dark_oak_planks
fill ~34 ~5 ~-3 ~34 ~6 ~-3 minecraft:dark_oak_planks
fill ~35 ~5 ~-3 ~35 ~5 ~-3 minecraft:dark_oak_planks
fill ~36 ~5 ~-3 ~36 ~5 ~-3 minecraft:dark_oak_planks
setblock ~34 ~6 ~-3 minecraft:glass_pane
setblock ~33 ~1 ~-3 minecraft:spruce_door[facing=north,half=lower,hinge=left]
setblock ~33 ~2 ~-3 minecraft:spruce_door[facing=north,half=upper,hinge=left]
fill ~35 ~2 ~-3 ~36 ~3 ~-3 minecraft:glass_pane
fill ~31 ~2 ~-1 ~31 ~3 ~1 minecraft:glass_pane
fill ~37 ~2 ~-1 ~37 ~3 ~0 minecraft:glass_pane
fill ~32 ~0 ~-6 ~34 ~0 ~-4 minecraft:gravel
# --- Four-room wing (c. 1901): single storey, glazed veranda on the east, roof ridge north-south ---
fill ~37 ~0 ~12 ~45 ~0 ~24 minecraft:spruce_planks
fill ~37 ~1 ~12 ~45 ~4 ~24 minecraft:dark_oak_planks
fill ~37 ~1 ~12 ~45 ~1 ~24 minecraft:spruce_planks
fill ~38 ~1 ~13 ~44 ~4 ~23 minecraft:air
fill ~45 ~2 ~13 ~45 ~3 ~23 minecraft:glass_pane
fill ~37 ~2 ~16 ~37 ~3 ~17 minecraft:glass_pane
fill ~37 ~2 ~20 ~37 ~3 ~21 minecraft:glass_pane
fill ~36 ~5 ~12 ~36 ~5 ~24 minecraft:mangrove_slab[type=bottom]
fill ~37 ~5 ~12 ~37 ~5 ~24 minecraft:mangrove_slab[type=top]
fill ~38 ~6 ~12 ~38 ~6 ~24 minecraft:mangrove_slab[type=bottom]
fill ~39 ~6 ~12 ~39 ~6 ~24 minecraft:mangrove_slab[type=top]
fill ~40 ~7 ~12 ~40 ~7 ~24 minecraft:mangrove_slab[type=bottom]
fill ~41 ~7 ~12 ~41 ~7 ~24 minecraft:mangrove_slab[type=top]
fill ~42 ~7 ~12 ~42 ~7 ~24 minecraft:mangrove_slab[type=bottom]
fill ~43 ~6 ~12 ~43 ~6 ~24 minecraft:mangrove_slab[type=top]
fill ~44 ~6 ~12 ~44 ~6 ~24 minecraft:mangrove_slab[type=bottom]
fill ~45 ~5 ~12 ~45 ~5 ~24 minecraft:mangrove_slab[type=top]
fill ~46 ~5 ~12 ~46 ~5 ~24 minecraft:mangrove_slab[type=bottom]

# --- Detached room (1918): Japanese-style exterior, roof ridge east-west; west wall in line with the four-room wing ---
fill ~37 ~0 ~25 ~50 ~0 ~33 minecraft:spruce_planks
fill ~37 ~1 ~25 ~50 ~4 ~33 minecraft:dark_oak_planks
fill ~37 ~1 ~25 ~50 ~1 ~33 minecraft:spruce_planks
fill ~38 ~1 ~26 ~49 ~4 ~32 minecraft:air
fill ~38 ~1 ~25 ~44 ~4 ~25 minecraft:air
fill ~36 ~5 ~24 ~51 ~5 ~24 minecraft:mangrove_slab[type=bottom]
fill ~36 ~5 ~25 ~51 ~5 ~25 minecraft:mangrove_slab[type=top]
fill ~36 ~6 ~26 ~51 ~6 ~26 minecraft:mangrove_slab[type=bottom]
fill ~36 ~6 ~27 ~51 ~6 ~27 minecraft:mangrove_slab[type=top]
fill ~36 ~7 ~28 ~51 ~7 ~28 minecraft:mangrove_slab[type=bottom]
fill ~36 ~7 ~29 ~51 ~7 ~29 minecraft:mangrove_slab[type=top]
fill ~36 ~7 ~30 ~51 ~7 ~30 minecraft:mangrove_slab[type=bottom]
fill ~36 ~6 ~31 ~51 ~6 ~31 minecraft:mangrove_slab[type=top]
fill ~36 ~6 ~32 ~51 ~6 ~32 minecraft:mangrove_slab[type=bottom]
fill ~36 ~5 ~33 ~51 ~5 ~33 minecraft:mangrove_slab[type=top]
fill ~36 ~5 ~34 ~51 ~5 ~34 minecraft:mangrove_slab[type=bottom]
fill ~37 ~5 ~26 ~37 ~5 ~26 minecraft:dark_oak_planks
fill ~37 ~5 ~27 ~37 ~5 ~27 minecraft:dark_oak_planks
fill ~37 ~5 ~28 ~37 ~6 ~28 minecraft:dark_oak_planks
fill ~37 ~5 ~29 ~37 ~6 ~29 minecraft:dark_oak_planks
fill ~37 ~5 ~30 ~37 ~6 ~30 minecraft:dark_oak_planks
fill ~37 ~5 ~31 ~37 ~5 ~31 minecraft:dark_oak_planks
fill ~37 ~5 ~32 ~37 ~5 ~32 minecraft:dark_oak_planks
fill ~50 ~5 ~26 ~50 ~5 ~26 minecraft:dark_oak_planks
fill ~50 ~5 ~27 ~50 ~5 ~27 minecraft:dark_oak_planks
fill ~50 ~5 ~28 ~50 ~6 ~28 minecraft:dark_oak_planks
fill ~50 ~5 ~29 ~50 ~6 ~29 minecraft:dark_oak_planks
fill ~50 ~5 ~30 ~50 ~6 ~30 minecraft:dark_oak_planks
fill ~50 ~5 ~31 ~50 ~5 ~31 minecraft:dark_oak_planks
fill ~50 ~5 ~32 ~50 ~5 ~32 minecraft:dark_oak_planks
fill ~37 ~2 ~27 ~37 ~3 ~31 minecraft:glass_pane
fill ~50 ~2 ~27 ~50 ~3 ~31 minecraft:glass_pane
fill ~39 ~2 ~33 ~42 ~3 ~33 minecraft:glass_pane
fill ~45 ~2 ~33 ~48 ~3 ~33 minecraft:glass_pane

# --- Bibai tondenhei-oku (soldier-settler house, Army standard type): 9 m x 6.4 m, board walls,
#     red gabled roof with a smoke vent on the ridge, small entrance canopy on the east side ---
fill ~8 ~0 ~12 ~14 ~0 ~21 minecraft:spruce_planks
fill ~8 ~1 ~12 ~14 ~3 ~21 minecraft:spruce_planks
fill ~9 ~1 ~13 ~13 ~3 ~20 minecraft:air
fill ~8 ~1 ~12 ~8 ~3 ~12 minecraft:stripped_spruce_log
fill ~14 ~1 ~12 ~14 ~3 ~12 minecraft:stripped_spruce_log
fill ~8 ~1 ~21 ~8 ~3 ~21 minecraft:stripped_spruce_log
fill ~14 ~1 ~21 ~14 ~3 ~21 minecraft:stripped_spruce_log
fill ~7 ~4 ~11 ~7 ~4 ~22 minecraft:mangrove_slab[type=bottom]
fill ~8 ~4 ~11 ~8 ~4 ~22 minecraft:mangrove_slab[type=top]
fill ~9 ~5 ~11 ~9 ~5 ~22 minecraft:mangrove_slab[type=bottom]
fill ~10 ~5 ~11 ~10 ~5 ~22 minecraft:mangrove_slab[type=top]
fill ~11 ~6 ~11 ~11 ~6 ~22 minecraft:mangrove_slab[type=bottom]
fill ~12 ~5 ~11 ~12 ~5 ~22 minecraft:mangrove_slab[type=top]
fill ~13 ~5 ~11 ~13 ~5 ~22 minecraft:mangrove_slab[type=bottom]
fill ~14 ~4 ~11 ~14 ~4 ~22 minecraft:mangrove_slab[type=top]
fill ~15 ~4 ~11 ~15 ~4 ~22 minecraft:mangrove_slab[type=bottom]
fill ~9 ~4 ~12 ~9 ~4 ~12 minecraft:spruce_planks
fill ~10 ~4 ~12 ~10 ~4 ~12 minecraft:spruce_planks
fill ~11 ~4 ~12 ~11 ~5 ~12 minecraft:spruce_planks
fill ~12 ~4 ~12 ~12 ~4 ~12 minecraft:spruce_planks
fill ~13 ~4 ~12 ~13 ~4 ~12 minecraft:spruce_planks
fill ~9 ~4 ~21 ~9 ~4 ~21 minecraft:spruce_planks
fill ~10 ~4 ~21 ~10 ~4 ~21 minecraft:spruce_planks
fill ~11 ~4 ~21 ~11 ~5 ~21 minecraft:spruce_planks
fill ~12 ~4 ~21 ~12 ~4 ~21 minecraft:spruce_planks
fill ~13 ~4 ~21 ~13 ~4 ~21 minecraft:spruce_planks
fill ~10 ~7 ~16 ~12 ~7 ~17 minecraft:spruce_planks
fill ~10 ~8 ~16 ~12 ~8 ~17 minecraft:mangrove_slab[type=bottom]
setblock ~14 ~1 ~16 minecraft:spruce_door[facing=east,half=lower,hinge=left]
setblock ~14 ~2 ~16 minecraft:spruce_door[facing=east,half=upper,hinge=left]
fill ~15 ~3 ~15 ~15 ~3 ~17 minecraft:spruce_slab[type=top]
fill ~14 ~2 ~13 ~14 ~2 ~14 minecraft:spruce_trapdoor[facing=east,half=bottom,open=true]
fill ~14 ~2 ~19 ~14 ~2 ~20 minecraft:spruce_trapdoor[facing=east,half=bottom,open=true]
# white name post
fill ~17 ~1 ~22 ~17 ~3 ~22 minecraft:smooth_quartz
setblock ~18 ~2 ~22 minecraft:birch_wall_sign[facing=east]{is_waxed:1b,front_text:{messages:["美 唄","屯田兵屋","",""],color:"black"}}

# Bust of Sakurai Shogo on a granite pedestal on the east lawn, facing the residence (west)
fill ~71 ~1 ~6 ~75 ~1 ~10 minecraft:polished_diorite_slab[type=bottom]
fill ~73 ~1 ~8 ~73 ~3 ~8 minecraft:polished_diorite
fill ~73 ~1 ~7 ~73 ~2 ~7 minecraft:polished_diorite
fill ~73 ~1 ~9 ~73 ~2 ~9 minecraft:polished_diorite
setblock ~73 ~4 ~8 minecraft:polished_blackstone_wall
setblock ~73 ~5 ~8 minecraft:player_head[rotation=4]
setblock ~72 ~3 ~8 minecraft:birch_wall_sign[facing=west]{is_waxed:1b,front_text:{messages:["桜井省吾","翁之像","",""],color:"black"}}

# Front entrance on the east (Route 12 side, photo sakurai06): two rough stone gate posts with a gravel path through,
# a hedge with a low wooden rail on its street side along the east, and round the front half of the north and south
# sides (sakurai07). The north hedge runs along the north edge of the strip up to the street. A white notice board
# stands outside the north gate post.
fill ~78 ~1 ~1 ~78 ~1 ~11 minecraft:spruce_leaves[persistent=true]
fill ~78 ~1 ~17 ~78 ~1 ~34 minecraft:spruce_leaves[persistent=true]
fill ~52 ~1 ~34 ~77 ~1 ~34 minecraft:spruce_leaves[persistent=true]
fill ~79 ~1 ~0 ~79 ~1 ~8 minecraft:spruce_fence
fill ~79 ~1 ~11 ~79 ~1 ~11 minecraft:spruce_fence
fill ~79 ~1 ~17 ~79 ~1 ~34 minecraft:spruce_fence
# north hedge: straight along the north edge of the strip (Z 1447). Near the Route 12 junction the street runs into
# the strip, so there the hedge stops at the last sidewalk row before the carriageway (from the base map), stepping
# south with short risers; the carriageway is never touched. Sidewalk inside the hedge becomes lawn.
# Gap for the north footpath (X 1355-1359), whose gravel runs out to the sidewalk.
fill ~46 ~1 ~-5 ~46 ~1 ~-1 minecraft:air
fill ~46 ~0 ~-5 ~46 ~0 ~-1 minecraft:grass_block
fill ~46 ~-1 ~-5 ~46 ~-1 ~-1 minecraft:dirt
fill ~47 ~1 ~-5 ~47 ~1 ~-1 minecraft:air
fill ~47 ~0 ~-5 ~47 ~0 ~-1 minecraft:grass_block
fill ~47 ~-1 ~-5 ~47 ~-1 ~-1 minecraft:dirt
fill ~48 ~1 ~-5 ~48 ~1 ~-1 minecraft:air
fill ~48 ~0 ~-5 ~48 ~0 ~-1 minecraft:grass_block
fill ~48 ~-1 ~-5 ~48 ~-1 ~-1 minecraft:dirt
fill ~54 ~1 ~-5 ~54 ~1 ~-1 minecraft:air
fill ~54 ~0 ~-5 ~54 ~0 ~-1 minecraft:grass_block
fill ~54 ~-1 ~-5 ~54 ~-1 ~-1 minecraft:dirt
fill ~55 ~1 ~-5 ~55 ~1 ~-1 minecraft:air
fill ~55 ~0 ~-5 ~55 ~0 ~-1 minecraft:grass_block
fill ~55 ~-1 ~-5 ~55 ~-1 ~-1 minecraft:dirt
fill ~56 ~1 ~-5 ~56 ~1 ~-1 minecraft:air
fill ~56 ~0 ~-5 ~56 ~0 ~-1 minecraft:grass_block
fill ~56 ~-1 ~-5 ~56 ~-1 ~-1 minecraft:dirt
fill ~57 ~1 ~-5 ~57 ~1 ~-1 minecraft:air
fill ~57 ~0 ~-5 ~57 ~0 ~-1 minecraft:grass_block
fill ~57 ~-1 ~-5 ~57 ~-1 ~-1 minecraft:dirt
fill ~58 ~1 ~-5 ~58 ~1 ~-1 minecraft:air
fill ~58 ~0 ~-5 ~58 ~0 ~-1 minecraft:grass_block
fill ~58 ~-1 ~-5 ~58 ~-1 ~-1 minecraft:dirt
fill ~59 ~1 ~-5 ~59 ~1 ~-1 minecraft:air
fill ~59 ~0 ~-5 ~59 ~0 ~-1 minecraft:grass_block
fill ~59 ~-1 ~-5 ~59 ~-1 ~-1 minecraft:dirt
fill ~60 ~1 ~-5 ~60 ~1 ~-1 minecraft:air
fill ~60 ~0 ~-5 ~60 ~0 ~-1 minecraft:grass_block
fill ~60 ~-1 ~-5 ~60 ~-1 ~-1 minecraft:dirt
fill ~61 ~1 ~-5 ~61 ~1 ~-1 minecraft:air
fill ~61 ~0 ~-5 ~61 ~0 ~-1 minecraft:grass_block
fill ~61 ~-1 ~-5 ~61 ~-1 ~-1 minecraft:dirt
fill ~62 ~1 ~-5 ~62 ~1 ~-1 minecraft:air
fill ~62 ~0 ~-5 ~62 ~0 ~-1 minecraft:grass_block
fill ~62 ~-1 ~-5 ~62 ~-1 ~-1 minecraft:dirt
fill ~63 ~1 ~-5 ~63 ~1 ~-1 minecraft:air
fill ~63 ~0 ~-5 ~63 ~0 ~-1 minecraft:grass_block
fill ~63 ~-1 ~-5 ~63 ~-1 ~-1 minecraft:dirt
fill ~64 ~1 ~-5 ~64 ~1 ~-1 minecraft:air
fill ~64 ~0 ~-5 ~64 ~0 ~-1 minecraft:grass_block
fill ~64 ~-1 ~-5 ~64 ~-1 ~-1 minecraft:dirt
fill ~65 ~1 ~-5 ~65 ~1 ~-1 minecraft:air
fill ~65 ~0 ~-5 ~65 ~0 ~-1 minecraft:grass_block
fill ~65 ~-1 ~-5 ~65 ~-1 ~-1 minecraft:dirt
fill ~66 ~1 ~-5 ~66 ~1 ~-1 minecraft:air
fill ~66 ~0 ~-5 ~66 ~0 ~-1 minecraft:grass_block
fill ~66 ~-1 ~-5 ~66 ~-1 ~-1 minecraft:dirt
fill ~67 ~1 ~-4 ~67 ~1 ~-1 minecraft:air
fill ~67 ~0 ~-4 ~67 ~0 ~-1 minecraft:grass_block
fill ~67 ~-1 ~-4 ~67 ~-1 ~-1 minecraft:dirt
fill ~68 ~1 ~-4 ~68 ~1 ~-1 minecraft:air
fill ~68 ~0 ~-4 ~68 ~0 ~-1 minecraft:grass_block
fill ~68 ~-1 ~-4 ~68 ~-1 ~-1 minecraft:dirt
fill ~69 ~1 ~-4 ~69 ~1 ~-1 minecraft:air
fill ~69 ~0 ~-4 ~69 ~0 ~-1 minecraft:grass_block
fill ~69 ~-1 ~-4 ~69 ~-1 ~-1 minecraft:dirt
fill ~70 ~1 ~-4 ~70 ~1 ~-1 minecraft:air
fill ~70 ~0 ~-4 ~70 ~0 ~-1 minecraft:grass_block
fill ~70 ~-1 ~-4 ~70 ~-1 ~-1 minecraft:dirt
fill ~71 ~1 ~-3 ~71 ~1 ~-1 minecraft:air
fill ~71 ~0 ~-3 ~71 ~0 ~-1 minecraft:grass_block
fill ~71 ~-1 ~-3 ~71 ~-1 ~-1 minecraft:dirt
fill ~72 ~1 ~-3 ~72 ~1 ~-1 minecraft:air
fill ~72 ~0 ~-3 ~72 ~0 ~-1 minecraft:grass_block
fill ~72 ~-1 ~-3 ~72 ~-1 ~-1 minecraft:dirt
fill ~73 ~1 ~-3 ~73 ~1 ~-1 minecraft:air
fill ~73 ~0 ~-3 ~73 ~0 ~-1 minecraft:grass_block
fill ~73 ~-1 ~-3 ~73 ~-1 ~-1 minecraft:dirt
fill ~74 ~1 ~-3 ~74 ~1 ~-1 minecraft:air
fill ~74 ~0 ~-3 ~74 ~0 ~-1 minecraft:grass_block
fill ~74 ~-1 ~-3 ~74 ~-1 ~-1 minecraft:dirt
fill ~75 ~1 ~-3 ~75 ~1 ~-1 minecraft:air
fill ~75 ~0 ~-3 ~75 ~0 ~-1 minecraft:grass_block
fill ~75 ~-1 ~-3 ~75 ~-1 ~-1 minecraft:dirt
fill ~76 ~1 ~-2 ~76 ~1 ~-1 minecraft:air
fill ~76 ~0 ~-2 ~76 ~0 ~-1 minecraft:grass_block
fill ~76 ~-1 ~-2 ~76 ~-1 ~-1 minecraft:dirt
fill ~77 ~1 ~-2 ~77 ~1 ~-1 minecraft:air
fill ~77 ~0 ~-2 ~77 ~0 ~-1 minecraft:grass_block
fill ~77 ~-1 ~-2 ~77 ~-1 ~-1 minecraft:dirt
fill ~78 ~1 ~-2 ~78 ~1 ~-1 minecraft:air
fill ~78 ~0 ~-2 ~78 ~0 ~-1 minecraft:grass_block
fill ~78 ~-1 ~-2 ~78 ~-1 ~-1 minecraft:dirt
fill ~46 ~1 ~-6 ~46 ~1 ~-6 minecraft:spruce_leaves[persistent=true]
fill ~47 ~1 ~-6 ~47 ~1 ~-6 minecraft:spruce_leaves[persistent=true]
fill ~48 ~1 ~-6 ~48 ~1 ~-6 minecraft:spruce_leaves[persistent=true]
fill ~54 ~1 ~-6 ~54 ~1 ~-6 minecraft:spruce_leaves[persistent=true]
fill ~55 ~1 ~-6 ~55 ~1 ~-6 minecraft:spruce_leaves[persistent=true]
fill ~56 ~1 ~-6 ~56 ~1 ~-6 minecraft:spruce_leaves[persistent=true]
fill ~57 ~1 ~-6 ~57 ~1 ~-6 minecraft:spruce_leaves[persistent=true]
fill ~58 ~1 ~-6 ~58 ~1 ~-6 minecraft:spruce_leaves[persistent=true]
fill ~59 ~1 ~-6 ~59 ~1 ~-6 minecraft:spruce_leaves[persistent=true]
fill ~60 ~1 ~-6 ~60 ~1 ~-6 minecraft:spruce_leaves[persistent=true]
fill ~61 ~1 ~-6 ~61 ~1 ~-6 minecraft:spruce_leaves[persistent=true]
fill ~62 ~1 ~-6 ~62 ~1 ~-6 minecraft:spruce_leaves[persistent=true]
fill ~63 ~1 ~-6 ~63 ~1 ~-6 minecraft:spruce_leaves[persistent=true]
fill ~64 ~1 ~-6 ~64 ~1 ~-6 minecraft:spruce_leaves[persistent=true]
fill ~65 ~1 ~-6 ~65 ~1 ~-6 minecraft:spruce_leaves[persistent=true]
fill ~66 ~1 ~-6 ~66 ~1 ~-5 minecraft:spruce_leaves[persistent=true]
fill ~67 ~1 ~-5 ~67 ~1 ~-5 minecraft:spruce_leaves[persistent=true]
fill ~68 ~1 ~-5 ~68 ~1 ~-5 minecraft:spruce_leaves[persistent=true]
fill ~69 ~1 ~-5 ~69 ~1 ~-5 minecraft:spruce_leaves[persistent=true]
fill ~70 ~1 ~-5 ~70 ~1 ~-4 minecraft:spruce_leaves[persistent=true]
fill ~71 ~1 ~-4 ~71 ~1 ~-4 minecraft:spruce_leaves[persistent=true]
fill ~72 ~1 ~-4 ~72 ~1 ~-4 minecraft:spruce_leaves[persistent=true]
fill ~73 ~1 ~-4 ~73 ~1 ~-4 minecraft:spruce_leaves[persistent=true]
fill ~74 ~1 ~-4 ~74 ~1 ~-4 minecraft:spruce_leaves[persistent=true]
fill ~75 ~1 ~-4 ~75 ~1 ~-3 minecraft:spruce_leaves[persistent=true]
fill ~76 ~1 ~-3 ~76 ~1 ~-3 minecraft:spruce_leaves[persistent=true]
fill ~77 ~1 ~-3 ~77 ~1 ~-3 minecraft:spruce_leaves[persistent=true]
fill ~78 ~1 ~-3 ~78 ~1 ~-3 minecraft:spruce_leaves[persistent=true]
fill ~78 ~1 ~-3 ~78 ~1 ~0 minecraft:spruce_leaves[persistent=true]
fill ~50 ~0 ~-6 ~52 ~0 ~-1 minecraft:gravel
fill ~50 ~1 ~-6 ~52 ~1 ~-1 minecraft:air
fill ~78 ~1 ~12 ~79 ~2 ~12 minecraft:andesite
fill ~78 ~3 ~12 ~79 ~3 ~12 minecraft:andesite_slab[type=bottom]
fill ~78 ~1 ~16 ~79 ~2 ~16 minecraft:andesite
fill ~78 ~3 ~16 ~79 ~3 ~16 minecraft:andesite_slab[type=bottom]
fill ~53 ~0 ~13 ~80 ~0 ~15 minecraft:gravel
# white notice board on two posts in the rail line outside the hedge, its sign facing the street
setblock ~79 ~1 ~9 minecraft:birch_fence
setblock ~79 ~1 ~10 minecraft:birch_fence
fill ~79 ~2 ~9 ~79 ~3 ~10 minecraft:smooth_quartz
setblock ~80 ~3 ~9 minecraft:birch_wall_sign[facing=east]{is_waxed:1b,front_text:{messages:["","桜井邸分館","",""],color:"black"}}

# Garden trees after the aerial screenshot (placed with the game's tree features; a tree without room is skipped)
place feature minecraft:fancy_oak ~6 ~1 ~4
place feature minecraft:oak ~22 ~1 ~5
place feature minecraft:birch ~24 ~1 ~27
place feature minecraft:oak ~30 ~1 ~31
place feature minecraft:fancy_oak ~60 ~1 ~25
place feature minecraft:birch ~67 ~1 ~31
place feature minecraft:spruce ~70 ~1 ~21
place feature minecraft:oak ~58 ~1 ~3
