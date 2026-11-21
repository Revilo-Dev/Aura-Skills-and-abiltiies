package net.revilodev.aura.abilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.revilodev.aura.ClientRuntimeBridge;
import net.revilodev.aura.CodexMod;
import net.revilodev.aura.abilities.event.AbilitySwitchEvent;
import net.revilodev.aura.attributes.CodexAttributes;
import net.revilodev.aura.abilities.logic.AbilityLogic;
import net.revilodev.aura.abilities.logic.AbilitySyncEvents;
import net.revilodev.aura.particle.FireAuraEffectConfig;
import net.revilodev.aura.particle.FireAuraEffectSettings;
import net.neoforged.neoforge.common.NeoForge;

// abilities network
public final class AbilitiesNetwork {
    private static final String VERSION = "1";
    private static boolean REGISTERED = false;

    private AbilitiesNetwork() {}

    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        if (REGISTERED) return;
        REGISTERED = true;

        PayloadRegistrar registrar = event.registrar(CodexMod.MOD_ID).versioned(VERSION);
        registrar.playToClient(AbilitiesSyncPayload.TYPE, AbilitiesSyncPayload.STREAM_CODEC, AbilitiesNetwork::handleSync);
        registrar.playToClient(FireAuraEffectSyncPayload.TYPE, FireAuraEffectSyncPayload.STREAM_CODEC, AbilitiesNetwork::handleFireAuraEffectSync);
        registrar.playToServer(AbilityActionPayload.TYPE, AbilityActionPayload.STREAM_CODEC, AbilitiesNetwork::handleAction);
        registrar.playToServer(AbilityUsePayload.TYPE, AbilityUsePayload.STREAM_CODEC, AbilitiesNetwork::handleUse);
    }

    public static void syncTo(ServerPlayer player) {
        PlayerAbilities abilities = player.getData(AbilitiesAttachments.PLAYER_ABILITIES.get());
        PacketDistributor.sendToPlayer(player, new AbilitiesSyncPayload(abilities.serializeNBT(player.level().registryAccess())));
        PacketDistributor.sendToPlayer(player, new FireAuraEffectSyncPayload(FireAuraEffectConfig.settings()));
    }

    public static void syncFireAuraEffectToAll() {
        PacketDistributor.sendToAllPlayers(new FireAuraEffectSyncPayload(FireAuraEffectConfig.settings()));
    }

    private static void handleSync(AbilitiesSyncPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player() != null ? ctx.player() : ClientRuntimeBridge.getClientPlayer();
            if (player == null) return;
            PlayerAbilities abilities = player.getData(AbilitiesAttachments.PLAYER_ABILITIES.get());
            CompoundTag tag = payload.data();
            if (tag == null) tag = new CompoundTag();
            abilities.deserializeNBT(player.level().registryAccess(), tag);
        });
    }

    private static void handleFireAuraEffectSync(FireAuraEffectSyncPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientRuntimeBridge.applyFireAuraEffectSettings(payload.settings()));
    }

    private static void handleAction(AbilityActionPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (CodexAttributes.isAbilitySkillEditLocked(player)) return;
            PlayerAbilities abilities = player.getData(AbilitiesAttachments.PLAYER_ABILITIES.get());
            AbilityId id = AbilityId.byOrdinal(payload.abilityOrdinal());
            boolean changed = switch (payload.action()) {
                case 0 -> !AbilityConfig.affinityLocked(abilities, id) && abilities.tryUpgrade(id, player.isCreative());
                case 1 -> abilities.tryDowngrade(id, player.isCreative());
                case 2 -> !AbilityConfig.affinityLocked(abilities, id) && trySwitchSpecialization(player, abilities, id);
                default -> false;
            };
            if (!changed) return;
            AbilitySyncEvents.markDirty(player);
        });
    }

    private static boolean trySwitchSpecialization(ServerPlayer player, PlayerAbilities abilities, AbilityId target) {
        if (player == null || abilities == null || target == null || !target.isSpecialization()) return false;
        if (AbilityConfig.affinityLocked(abilities, target)) return false;
        if (AbilityLogic.effectiveCoreRank(player, abilities, target) <= 0) return false;
        if (!player.isCreative() && AbilityConfig.switchCooldownsEnabled() && abilities.switchCooldownTicks(target.core()) > 0) return false;

        AbilityId current = abilities.selectedSpecialization(target.element());
        if (current == target) return false;
        if (!player.isCreative() && abilities.cooldownTicks(target) > 0) return false;
        if (!player.isCreative() && current != null && abilities.cooldownTicks(current) > 0) return false;

        AbilitySwitchEvent.Pre pre = new AbilitySwitchEvent.Pre(player, target.element(), current, target);
        if (NeoForge.EVENT_BUS.post(pre).isCanceled()) return false;

        boolean changed = abilities.selectSpecialization(target);
        if (changed) {
            abilities.markSpecializationSwitched(current, target);
            NeoForge.EVENT_BUS.post(new AbilitySwitchEvent.Post(player, target.element(), current, target));
        }
        return changed;
    }

    private static void handleUse(AbilityUsePayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            AbilityId id = AbilityId.byOrdinal(payload.abilityOrdinal());
            if (AbilityLogic.tryActivate(player, id)) {
                AbilitySyncEvents.markDirty(player);
            }
        });
    }

    public record AbilitiesSyncPayload(CompoundTag data) implements CustomPacketPayload {
        public static final Type<AbilitiesSyncPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "abilities_sync"));

        public static final StreamCodec<RegistryFriendlyByteBuf, AbilitiesSyncPayload> STREAM_CODEC =
                StreamCodec.of(
                        (buf, msg) -> buf.writeNbt(msg.data),
                        buf -> {
                            CompoundTag tag = buf.readNbt();
                            return new AbilitiesSyncPayload(tag == null ? new CompoundTag() : tag);
                        }
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record AbilityActionPayload(int action, int abilityOrdinal) implements CustomPacketPayload {
        public static final Type<AbilityActionPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "ability_action"));

        public static final StreamCodec<RegistryFriendlyByteBuf, AbilityActionPayload> STREAM_CODEC =
                StreamCodec.of(
                        (buf, msg) -> {
                            buf.writeVarInt(msg.action);
                            buf.writeVarInt(msg.abilityOrdinal);
                        },
                        buf -> new AbilityActionPayload(buf.readVarInt(), buf.readVarInt())
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record FireAuraEffectSyncPayload(FireAuraEffectSettings settings) implements CustomPacketPayload {
        public static final Type<FireAuraEffectSyncPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "fire_aura_effect_sync"));

        public static final StreamCodec<RegistryFriendlyByteBuf, FireAuraEffectSyncPayload> STREAM_CODEC =
                StreamCodec.of(
                        (buf, msg) -> writeSettings(buf, msg.settings()),
                        buf -> new FireAuraEffectSyncPayload(readSettings(buf))
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        private static void writeSettings(RegistryFriendlyByteBuf buf, FireAuraEffectSettings settings) {
            buf.writeVarInt(settings.minimumRingPoints());
            buf.writeDouble(settings.ringPointsPerBlock());
            buf.writeDouble(settings.particleSpeed());
            buf.writeDouble(settings.lightBandEnd());
            buf.writeDouble(settings.midBandEnd());
            buf.writeDouble(settings.redAccentChance());
            writeStyle(buf, settings.light());
            writeStyle(buf, settings.mid());
            writeStyle(buf, settings.dark());
        }

        private static FireAuraEffectSettings readSettings(RegistryFriendlyByteBuf buf) {
            int minimumRingPoints = buf.readVarInt();
            double ringPointsPerBlock = buf.readDouble();
            double particleSpeed = buf.readDouble();
            double lightBandEnd = buf.readDouble();
            double midBandEnd = buf.readDouble();
            double redAccentChance = buf.readDouble();
            return new FireAuraEffectSettings(minimumRingPoints, ringPointsPerBlock, particleSpeed, lightBandEnd, midBandEnd,
                    redAccentChance, readStyle(buf), readStyle(buf), readStyle(buf));
        }

        private static void writeStyle(RegistryFriendlyByteBuf buf, FireAuraEffectSettings.EmberStyle style) {
            buf.writeFloat(style.minScale());
            buf.writeFloat(style.maxScale());
            buf.writeFloat(style.minAlpha());
            buf.writeFloat(style.maxAlpha());
            buf.writeFloat(style.minRed());
            buf.writeFloat(style.maxRed());
            buf.writeFloat(style.minGreen());
            buf.writeFloat(style.maxGreen());
            buf.writeFloat(style.minBlue());
            buf.writeFloat(style.maxBlue());
        }

        private static FireAuraEffectSettings.EmberStyle readStyle(RegistryFriendlyByteBuf buf) {
            return new FireAuraEffectSettings.EmberStyle(
                    buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(),
                    buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat()
            );
        }
    }

    public record AbilityUsePayload(int abilityOrdinal) implements CustomPacketPayload {
        public static final Type<AbilityUsePayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "ability_use"));

        public static final StreamCodec<RegistryFriendlyByteBuf, AbilityUsePayload> STREAM_CODEC =
                StreamCodec.of((buf, msg) -> buf.writeVarInt(msg.abilityOrdinal), buf -> new AbilityUsePayload(buf.readVarInt()));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

}
