package com.enchantextras;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public class EEMod implements ModInitializer {
    public static final String MOD_ID = "enchantextras";

    @Override
    public void onInitialize() {
        ServerLivingEntityEvents.AFTER_DEATH.register(EEAbilities::handleKill);
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> EEAbilities.handleBlockBreak(player, pos, state));
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
            EEAbilities.handleThornsoulHit(entity, source);
            EEAbilities.handleExecutioner(entity, source, damageTaken);
            if (blocked) {
                EEAbilities.handleSpikesBlocked(entity, source);
            }
        });
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.PASS;
            }
            if (EEAbilities.getPlanterLevel(serverPlayer) <= 1) {
                return InteractionResult.PASS;
            }
            BlockPos predicted = hitResult.getBlockPos().relative(hitResult.getDirection());
            ((ServerLevel) serverPlayer.level()).getServer().executeIfPossible(() ->
                    EEAbilities.applyPlanter(serverPlayer, predicted, serverPlayer.level().getBlockState(predicted)));
            return InteractionResult.PASS;
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                EEAbilities.tickPlayer(player);
            }
        });
        ServerLifecycleEvents.SERVER_STARTING.register(server -> EEAbilities.addSwiftnessTrade(server.registryAccess()));
    }
}