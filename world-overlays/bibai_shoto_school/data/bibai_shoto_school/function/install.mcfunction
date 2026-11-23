# Rebuilds the school as it looked in 1976 (exterior only). The forest over the site is cleared.
execute positioned 8736 59 2827 run function bibai_shoto_school:school
data modify storage bibai_shoto_school:state installed set value 1b
execute if data storage bibai_shoto_school:state {ticket_owned:1b} run forceload remove 8736 2827 8794 2912
data remove storage bibai_shoto_school:state ticket_owned
