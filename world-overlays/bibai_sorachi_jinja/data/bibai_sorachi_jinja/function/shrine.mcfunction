# Sorachi Shrine (空知神社), Bibai: approach from the east, hall on the west facing east.
# After reference photos jinja01-07 (screenshots supplied by the team) and the OpenStreetMap outline of the shrine.
# Relative origin: (X 1112, Y -57, Z 1340) at ground level. Writes only inside ~0..~74, ~-1..~30, ~0..~40
# (X 1112-1186, Y -58..-27, Z 1340-1380). Only the crowns of the trees at the west and south edges reach a few
# blocks past the grounds, into the existing woods (still inside the force-loaded chunks).

# Clear the generated placeholder buildings and trees, level the grounds
fill ~ ~1 ~ ~74 ~10 ~40 minecraft:air
fill ~ ~11 ~ ~74 ~20 ~40 minecraft:air
fill ~ ~21 ~ ~74 ~30 ~40 minecraft:air
fill ~ ~ ~ ~74 ~ ~40 minecraft:grass_block
fill ~ ~-1 ~ ~74 ~-1 ~40 minecraft:dirt

# Stone-paved approach (Z 1348-1352) with gravel either side, from the road end to the hall steps
fill ~32 ~0 ~6 ~74 ~0 ~14 minecraft:gravel
fill ~32 ~0 ~8 ~74 ~0 ~12 minecraft:smooth_stone

# First copper torii at the entrance, flanked by stone fences, big lanterns and the shrine name stone
fill ~70 ~1 ~6 ~70 ~7 ~6 minecraft:waxed_copper_block
fill ~70 ~1 ~14 ~70 ~7 ~14 minecraft:waxed_copper_block
fill ~70 ~6 ~7 ~70 ~6 ~13 minecraft:waxed_cut_copper
fill ~70 ~8 ~4 ~70 ~8 ~16 minecraft:waxed_copper_block
fill ~71 ~1 ~1 ~71 ~1 ~5 minecraft:stone_bricks
fill ~71 ~2 ~1 ~71 ~2 ~5 minecraft:stone_brick_wall
fill ~71 ~1 ~15 ~71 ~1 ~19 minecraft:stone_bricks
fill ~71 ~2 ~15 ~71 ~2 ~19 minecraft:stone_brick_wall
setblock ~72 ~1 ~3 minecraft:polished_andesite
setblock ~72 ~2 ~3 minecraft:andesite_wall
setblock ~72 ~3 ~3 minecraft:andesite_wall
setblock ~72 ~4 ~3 minecraft:andesite_wall
setblock ~72 ~5 ~3 minecraft:lantern
setblock ~72 ~6 ~3 minecraft:stone_brick_slab[type=bottom]
setblock ~72 ~1 ~17 minecraft:polished_andesite
setblock ~72 ~2 ~17 minecraft:andesite_wall
setblock ~72 ~3 ~17 minecraft:andesite_wall
setblock ~72 ~4 ~17 minecraft:andesite_wall
setblock ~72 ~5 ~17 minecraft:lantern
setblock ~72 ~6 ~17 minecraft:stone_brick_slab[type=bottom]
fill ~72 ~1 ~6 ~72 ~4 ~6 minecraft:smooth_stone
setblock ~73 ~3 ~6 minecraft:birch_wall_sign[facing=east]{is_waxed:1b,front_text:{messages:["","空 知","神 社",""],color:"black"}}

# Lanterns along the approach
setblock ~62 ~1 ~6 minecraft:polished_andesite
setblock ~62 ~2 ~6 minecraft:andesite_wall
setblock ~62 ~3 ~6 minecraft:andesite_wall
setblock ~62 ~4 ~6 minecraft:lantern
setblock ~62 ~5 ~6 minecraft:stone_brick_slab[type=bottom]
setblock ~62 ~1 ~14 minecraft:polished_andesite
setblock ~62 ~2 ~14 minecraft:andesite_wall
setblock ~62 ~3 ~14 minecraft:andesite_wall
setblock ~62 ~4 ~14 minecraft:lantern
setblock ~62 ~5 ~14 minecraft:stone_brick_slab[type=bottom]
setblock ~50 ~1 ~6 minecraft:polished_andesite
setblock ~50 ~2 ~6 minecraft:andesite_wall
setblock ~50 ~3 ~6 minecraft:andesite_wall
setblock ~50 ~4 ~6 minecraft:lantern
setblock ~50 ~5 ~6 minecraft:stone_brick_slab[type=bottom]
setblock ~50 ~1 ~14 minecraft:polished_andesite
setblock ~50 ~2 ~14 minecraft:andesite_wall
setblock ~50 ~3 ~14 minecraft:andesite_wall
setblock ~50 ~4 ~14 minecraft:lantern
setblock ~50 ~5 ~14 minecraft:stone_brick_slab[type=bottom]

# Purification pavilion (temizuya) on the south side: four posts, grey gabled roof, stone basin with water
fill ~54 ~0 ~16 ~58 ~0 ~19 minecraft:stone_bricks
fill ~54 ~1 ~16 ~54 ~3 ~16 minecraft:stripped_spruce_log
fill ~58 ~1 ~16 ~58 ~3 ~16 minecraft:stripped_spruce_log
fill ~54 ~1 ~19 ~54 ~3 ~19 minecraft:stripped_spruce_log
fill ~58 ~1 ~19 ~58 ~3 ~19 minecraft:stripped_spruce_log
fill ~53 ~4 ~15 ~59 ~4 ~15 minecraft:polished_deepslate_slab[type=bottom]
fill ~53 ~4 ~16 ~59 ~4 ~16 minecraft:polished_deepslate_slab[type=top]
fill ~53 ~5 ~17 ~59 ~5 ~18 minecraft:polished_deepslate_slab[type=bottom]
fill ~53 ~4 ~19 ~59 ~4 ~19 minecraft:polished_deepslate_slab[type=top]
fill ~53 ~4 ~20 ~59 ~4 ~20 minecraft:polished_deepslate_slab[type=bottom]
setblock ~55 ~1 ~17 minecraft:polished_andesite
setblock ~57 ~1 ~17 minecraft:polished_andesite
setblock ~56 ~1 ~17 minecraft:water_cauldron[level=3]

# Second copper torii and the pair of guardian lion-dogs on pedestals
fill ~46 ~1 ~7 ~46 ~6 ~7 minecraft:waxed_copper_block
fill ~46 ~1 ~13 ~46 ~6 ~13 minecraft:waxed_copper_block
fill ~46 ~5 ~8 ~46 ~5 ~12 minecraft:waxed_cut_copper
fill ~46 ~7 ~5 ~46 ~7 ~15 minecraft:waxed_copper_block
fill ~42 ~1 ~5 ~42 ~2 ~5 minecraft:polished_andesite
setblock ~42 ~3 ~5 minecraft:chiseled_stone_bricks
setblock ~42 ~4 ~5 minecraft:stone_brick_stairs[facing=west]
fill ~42 ~1 ~15 ~42 ~2 ~15 minecraft:polished_andesite
setblock ~42 ~3 ~15 minecraft:chiseled_stone_bricks
setblock ~42 ~4 ~15 minecraft:stone_brick_stairs[facing=west]
# Stone pillar "県社 空知神社" by the approach
fill ~64 ~1 ~5 ~64 ~5 ~5 minecraft:smooth_stone
setblock ~64 ~4 ~4 minecraft:birch_wall_sign[facing=north]{is_waxed:1b,front_text:{messages:["県 社","空 知","神 社",""],color:"black"}}

# Inari branch: a row of red torii leading south to the small Inari shrine
fill ~38 ~0 ~13 ~40 ~0 ~26 minecraft:gray_concrete_powder
fill ~37 ~1 ~16 ~37 ~5 ~16 minecraft:red_concrete
fill ~41 ~1 ~16 ~41 ~5 ~16 minecraft:red_concrete
fill ~36 ~5 ~16 ~42 ~5 ~16 minecraft:red_concrete
fill ~36 ~6 ~16 ~42 ~6 ~16 minecraft:red_nether_brick_slab[type=bottom]
setblock ~35 ~6 ~16 minecraft:red_nether_brick_slab[type=top]
setblock ~43 ~6 ~16 minecraft:red_nether_brick_slab[type=top]
fill ~37 ~1 ~19 ~37 ~5 ~19 minecraft:red_concrete
fill ~41 ~1 ~19 ~41 ~5 ~19 minecraft:red_concrete
fill ~36 ~5 ~19 ~42 ~5 ~19 minecraft:red_concrete
fill ~36 ~6 ~19 ~42 ~6 ~19 minecraft:red_nether_brick_slab[type=bottom]
setblock ~35 ~6 ~19 minecraft:red_nether_brick_slab[type=top]
setblock ~43 ~6 ~19 minecraft:red_nether_brick_slab[type=top]
fill ~37 ~1 ~22 ~37 ~5 ~22 minecraft:red_concrete
fill ~41 ~1 ~22 ~41 ~5 ~22 minecraft:red_concrete
fill ~36 ~5 ~22 ~42 ~5 ~22 minecraft:red_concrete
fill ~36 ~6 ~22 ~42 ~6 ~22 minecraft:red_nether_brick_slab[type=bottom]
setblock ~35 ~6 ~22 minecraft:red_nether_brick_slab[type=top]
setblock ~43 ~6 ~22 minecraft:red_nether_brick_slab[type=top]
fill ~37 ~1 ~25 ~37 ~5 ~25 minecraft:red_concrete
fill ~41 ~1 ~25 ~41 ~5 ~25 minecraft:red_concrete
fill ~36 ~5 ~25 ~42 ~5 ~25 minecraft:red_concrete
fill ~36 ~6 ~25 ~42 ~6 ~25 minecraft:red_nether_brick_slab[type=bottom]
setblock ~35 ~6 ~25 minecraft:red_nether_brick_slab[type=top]
setblock ~43 ~6 ~25 minecraft:red_nether_brick_slab[type=top]
# Inari shrine: grey stone base and steps, white walls, red posts and frame, red roof, lattice door
fill ~36 ~1 ~28 ~42 ~1 ~34 minecraft:polished_andesite
fill ~38 ~1 ~27 ~40 ~1 ~27 minecraft:stone_brick_stairs[facing=south]
fill ~37 ~2 ~29 ~41 ~4 ~33 minecraft:white_concrete
fill ~37 ~2 ~29 ~37 ~4 ~29 minecraft:red_concrete
fill ~41 ~2 ~29 ~41 ~4 ~29 minecraft:red_concrete
fill ~37 ~2 ~33 ~37 ~4 ~33 minecraft:red_concrete
fill ~41 ~2 ~33 ~41 ~4 ~33 minecraft:red_concrete
fill ~37 ~4 ~29 ~41 ~4 ~29 minecraft:red_concrete
fill ~39 ~2 ~29 ~39 ~3 ~29 minecraft:red_stained_glass_pane
setblock ~39 ~4 ~29 minecraft:red_concrete
fill ~38 ~2 ~30 ~40 ~3 ~32 minecraft:air
fill ~36 ~5 ~28 ~42 ~5 ~34 minecraft:red_nether_brick_slab[type=bottom]
fill ~37 ~5 ~29 ~41 ~5 ~33 minecraft:red_nether_bricks
fill ~38 ~6 ~30 ~40 ~6 ~32 minecraft:red_nether_brick_slab[type=bottom]
fill ~36 ~2 ~28 ~36 ~2 ~30 minecraft:crimson_fence
fill ~42 ~2 ~28 ~42 ~2 ~30 minecraft:crimson_fence
setblock ~39 ~4 ~28 minecraft:birch_wall_sign[facing=north]{is_waxed:1b,front_text:{messages:["","稲荷神社","",""],color:"black"}}

# Stone base, wide steps up from the approach, and the two inscribed stone pillars
fill ~6 ~1 ~1 ~29 ~2 ~19 minecraft:polished_andesite
fill ~32 ~1 ~5 ~32 ~1 ~15 minecraft:stone_brick_stairs[facing=west]
fill ~31 ~1 ~5 ~31 ~1 ~15 minecraft:stone_bricks
fill ~31 ~2 ~5 ~31 ~2 ~15 minecraft:stone_brick_stairs[facing=west]
fill ~30 ~1 ~5 ~30 ~2 ~15 minecraft:stone_bricks
fill ~30 ~3 ~5 ~30 ~3 ~15 minecraft:stone_brick_stairs[facing=west]
fill ~34 ~1 ~3 ~34 ~5 ~3 minecraft:smooth_stone
fill ~34 ~1 ~17 ~34 ~5 ~17 minecraft:smooth_stone
setblock ~35 ~4 ~3 minecraft:birch_wall_sign[facing=east]{is_waxed:1b,front_text:{messages:["神 徳","似 風","",""],color:"black"}}
setblock ~35 ~4 ~17 minecraft:birch_wall_sign[facing=east]{is_waxed:1b,front_text:{messages:["神 明","如 鏡","",""],color:"black"}}
# Timber hall: floor, posts, plank walls, lattice windows, veranda with railing
fill ~6 ~3 ~1 ~29 ~3 ~19 minecraft:spruce_planks
fill ~8 ~4 ~4 ~26 ~8 ~16 minecraft:spruce_planks
fill ~9 ~4 ~5 ~25 ~8 ~15 minecraft:air
fill ~8 ~4 ~4 ~8 ~8 ~4 minecraft:stripped_spruce_log
fill ~8 ~4 ~16 ~8 ~8 ~16 minecraft:stripped_spruce_log
fill ~12 ~4 ~4 ~12 ~8 ~4 minecraft:stripped_spruce_log
fill ~12 ~4 ~16 ~12 ~8 ~16 minecraft:stripped_spruce_log
fill ~16 ~4 ~4 ~16 ~8 ~4 minecraft:stripped_spruce_log
fill ~16 ~4 ~16 ~16 ~8 ~16 minecraft:stripped_spruce_log
fill ~20 ~4 ~4 ~20 ~8 ~4 minecraft:stripped_spruce_log
fill ~20 ~4 ~16 ~20 ~8 ~16 minecraft:stripped_spruce_log
fill ~24 ~4 ~4 ~24 ~8 ~4 minecraft:stripped_spruce_log
fill ~24 ~4 ~16 ~24 ~8 ~16 minecraft:stripped_spruce_log
fill ~26 ~4 ~4 ~26 ~8 ~4 minecraft:stripped_spruce_log
fill ~26 ~4 ~16 ~26 ~8 ~16 minecraft:stripped_spruce_log
fill ~8 ~4 ~4 ~8 ~8 ~4 minecraft:stripped_spruce_log
fill ~26 ~4 ~4 ~26 ~8 ~4 minecraft:stripped_spruce_log
fill ~8 ~4 ~8 ~8 ~8 ~8 minecraft:stripped_spruce_log
fill ~26 ~4 ~8 ~26 ~8 ~8 minecraft:stripped_spruce_log
fill ~8 ~4 ~12 ~8 ~8 ~12 minecraft:stripped_spruce_log
fill ~26 ~4 ~12 ~26 ~8 ~12 minecraft:stripped_spruce_log
fill ~8 ~4 ~16 ~8 ~8 ~16 minecraft:stripped_spruce_log
fill ~26 ~4 ~16 ~26 ~8 ~16 minecraft:stripped_spruce_log
fill ~26 ~5 ~5 ~26 ~6 ~7 minecraft:spruce_trapdoor[facing=east,half=bottom,open=true]
fill ~26 ~5 ~13 ~26 ~6 ~15 minecraft:spruce_trapdoor[facing=east,half=bottom,open=true]
fill ~26 ~4 ~9 ~26 ~6 ~11 minecraft:air
fill ~27 ~7 ~7 ~27 ~7 ~13 minecraft:white_wool
fill ~29 ~4 ~1 ~29 ~4 ~7 minecraft:spruce_fence
fill ~29 ~4 ~13 ~29 ~4 ~19 minecraft:spruce_fence
fill ~27 ~4 ~1 ~28 ~4 ~1 minecraft:spruce_fence
fill ~27 ~4 ~19 ~28 ~4 ~19 minecraft:spruce_fence
# Verdigris copper roof: ridge running north-south, 2 m eaves
fill ~6 ~9 ~2 ~6 ~9 ~18 minecraft:waxed_oxidized_cut_copper_slab[type=bottom]
fill ~7 ~9 ~2 ~7 ~9 ~18 minecraft:waxed_oxidized_cut_copper_slab[type=top]
fill ~8 ~10 ~2 ~8 ~10 ~18 minecraft:waxed_oxidized_cut_copper_slab[type=bottom]
fill ~8 ~9 ~4 ~8 ~9 ~4 minecraft:spruce_planks
fill ~8 ~9 ~16 ~8 ~9 ~16 minecraft:spruce_planks
fill ~9 ~10 ~2 ~9 ~10 ~18 minecraft:waxed_oxidized_cut_copper_slab[type=top]
fill ~9 ~9 ~4 ~9 ~9 ~4 minecraft:spruce_planks
fill ~9 ~9 ~16 ~9 ~9 ~16 minecraft:spruce_planks
fill ~10 ~11 ~2 ~10 ~11 ~18 minecraft:waxed_oxidized_cut_copper_slab[type=bottom]
fill ~10 ~9 ~4 ~10 ~10 ~4 minecraft:spruce_planks
fill ~10 ~9 ~16 ~10 ~10 ~16 minecraft:spruce_planks
fill ~11 ~11 ~2 ~11 ~11 ~18 minecraft:waxed_oxidized_cut_copper_slab[type=top]
fill ~11 ~9 ~4 ~11 ~10 ~4 minecraft:spruce_planks
fill ~11 ~9 ~16 ~11 ~10 ~16 minecraft:spruce_planks
fill ~12 ~12 ~2 ~12 ~12 ~18 minecraft:waxed_oxidized_cut_copper_slab[type=bottom]
fill ~12 ~9 ~4 ~12 ~11 ~4 minecraft:spruce_planks
fill ~12 ~9 ~16 ~12 ~11 ~16 minecraft:spruce_planks
fill ~13 ~12 ~2 ~13 ~12 ~18 minecraft:waxed_oxidized_cut_copper_slab[type=top]
fill ~13 ~9 ~4 ~13 ~11 ~4 minecraft:spruce_planks
fill ~13 ~9 ~16 ~13 ~11 ~16 minecraft:spruce_planks
fill ~14 ~13 ~2 ~14 ~13 ~18 minecraft:waxed_oxidized_cut_copper_slab[type=bottom]
fill ~14 ~9 ~4 ~14 ~12 ~4 minecraft:spruce_planks
fill ~14 ~9 ~16 ~14 ~12 ~16 minecraft:spruce_planks
fill ~15 ~13 ~2 ~15 ~13 ~18 minecraft:waxed_oxidized_cut_copper_slab[type=top]
fill ~15 ~9 ~4 ~15 ~12 ~4 minecraft:spruce_planks
fill ~15 ~9 ~16 ~15 ~12 ~16 minecraft:spruce_planks
fill ~16 ~14 ~2 ~16 ~14 ~18 minecraft:waxed_oxidized_cut_copper_slab[type=bottom]
fill ~16 ~9 ~4 ~16 ~13 ~4 minecraft:spruce_planks
fill ~16 ~9 ~16 ~16 ~13 ~16 minecraft:spruce_planks
fill ~17 ~14 ~2 ~17 ~14 ~18 minecraft:waxed_oxidized_cut_copper_slab[type=top]
fill ~17 ~9 ~4 ~17 ~13 ~4 minecraft:spruce_planks
fill ~17 ~9 ~16 ~17 ~13 ~16 minecraft:spruce_planks
fill ~18 ~14 ~2 ~18 ~14 ~18 minecraft:waxed_oxidized_cut_copper_slab[type=bottom]
fill ~18 ~9 ~4 ~18 ~13 ~4 minecraft:spruce_planks
fill ~18 ~9 ~16 ~18 ~13 ~16 minecraft:spruce_planks
fill ~19 ~13 ~2 ~19 ~13 ~18 minecraft:waxed_oxidized_cut_copper_slab[type=top]
fill ~19 ~9 ~4 ~19 ~12 ~4 minecraft:spruce_planks
fill ~19 ~9 ~16 ~19 ~12 ~16 minecraft:spruce_planks
fill ~20 ~13 ~2 ~20 ~13 ~18 minecraft:waxed_oxidized_cut_copper_slab[type=bottom]
fill ~20 ~9 ~4 ~20 ~12 ~4 minecraft:spruce_planks
fill ~20 ~9 ~16 ~20 ~12 ~16 minecraft:spruce_planks
fill ~21 ~12 ~2 ~21 ~12 ~18 minecraft:waxed_oxidized_cut_copper_slab[type=top]
fill ~21 ~9 ~4 ~21 ~11 ~4 minecraft:spruce_planks
fill ~21 ~9 ~16 ~21 ~11 ~16 minecraft:spruce_planks
fill ~22 ~12 ~2 ~22 ~12 ~18 minecraft:waxed_oxidized_cut_copper_slab[type=bottom]
fill ~22 ~9 ~4 ~22 ~11 ~4 minecraft:spruce_planks
fill ~22 ~9 ~16 ~22 ~11 ~16 minecraft:spruce_planks
fill ~23 ~11 ~2 ~23 ~11 ~18 minecraft:waxed_oxidized_cut_copper_slab[type=top]
fill ~23 ~9 ~4 ~23 ~10 ~4 minecraft:spruce_planks
fill ~23 ~9 ~16 ~23 ~10 ~16 minecraft:spruce_planks
fill ~24 ~11 ~2 ~24 ~11 ~18 minecraft:waxed_oxidized_cut_copper_slab[type=bottom]
fill ~24 ~9 ~4 ~24 ~10 ~4 minecraft:spruce_planks
fill ~24 ~9 ~16 ~24 ~10 ~16 minecraft:spruce_planks
fill ~25 ~10 ~2 ~25 ~10 ~18 minecraft:waxed_oxidized_cut_copper_slab[type=top]
fill ~25 ~9 ~4 ~25 ~9 ~4 minecraft:spruce_planks
fill ~25 ~9 ~16 ~25 ~9 ~16 minecraft:spruce_planks
fill ~26 ~10 ~2 ~26 ~10 ~18 minecraft:waxed_oxidized_cut_copper_slab[type=bottom]
fill ~26 ~9 ~4 ~26 ~9 ~4 minecraft:spruce_planks
fill ~26 ~9 ~16 ~26 ~9 ~16 minecraft:spruce_planks
fill ~27 ~9 ~2 ~27 ~9 ~18 minecraft:waxed_oxidized_cut_copper_slab[type=top]
fill ~28 ~9 ~2 ~28 ~9 ~18 minecraft:waxed_oxidized_cut_copper_slab[type=bottom]
fill ~17 ~14 ~2 ~17 ~14 ~18 minecraft:waxed_oxidized_cut_copper
fill ~8 ~9 ~4 ~8 ~9 ~16 minecraft:spruce_planks
fill ~26 ~9 ~4 ~26 ~9 ~16 minecraft:spruce_planks
# Katsuogi logs across the ridge and chigi at both ends
fill ~16 ~15 ~5 ~18 ~15 ~5 minecraft:dark_oak_log[axis=x]
fill ~16 ~15 ~8 ~18 ~15 ~8 minecraft:dark_oak_log[axis=x]
fill ~16 ~15 ~12 ~18 ~15 ~12 minecraft:dark_oak_log[axis=x]
fill ~16 ~15 ~15 ~18 ~15 ~15 minecraft:dark_oak_log[axis=x]
setblock ~16 ~15 ~2 minecraft:dark_oak_fence
setblock ~18 ~15 ~2 minecraft:dark_oak_fence
setblock ~16 ~16 ~2 minecraft:dark_oak_fence
setblock ~18 ~16 ~2 minecraft:dark_oak_fence
setblock ~16 ~15 ~18 minecraft:dark_oak_fence
setblock ~18 ~15 ~18 minecraft:dark_oak_fence
setblock ~16 ~16 ~18 minecraft:dark_oak_fence
setblock ~18 ~16 ~18 minecraft:dark_oak_fence
# Front cross gable (chidori-hafu) over the entrance, with the shrine name plaque
fill ~24 ~9 ~6 ~30 ~11 ~14 minecraft:air
fill ~24 ~9 ~6 ~30 ~9 ~6 minecraft:waxed_oxidized_cut_copper_slab[type=bottom]
fill ~24 ~9 ~7 ~30 ~9 ~7 minecraft:waxed_oxidized_cut_copper_slab[type=top]
fill ~24 ~10 ~8 ~30 ~10 ~8 minecraft:waxed_oxidized_cut_copper_slab[type=bottom]
fill ~24 ~10 ~9 ~30 ~10 ~9 minecraft:waxed_oxidized_cut_copper_slab[type=top]
fill ~24 ~11 ~10 ~30 ~11 ~10 minecraft:waxed_oxidized_cut_copper_slab[type=bottom]
fill ~24 ~10 ~11 ~30 ~10 ~11 minecraft:waxed_oxidized_cut_copper_slab[type=top]
fill ~24 ~10 ~12 ~30 ~10 ~12 minecraft:waxed_oxidized_cut_copper_slab[type=bottom]
fill ~24 ~9 ~13 ~30 ~9 ~13 minecraft:waxed_oxidized_cut_copper_slab[type=top]
fill ~24 ~9 ~14 ~30 ~9 ~14 minecraft:waxed_oxidized_cut_copper_slab[type=bottom]
fill ~27 ~9 ~8 ~27 ~9 ~12 minecraft:spruce_planks
fill ~27 ~10 ~10 ~27 ~10 ~10 minecraft:spruce_planks
setblock ~28 ~9 ~10 minecraft:birch_wall_sign[facing=east]{is_waxed:1b,front_text:{messages:["","空知神社","",""],color:"black"}}
fill ~27 ~4 ~9 ~27 ~6 ~9 minecraft:stripped_spruce_log
fill ~27 ~4 ~11 ~27 ~6 ~11 minecraft:stripped_spruce_log

# Trees for the shrine grove, placed with the game's own tree features. Each stands clear of the buildings;
# a feature that has no room simply is not placed. Cherry stands in for the dark red maples, which vanilla lacks.
place feature minecraft:mega_spruce ~1 ~1 ~25
place feature minecraft:mega_spruce ~1 ~1 ~35
place feature minecraft:mega_spruce ~11 ~1 ~33
place feature minecraft:mega_spruce ~22 ~1 ~37
place feature minecraft:spruce ~66 ~1 ~1
place feature minecraft:spruce ~66 ~1 ~19
place feature minecraft:spruce ~2 ~1 ~4
place feature minecraft:spruce ~2 ~1 ~14
place feature minecraft:fancy_oak ~50 ~1 ~30
place feature minecraft:oak ~29 ~1 ~26
place feature minecraft:birch ~32 ~1 ~37
place feature minecraft:oak ~59 ~1 ~37
place feature minecraft:birch ~46 ~1 ~38
place feature minecraft:oak ~16 ~1 ~26
place feature minecraft:birch ~38 ~1 ~2
place feature minecraft:oak ~55 ~1 ~1
place feature minecraft:cherry ~64 ~1 ~26
place feature minecraft:cherry ~54 ~1 ~25
