# Front (smokebox) faces west toward the station. Origin = west end, north side, ground level (Y -14).
execute positioned 4853 -14 1192 run function bibai_tomei_sl:locomotive
data modify storage bibai_tomei_sl:state installed set value 1b
execute if data storage bibai_tomei_sl:state {ticket_owned:1b} run forceload remove 4849 1190 4868 1196
data remove storage bibai_tomei_sl:state ticket_owned
