execute unless loaded 1104 -57 1248 run return 0
execute unless loaded 1104 -57 1264 run return 0
execute unless loaded 1104 -57 1280 run return 0
execute unless loaded 1104 -57 1296 run return 0
execute unless loaded 1120 -57 1248 run return 0
execute unless loaded 1120 -57 1264 run return 0
execute unless loaded 1120 -57 1280 run return 0
execute unless loaded 1120 -57 1296 run return 0
execute unless loaded 1136 -57 1264 run return 0
execute unless loaded 1136 -57 1280 run return 0
execute unless loaded 1136 -57 1296 run return 0
execute unless loaded 1152 -57 1264 run return 0
execute unless loaded 1152 -57 1280 run return 0
execute unless loaded 1152 -57 1296 run return 0
execute unless loaded 1152 -57 1312 run return 0
execute unless loaded 1168 -57 1280 run return 0
execute unless loaded 1168 -57 1296 run return 0
execute unless loaded 1168 -57 1312 run return 0
execute unless data storage bibai_city_hospital:state {section_34_39:1b} store success storage bibai_city_hospital:state section_34_39 byte 1 run place template bibai_city_hospital:section_34_39 1113 -58 1261
execute unless data storage bibai_city_hospital:state {section_34_39:1b} run return 0
execute unless data storage bibai_city_hospital:state {section_34_40:1b} store success storage bibai_city_hospital:state section_34_40 byte 1 run place template bibai_city_hospital:section_34_40 1113 -58 1280
execute unless data storage bibai_city_hospital:state {section_34_40:1b} run return 0
execute unless data storage bibai_city_hospital:state {section_35_39:1b} store success storage bibai_city_hospital:state section_35_39 byte 1 run place template bibai_city_hospital:section_35_39 1120 -58 1261
execute unless data storage bibai_city_hospital:state {section_35_39:1b} run return 0
execute unless data storage bibai_city_hospital:state {section_35_40:1b} store success storage bibai_city_hospital:state section_35_40 byte 1 run place template bibai_city_hospital:section_35_40 1120 -58 1280
execute unless data storage bibai_city_hospital:state {section_35_40:1b} run return 0
execute unless data storage bibai_city_hospital:state {section_36_39:1b} store success storage bibai_city_hospital:state section_36_39 byte 1 run place template bibai_city_hospital:section_36_39 1152 -58 1278
execute unless data storage bibai_city_hospital:state {section_36_39:1b} run return 0
execute unless data storage bibai_city_hospital:state {section_36_40:1b} store success storage bibai_city_hospital:state section_36_40 byte 1 run place template bibai_city_hospital:section_36_40 1152 -58 1280
execute unless data storage bibai_city_hospital:state {section_36_40:1b} run return 0
execute unless data storage bibai_city_hospital:state {section_36_41:1b} store success storage bibai_city_hospital:state section_36_41 byte 1 run place template bibai_city_hospital:section_36_41 1162 -58 1312
execute unless data storage bibai_city_hospital:state {section_36_41:1b} run return 0
execute unless entity @e[type=text_display,tag=bibai_hospital_name] run summon text_display 1170.99 -43.65 1285.5 {Tags:["bibai_hospital_name"],Rotation:[-90f,0f],text:{text:"市立美唄病院",color:"white",bold:true},background:0,shadow:1b,alignment:"center",transformation:{translation:[0f,0f,0.035f],left_rotation:[0f,0f,0f,1f],scale:[2.4f,2.4f,2.4f],right_rotation:[0f,0f,0f,1f]}}
execute if entity @e[type=text_display,tag=bibai_hospital_name] run data modify storage bibai_city_hospital:state installed set value 1b
