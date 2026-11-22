# Mitsubishi Bibai Memorial Hall (三菱美唄記念館), single storey with a tall gable and a deep recessed entrance.
# The real building is turned about 23 degrees; here it sits on the block grid, 2 m north of its mapped spot to keep clear of Route 135.
# Relative origin: northwest corner of the plot at floor level. Building x6..19 (14 m) by z10..22 (13 m), entrance on the north (z10).
# Writes only inside ~0..~23, ~-1..~18, ~0..~24 (X 8678-8701, Y 53..72, Z 2492-2516).

# Remove the generated three-storey placeholder. Its walls reach down to Y 53, so only its own blocks are
# swapped for soil there; the car-park lane (gray concrete at Y 53) in the northeast corner stays untouched.
fill ~ ~1 ~ ~23 ~18 ~24 minecraft:air
fill ~ ~ ~ ~23 ~ ~24 minecraft:grass_block replace minecraft:mushroom_stem
fill ~ ~ ~ ~23 ~ ~24 minecraft:grass_block replace minecraft:polished_andesite
fill ~ ~ ~ ~23 ~ ~24 minecraft:grass_block replace minecraft:cobbled_deepslate
fill ~ ~ ~ ~23 ~ ~24 minecraft:grass_block replace minecraft:stone_bricks
fill ~ ~ ~ ~23 ~ ~24 minecraft:air replace minecraft:gray_stained_glass
fill ~ ~ ~ ~23 ~ ~24 minecraft:air replace minecraft:light
fill ~ ~ ~ ~23 ~ ~24 minecraft:air replace minecraft:dark_oak_door
fill ~ ~ ~ ~23 ~ ~24 minecraft:air replace minecraft:stone_brick_stairs
fill ~ ~ ~ ~23 ~ ~24 minecraft:grass_block replace minecraft:dirt
fill ~ ~-1 ~ ~23 ~-1 ~24 minecraft:dirt replace minecraft:mushroom_stem
fill ~ ~-1 ~ ~23 ~-1 ~24 minecraft:dirt replace minecraft:polished_andesite
fill ~ ~-1 ~ ~23 ~-1 ~24 minecraft:dirt replace minecraft:cobbled_deepslate
fill ~ ~-1 ~ ~23 ~-1 ~24 minecraft:dirt replace minecraft:stone_bricks
# Solid ground under the building
fill ~6 ~-1 ~10 ~19 ~-1 ~22 minecraft:dirt

# Floor and cream walls (side walls 4 m to the eaves)
fill ~6 ~ ~10 ~19 ~ ~22 minecraft:cut_sandstone
fill ~6 ~1 ~10 ~19 ~4 ~22 minecraft:smooth_sandstone
fill ~7 ~1 ~11 ~18 ~4 ~21 minecraft:air

# Steep dark roof, ridge running front to back, 1 m overhang at the gables
fill ~5 ~4 ~9 ~5 ~4 ~23 minecraft:dark_oak_stairs[facing=east]
fill ~6 ~5 ~9 ~6 ~5 ~23 minecraft:dark_oak_stairs[facing=east]
fill ~7 ~6 ~9 ~7 ~6 ~23 minecraft:dark_oak_stairs[facing=east]
fill ~8 ~7 ~9 ~8 ~7 ~23 minecraft:dark_oak_stairs[facing=east]
fill ~9 ~8 ~9 ~9 ~8 ~23 minecraft:dark_oak_stairs[facing=east]
fill ~10 ~9 ~9 ~10 ~9 ~23 minecraft:dark_oak_stairs[facing=east]
fill ~11 ~10 ~9 ~11 ~10 ~23 minecraft:dark_oak_stairs[facing=east]
fill ~12 ~11 ~9 ~12 ~11 ~23 minecraft:dark_oak_stairs[facing=east]
fill ~20 ~4 ~9 ~20 ~4 ~23 minecraft:dark_oak_stairs[facing=west]
fill ~19 ~5 ~9 ~19 ~5 ~23 minecraft:dark_oak_stairs[facing=west]
fill ~18 ~6 ~9 ~18 ~6 ~23 minecraft:dark_oak_stairs[facing=west]
fill ~17 ~7 ~9 ~17 ~7 ~23 minecraft:dark_oak_stairs[facing=west]
fill ~16 ~8 ~9 ~16 ~8 ~23 minecraft:dark_oak_stairs[facing=west]
fill ~15 ~9 ~9 ~15 ~9 ~23 minecraft:dark_oak_stairs[facing=west]
fill ~14 ~10 ~9 ~14 ~10 ~23 minecraft:dark_oak_stairs[facing=west]
fill ~13 ~11 ~9 ~13 ~11 ~23 minecraft:dark_oak_stairs[facing=west]

# Cream gable walls front (z10) and back (z22) under the roof
fill ~7 ~5 ~10 ~18 ~5 ~10 minecraft:smooth_sandstone
fill ~8 ~6 ~10 ~17 ~6 ~10 minecraft:smooth_sandstone
fill ~9 ~7 ~10 ~16 ~7 ~10 minecraft:smooth_sandstone
fill ~10 ~8 ~10 ~15 ~8 ~10 minecraft:smooth_sandstone
fill ~11 ~9 ~10 ~14 ~9 ~10 minecraft:smooth_sandstone
fill ~12 ~10 ~10 ~13 ~10 ~10 minecraft:smooth_sandstone
fill ~7 ~5 ~22 ~18 ~5 ~22 minecraft:smooth_sandstone
fill ~8 ~6 ~22 ~17 ~6 ~22 minecraft:smooth_sandstone
fill ~9 ~7 ~22 ~16 ~7 ~22 minecraft:smooth_sandstone
fill ~10 ~8 ~22 ~15 ~8 ~22 minecraft:smooth_sandstone
fill ~11 ~9 ~22 ~14 ~9 ~22 minecraft:smooth_sandstone
fill ~12 ~10 ~22 ~13 ~10 ~22 minecraft:smooth_sandstone

# Deep recessed entrance under the gable: side walls, ceiling, and the door wall set back 3 m
fill ~9 ~1 ~10 ~9 ~7 ~13 minecraft:smooth_sandstone
fill ~16 ~1 ~10 ~16 ~7 ~13 minecraft:smooth_sandstone
fill ~10 ~8 ~10 ~15 ~8 ~13 minecraft:smooth_sandstone
fill ~10 ~1 ~13 ~15 ~7 ~13 minecraft:smooth_sandstone
fill ~10 ~1 ~10 ~15 ~7 ~12 minecraft:air
fill ~10 ~ ~10 ~15 ~ ~12 minecraft:stone_bricks
# Brown double door with a small dark canopy, dark glass either side
setblock ~12 ~1 ~13 minecraft:dark_oak_door[facing=north,half=lower,hinge=left]
setblock ~12 ~2 ~13 minecraft:dark_oak_door[facing=north,half=upper,hinge=left]
setblock ~13 ~1 ~13 minecraft:dark_oak_door[facing=north,half=lower,hinge=right]
setblock ~13 ~2 ~13 minecraft:dark_oak_door[facing=north,half=upper,hinge=right]
fill ~11 ~3 ~12 ~14 ~3 ~12 minecraft:dark_oak_stairs[facing=south]
fill ~10 ~1 ~13 ~10 ~2 ~13 minecraft:gray_stained_glass_pane
fill ~15 ~1 ~13 ~15 ~2 ~13 minecraft:gray_stained_glass_pane
# Inside: keep the space behind the recess open as part of the hall
fill ~10 ~1 ~14 ~15 ~4 ~14 minecraft:air

# Windows: small ones on the front walls, tall ones elsewhere.
# The southwest corner (nearest Route 135) has a window on each wall, as in the reference photo.
fill ~7 ~2 ~10 ~7 ~3 ~10 minecraft:glass_pane
fill ~18 ~2 ~10 ~18 ~3 ~10 minecraft:glass_pane
fill ~6 ~1 ~20 ~6 ~3 ~20 minecraft:glass_pane
fill ~8 ~1 ~22 ~8 ~3 ~22 minecraft:glass_pane
fill ~6 ~1 ~15 ~6 ~3 ~15 minecraft:glass_pane
fill ~19 ~1 ~15 ~19 ~3 ~15 minecraft:glass_pane
fill ~19 ~1 ~20 ~19 ~3 ~20 minecraft:glass_pane
fill ~17 ~1 ~22 ~17 ~3 ~22 minecraft:glass_pane

# Hidden light sources so the hall is not dark
setblock ~9 ~4 ~17 minecraft:light[level=15]
setblock ~16 ~4 ~17 minecraft:light[level=15]
setblock ~12 ~6 ~20 minecraft:light[level=15]

# Pale blue name pole in front of the entrance
fill ~13 ~1 ~8 ~13 ~12 ~8 minecraft:light_blue_terracotta
setblock ~13 ~3 ~7 minecraft:birch_wall_sign[facing=north]{is_waxed:1b,front_text:{messages:["三菱","美唄","記念館",""],color:"black"}}

# Leather armour set on an armour stand in the southwest corner, facing into the room (after reference photo 4)
summon minecraft:armor_stand ~7.5 ~1 ~21.5 {Rotation:[-135.0f,0.0f],ShowArms:1b,Invulnerable:1b,DisabledSlots:4144959,Tags:["bibai_kinenkan_armor"],equipment:{head:{id:"minecraft:leather_helmet",count:1,components:{"minecraft:dyed_color":13684936}},chest:{id:"minecraft:leather_chestplate",count:1,components:{"minecraft:dyed_color":14207146}},legs:{id:"minecraft:leather_leggings",count:1,components:{"minecraft:dyed_color":14207146}},feet:{id:"minecraft:leather_boots",count:1,components:{"minecraft:dyed_color":3878693}}}}
