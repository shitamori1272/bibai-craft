# The bus is entirely in one chunk. Build when someone first visits the station.
execute in minecraft:overworld if loaded 1509 -53 1574 run return run function bibai_noruto_bus:install
schedule function bibai_noruto_bus:wait 1s replace
