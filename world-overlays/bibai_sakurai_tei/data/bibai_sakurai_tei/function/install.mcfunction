# Sakurai residence branch: residence (north-south chain, entrance on the east), tondenhei-oku to the west, bust on the east lawn.
# Origin = northwest corner of the grounds at ground level (Y -55).
execute positioned 1306 -55 1453 run function bibai_sakurai_tei:grounds
data modify storage bibai_sakurai_tei:state installed set value 1b
execute if data storage bibai_sakurai_tei:state {ticket_owned:1b} run forceload remove 1306 1447 1386 1487
data remove storage bibai_sakurai_tei:state ticket_owned
