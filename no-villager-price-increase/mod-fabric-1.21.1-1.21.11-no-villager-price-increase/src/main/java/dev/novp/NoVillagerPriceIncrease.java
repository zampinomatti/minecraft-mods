package dev.novp;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.npc.Villager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoVillagerPriceIncrease implements ModInitializer {

    public static final String MOD_ID = "no_vp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Only these two gossip types raise prices — they get removed.
    // MINOR_POSITIVE, MAJOR_POSITIVE (zombie curing) and TRADING
    // are left untouched, since they only ever lower prices.
    private static final int INTERVAL = 60; // 3 seconds
    private int ticks = 0;

    @Override
    public void onInitialize() {
        LOGGER.info("[No Villager Price Increase] Active. Negative gossips cleared every 3s.");
        ServerTickEvents.END_SERVER_TICK.register(this::onTick);
    }

    private void onTick(MinecraftServer server) {
        if (++ticks < INTERVAL) return;
        ticks = 0;

        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof Villager villager) {
                    clearNegativeGossips(villager);
                }
            }
        }
    }

    private void clearNegativeGossips(Villager villager) {
        GossipContainer gossips = villager.getGossips();
        gossips.remove(GossipType.MINOR_NEGATIVE);
        gossips.remove(GossipType.MAJOR_NEGATIVE);
    }
}
