package dev.novp;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NoVillagerPriceIncrease implements ModInitializer {

    public static final String MOD_ID = "no_vp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Set<String> NEGATIVE_TYPES = Set.of(
        "minor_negative", "major_negative", "villager_killed", "golem_killed",
        "MINOR_NEGATIVE", "MAJOR_NEGATIVE", "VILLAGER_KILLED", "GOLEM_KILLED"
    );

    private static final int INTERVAL = 60;
    private int ticks = 0;

    private Field gossipsField = null;
    private Class<?> gossipContainerClass = null;
    private Method removePredicateMethod = null;
    private boolean reflectionReady = false;
    private boolean reflectionFailed = false;
    private int gossipsRemovedTotal = 0;

    @Override
    public void onInitialize() {
        LOGGER.info("[NVP] Mod initialized. Will clear negative gossips every 3s.");
        ServerTickEvents.END_SERVER_TICK.register(this::onTick);
    }

    private void onTick(MinecraftServer server) {
        if (++ticks < INTERVAL) return;
        ticks = 0;
        if (reflectionFailed) return;

        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                String simpleName = entity.getClass().getSimpleName();
                if (simpleName.equals("Villager")) {
                    if (!reflectionReady) tryInitReflection(entity);
                    if (reflectionReady) clearNegativeGossips(entity);
                }
            }
        }
    }

    private void tryInitReflection(Entity villager) {
        try {
            // Find the 'gossips' field walking up the class hierarchy
            Class<?> clazz = villager.getClass();
            while (clazz != null) {
                try {
                    gossipsField = clazz.getDeclaredField("gossips");
                    gossipsField.setAccessible(true);
                    gossipContainerClass = gossipsField.getType();
                    LOGGER.info("[NVP] Found gossips field. GossipContainer = {}",
                        gossipContainerClass.getName());
                    break;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }

            if (gossipContainerClass == null) {
                LOGGER.error("[NVP] Could not find 'gossips' field in Villager hierarchy!");
                reflectionFailed = true;
                return;
            }

            // Log all methods so we can see what's available
            LOGGER.info("[NVP] GossipContainer methods:");
            for (Method m : gossipContainerClass.getDeclaredMethods()) {
                LOGGER.info("[NVP]   {} ({})", m.getName(),
                    Arrays.stream(m.getParameterTypes())
                        .map(Class::getSimpleName)
                        .collect(Collectors.joining(", ")));
            }

            // Log all fields
            LOGGER.info("[NVP] GossipContainer fields:");
            for (Field f : gossipContainerClass.getDeclaredFields()) {
                LOGGER.info("[NVP]   {} : {}", f.getName(), f.getType().getSimpleName());
            }

            // Try to find remove(Predicate) method
            for (Method m : gossipContainerClass.getDeclaredMethods()) {
                if (m.getName().equals("remove") && m.getParameterCount() == 1
                    && m.getParameterTypes()[0].equals(Predicate.class)) {
                    removePredicateMethod = m;
                    removePredicateMethod.setAccessible(true);
                    LOGGER.info("[NVP] Found remove(Predicate) method — will use it.");
                    break;
                }
            }

            if (removePredicateMethod == null) {
                LOGGER.warn("[NVP] remove(Predicate) not found — will use field manipulation.");
            }

            reflectionReady = true;

        } catch (Exception e) {
            LOGGER.error("[NVP] Reflection init failed: {}", e.getMessage(), e);
            reflectionFailed = true;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void clearNegativeGossips(Entity villager) {
        try {
            Object gossipContainer = gossipsField.get(villager);
            if (gossipContainer == null) return;

            if (removePredicateMethod != null) {
                // Best case: call remove(Predicate<GossipType>) directly
                Predicate<Object> isNegative = type -> {
                    String name = type.toString();
                    return NEGATIVE_TYPES.contains(name) ||
                           NEGATIVE_TYPES.contains(name.toLowerCase());
                };
                removePredicateMethod.invoke(gossipContainer, isNegative);
                gossipsRemovedTotal++;
                if (gossipsRemovedTotal % 20 == 1) {
                    LOGGER.info("[NVP] remove(Predicate) called successfully (run #{})", gossipsRemovedTotal);
                }
            } else {
                // Fallback: walk fields of GossipContainer, find internal map
                for (Field f : gossipContainerClass.getDeclaredFields()) {
                    f.setAccessible(true);
                    Object val = f.get(gossipContainer);
                    if (val instanceof java.util.Map<?, ?> outerMap) {
                        LOGGER.info("[NVP] Found map field '{}' with {} entries", f.getName(), outerMap.size());
                        for (Object innerObj : outerMap.values()) {
                            if (innerObj == null) continue;
                            // EntityGossips object — find its entries map
                            for (Field innerField : innerObj.getClass().getDeclaredFields()) {
                                innerField.setAccessible(true);
                                Object innerVal = innerField.get(innerObj);
                                if (innerVal instanceof java.util.Map<?, ?> entriesMap) {
                                    LOGGER.info("[NVP] Found entries map '{}' with {} types",
                                        innerField.getName(), entriesMap.size());
                                    // Print the keys so we know the type format
                                    for (Object key : entriesMap.keySet()) {
                                        LOGGER.info("[NVP]   gossip type key: '{}' ({})",
                                            key, key.getClass().getSimpleName());
                                    }
                                    // Remove negative entries
                                    ((java.util.Map) entriesMap).entrySet().removeIf(e -> {
                                        String name = ((java.util.Map.Entry<?,?>)e).getKey().toString();
                                        return NEGATIVE_TYPES.contains(name) ||
                                               NEGATIVE_TYPES.contains(name.toLowerCase());
                                    });
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("[NVP] clearNegativeGossips failed: {}", e.getMessage(), e);
        }
    }
}
