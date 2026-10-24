# ============================================================
# NO VILLAGER PRICE INCREASE - load.mcfunction
# Runs automatically when the datapack is loaded or the world
# is opened. Sets up the scoreboard and starts the reset cycle.
# ============================================================

# Create the scoreboard used for conditional checks and the
# active flag. The "remove" before "add" prevents "add" from
# failing on /reload if the objective already exists.
# On the very first install, "remove" will show a harmless
# red error — this is expected, just ignore it.
scoreboard objectives remove no_vp_tmp
scoreboard objectives add no_vp_tmp dummy

# Flag that tells the cycle the datapack is active.
# When "uninstall" removes the scoreboard, this flag disappears
# and the cycle stops rescheduling itself automatically.
scoreboard players set #active no_vp_tmp 1

# Confirmation message
tellraw @a [{"text":"["},{"text":"No Villager Price Increase","color":"green","bold":true},{"text":"] ","color":"white"},{"text":"Datapack active! Villager prices will no longer increase.","color":"gray"}]

# Immediate reset of all existing villagers in the world
function no_vp:reset_all

# Start the automatic cycle (every 60 ticks = 3 seconds)
schedule function no_vp:cycle 60t replace
