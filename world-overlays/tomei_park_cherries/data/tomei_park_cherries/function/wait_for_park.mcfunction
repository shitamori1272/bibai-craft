# Wait for the park and the neighboring chunks required by /place feature.
execute in minecraft:overworld if loaded 4272 -13 400 if loaded 4272 -13 464 if loaded 4352 -13 400 if loaded 4352 -13 464 run function tomei_park_cherries:plant
execute unless data storage tomei_park_cherries:state finished run schedule function tomei_park_cherries:wait_for_park 5s replace
