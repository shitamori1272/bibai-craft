# The entrance faces north toward the footpath from the car park; Route 135 runs just south of the building.
# Origin = northwest corner of the cleared plot at floor level (Y 54).
execute positioned 8678 54 2492 run function bibai_kinenkan:hall
data modify storage bibai_kinenkan:state installed set value 1b
execute if data storage bibai_kinenkan:state {ticket_owned:1b} run forceload remove 8678 2492 8701 2516
data remove storage bibai_kinenkan:state ticket_owned
