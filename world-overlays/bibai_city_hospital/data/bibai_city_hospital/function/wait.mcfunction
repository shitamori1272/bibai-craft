execute in minecraft:overworld run function bibai_city_hospital:try_install
execute unless data storage bibai_city_hospital:state installed run schedule function bibai_city_hospital:wait 5s replace
