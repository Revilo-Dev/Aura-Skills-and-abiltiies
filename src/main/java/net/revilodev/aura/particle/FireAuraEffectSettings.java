package net.revilodev.aura.particle;

public record FireAuraEffectSettings(int minimumRingPoints, double ringPointsPerBlock, double particleSpeed,
                                     double lightBandEnd, double midBandEnd, double redAccentChance,
                                     EmberStyle light, EmberStyle mid, EmberStyle dark) {
    public static final FireAuraEffectSettings DEFAULT = new FireAuraEffectSettings(
            36, 16.0D, 0.018D, 0.34D, 0.67D, 0.24D,
            new EmberStyle(0.035F, 0.080F, 0.70F, 0.95F, 1.0F, 1.0F, 0.48F, 0.72F, 0.04F, 0.16F),
            new EmberStyle(0.030F, 0.070F, 0.58F, 0.82F, 0.94F, 1.0F, 0.20F, 0.46F, 0.01F, 0.08F),
            new EmberStyle(0.025F, 0.060F, 0.36F, 0.62F, 0.62F, 0.92F, 0.04F, 0.20F, 0.0F, 0.04F)
    );

    public record EmberStyle(float minScale, float maxScale, float minAlpha, float maxAlpha,
                             float minRed, float maxRed, float minGreen, float maxGreen,
                             float minBlue, float maxBlue) {
    }
}
