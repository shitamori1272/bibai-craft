# The portal faces south; the headframes (bibai_headframes) rise behind it to the north.
# Origin = northwest corner at ground level (Y 115). Keeps a 2-block gap from the southern tower (Z 1231).
execute positioned 10184 115 1234 run function bibai_mine_entrance:entrance
data modify storage bibai_mine_entrance:state installed set value 1b
execute if data storage bibai_mine_entrance:state {ticket_owned:1b} run forceload remove 10184 1234 10192 1245
data remove storage bibai_mine_entrance:state ticket_owned
