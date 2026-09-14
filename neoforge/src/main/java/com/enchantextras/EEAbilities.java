package com.enchantextras;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

public final class EEAbilities {
    public static final ResourceKey<Enchantment> SWIFTNESS = ResourceKey.create(
            Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(EEMod.MOD_ID, "swiftness"));
    public static final ResourceKey<Enchantment> LEECH = ResourceKey.create(
            Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(EEMod.MOD_ID, "leech"));
    public static final ResourceKey<Enchantment> ZEALOT = ResourceKey.create(
            Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(EEMod.MOD_ID, "zealot"));
    public static final ResourceKey<Enchantment> VEIN_MINER = ResourceKey.create(
            Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(EEMod.MOD_ID, "vein_miner"));
    public static final ResourceKey<Enchantment> TIMBER = ResourceKey.create(
            Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(EEMod.MOD_ID, "timber"));
    public static final ResourceKey<Enchantment> PLANTER = ResourceKey.create(
            Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(EEMod.MOD_ID, "planter"));
    public static final ResourceKey<Enchantment> SPIKES = ResourceKey.create(
            Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(EEMod.MOD_ID, "spikes"));
    public static final ResourceKey<Enchantment> BERSERKER = ResourceKey.create(
            Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(EEMod.MOD_ID, "berserker"));
    public static final ResourceKey<Enchantment> EXECUTIONER = ResourceKey.create(
            Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(EEMod.MOD_ID, "executioner"));
    public static final ResourceKey<Enchantment> MOMENTUM = ResourceKey.create(
            Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(EEMod.MOD_ID, "momentum"));
    public static final ResourceKey<Enchantment> OVERCHARGE = ResourceKey.create(
            Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(EEMod.MOD_ID, "overcharge"));
    public static final ResourceKey<Enchantment> THORNSOUL = ResourceKey.create(
            Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(EEMod.MOD_ID, "thornsoul"));

    private static final int MAX_VEIN_BLOCKS = 64;
    private static final int MAX_TREE_BLOCKS = 256;
    private static final float BERSERKER_HEALTH_FRACTION = 0.35F;

    private static final TagKey<Block> LOG_BLOCKS = TagKey.create(
            Registries.BLOCK, ResourceLocation.withDefaultNamespace("logs"));

    private static final ResourceLocation BERSERKER_DAMAGE_MODIFIER = ResourceLocation.fromNamespaceAndPath(
            EEMod.MOD_ID, "berserker_attack_damage");
    private static final ResourceLocation BERSERKER_SPEED_MODIFIER = ResourceLocation.fromNamespaceAndPath(
            EEMod.MOD_ID, "berserker_attack_speed");
    private static final ResourceLocation MOMENTUM_MODIFIER = ResourceLocation.fromNamespaceAndPath(
            EEMod.MOD_ID, "momentum_speed");
    private static final ResourceLocation THORNSOUL_MODIFIER = ResourceLocation.fromNamespaceAndPath(
            EEMod.MOD_ID, "thornsoul_attack_damage");
    private static final ResourceLocation OVERCHARGE_MODIFIER = ResourceLocation.fromNamespaceAndPath(
            EEMod.MOD_ID, "overcharge_block_break_speed");

    private static final Map<UUID, Float> MOMENTUM_CHARGES = new HashMap<>();
    private static final Map<UUID, Integer> THORNSOUL_TIMERS = new HashMap<>();
    private static final Map<UUID, Long> EXECUTIONER_DEBOUNCE = new HashMap<>();
    private static final Map<UUID, Block> OVERCHARGE_LAST_BLOCK = new HashMap<>();
    private static final Map<UUID, Integer> OVERCHARGE_CHAINS = new HashMap<>();
    private static final Map<UUID, Integer> OVERCHARGE_TIMERS = new HashMap<>();

    private static final Set<TagKey<Block>> ORE_TAGS = Set.of(
            TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("coal_ores")),
            TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("copper_ores")),
            TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("diamond_ores")),
            TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("emerald_ores")),
            TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("gold_ores")),
            TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("iron_ores")),
            TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("lapis_ores")),
            TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("redstone_ores")));

    private EEAbilities() {
    }

    public static void handleKill(LivingEntity victim, DamageSource source) {
        if (victim.level().isClientSide) {
            return;
        }
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        RegistryAccess access = player.level().registryAccess();
        ItemStack weapon = source.getWeaponItem();
        int leechLevel = getEnchantmentLevel(access, LEECH, weapon);
        if (leechLevel > 0) {
            player.heal(2.0F * leechLevel);
        }
        int zealotLevel = getEnchantmentLevel(access, ZEALOT, weapon);
        if (zealotLevel > 0) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60 * zealotLevel, 0));
        }
    }

    public static void tickPlayer(ServerPlayer player) {
        if (player.level().isClientSide) {
            return;
        }
        tickBerserker(player);
        tickMomentum(player);
        tickThornsoul(player);
        tickOvercharge(player);
    }

    private static void tickBerserker(ServerPlayer player) {
        RegistryAccess access = player.level().registryAccess();
        int level = getEnchantmentLevel(access, BERSERKER, player.getMainHandItem());
        AttributeInstance damage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance speed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (damage == null || speed == null) {
            return;
        }
        if (level > 0 && player.getHealth() / player.getMaxHealth() < BERSERKER_HEALTH_FRACTION) {
            damage.removeModifier(BERSERKER_DAMAGE_MODIFIER);
            damage.addTransientModifier(new AttributeModifier(BERSERKER_DAMAGE_MODIFIER,
                    berserkerDamageBonus(level), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            speed.removeModifier(BERSERKER_SPEED_MODIFIER);
            speed.addTransientModifier(new AttributeModifier(BERSERKER_SPEED_MODIFIER,
                    berserkerSpeedBonus(level), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else {
            damage.removeModifier(BERSERKER_DAMAGE_MODIFIER);
            speed.removeModifier(BERSERKER_SPEED_MODIFIER);
        }
    }

    private static float berserkerDamageBonus(int level) {
        return switch (level) {
            case 2 -> 0.25F;
            case 3 -> 0.40F;
            default -> 0.15F;
        };
    }

    private static float berserkerSpeedBonus(int level) {
        return switch (level) {
            case 2 -> 0.15F;
            case 3 -> 0.25F;
            default -> 0.10F;
        };
    }

    private static void tickMomentum(ServerPlayer player) {
        AttributeInstance attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute == null) {
            return;
        }
        UUID id = player.getUUID();
        int level = getEnchantmentLevel(player.level().registryAccess(), MOMENTUM,
                player.getItemBySlot(EquipmentSlot.FEET));
        if (level <= 0) {
            MOMENTUM_CHARGES.remove(id);
            attribute.removeModifier(MOMENTUM_MODIFIER);
            return;
        }
        float charge = MOMENTUM_CHARGES.getOrDefault(id, 0.0F);
        double dx = player.getDeltaMovement().x;
        double dz = player.getDeltaMovement().z;
        double horizontalSpeedSquared = dx * dx + dz * dz;
        if (player.hurtTime > 0 || horizontalSpeedSquared <= 0.0001) {
            charge = 0.0F;
        } else {
            charge = Math.min(1.0F, charge + level * 0.01F);
        }
        if (charge > 0.0F) {
            float cap = switch (level) {
                case 2 -> 0.09F;
                case 3 -> 0.14F;
                default -> 0.05F;
            };
            attribute.removeModifier(MOMENTUM_MODIFIER);
            attribute.addTransientModifier(new AttributeModifier(MOMENTUM_MODIFIER, charge * cap,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else {
            attribute.removeModifier(MOMENTUM_MODIFIER);
        }
        MOMENTUM_CHARGES.put(id, charge);
    }

    private static void tickThornsoul(ServerPlayer player) {
        AttributeInstance attribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attribute == null) {
            return;
        }
        UUID id = player.getUUID();
        int remaining = THORNSOUL_TIMERS.getOrDefault(id, 0);
        if (remaining <= 0) {
            THORNSOUL_TIMERS.remove(id);
            attribute.removeModifier(THORNSOUL_MODIFIER);
            return;
        }
        int level = getEnchantmentLevel(player.level().registryAccess(), THORNSOUL,
                player.getItemBySlot(EquipmentSlot.CHEST));
        if (level <= 0) {
            THORNSOUL_TIMERS.remove(id);
            attribute.removeModifier(THORNSOUL_MODIFIER);
            return;
        }
        attribute.removeModifier(THORNSOUL_MODIFIER);
        attribute.addTransientModifier(new AttributeModifier(THORNSOUL_MODIFIER,
                thornsoulDamageBonus(level), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        THORNSOUL_TIMERS.put(id, remaining - 1);
    }

    private static float thornsoulDamageBonus(int level) {
        return switch (level) {
            case 2 -> 0.30F;
            case 3 -> 0.45F;
            default -> 0.15F;
        };
    }

    public static void handleBlockBreak(Player player, BlockPos pos, BlockState state) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ServerLevel level = (ServerLevel) serverPlayer.level();
        ItemStack tool = serverPlayer.getMainHandItem();
        RegistryAccess access = level.registryAccess();
        boolean mined = false;
        if (getEnchantmentLevel(access, VEIN_MINER, tool) > 0) {
            OreFamilies families = oreFamilies(state);
            if (families.matches(state)) {
                mineVein(level, serverPlayer, pos, families);
                mined = true;
            }
        }
        if (!mined && getEnchantmentLevel(access, TIMBER, tool) > 0 && state.is(LOG_BLOCKS)) {
            mineTree(level, serverPlayer, pos);
        }
        handleOvercharge(serverPlayer, state);
    }

    private static void mineVein(ServerLevel level, ServerPlayer player, BlockPos origin, OreFamilies families) {
        boolean creative = player.isCreative();
        List<BlockPos> vein = new ArrayList<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<Long> seen = new HashSet<>();
        queue.add(origin);
        seen.add(origin.asLong());
        while (!queue.isEmpty() && vein.size() < MAX_VEIN_BLOCKS) {
            BlockPos current = queue.poll();
            vein.add(current);
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);
                if (!seen.add(neighbor.asLong())) {
                    continue;
                }
                if (families.matches(level.getBlockState(neighbor))) {
                    queue.add(neighbor);
                }
            }
        }
        vein.remove(origin);
        for (BlockPos pos : vein) {
            if (!creative && player.getMainHandItem().isEmpty()) {
                break;
            }
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }
            if (!creative) {
                BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
                Block.dropResources(state, level, pos, blockEntity, player, player.getMainHandItem());
                player.getMainHandItem().hurtAndBreak(1, level, player, item -> {
                });
            }
            level.removeBlock(pos, false);
        }
    }

    private static void mineTree(ServerLevel level, ServerPlayer player, BlockPos origin) {
        List<BlockPos> cluster = collectTreeCluster(level, origin);
        if (!isTreeLike(cluster)) {
            return;
        }
        boolean creative = player.isCreative();
        for (BlockPos pos : cluster) {
            if (pos.equals(origin)) {
                continue;
            }
            if (!creative && player.getMainHandItem().isEmpty()) {
                break;
            }
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }
            if (!creative) {
                BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
                Block.dropResources(state, level, pos, blockEntity, player, player.getMainHandItem());
                player.getMainHandItem().hurtAndBreak(1, level, player, item -> {
                });
            }
            level.removeBlock(pos, false);
        }
    }

    private static List<BlockPos> collectTreeCluster(ServerLevel level, BlockPos origin) {
        List<BlockPos> cluster = new ArrayList<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<Long> seen = new HashSet<>();
        queue.add(origin);
        seen.add(origin.asLong());
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            cluster.add(current);
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);
                if (!seen.add(neighbor.asLong())) {
                    continue;
                }
                if (level.getBlockState(neighbor).is(LOG_BLOCKS)) {
                    queue.add(neighbor);
                }
            }
            if (cluster.size() >= MAX_TREE_BLOCKS) {
                break;
            }
        }
        return cluster;
    }

    private static boolean isTreeLike(List<BlockPos> cluster) {
        if (cluster.size() < 3) {
            return false;
        }
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        Map<Long, Integer> columnCounts = new HashMap<>();
        for (BlockPos pos : cluster) {
            minY = Math.min(minY, pos.getY());
            maxY = Math.max(maxY, pos.getY());
            long columnKey = ((long) pos.getX() << 32) ^ (pos.getZ() & 0xFFFFFFFFL);
            columnCounts.merge(columnKey, 1, (a, b) -> a + b);
        }
        if (maxY - minY < 2) {
            return false;
        }
        for (int count : columnCounts.values()) {
            if (count >= 3) {
                return true;
            }
        }
        return false;
    }

    private static void handleOvercharge(ServerPlayer player, BlockState state) {
        UUID id = player.getUUID();
        RegistryAccess access = player.level().registryAccess();
        int level = getEnchantmentLevel(access, OVERCHARGE, player.getMainHandItem());
        if (level <= 0) {
            clearOvercharge(player);
            return;
        }
        Block block = state.getBlock();
        int chain;
        Block last = OVERCHARGE_LAST_BLOCK.get(id);
        if (last != null && last == block) {
            chain = OVERCHARGE_CHAINS.getOrDefault(id, 1) + 1;
        } else {
            chain = 1;
            removeOverchargeBonus(player);
        }
        OVERCHARGE_LAST_BLOCK.put(id, block);
        OVERCHARGE_CHAINS.put(id, chain);
        if (chain <= 1) {
            return;
        }
        int amplifier = Math.min(chain - 1, level * 2);
        OVERCHARGE_TIMERS.put(id, 100);
        AttributeInstance attribute = player.getAttribute(Attributes.BLOCK_BREAK_SPEED);
        if (attribute != null) {
            attribute.removeModifier(OVERCHARGE_MODIFIER);
            attribute.addTransientModifier(new AttributeModifier(OVERCHARGE_MODIFIER,
                    0.2F * amplifier, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    private static void tickOvercharge(ServerPlayer player) {
        Integer remaining = OVERCHARGE_TIMERS.get(player.getUUID());
        if (remaining == null) {
            return;
        }
        if (remaining <= 1) {
            clearOvercharge(player);
            return;
        }
        OVERCHARGE_TIMERS.put(player.getUUID(), remaining - 1);
    }

    private static void clearOvercharge(ServerPlayer player) {
        UUID id = player.getUUID();
        OVERCHARGE_TIMERS.remove(id);
        OVERCHARGE_LAST_BLOCK.remove(id);
        OVERCHARGE_CHAINS.remove(id);
        AttributeInstance attribute = player.getAttribute(Attributes.BLOCK_BREAK_SPEED);
        if (attribute != null) {
            attribute.removeModifier(OVERCHARGE_MODIFIER);
        }
    }

    private static void removeOverchargeBonus(ServerPlayer player) {
        OVERCHARGE_TIMERS.remove(player.getUUID());
        AttributeInstance attribute = player.getAttribute(Attributes.BLOCK_BREAK_SPEED);
        if (attribute != null) {
            attribute.removeModifier(OVERCHARGE_MODIFIER);
        }
    }

    private static OreFamilies oreFamilies(BlockState state) {
        Set<TagKey<Block>> tags = new HashSet<>();
        Set<Block> blocks = new HashSet<>();
        for (TagKey<Block> tag : ORE_TAGS) {
            if (state.is(tag)) {
                tags.add(tag);
            }
        }
        if (state.is(Blocks.NETHER_QUARTZ_ORE)) {
            blocks.add(Blocks.NETHER_QUARTZ_ORE);
        }
        if (state.is(Blocks.ANCIENT_DEBRIS)) {
            blocks.add(Blocks.ANCIENT_DEBRIS);
        }
        return new OreFamilies(tags, blocks);
    }

    private record OreFamilies(Set<TagKey<Block>> tags, Set<Block> blocks) {
        boolean matches(BlockState state) {
            for (TagKey<Block> tag : this.tags) {
                if (state.is(tag)) {
                    return true;
                }
            }
            for (Block block : this.blocks) {
                if (state.is(block)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static void applyPlanter(ServerPlayer player, BlockPos pos, BlockState state) {
        if (state.isAir() || player.level().isClientSide) {
            return;
        }
        Property<?> property = state.getBlock().getStateDefinition().getProperty("age");
        if (!(property instanceof IntegerProperty age)) {
            return;
        }
        int level = getPlanterLevel(player);
        if (level <= 1) {
            return;
        }
        int minValue = Collections.min(age.getPossibleValues());
        if (state.getValue(age) != minValue) {
            return;
        }
        int maxValue = Collections.max(age.getPossibleValues());
        int target = Math.min(level, maxValue);
        if (target <= 1 || !age.getPossibleValues().contains(target)) {
            return;
        }
        ((ServerLevel) player.level()).setBlock(pos, state.setValue(age, target), 3);
    }

    public static int getPlanterLevel(ServerPlayer player) {
        RegistryAccess access = player.level().registryAccess();
        return Math.max(getEnchantmentLevel(access, PLANTER, player.getMainHandItem()),
                getEnchantmentLevel(access, PLANTER, player.getOffhandItem()));
    }

    public static void handleSpikesBlocked(LivingEntity victim, DamageSource source) {
        if (!(victim instanceof ServerPlayer player) || victim.level().isClientSide) {
            return;
        }
        ItemStack shield = player.getUseItem();
        if (!shield.is(Items.SHIELD)) {
            return;
        }
        int level = getEnchantmentLevel(victim.level().registryAccess(), SPIKES, shield);
        if (level <= 0) {
            return;
        }
        if (source.getEntity() == null || !(source.getEntity() instanceof LivingEntity attacker)) {
            return;
        }
        if (attacker == player) {
            return;
        }
        float amount = switch (level) {
            case 2 -> 4.0F;
            case 3 -> 7.0F;
            default -> 2.0F;
        };
        attacker.hurt(player.damageSources().thorns(player), amount);
    }

    public static void handleThornsoulHit(LivingEntity victim, DamageSource source) {
        if (!(victim instanceof ServerPlayer player) || victim.level().isClientSide) {
            return;
        }
        if (!(source.getDirectEntity() instanceof LivingEntity)) {
            return;
        }
        if (source.is(DamageTypeTags.IS_PROJECTILE) || source.is(DamageTypeTags.IS_EXPLOSION)) {
            return;
        }
        int level = getEnchantmentLevel(victim.level().registryAccess(), THORNSOUL,
                player.getItemBySlot(EquipmentSlot.CHEST));
        if (level <= 0) {
            return;
        }
        int ticks = switch (level) {
            case 2 -> 120;
            case 3 -> 160;
            default -> 80;
        };
        THORNSOUL_TIMERS.put(player.getUUID(), ticks);
    }

    public static void handleExecutioner(LivingEntity victim, DamageSource source, float damageTaken) {
        if (victim.level().isClientSide || victim.getHealth() <= 0.0F) {
            return;
        }
        if (!(source.getEntity() instanceof ServerPlayer player) || player == victim) {
            return;
        }
        ItemStack weapon = source.getWeaponItem();
        int level = getEnchantmentLevel(victim.level().registryAccess(), EXECUTIONER, weapon);
        if (level <= 0) {
            return;
        }
        UUID id = victim.getUUID();
        long gameTime = victim.level().getGameTime();
        if (EXECUTIONER_DEBOUNCE.getOrDefault(id, -1L) == gameTime) {
            return;
        }
        float maxHealth = victim.getMaxHealth();
        float before = Math.min(victim.getHealth() + damageTaken, maxHealth);
        float fraction = before / Math.max(maxHealth, 1.0F);
        float bonus;
        if (fraction <= 0.35F) {
            bonus = level * 4.0F;
        } else if (fraction <= 0.50F) {
            bonus = level * 2.5F;
        } else if (fraction <= 0.70F) {
            bonus = level;
        } else {
            return;
        }
        EXECUTIONER_DEBOUNCE.put(id, gameTime);
        victim.hurt(player.damageSources().mobAttack(player), bonus);
    }

    private static int getEnchantmentLevel(RegistryAccess access, ResourceKey<Enchantment> key, ItemStack stack) {
        return access.registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(key)
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack))
                .orElse(0);
    }

    public static void addSwiftnessTrade(RegistryAccess access) {
        Holder<Enchantment> swiftness = access.registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(SWIFTNESS);
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        mutable.set(swiftness, 4);
        book.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
        VillagerTrades.ItemListing listing = (entity, random) -> new MerchantOffer(
                new ItemCost(Items.EMERALD, 32), Optional.empty(), book, 4, 12, 30, 0.05F);
        Int2ObjectMap<VillagerTrades.ItemListing[]> librarian = VillagerTrades.TRADES.get(VillagerProfession.LIBRARIAN);
        if (librarian == null) {
            return;
        }
        VillagerTrades.ItemListing[] master = librarian.get(5);
        if (master == null) {
            return;
        }
        VillagerTrades.ItemListing[] updated = Arrays.copyOf(master, master.length + 1);
        updated[master.length] = listing;
        librarian.put(5, updated);
    }
}