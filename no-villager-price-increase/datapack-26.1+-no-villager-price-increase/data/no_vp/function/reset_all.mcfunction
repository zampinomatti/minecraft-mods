# ============================================================
# reset_all.mcfunction
# Selects all villagers and applies the price reset to each.
# Baby villagers and nitwits have no Offers NBT — commands
# will fail silently on them, no issue.
# ============================================================

execute as @e[type=minecraft:villager] run function no_vp:reset_villager
