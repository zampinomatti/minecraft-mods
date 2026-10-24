# ============================================================
# reset_villager.mcfunction (fixed for MC 26.1)
#
# Removes ONLY the gossip types that raise prices.
# Keeps the ones that lower prices intact.
#
# Gossip types (unchanged in 26.1):
#   minor_negative → hit the villager         → price UP   → REMOVED
#   major_negative → killed a villager nearby  → price UP   → REMOVED
#   minor_positive → cured a zombie villager    → price DOWN → kept
#   major_positive → cured, permanent bonus     → price DOWN → kept
#   trading        → frequent trading bonus     → price DOWN → kept
#
# The Gossips[{Type:"..."}] path matches every entry in the
# list whose Type equals the given value, so "data remove"
# deletes ALL matching entries in one command — no loop,
# no index guessing needed.
# ============================================================

data remove entity @s Gossips[{Type:"minor_negative"}]
data remove entity @s Gossips[{Type:"major_negative"}]
