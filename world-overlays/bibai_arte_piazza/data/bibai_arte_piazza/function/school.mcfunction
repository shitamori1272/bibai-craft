# Former Sakae elementary school (now the gallery), exterior only. Two storeys, dark board walls, red roofs.
# The real building is turned about 34 degrees; here its wings are laid out on the block grid.
# Relative origin: northwest corner of the plot at floor level. Writes only inside ~0..~63, ~0..~30, ~0..~50
# (X 5464-5527, Y -6..24, Z 1366-1416).

# Remove the generated placeholder building and trees, then level the plot
fill ~ ~1 ~ ~63 ~10 ~50 minecraft:air
fill ~ ~11 ~ ~63 ~20 ~50 minecraft:air
fill ~ ~21 ~ ~63 ~30 ~50 minecraft:air
fill ~ ~ ~ ~63 ~ ~50 minecraft:grass_block
fill ~ ~-1 ~ ~63 ~-1 ~50 minecraft:dirt

# Floors, board walls and the floor between the storeys
fill ~11 ~ ~16 ~15 ~ ~34 minecraft:spruce_planks
fill ~11 ~1 ~16 ~15 ~7 ~34 minecraft:dark_oak_planks
fill ~16 ~ ~16 ~23 ~ ~20 minecraft:spruce_planks
fill ~16 ~1 ~16 ~23 ~7 ~20 minecraft:dark_oak_planks
fill ~30 ~ ~20 ~62 ~ ~26 minecraft:spruce_planks
fill ~30 ~1 ~20 ~62 ~7 ~26 minecraft:dark_oak_planks
fill ~24 ~ ~13 ~30 ~ ~20 minecraft:spruce_planks
fill ~24 ~1 ~13 ~30 ~7 ~20 minecraft:dark_oak_planks
fill ~24 ~ ~20 ~30 ~ ~45 minecraft:spruce_planks
fill ~24 ~1 ~20 ~30 ~7 ~45 minecraft:dark_oak_planks
fill ~12 ~1 ~17 ~14 ~7 ~33 minecraft:air
fill ~12 ~4 ~17 ~14 ~4 ~33 minecraft:spruce_planks
fill ~17 ~1 ~17 ~22 ~7 ~19 minecraft:air
fill ~17 ~4 ~17 ~22 ~4 ~19 minecraft:spruce_planks
fill ~31 ~1 ~21 ~61 ~7 ~25 minecraft:air
fill ~31 ~4 ~21 ~61 ~4 ~25 minecraft:spruce_planks
fill ~25 ~1 ~14 ~29 ~7 ~19 minecraft:air
fill ~25 ~4 ~14 ~29 ~4 ~19 minecraft:spruce_planks
fill ~25 ~1 ~21 ~29 ~7 ~44 minecraft:air
fill ~25 ~4 ~21 ~29 ~4 ~44 minecraft:spruce_planks

# Rows of grid windows on both storeys, outside walls only
fill ~11 ~2 ~18 ~11 ~3 ~19 minecraft:glass_pane
fill ~11 ~5 ~18 ~11 ~6 ~19 minecraft:glass_pane
fill ~11 ~2 ~22 ~11 ~3 ~23 minecraft:glass_pane
fill ~11 ~5 ~22 ~11 ~6 ~23 minecraft:glass_pane
fill ~11 ~2 ~26 ~11 ~3 ~27 minecraft:glass_pane
fill ~11 ~5 ~26 ~11 ~6 ~27 minecraft:glass_pane
fill ~11 ~2 ~30 ~11 ~3 ~31 minecraft:glass_pane
fill ~11 ~5 ~30 ~11 ~6 ~31 minecraft:glass_pane
fill ~15 ~2 ~22 ~15 ~3 ~23 minecraft:glass_pane
fill ~15 ~5 ~22 ~15 ~6 ~23 minecraft:glass_pane
fill ~15 ~2 ~26 ~15 ~3 ~27 minecraft:glass_pane
fill ~15 ~5 ~26 ~15 ~6 ~27 minecraft:glass_pane
fill ~15 ~2 ~30 ~15 ~3 ~31 minecraft:glass_pane
fill ~15 ~5 ~30 ~15 ~6 ~31 minecraft:glass_pane
fill ~18 ~2 ~16 ~19 ~3 ~16 minecraft:glass_pane
fill ~18 ~5 ~16 ~19 ~6 ~16 minecraft:glass_pane
fill ~18 ~2 ~20 ~19 ~3 ~20 minecraft:glass_pane
fill ~18 ~5 ~20 ~19 ~6 ~20 minecraft:glass_pane
fill ~32 ~2 ~20 ~33 ~3 ~20 minecraft:glass_pane
fill ~32 ~5 ~20 ~33 ~6 ~20 minecraft:glass_pane
fill ~36 ~2 ~20 ~37 ~3 ~20 minecraft:glass_pane
fill ~36 ~5 ~20 ~37 ~6 ~20 minecraft:glass_pane
fill ~40 ~2 ~20 ~41 ~3 ~20 minecraft:glass_pane
fill ~40 ~5 ~20 ~41 ~6 ~20 minecraft:glass_pane
fill ~44 ~2 ~20 ~45 ~3 ~20 minecraft:glass_pane
fill ~44 ~5 ~20 ~45 ~6 ~20 minecraft:glass_pane
fill ~48 ~2 ~20 ~49 ~3 ~20 minecraft:glass_pane
fill ~48 ~5 ~20 ~49 ~6 ~20 minecraft:glass_pane
fill ~52 ~2 ~20 ~53 ~3 ~20 minecraft:glass_pane
fill ~52 ~5 ~20 ~53 ~6 ~20 minecraft:glass_pane
fill ~56 ~2 ~20 ~57 ~3 ~20 minecraft:glass_pane
fill ~56 ~5 ~20 ~57 ~6 ~20 minecraft:glass_pane
fill ~32 ~2 ~26 ~33 ~3 ~26 minecraft:glass_pane
fill ~32 ~5 ~26 ~33 ~6 ~26 minecraft:glass_pane
fill ~36 ~2 ~26 ~37 ~3 ~26 minecraft:glass_pane
fill ~36 ~5 ~26 ~37 ~6 ~26 minecraft:glass_pane
fill ~40 ~2 ~26 ~41 ~3 ~26 minecraft:glass_pane
fill ~40 ~5 ~26 ~41 ~6 ~26 minecraft:glass_pane
fill ~44 ~2 ~26 ~45 ~3 ~26 minecraft:glass_pane
fill ~44 ~5 ~26 ~45 ~6 ~26 minecraft:glass_pane
fill ~48 ~2 ~26 ~49 ~3 ~26 minecraft:glass_pane
fill ~48 ~5 ~26 ~49 ~6 ~26 minecraft:glass_pane
fill ~52 ~2 ~26 ~53 ~3 ~26 minecraft:glass_pane
fill ~52 ~5 ~26 ~53 ~6 ~26 minecraft:glass_pane
fill ~56 ~2 ~26 ~57 ~3 ~26 minecraft:glass_pane
fill ~56 ~5 ~26 ~57 ~6 ~26 minecraft:glass_pane
fill ~62 ~2 ~22 ~62 ~3 ~23 minecraft:glass_pane
fill ~62 ~5 ~22 ~62 ~6 ~23 minecraft:glass_pane
fill ~24 ~2 ~15 ~24 ~3 ~16 minecraft:glass_pane
fill ~24 ~5 ~15 ~24 ~6 ~16 minecraft:glass_pane
fill ~30 ~2 ~15 ~30 ~3 ~16 minecraft:glass_pane
fill ~30 ~5 ~15 ~30 ~6 ~16 minecraft:glass_pane
fill ~26 ~2 ~45 ~27 ~3 ~45 minecraft:glass_pane
fill ~26 ~5 ~45 ~27 ~6 ~45 minecraft:glass_pane
fill ~24 ~2 ~22 ~24 ~3 ~23 minecraft:glass_pane
fill ~24 ~5 ~22 ~24 ~6 ~23 minecraft:glass_pane
fill ~24 ~2 ~26 ~24 ~3 ~27 minecraft:glass_pane
fill ~24 ~5 ~26 ~24 ~6 ~27 minecraft:glass_pane
fill ~24 ~2 ~30 ~24 ~3 ~31 minecraft:glass_pane
fill ~24 ~5 ~30 ~24 ~6 ~31 minecraft:glass_pane
fill ~24 ~2 ~34 ~24 ~3 ~35 minecraft:glass_pane
fill ~24 ~5 ~34 ~24 ~6 ~35 minecraft:glass_pane
fill ~24 ~2 ~38 ~24 ~3 ~39 minecraft:glass_pane
fill ~24 ~5 ~38 ~24 ~6 ~39 minecraft:glass_pane
fill ~24 ~2 ~42 ~24 ~3 ~43 minecraft:glass_pane
fill ~24 ~5 ~42 ~24 ~6 ~43 minecraft:glass_pane
fill ~30 ~2 ~30 ~30 ~3 ~31 minecraft:glass_pane
fill ~30 ~5 ~30 ~30 ~6 ~31 minecraft:glass_pane
fill ~30 ~2 ~34 ~30 ~3 ~35 minecraft:glass_pane
fill ~30 ~5 ~34 ~30 ~6 ~35 minecraft:glass_pane
fill ~30 ~2 ~38 ~30 ~3 ~39 minecraft:glass_pane
fill ~30 ~5 ~38 ~30 ~6 ~39 minecraft:glass_pane
fill ~30 ~2 ~42 ~30 ~3 ~43 minecraft:glass_pane
fill ~30 ~5 ~42 ~30 ~6 ~43 minecraft:glass_pane

# Main entrance on the north side
setblock ~26 ~1 ~13 minecraft:spruce_door[facing=north,half=lower,hinge=left]
setblock ~26 ~2 ~13 minecraft:spruce_door[facing=north,half=upper,hinge=left]
setblock ~27 ~1 ~13 minecraft:spruce_door[facing=north,half=lower,hinge=right]
setblock ~27 ~2 ~13 minecraft:spruce_door[facing=north,half=upper,hinge=right]

# Red sheet-metal roofs: shallow gables along each wing, 1 m eaves
fill ~10 ~8 ~15 ~10 ~8 ~35 minecraft:mangrove_slab[type=bottom]
fill ~11 ~8 ~15 ~11 ~8 ~35 minecraft:mangrove_slab[type=top]
fill ~12 ~9 ~15 ~12 ~9 ~35 minecraft:mangrove_slab[type=bottom]
fill ~13 ~9 ~15 ~13 ~9 ~35 minecraft:mangrove_slab[type=top]
fill ~14 ~9 ~15 ~14 ~9 ~35 minecraft:mangrove_slab[type=bottom]
fill ~15 ~8 ~15 ~15 ~8 ~35 minecraft:mangrove_slab[type=top]
fill ~16 ~8 ~15 ~16 ~8 ~35 minecraft:mangrove_slab[type=bottom]
fill ~15 ~8 ~15 ~24 ~8 ~15 minecraft:mangrove_slab[type=bottom]
fill ~15 ~8 ~16 ~24 ~8 ~16 minecraft:mangrove_slab[type=top]
fill ~15 ~9 ~17 ~24 ~9 ~17 minecraft:mangrove_slab[type=bottom]
fill ~15 ~9 ~18 ~24 ~9 ~18 minecraft:mangrove_slab[type=top]
fill ~15 ~9 ~19 ~24 ~9 ~19 minecraft:mangrove_slab[type=bottom]
fill ~15 ~8 ~20 ~24 ~8 ~20 minecraft:mangrove_slab[type=top]
fill ~15 ~8 ~21 ~24 ~8 ~21 minecraft:mangrove_slab[type=bottom]
fill ~29 ~8 ~19 ~63 ~8 ~19 minecraft:mangrove_slab[type=bottom]
fill ~29 ~8 ~20 ~63 ~8 ~20 minecraft:mangrove_slab[type=top]
fill ~29 ~9 ~21 ~63 ~9 ~21 minecraft:mangrove_slab[type=bottom]
fill ~29 ~9 ~22 ~63 ~9 ~22 minecraft:mangrove_slab[type=top]
fill ~29 ~10 ~23 ~63 ~10 ~23 minecraft:mangrove_slab[type=bottom]
fill ~29 ~9 ~24 ~63 ~9 ~24 minecraft:mangrove_slab[type=top]
fill ~29 ~9 ~25 ~63 ~9 ~25 minecraft:mangrove_slab[type=bottom]
fill ~29 ~8 ~26 ~63 ~8 ~26 minecraft:mangrove_slab[type=top]
fill ~29 ~8 ~27 ~63 ~8 ~27 minecraft:mangrove_slab[type=bottom]
fill ~23 ~8 ~12 ~23 ~8 ~21 minecraft:mangrove_slab[type=bottom]
fill ~24 ~8 ~12 ~24 ~8 ~21 minecraft:mangrove_slab[type=top]
fill ~25 ~9 ~12 ~25 ~9 ~21 minecraft:mangrove_slab[type=bottom]
fill ~26 ~9 ~12 ~26 ~9 ~21 minecraft:mangrove_slab[type=top]
fill ~27 ~10 ~12 ~27 ~10 ~21 minecraft:mangrove_slab[type=bottom]
fill ~28 ~9 ~12 ~28 ~9 ~21 minecraft:mangrove_slab[type=top]
fill ~29 ~9 ~12 ~29 ~9 ~21 minecraft:mangrove_slab[type=bottom]
fill ~30 ~8 ~12 ~30 ~8 ~21 minecraft:mangrove_slab[type=top]
fill ~31 ~8 ~12 ~31 ~8 ~21 minecraft:mangrove_slab[type=bottom]
fill ~23 ~8 ~19 ~23 ~8 ~46 minecraft:mangrove_slab[type=bottom]
fill ~24 ~8 ~19 ~24 ~8 ~46 minecraft:mangrove_slab[type=top]
fill ~25 ~9 ~19 ~25 ~9 ~46 minecraft:mangrove_slab[type=bottom]
fill ~26 ~9 ~19 ~26 ~9 ~46 minecraft:mangrove_slab[type=top]
fill ~27 ~10 ~19 ~27 ~10 ~46 minecraft:mangrove_slab[type=bottom]
fill ~28 ~9 ~19 ~28 ~9 ~46 minecraft:mangrove_slab[type=top]
fill ~29 ~9 ~19 ~29 ~9 ~46 minecraft:mangrove_slab[type=bottom]
fill ~30 ~8 ~19 ~30 ~8 ~46 minecraft:mangrove_slab[type=top]
fill ~31 ~8 ~19 ~31 ~8 ~46 minecraft:mangrove_slab[type=bottom]
# Board gable ends
fill ~30 ~8 ~21 ~30 ~8 ~25 minecraft:dark_oak_planks
fill ~62 ~8 ~21 ~62 ~8 ~25 minecraft:dark_oak_planks
fill ~30 ~9 ~23 ~30 ~9 ~23 minecraft:dark_oak_planks
fill ~62 ~9 ~23 ~62 ~9 ~23 minecraft:dark_oak_planks
fill ~25 ~8 ~45 ~29 ~8 ~45 minecraft:dark_oak_planks
setblock ~27 ~9 ~45 minecraft:dark_oak_planks
fill ~25 ~8 ~13 ~29 ~8 ~13 minecraft:dark_oak_planks
setblock ~27 ~9 ~13 minecraft:dark_oak_planks
fill ~12 ~8 ~16 ~14 ~8 ~16 minecraft:dark_oak_planks
fill ~12 ~8 ~34 ~14 ~8 ~34 minecraft:dark_oak_planks

# Tall concrete chimneys standing against the walls
fill ~43 ~1 ~27 ~43 ~12 ~27 minecraft:light_gray_concrete
fill ~51 ~1 ~27 ~51 ~12 ~27 minecraft:light_gray_concrete
fill ~47 ~1 ~19 ~47 ~12 ~19 minecraft:light_gray_concrete
fill ~31 ~1 ~33 ~31 ~12 ~33 minecraft:light_gray_concrete

# Red spiral fire escape at the south gable of the long wing, up to a first-floor door
fill ~27 ~1 ~47 ~27 ~6 ~47 minecraft:red_concrete
setblock ~28 ~1 ~46 minecraft:red_nether_brick_slab[type=bottom]
setblock ~28 ~1 ~47 minecraft:red_nether_brick_slab[type=top]
setblock ~28 ~2 ~48 minecraft:red_nether_brick_slab[type=bottom]
setblock ~27 ~2 ~48 minecraft:red_nether_brick_slab[type=top]
setblock ~26 ~3 ~48 minecraft:red_nether_brick_slab[type=bottom]
setblock ~26 ~3 ~47 minecraft:red_nether_brick_slab[type=top]
setblock ~26 ~4 ~46 minecraft:red_nether_brick_slab[type=bottom]
setblock ~27 ~4 ~46 minecraft:red_nether_brick_slab[type=top]
setblock ~27 ~5 ~45 minecraft:spruce_door[facing=south,half=lower,hinge=left]
setblock ~27 ~6 ~45 minecraft:spruce_door[facing=south,half=upper,hinge=left]
