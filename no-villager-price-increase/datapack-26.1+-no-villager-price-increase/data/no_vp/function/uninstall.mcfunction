# ============================================================
# uninstall.mcfunction
# Run this BEFORE removing the datapack files.
# Usage: /function no_vp:uninstall
#
# Removing the scoreboard also destroys the #active flag.
# On the next tick, the cycle will run one final reset
# (harmless), then check #active → fails → does NOT
# reschedule. The loop stops on its own within 3 seconds.
# ============================================================

scoreboard objectives remove no_vp_tmp

tellraw @a [{"text":"["},{"text":"No Villager Price Increase","color":"red","bold":true},{"text":"] ","color":"white"},{"text":"Datapack uninstalled. You can now remove it from the datapacks folder.","color":"gray"}]
