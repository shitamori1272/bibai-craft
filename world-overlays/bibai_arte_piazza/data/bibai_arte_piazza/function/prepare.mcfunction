# Area 1: school, water plaza, frame and ring. Area 2: the arch by the road.
execute unless data storage bibai_arte_piazza:state ticket_owned store success storage bibai_arte_piazza:state ticket_owned byte 1 run forceload add 5464 1366 5590 1455
execute if data storage bibai_arte_piazza:state {ticket_owned:1b} run forceload add 5484 1268 5492 1286
schedule function bibai_arte_piazza:wait_for_chunk 1t replace
