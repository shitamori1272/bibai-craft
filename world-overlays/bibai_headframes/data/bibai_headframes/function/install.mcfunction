execute positioned 10177 115 1217 run function bibai_headframes:tower
execute positioned 10185 115 1225 run function bibai_headframes:tower
data modify storage bibai_headframes:state installed set value 1b
execute if data storage bibai_headframes:state {ticket_owned:1b} run forceload remove 10177 1217
data remove storage bibai_headframes:state ticket_owned
