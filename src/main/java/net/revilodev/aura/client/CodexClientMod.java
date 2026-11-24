package net.revilodev.aura.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.SnowflakeParticle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.NeoForge;
import net.revilodev.aura.client.abilities.AbilityHudOverlay;
import net.revilodev.aura.client.abilities.AbilityKeybinds;
import net.revilodev.aura.client.abilities.BurstCubeProjectileRenderer;
import net.revilodev.aura.client.abilities.SmallLightningStrikeRenderer;
import net.revilodev.aura.client.particle.IcicleParticle;
import net.revilodev.aura.client.particle.AegisOrbParticle;
import net.revilodev.aura.client.particle.AnimatedSparkParticle;
import net.revilodev.aura.client.particle.FireAuraEmberParticle;
import net.revilodev.aura.CodexMod;
import net.revilodev.aura.client.skills.SkillsPanelClient;
import net.revilodev.aura.client.toast.LevelUpToast;
import net.revilodev.aura.effect.CodexMobEffects;
import net.revilodev.aura.entity.ModEntities;
import net.revilodev.aura.particle.ModParticles;

@Mod(value = CodexMod.MOD_ID, dist = Dist.CLIENT)
public final class CodexClientMod {
    private static final ResourceLocation ELECTROCUTED_LIGHTNING_ICON =
            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/abilities/lightning.png");
    private static final ResourceLocation ELECTROCUTED_PLASMA_ICON =
            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "textures/gui/abilities/ultimate-plasma.png");

    public CodexClientMod(IEventBus modBus) {
        // client config and screens
        AuraClientConfig.load();
        SkillsPanelClient.register();

        // keybinds and render hooks
        AbilityKeybinds.register(modBus);
        SkillsBookKeybinds.register(modBus);
        modBus.addListener(CodexClientMod::onRegisterRenderers);
        modBus.addListener(CodexClientMod::onRegisterParticles);
        modBus.addListener(CodexClientMod::onRegisterClientExtensions);
        NeoForge.EVENT_BUS.addListener(AbilityHudOverlay::render);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, RenderGuiEvent.Post.class, LevelUpToast::render);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // custom projectile renderer
        event.registerEntityRenderer(ModEntities.BURST_CUBE.get(), BurstCubeProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.SMALL_LIGHTNING_STRIKE.get(), SmallLightningStrikeRenderer::new);
    }

    private static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.FIRE.get(), FlameParticle.Provider::new);
        event.registerSpriteSet(ModParticles.SOULFIRE.get(), FlameParticle.Provider::new);
        event.registerSpriteSet(ModParticles.POISON.get(), FlameParticle.Provider::new);
        event.registerSpriteSet(ModParticles.TOXIN.get(), FlameParticle.Provider::new);
        event.registerSpriteSet(ModParticles.LIGHTNING_STRIKE.get(), AnimatedSparkParticle.Provider::new);
        event.registerSpriteSet(ModParticles.PLASMA_STRIKE.get(), AnimatedSparkParticle.Provider::new);
        event.registerSpriteSet(ModParticles.PLASMA_BLAST.get(), AnimatedSparkParticle.Provider::new);
        event.registerSpriteSet(ModParticles.FIRE_EMBER.get(), AnimatedSparkParticle.Provider::new);
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
        event.registerSpriteSet(ModParticles.SOUL_FIRE_EMBER.get(), AnimatedSparkParticle.Provider::new);
        event.registerSpriteSet(ModParticles.ICICLE.get(), IcicleParticle.Provider::new);
        event.registerSpriteSet(ModParticles.AEGIS.get(), AegisOrbParticle.Provider::new);
        event.registerSpriteSet(ModParticles.AEGIS_MAX.get(), AegisOrbParticle.Provider::new);
    }

    private static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerMobEffect(new IClientMobEffectExtensions() {
            @Override
            public boolean renderGuiIcon(MobEffectInstance instance, Gui gui, GuiGraphics graphics, int x, int y, float z, float alpha) {
                graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
                graphics.blit(electrocutedIcon(instance), x + 3, y + 3, 0, 0, 18, 18, 18, 18);
                graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                return true;
            }

            @Override
            public boolean renderInventoryIcon(MobEffectInstance instance, EffectRenderingInventoryScreen<?> screen, GuiGraphics graphics, int x, int y, int blitOffset) {
                graphics.blit(electrocutedIcon(instance), x, y + 7, 0, 0, 18, 18, 18, 18);
                return true;
            }
        }, CodexMobEffects.ELECTROCUTED);
    }

    private static ResourceLocation electrocutedIcon(MobEffectInstance instance) {
        return instance.getAmplifier() > 0 ? ELECTROCUTED_PLASMA_ICON : ELECTROCUTED_LIGHTNING_ICON;
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
