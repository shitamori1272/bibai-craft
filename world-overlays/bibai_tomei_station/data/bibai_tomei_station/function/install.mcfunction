# Front (station name gable) faces south toward the road; the canopy faces the old platform to the north.
# Origin = northwest corner of the cleared plot at ground level (Y -14).
execute positioned 4824 -14 1205 run function bibai_tomei_station:station
data modify storage bibai_tomei_station:state installed set value 1b
execute if data storage bibai_tomei_station:state {ticket_owned:1b} run forceload remove 4824 1205 4846 1219
data remove storage bibai_tomei_station:state ticket_owned
