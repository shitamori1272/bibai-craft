# Mitsubishi Bibai Railway Class 4110 No. 2 tank locomotive, roughly 1:1 (12 long, 3 wide).
# Relative origin: west end (front), north side, ground level. +X = rear, +Z = south.
# Writes only inside ~-4..~15, ~0..~8, ~-1..~3 (X 4849-4868, Z 1191-1195).

# Clear plants above the ground and lay the old track bed
fill ~-4 ~1 ~-1 ~15 ~8 ~3 minecraft:air
fill ~-3 ~ ~ ~14 ~ ~2 minecraft:gravel
fill ~-3 ~1 ~1 ~-1 ~1 ~1 minecraft:rail[shape=east_west]
fill ~12 ~1 ~1 ~14 ~1 ~1 minecraft:rail[shape=east_west]

# Layer 1: frame, buffer beam and wheels
fill ~ ~1 ~ ~11 ~1 ~2 minecraft:black_concrete
# Five driving axles (silver wheel centres) on both sides
fill ~2 ~1 ~-1 ~6 ~1 ~-1 minecraft:stone_button[face=wall,facing=north]
fill ~2 ~1 ~3 ~6 ~1 ~3 minecraft:stone_button[face=wall,facing=south]

# Layer 2: running board, side tanks, cab floor level
fill ~1 ~2 ~ ~11 ~2 ~2 minecraft:black_concrete

# Layer 3: smokebox, side tanks, cab windows, coal bunker
fill ~1 ~3 ~ ~11 ~3 ~2 minecraft:black_concrete
setblock ~8 ~3 ~ minecraft:glass_pane
setblock ~8 ~3 ~2 minecraft:glass_pane
setblock ~9 ~3 ~ minecraft:glass_pane
setblock ~9 ~3 ~2 minecraft:glass_pane
setblock ~10 ~3 ~1 minecraft:glass_pane

# Layer 4: round smokebox top, boiler with white handrails, cab walls, coal
setblock ~1 ~4 ~1 minecraft:black_concrete
setblock ~2 ~4 ~1 minecraft:black_concrete
fill ~1 ~4 ~ ~2 ~4 ~ minecraft:polished_blackstone_slab[type=bottom]
fill ~1 ~4 ~2 ~2 ~4 ~2 minecraft:polished_blackstone_slab[type=bottom]
fill ~3 ~4 ~1 ~7 ~4 ~1 minecraft:black_concrete
fill ~3 ~4 ~ ~7 ~4 ~ minecraft:smooth_quartz_slab[type=bottom]
fill ~3 ~4 ~2 ~7 ~4 ~2 minecraft:smooth_quartz_slab[type=bottom]
fill ~8 ~4 ~ ~10 ~4 ~2 minecraft:black_concrete
fill ~11 ~4 ~ ~11 ~4 ~2 minecraft:coal_block

# Cab interior
fill ~9 ~2 ~1 ~9 ~4 ~1 minecraft:air

# Layer 5: headlamp, chimney, domes, brass whistle, cab roof
setblock ~1 ~5 ~1 minecraft:lantern
setblock ~2 ~5 ~1 minecraft:blackstone_wall
setblock ~4 ~5 ~1 minecraft:polished_blackstone_wall
setblock ~6 ~5 ~1 minecraft:polished_blackstone_wall
setblock ~7 ~5 ~1 minecraft:lightning_rod
fill ~8 ~5 ~ ~10 ~5 ~2 minecraft:black_concrete

# Red number plates "2": smokebox front and both side tanks
setblock ~ ~3 ~1 minecraft:crimson_wall_sign[facing=west]{is_waxed:1b,front_text:{messages:["","2","",""],color:"yellow",has_glowing_text:1b}}
setblock ~7 ~3 ~-1 minecraft:crimson_wall_sign[facing=north]{is_waxed:1b,front_text:{messages:["","2","",""],color:"yellow",has_glowing_text:1b}}
setblock ~7 ~3 ~3 minecraft:crimson_wall_sign[facing=south]{is_waxed:1b,front_text:{messages:["","2","",""],color:"yellow",has_glowing_text:1b}}

# Cab steps on both sides
fill ~9 ~1 ~-1 ~9 ~2 ~-1 minecraft:ladder[facing=north]
fill ~9 ~1 ~3 ~9 ~2 ~3 minecraft:ladder[facing=south]
