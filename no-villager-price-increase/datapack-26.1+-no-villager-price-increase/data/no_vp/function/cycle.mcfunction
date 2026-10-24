# ============================================================
# cycle.mcfunction
# Main loop. Runs every 60 ticks (3 seconds) and reschedules
# itself ONLY while the datapack is active.
# When "uninstall" removes the scoreboard, the #active flag
# disappears, this check fails, and the loop stops on its own.
# ============================================================

# Reset all loaded villagers
function no_vp:reset_all

# Reschedule only if the datapack is still active
execute if score #active no_vp_tmp matches 1.. run schedule function no_vp:cycle 60t replace
