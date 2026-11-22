# Mitsubishi Bibai mine shaft entrance (竪坑々口), after archive photo 05_k_001_0005.
# Relative origin: northwest corner at ground level. +X = east, +Z = south. The portal faces south (+Z).
# Writes only inside ~0..~8, ~-4..~10, ~0..~11 (X 10184-10192, Y 111-125, Z 1234-1245).

# Clear plants and lay the floor, the portal footing and a muddy approach
fill ~ ~1 ~ ~8 ~10 ~11 minecraft:air
fill ~1 ~ ~ ~7 ~ ~7 minecraft:stone_bricks
fill ~ ~ ~8 ~8 ~ ~8 minecraft:stone_bricks
fill ~2 ~ ~9 ~6 ~ ~11 minecraft:coarse_dirt

# Weathered shed behind the portal, leading down to the shaft
fill ~1 ~1 ~ ~7 ~4 ~7 minecraft:light_gray_concrete
fill ~2 ~1 ~1 ~6 ~4 ~7 minecraft:air
fill ~1 ~5 ~ ~7 ~5 ~7 minecraft:smooth_stone_slab[type=bottom]

# White board portal with a dark top band
fill ~1 ~1 ~8 ~7 ~6 ~8 minecraft:white_concrete
fill ~1 ~6 ~8 ~7 ~6 ~8 minecraft:dark_oak_planks

# Arched opening with a dark frame
fill ~2 ~1 ~8 ~2 ~3 ~8 minecraft:dark_oak_planks
fill ~6 ~1 ~8 ~6 ~3 ~8 minecraft:dark_oak_planks
fill ~3 ~1 ~8 ~5 ~3 ~8 minecraft:air
setblock ~4 ~4 ~8 minecraft:air
setblock ~3 ~4 ~8 minecraft:dark_oak_stairs[facing=west,half=top]
setblock ~5 ~4 ~8 minecraft:dark_oak_stairs[facing=east,half=top]

# Gate posts with caps and safety flags
fill ~ ~1 ~8 ~ ~7 ~8 minecraft:white_concrete
fill ~8 ~1 ~8 ~8 ~7 ~8 minecraft:white_concrete
setblock ~ ~8 ~8 minecraft:dark_oak_planks
setblock ~8 ~8 ~8 minecraft:dark_oak_planks
setblock ~ ~9 ~8 minecraft:white_banner[rotation=0]{patterns:[{pattern:"minecraft:circle",color:"black"},{pattern:"minecraft:straight_cross",color:"white"}]}
setblock ~8 ~9 ~8 minecraft:white_banner[rotation=0]{patterns:[{pattern:"minecraft:circle",color:"black"},{pattern:"minecraft:straight_cross",color:"white"}]}

# Name board above the arch and the safety campaign notice on the east post
setblock ~4 ~5 ~9 minecraft:birch_wall_sign[facing=south]{is_waxed:1b,front_text:{messages:["","竪坑々口","",""],color:"black"}}
setblock ~8 ~5 ~9 minecraft:birch_wall_sign[facing=south]{is_waxed:1b,front_text:{messages:["炭じん","ガス爆発","防止","強調期間"],color:"black"}}
setblock ~8 ~4 ~9 minecraft:birch_wall_sign[facing=south]{is_waxed:1b,front_text:{messages:["","自 十二月","至 三月",""],color:"black"}}

# Steps down toward the shaft, lined with timber, ending in darkness
fill ~3 ~ ~5 ~5 ~ ~5 minecraft:stone_brick_stairs[facing=south]
fill ~3 ~ ~4 ~5 ~ ~4 minecraft:air
fill ~3 ~-1 ~4 ~5 ~-1 ~4 minecraft:stone_brick_stairs[facing=south]
fill ~3 ~-1 ~3 ~5 ~ ~3 minecraft:air
fill ~3 ~-2 ~3 ~5 ~-2 ~3 minecraft:stone_brick_stairs[facing=south]
fill ~3 ~-2 ~2 ~5 ~ ~2 minecraft:air
fill ~3 ~-3 ~2 ~5 ~-3 ~2 minecraft:stone_brick_stairs[facing=south]
fill ~3 ~-3 ~1 ~5 ~ ~1 minecraft:air
fill ~3 ~-4 ~1 ~5 ~-4 ~1 minecraft:stone_brick_stairs[facing=south]
fill ~2 ~-4 ~1 ~2 ~-1 ~4 minecraft:spruce_planks
fill ~6 ~-4 ~1 ~6 ~-1 ~4 minecraft:spruce_planks
fill ~3 ~-4 ~ ~5 ~-1 ~ minecraft:black_concrete
setblock ~4 ~4 ~4 minecraft:lantern[hanging=true]

# Wooden frame in front of the entrance (tall enough to walk under)
fill ~3 ~1 ~10 ~3 ~3 ~10 minecraft:spruce_fence
fill ~5 ~1 ~10 ~5 ~3 ~10 minecraft:spruce_fence
setblock ~4 ~3 ~10 minecraft:spruce_fence
