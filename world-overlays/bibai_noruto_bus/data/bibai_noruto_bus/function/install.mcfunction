# South-facing van in the west station rotary. The open sliding door faces the sidewalk (+X).
fill 1508 -53 1571 1510 -53 1578 minecraft:white_concrete
fill 1509 -53 1574 1510 -53 1575 minecraft:iron_block
fill 1508 -53 1572 1510 -53 1572 minecraft:black_concrete
fill 1508 -53 1577 1510 -53 1577 minecraft:black_concrete
fill 1508 -52 1571 1508 -52 1577 minecraft:white_concrete
fill 1510 -52 1571 1510 -52 1573 minecraft:white_concrete
fill 1510 -52 1576 1510 -52 1577 minecraft:white_concrete
fill 1508 -52 1571 1510 -52 1571 minecraft:light_blue_concrete
fill 1508 -52 1578 1510 -52 1578 minecraft:white_concrete
fill 1508 -51 1571 1508 -51 1577 minecraft:black_stained_glass
fill 1510 -51 1571 1510 -51 1573 minecraft:black_stained_glass
fill 1510 -51 1576 1510 -51 1577 minecraft:black_stained_glass
fill 1508 -51 1571 1510 -51 1571 minecraft:black_stained_glass
fill 1508 -51 1577 1510 -51 1577 minecraft:gray_stained_glass
fill 1508 -50 1571 1510 -50 1577 minecraft:smooth_quartz_slab[type=bottom]
fill 1511 -53 1574 1511 -53 1575 minecraft:smooth_stone_slab[type=bottom]
setblock 1508 -52 1578 minecraft:sea_lantern
setblock 1510 -52 1578 minecraft:sea_lantern
setblock 1509 -52 1578 minecraft:polished_blackstone
setblock 1509 -52 1572 minecraft:warped_stairs[facing=north]
setblock 1509 -52 1576 minecraft:spruce_stairs[facing=south]
setblock 1511 -52 1573 minecraft:birch_wall_sign[facing=east]{is_waxed:1b,front_text:{messages:["のるーと美唄","01  BIBAI CITY","ゲーム内行先","炭鉱メモリアル公園"]}}
setblock 1507 -52 1574 minecraft:birch_wall_sign[facing=west]{is_waxed:1b,front_text:{messages:["01","のるーと美唄","BIBAI CITY",""]}}
data modify storage bibai_noruto_bus:state installed set value 1b
