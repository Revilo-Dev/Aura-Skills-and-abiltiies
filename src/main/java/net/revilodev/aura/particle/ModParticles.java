package net.revilodev.aura.particle;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.revilodev.aura.CodexMod;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> REGISTER = DeferredRegister.create(Registries.PARTICLE_TYPE, CodexMod.MOD_ID);
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FIRE = REGISTER.register("fire", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SOULFIRE = REGISTER.register("soulfire", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> POISON = REGISTER.register("poison", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TOXIN = REGISTER.register("toxin", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> LIGHTNING_STRIKE = REGISTER.register("lightning_strike", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PLASMA_STRIKE = REGISTER.register("plasma_strike", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PLASMA_BLAST = REGISTER.register("plasma_blast", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FIRE_EMBER = REGISTER.register("fire_ember", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FIRE_AURA_EMBER_LIGHT = REGISTER.register("fire_aura_ember_light", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FIRE_AURA_EMBER_MID = REGISTER.register("fire_aura_ember_mid", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FIRE_AURA_EMBER_DARK = REGISTER.register("fire_aura_ember_dark", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SOUL_FIRE_AURA_EMBER_LIGHT = REGISTER.register("soul_fire_aura_ember_light", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SOUL_FIRE_AURA_EMBER_MID = REGISTER.register("soul_fire_aura_ember_mid", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SOUL_FIRE_AURA_EMBER_DARK = REGISTER.register("soul_fire_aura_ember_dark", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ICE_AURA_EMBER_LIGHT = REGISTER.register("ice_aura_ember_light", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ICE_AURA_EMBER_MID = REGISTER.register("ice_aura_ember_mid", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ICE_AURA_EMBER_DARK = REGISTER.register("ice_aura_ember_dark", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FROST_AURA_EMBER_LIGHT = REGISTER.register("frost_aura_ember_light", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FROST_AURA_EMBER_MID = REGISTER.register("frost_aura_ember_mid", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FROST_AURA_EMBER_DARK = REGISTER.register("frost_aura_ember_dark", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> LIGHTNING_AURA_EMBER_LIGHT = REGISTER.register("lightning_aura_ember_light", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> LIGHTNING_AURA_EMBER_MID = REGISTER.register("lightning_aura_ember_mid", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> LIGHTNING_AURA_EMBER_DARK = REGISTER.register("lightning_aura_ember_dark", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PLASMA_AURA_EMBER_LIGHT = REGISTER.register("plasma_aura_ember_light", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PLASMA_AURA_EMBER_MID = REGISTER.register("plasma_aura_ember_mid", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PLASMA_AURA_EMBER_DARK = REGISTER.register("plasma_aura_ember_dark", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> POISON_AURA_EMBER_LIGHT = REGISTER.register("poison_aura_ember_light", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> POISON_AURA_EMBER_MID = REGISTER.register("poison_aura_ember_mid", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> POISON_AURA_EMBER_DARK = REGISTER.register("poison_aura_ember_dark", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TOXIN_AURA_EMBER_LIGHT = REGISTER.register("toxin_aura_ember_light", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TOXIN_AURA_EMBER_MID = REGISTER.register("toxin_aura_ember_mid", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TOXIN_AURA_EMBER_DARK = REGISTER.register("toxin_aura_ember_dark", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SOUL_FIRE_EMBER = REGISTER.register("soul_fire_ember", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ICICLE = REGISTER.register("icicle", () -> new SimpleParticleType(false));

    private ModParticles() {}

    public static void register(IEventBus modBus) {
        REGISTER.register(modBus);
    }
}
