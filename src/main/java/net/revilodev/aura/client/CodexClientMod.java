package net.revilodev.aura.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.SnowflakeParticle;
import net.neoforged.neoforge.common.NeoForge;
import net.revilodev.aura.client.abilities.AbilityHudOverlay;
import net.revilodev.aura.client.abilities.AbilityKeybinds;
import net.revilodev.aura.client.abilities.BurstCubeProjectileRenderer;
import net.revilodev.aura.client.particle.IcicleParticle;
import net.revilodev.aura.client.particle.FireAuraEmberParticle;
import net.revilodev.aura.CodexMod;
import net.revilodev.aura.client.skills.SkillsPanelClient;
import net.revilodev.aura.client.toast.LevelUpToast;
import net.revilodev.aura.entity.ModEntities;
import net.revilodev.aura.particle.ModParticles;

@Mod(value = CodexMod.MOD_ID, dist = Dist.CLIENT)
public final class CodexClientMod {
    public CodexClientMod(IEventBus modBus) {
        // client config and screens
        AuraClientConfig.load();
        SkillsPanelClient.register();

        // keybinds and render hooks
        AbilityKeybinds.register(modBus);
        SkillsBookKeybinds.register(modBus);
        modBus.addListener(CodexClientMod::onRegisterRenderers);
        modBus.addListener(CodexClientMod::onRegisterParticles);
        NeoForge.EVENT_BUS.addListener(AbilityHudOverlay::render);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, RenderGuiEvent.Post.class, LevelUpToast::render);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // custom projectile renderer
        event.registerEntityRenderer(ModEntities.BURST_CUBE.get(), BurstCubeProjectileRenderer::new);
    }

    private static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.FIRE.get(), FlameParticle.Provider::new);
        event.registerSpriteSet(ModParticles.SOULFIRE.get(), FlameParticle.Provider::new);
        event.registerSpriteSet(ModParticles.POISON.get(), FlameParticle.Provider::new);
        event.registerSpriteSet(ModParticles.TOXIN.get(), FlameParticle.Provider::new);
        event.registerSpriteSet(ModParticles.LIGHTNING_STRIKE.get(), FlameParticle.Provider::new);
        event.registerSpriteSet(ModParticles.PLASMA_STRIKE.get(), FlameParticle.Provider::new);
        event.registerSpriteSet(ModParticles.PLASMA_BLAST.get(), FlameParticle.Provider::new);
        event.registerSpriteSet(ModParticles.FIRE_EMBER.get(), SnowflakeParticle.Provider::new);
        event.registerSpriteSet(ModParticles.FIRE_AURA_EMBER_LIGHT.get(), FireAuraEmberParticle.LightProvider::new);
        event.registerSpriteSet(ModParticles.FIRE_AURA_EMBER_MID.get(), FireAuraEmberParticle.MidProvider::new);
        event.registerSpriteSet(ModParticles.FIRE_AURA_EMBER_DARK.get(), FireAuraEmberParticle.DarkProvider::new);
        registerAuraEmberPalette(event, ModParticles.SOUL_FIRE_AURA_EMBER_LIGHT.get(), ModParticles.SOUL_FIRE_AURA_EMBER_MID.get(), ModParticles.SOUL_FIRE_AURA_EMBER_DARK.get(), FireAuraEmberParticle.Palette.SOUL_FIRE);
        registerAuraEmberPalette(event, ModParticles.ICE_AURA_EMBER_LIGHT.get(), ModParticles.ICE_AURA_EMBER_MID.get(), ModParticles.ICE_AURA_EMBER_DARK.get(), FireAuraEmberParticle.Palette.ICE);
        registerAuraEmberPalette(event, ModParticles.FROST_AURA_EMBER_LIGHT.get(), ModParticles.FROST_AURA_EMBER_MID.get(), ModParticles.FROST_AURA_EMBER_DARK.get(), FireAuraEmberParticle.Palette.FROST);
        registerAuraEmberPalette(event, ModParticles.LIGHTNING_AURA_EMBER_LIGHT.get(), ModParticles.LIGHTNING_AURA_EMBER_MID.get(), ModParticles.LIGHTNING_AURA_EMBER_DARK.get(), FireAuraEmberParticle.Palette.LIGHTNING);
        registerAuraEmberPalette(event, ModParticles.PLASMA_AURA_EMBER_LIGHT.get(), ModParticles.PLASMA_AURA_EMBER_MID.get(), ModParticles.PLASMA_AURA_EMBER_DARK.get(), FireAuraEmberParticle.Palette.PLASMA);
        registerAuraEmberPalette(event, ModParticles.POISON_AURA_EMBER_LIGHT.get(), ModParticles.POISON_AURA_EMBER_MID.get(), ModParticles.POISON_AURA_EMBER_DARK.get(), FireAuraEmberParticle.Palette.POISON);
        registerAuraEmberPalette(event, ModParticles.TOXIN_AURA_EMBER_LIGHT.get(), ModParticles.TOXIN_AURA_EMBER_MID.get(), ModParticles.TOXIN_AURA_EMBER_DARK.get(), FireAuraEmberParticle.Palette.TOXIN);
        event.registerSpriteSet(ModParticles.SOUL_FIRE_EMBER.get(), SnowflakeParticle.Provider::new);
        event.registerSpriteSet(ModParticles.ICICLE.get(), IcicleParticle.Provider::new);
    }

    private static void registerAuraEmberPalette(RegisterParticleProvidersEvent event,
                                                  net.minecraft.core.particles.SimpleParticleType light,
                                                  net.minecraft.core.particles.SimpleParticleType mid,
                                                  net.minecraft.core.particles.SimpleParticleType dark,
                                                  FireAuraEmberParticle.Palette palette) {
        event.registerSpriteSet(light, sprites -> new FireAuraEmberParticle.Provider(sprites, palette, FireAuraEmberParticle.Band.LIGHT));
        event.registerSpriteSet(mid, sprites -> new FireAuraEmberParticle.Provider(sprites, palette, FireAuraEmberParticle.Band.MID));
        event.registerSpriteSet(dark, sprites -> new FireAuraEmberParticle.Provider(sprites, palette, FireAuraEmberParticle.Band.DARK));
    }
}
