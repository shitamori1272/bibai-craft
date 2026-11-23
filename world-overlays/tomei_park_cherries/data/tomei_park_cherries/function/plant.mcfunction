# Record each successful placement so retries and reloads preserve existing trees and player edits.
execute unless data storage tomei_park_cherries:state {tree1:1b} store success storage tomei_park_cherries:state tree1 byte 1 run place feature minecraft:cherry 4289 -13 417
execute unless data storage tomei_park_cherries:state {tree2:1b} store success storage tomei_park_cherries:state tree2 byte 1 run place feature minecraft:cherry 4300 -13 423
execute unless data storage tomei_park_cherries:state {tree3:1b} store success storage tomei_park_cherries:state tree3 byte 1 run place feature minecraft:cherry 4309 -13 429
execute unless data storage tomei_park_cherries:state {tree4:1b} store success storage tomei_park_cherries:state tree4 byte 1 run place feature minecraft:cherry 4320 -13 434
execute unless data storage tomei_park_cherries:state {tree5:1b} store success storage tomei_park_cherries:state tree5 byte 1 run place feature minecraft:cherry 4329 -13 440
execute if data storage tomei_park_cherries:state {tree1:1b,tree2:1b,tree3:1b,tree4:1b,tree5:1b} run data modify storage tomei_park_cherries:state finished set value 1b
