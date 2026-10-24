package dev.novp;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

@Mod(NoVillagerPriceIncrease.MOD_ID)
public class NoVillagerPriceIncrease {

    public static final String MOD_ID = "no_vp";
    private static final Logger LOGGER = LogUtils.getLogger();

    // Only these two gossip types raise prices — they get removed.
    // MINOR_POSITIVE, MAJOR_POSITIVE (zombie curing) and TRADING
    // are left untouched, since they only ever lower prices.
    private static final int INTERVAL = 60; // 3 seconds
    private int ticks = 0;

    public NoVillagerPriceIncrease(IEventBus modEventBus, ModContainer modContainer) {
        // Register this class for game events (NeoForge.EVENT_BUS),
        // needed because onServerTick below is annotated with @SubscribeEvent.
        NeoForge.EVENT_BUS.register(this);
        LOGGER.info("[No Villager Price Increase] Active. Negative gossips cleared every 3s.");
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        if (++ticks < INTERVAL) return;
        ticks = 0;

        MinecraftServer server = event.getServer();
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
