# Positions follow the official site map (artepiazza.jp, drawn south-up) matched to OpenStreetMap; small offsets are expected.
execute positioned 5464 -6 1366 run function bibai_arte_piazza:school
execute positioned 5515 -5 1400 run function bibai_arte_piazza:water_plaza
execute positioned 5554 -5 1388 run function bibai_arte_piazza:frame
execute positioned 5581 -5 1373 run function bibai_arte_piazza:ring
execute positioned 5484 -7 1268 run function bibai_arte_piazza:arch
data modify storage bibai_arte_piazza:state installed set value 1b
execute if data storage bibai_arte_piazza:state {ticket_owned:1b} run forceload remove 5464 1366 5590 1455
execute if data storage bibai_arte_piazza:state {ticket_owned:1b} run forceload remove 5484 1268 5492 1286
data remove storage bibai_arte_piazza:state ticket_owned
