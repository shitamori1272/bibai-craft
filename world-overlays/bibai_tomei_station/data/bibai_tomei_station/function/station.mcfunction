# Old Tomei station (Bibai Railway), single-storey timber building with a red gabled roof, roughly 1:1.
# Relative origin: northwest corner of the plot at ground level. +X = east, +Z = south.
# Body x3..19 (17 m) by z4..10 (7 m); front gable porch z11..12 to the south; canopy z1..3 to the north.
# Writes only inside ~0..~22, ~0..~16, ~0..~14 (X 4824-4846, Y -14..2, Z 1205-1219).

# Remove the generated placeholder building and restore the ground
fill ~ ~1 ~ ~22 ~16 ~14 minecraft:air
fill ~ ~ ~ ~22 ~ ~14 minecraft:grass_block
fill ~10 ~ ~13 ~11 ~ ~14 minecraft:gravel
fill ~10 ~ ~1 ~18 ~ ~3 minecraft:smooth_stone

# Floor
fill ~3 ~ ~4 ~19 ~ ~10 minecraft:spruce_planks
fill ~9 ~ ~11 ~13 ~ ~12 minecraft:spruce_planks

# Walls: green board skirting below, white plaster above
fill ~3 ~1 ~4 ~19 ~1 ~10 minecraft:waxed_oxidized_cut_copper
fill ~3 ~2 ~4 ~19 ~4 ~10 minecraft:white_concrete
fill ~9 ~1 ~11 ~13 ~1 ~12 minecraft:waxed_oxidized_cut_copper
fill ~9 ~2 ~11 ~13 ~4 ~12 minecraft:white_concrete
fill ~4 ~1 ~5 ~18 ~4 ~9 minecraft:air
fill ~10 ~1 ~10 ~12 ~4 ~11 minecraft:air

# South (front) windows: two pairs either side of the porch
fill ~4 ~2 ~10 ~5 ~3 ~10 minecraft:white_stained_glass_pane
fill ~7 ~2 ~10 ~8 ~3 ~10 minecraft:white_stained_glass_pane
fill ~14 ~2 ~10 ~15 ~3 ~10 minecraft:white_stained_glass_pane
fill ~17 ~2 ~10 ~18 ~3 ~10 minecraft:white_stained_glass_pane
# Porch: green double door and a small notice window
setblock ~10 ~1 ~12 minecraft:waxed_oxidized_copper_door[facing=south,half=lower,hinge=left]
setblock ~10 ~2 ~12 minecraft:waxed_oxidized_copper_door[facing=south,half=upper,hinge=left]
setblock ~11 ~1 ~12 minecraft:waxed_oxidized_copper_door[facing=south,half=lower,hinge=right]
setblock ~11 ~2 ~12 minecraft:waxed_oxidized_copper_door[facing=south,half=upper,hinge=right]
setblock ~12 ~2 ~12 minecraft:white_stained_glass_pane
setblock ~12 ~3 ~12 minecraft:white_stained_glass_pane

# North (platform) side windows and back door under the canopy
fill ~4 ~2 ~4 ~5 ~3 ~4 minecraft:white_stained_glass_pane
fill ~7 ~2 ~4 ~8 ~3 ~4 minecraft:white_stained_glass_pane
fill ~11 ~2 ~4 ~12 ~3 ~4 minecraft:white_stained_glass_pane
setblock ~14 ~1 ~4 minecraft:waxed_oxidized_copper_door[facing=north,half=lower,hinge=left]
setblock ~14 ~2 ~4 minecraft:waxed_oxidized_copper_door[facing=north,half=upper,hinge=left]
fill ~16 ~2 ~4 ~17 ~3 ~4 minecraft:white_stained_glass_pane
# Gable-end windows
fill ~3 ~2 ~7 ~3 ~3 ~7 minecraft:white_stained_glass_pane
fill ~19 ~2 ~7 ~19 ~3 ~7 minecraft:white_stained_glass_pane

# Main red roof: shallow gable along X, ridge over z7, 1 m overhang all round
fill ~2 ~5 ~3 ~20 ~5 ~3 minecraft:mangrove_slab[type=bottom]
fill ~2 ~5 ~4 ~20 ~5 ~4 minecraft:mangrove_slab[type=top]
fill ~2 ~6 ~5 ~20 ~6 ~5 minecraft:mangrove_slab[type=bottom]
fill ~2 ~6 ~6 ~20 ~6 ~6 minecraft:mangrove_slab[type=top]
fill ~2 ~7 ~7 ~20 ~7 ~7 minecraft:mangrove_slab[type=bottom]
fill ~2 ~6 ~8 ~20 ~6 ~8 minecraft:mangrove_slab[type=top]
fill ~2 ~6 ~9 ~20 ~6 ~9 minecraft:mangrove_slab[type=bottom]
fill ~2 ~5 ~10 ~20 ~5 ~10 minecraft:mangrove_slab[type=top]
fill ~2 ~5 ~11 ~20 ~5 ~11 minecraft:mangrove_slab[type=bottom]
# White gable ends under the roof
fill ~3 ~5 ~5 ~3 ~5 ~9 minecraft:white_concrete
fill ~3 ~6 ~7 ~3 ~6 ~7 minecraft:white_concrete
fill ~19 ~5 ~5 ~19 ~5 ~9 minecraft:white_concrete
fill ~19 ~6 ~7 ~19 ~6 ~7 minecraft:white_concrete

# Front cross gable over the porch, with the station name
fill ~8 ~5 ~11 ~14 ~6 ~13 minecraft:air
fill ~8 ~5 ~11 ~8 ~5 ~13 minecraft:mangrove_slab[type=bottom]
fill ~9 ~5 ~11 ~9 ~5 ~13 minecraft:mangrove_slab[type=top]
fill ~10 ~6 ~11 ~10 ~6 ~13 minecraft:mangrove_slab[type=bottom]
fill ~11 ~6 ~11 ~11 ~6 ~13 minecraft:mangrove_slab[type=top]
fill ~12 ~6 ~11 ~12 ~6 ~13 minecraft:mangrove_slab[type=bottom]
fill ~13 ~5 ~11 ~13 ~5 ~13 minecraft:mangrove_slab[type=top]
fill ~14 ~5 ~11 ~14 ~5 ~13 minecraft:mangrove_slab[type=bottom]
fill ~10 ~5 ~12 ~12 ~5 ~12 minecraft:white_concrete
setblock ~11 ~5 ~13 minecraft:birch_wall_sign[facing=south]{is_waxed:1b,front_text:{messages:["","東 明 駅","",""],color:"black"}}

# Red canopy over the platform side, on green posts, with a white handrail
fill ~10 ~4 ~1 ~18 ~4 ~3 minecraft:mangrove_slab[type=top]
fill ~10 ~1 ~1 ~10 ~3 ~1 minecraft:green_terracotta
fill ~14 ~1 ~1 ~14 ~3 ~1 minecraft:green_terracotta
fill ~18 ~1 ~1 ~18 ~3 ~1 minecraft:green_terracotta
fill ~15 ~1 ~1 ~17 ~1 ~1 minecraft:iron_bars

# Bench in front of the station
fill ~14 ~1 ~11 ~15 ~1 ~11 minecraft:spruce_stairs[facing=north]
