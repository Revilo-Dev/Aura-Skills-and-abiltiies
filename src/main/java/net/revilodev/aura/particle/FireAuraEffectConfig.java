package net.revilodev.aura.particle;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.revilodev.aura.abilities.AbilitiesNetwork;

public final class FireAuraEffectConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.IntValue MINIMUM_RING_POINTS;
    private static final ModConfigSpec.DoubleValue RING_POINTS_PER_BLOCK;
    private static final ModConfigSpec.DoubleValue PARTICLE_SPEED;
    private static final ModConfigSpec.DoubleValue LIGHT_BAND_END;
    private static final ModConfigSpec.DoubleValue MID_BAND_END;
    private static final ModConfigSpec.DoubleValue RED_ACCENT_CHANCE;
    private static final StyleValues LIGHT;
    private static final StyleValues MID;
    private static final StyleValues DARK;
    private static volatile FireAuraEffectSettings settings = FireAuraEffectSettings.DEFAULT;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("fireAura");
        MINIMUM_RING_POINTS = builder.defineInRange("minimumRingPoints", FireAuraEffectSettings.DEFAULT.minimumRingPoints(), 1, 256);
        RING_POINTS_PER_BLOCK = builder.defineInRange("ringPointsPerBlock", FireAuraEffectSettings.DEFAULT.ringPointsPerBlock(), 0.1D, 64.0D);
        PARTICLE_SPEED = builder.defineInRange("particleSpeed", FireAuraEffectSettings.DEFAULT.particleSpeed(), 0.0D, 1.0D);
        LIGHT_BAND_END = builder.defineInRange("lightBandEnd", FireAuraEffectSettings.DEFAULT.lightBandEnd(), 0.01D, 0.98D);
        MID_BAND_END = builder.defineInRange("midBandEnd", FireAuraEffectSettings.DEFAULT.midBandEnd(), 0.02D, 0.99D);
        RED_ACCENT_CHANCE = builder.defineInRange("redAccentChance", FireAuraEffectSettings.DEFAULT.redAccentChance(), 0.0D, 1.0D);
        LIGHT = defineStyle(builder, "light", FireAuraEffectSettings.DEFAULT.light());
        MID = defineStyle(builder, "mid", FireAuraEffectSettings.DEFAULT.mid());
        DARK = defineStyle(builder, "dark", FireAuraEffectSettings.DEFAULT.dark());
        builder.pop();
        SPEC = builder.build();
    }

    private FireAuraEffectConfig() {
    }

    public static FireAuraEffectSettings settings() {
        return settings;
    }

    public static void onConfigLoading(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) refresh();
    }

    public static void onConfigReloading(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() != SPEC) return;
        refresh();
        AbilitiesNetwork.syncFireAuraEffectToAll();
    }

    private static void refresh() {
        double lightBandEnd = LIGHT_BAND_END.get();
        double midBandEnd = Math.max(lightBandEnd, MID_BAND_END.get());
        settings = new FireAuraEffectSettings(
                MINIMUM_RING_POINTS.get(), RING_POINTS_PER_BLOCK.get(), PARTICLE_SPEED.get(),
                lightBandEnd, midBandEnd, RED_ACCENT_CHANCE.get(),
                LIGHT.toStyle(), MID.toStyle(), DARK.toStyle()
        );
    }

    private static StyleValues defineStyle(ModConfigSpec.Builder builder, String name, FireAuraEffectSettings.EmberStyle defaults) {
        builder.push(name);
        StyleValues values = new StyleValues(
                builder.defineInRange("minScale", (double) defaults.minScale(), 0.001D, 2.0D),
                builder.defineInRange("maxScale", (double) defaults.maxScale(), 0.001D, 2.0D),
                builder.defineInRange("minAlpha", (double) defaults.minAlpha(), 0.0D, 1.0D),
                builder.defineInRange("maxAlpha", (double) defaults.maxAlpha(), 0.0D, 1.0D),
                builder.defineInRange("minRed", (double) defaults.minRed(), 0.0D, 1.0D),
                builder.defineInRange("maxRed", (double) defaults.maxRed(), 0.0D, 1.0D),
                builder.defineInRange("minGreen", (double) defaults.minGreen(), 0.0D, 1.0D),
                builder.defineInRange("maxGreen", (double) defaults.maxGreen(), 0.0D, 1.0D),
                builder.defineInRange("minBlue", (double) defaults.minBlue(), 0.0D, 1.0D),
                builder.defineInRange("maxBlue", (double) defaults.maxBlue(), 0.0D, 1.0D)
        );
        builder.pop();
        return values;
    }

    private record StyleValues(ModConfigSpec.DoubleValue minScale, ModConfigSpec.DoubleValue maxScale,
                               ModConfigSpec.DoubleValue minAlpha, ModConfigSpec.DoubleValue maxAlpha,
                               ModConfigSpec.DoubleValue minRed, ModConfigSpec.DoubleValue maxRed,
                               ModConfigSpec.DoubleValue minGreen, ModConfigSpec.DoubleValue maxGreen,
                               ModConfigSpec.DoubleValue minBlue, ModConfigSpec.DoubleValue maxBlue) {
        private FireAuraEffectSettings.EmberStyle toStyle() {
            float scaleMin = minScale.get().floatValue();
            float scaleMax = Math.max(scaleMin, maxScale.get().floatValue());
            float alphaMin = minAlpha.get().floatValue();
            float alphaMax = Math.max(alphaMin, maxAlpha.get().floatValue());
            return new FireAuraEffectSettings.EmberStyle(
                    scaleMin, scaleMax, alphaMin, alphaMax,
                    minRed.get().floatValue(), Math.max(minRed.get().floatValue(), maxRed.get().floatValue()),
                    minGreen.get().floatValue(), Math.max(minGreen.get().floatValue(), maxGreen.get().floatValue()),
                    minBlue.get().floatValue(), Math.max(minBlue.get().floatValue(), maxBlue.get().floatValue())
            );
        }
    }
}
