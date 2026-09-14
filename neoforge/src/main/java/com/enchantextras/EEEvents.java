package com.enchantextras;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = EEMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class EEEvents {
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        EEAbilities.handleKill(event.getEntity(), event.getSource());
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        EEAbilities.handleBlockBreak(event.getPlayer(), event.getPos(), event.getState());
    }

    @SubscribeEvent
    public static void onLivingDamagePost(LivingDamageEvent.Post event) {
        EEAbilities.handleThornsoulHit(event.getEntity(), event.getSource());
        EEAbilities.handleExecutioner(event.getEntity(), event.getSource(), event.getNewDamage());
        if (event.getEntity() instanceof ServerPlayer && event.getBlockedDamage() > 0 && event.getEntity().getUseItem().is(Items.SHIELD)) {
            EEAbilities.handleSpikesBlocked(event.getEntity(), event.getSource());
        }
    }

    @SubscribeEvent
    public static void onEntityPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EEAbilities.applyPlanter(player, event.getPos(), event.getPlacedBlock());
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            EEAbilities.tickPlayer(player);
        }
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        EEAbilities.addSwiftnessTrade(event.getServer().registryAccess());
    }
}