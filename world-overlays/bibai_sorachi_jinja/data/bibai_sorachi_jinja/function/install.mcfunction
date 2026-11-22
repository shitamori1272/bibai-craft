# Approach from the east (first copper torii) to the hall on the west, facing east. Origin = northwest corner of the grounds at ground level (Y -57).
execute positioned 1112 -57 1340 run function bibai_sorachi_jinja:shrine
data modify storage bibai_sorachi_jinja:state installed set value 1b
execute if data storage bibai_sorachi_jinja:state {ticket_owned:1b} run forceload remove 1112 1340 1186 1380
data remove storage bibai_sorachi_jinja:state ticket_owned
