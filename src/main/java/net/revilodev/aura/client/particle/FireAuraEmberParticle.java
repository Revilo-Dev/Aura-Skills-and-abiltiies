package net.revilodev.aura.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.revilodev.aura.particle.FireAuraEffectSettings;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class FireAuraEmberParticle extends TextureSheetParticle {
    private static volatile FireAuraEffectSettings settings = FireAuraEffectSettings.DEFAULT;
    private final float initialAlpha;

    private FireAuraEmberParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed,
                                  SpriteSet sprites, Palette palette, Band band) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        FireAuraEffectSettings currentSettings = settings;
        FireAuraEffectSettings.EmberStyle style = palette.style(band, currentSettings);
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.friction = 0.88F;
        this.quadSize = randomBetween(style.minScale(), style.maxScale());
        this.lifetime = 9 + this.random.nextInt(7);
        this.initialAlpha = randomBetween(style.minAlpha(), style.maxAlpha());
        float red = randomBetween(style.minRed(), style.maxRed());
        float green = randomBetween(style.minGreen(), style.maxGreen());
        float blue = randomBetween(style.minBlue(), style.maxBlue());
        if (palette == Palette.FIRE && this.random.nextFloat() < currentSettings.redAccentChance()) {
            red = 1.0F;
            green *= 0.35F;
            blue *= 0.15F;
        }
        this.setColor(red, green, blue);
        this.setAlpha(this.initialAlpha);
        this.pickSprite(sprites);
    }

    public static void applySettings(FireAuraEffectSettings settings) {
        FireAuraEmberParticle.settings = settings == null ? FireAuraEffectSettings.DEFAULT : settings;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        Quaternionf rotation = new Quaternionf();
        this.getFacingCameraMode().setRotation(rotation, camera, partialTick);
        if (this.roll != 0.0F) rotation.rotateZ(Mth.lerp(partialTick, this.oRoll, this.roll));

        Vec3 cameraPosition = camera.getPosition();
        float x = (float) (Mth.lerp(partialTick, this.xo, this.x) - cameraPosition.x());
        float y = (float) (Mth.lerp(partialTick, this.yo, this.y) - cameraPosition.y());
        float z = (float) (Mth.lerp(partialTick, this.zo, this.z) - cameraPosition.z());
        int light = this.getLightColor(partialTick);
        float coreSize = this.getQuadSize(partialTick);
        renderQuad(buffer, rotation, x, y, z, coreSize * 2.3F, this.alpha * 0.16F, light);
        renderQuad(buffer, rotation, x, y, z, coreSize, this.alpha, light);
    }

    @Override
    public void tick() {
        double previousX = this.x;
        double previousZ = this.z;
        double horizontalSpeed = Math.sqrt(this.xd * this.xd + this.zd * this.zd);
        super.tick();
        if (this.removed) return;

        double movedHorizontally = Math.sqrt((this.x - previousX) * (this.x - previousX) + (this.z - previousZ) * (this.z - previousZ));
        if (horizontalSpeed > 0.003D && movedHorizontally < horizontalSpeed * 0.35D) {
            this.remove();
            return;
        }
        float lifeProgress = this.age / (float) this.lifetime;
        this.setAlpha(this.initialAlpha * (1.0F - lifeProgress * lifeProgress));
    }

    private float randomBetween(float min, float max) {
        return min + this.random.nextFloat() * (max - min);
    }

    private void renderQuad(VertexConsumer buffer, Quaternionf rotation, float x, float y, float z, float size, float alpha, int light) {
        renderVertex(buffer, rotation, x, y, z, size, -size, this.getU1(), this.getV1(), alpha, light);
        renderVertex(buffer, rotation, x, y, z, size, size, this.getU1(), this.getV0(), alpha, light);
        renderVertex(buffer, rotation, x, y, z, -size, size, this.getU0(), this.getV0(), alpha, light);
        renderVertex(buffer, rotation, x, y, z, -size, -size, this.getU0(), this.getV1(), alpha, light);
    }

    private void renderVertex(VertexConsumer buffer, Quaternionf rotation, float x, float y, float z,
                              float xOffset, float yOffset, float u, float v, float alpha, int light) {
        Vector3f position = new Vector3f(xOffset, yOffset, 0.0F).rotate(rotation).add(x, y, z);
        buffer.addVertex(position.x(), position.y(), position.z())
                .setUv(u, v)
                .setColor(this.rCol, this.gCol, this.bCol, alpha)
                .setLight(light);
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final Palette palette;
        private final Band band;

        public Provider(SpriteSet sprites, Palette palette, Band band) {
            this.sprites = sprites;
            this.palette = palette;
            this.band = band;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new FireAuraEmberParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites, this.palette, this.band);
        }
    }

    public static final class LightProvider implements ParticleProvider<SimpleParticleType> {
        private final Provider delegate;

        public LightProvider(SpriteSet sprites) {
            this.delegate = new Provider(sprites, Palette.FIRE, Band.LIGHT);
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return delegate.createParticle(type, level, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }

    public static final class MidProvider implements ParticleProvider<SimpleParticleType> {
        private final Provider delegate;

        public MidProvider(SpriteSet sprites) {
            this.delegate = new Provider(sprites, Palette.FIRE, Band.MID);
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return delegate.createParticle(type, level, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }

    public static final class DarkProvider implements ParticleProvider<SimpleParticleType> {
        private final Provider delegate;

        public DarkProvider(SpriteSet sprites) {
            this.delegate = new Provider(sprites, Palette.FIRE, Band.DARK);
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return delegate.createParticle(type, level, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }

    public enum Band {
        LIGHT,
        MID,
        DARK;

        private FireAuraEffectSettings.EmberStyle fireStyle(FireAuraEffectSettings settings) {
            return switch (this) {
                case LIGHT -> settings.light();
                case MID -> settings.mid();
                case DARK -> settings.dark();
            };
        }
    }

    public enum Palette {
        FIRE(null, null, null),
        SOUL_FIRE(new ColorRange(0.60F, 0.90F, 0.80F, 1.0F, 0.95F, 1.0F), new ColorRange(0.30F, 0.65F, 0.55F, 0.90F, 0.82F, 1.0F), new ColorRange(0.12F, 0.38F, 0.32F, 0.70F, 0.62F, 0.95F)),
        ICE(new ColorRange(0.88F, 1.0F, 0.94F, 1.0F, 1.0F, 1.0F), new ColorRange(0.55F, 0.82F, 0.75F, 0.95F, 0.92F, 1.0F), new ColorRange(0.22F, 0.54F, 0.50F, 0.82F, 0.72F, 0.96F)),
        FROST(new ColorRange(0.72F, 0.94F, 0.90F, 1.0F, 1.0F, 1.0F), new ColorRange(0.35F, 0.68F, 0.65F, 0.92F, 0.88F, 1.0F), new ColorRange(0.12F, 0.38F, 0.38F, 0.72F, 0.72F, 0.96F)),
        LIGHTNING(new ColorRange(1.0F, 1.0F, 0.70F, 0.96F, 0.08F, 0.26F), new ColorRange(0.92F, 1.0F, 0.38F, 0.70F, 0.02F, 0.12F), new ColorRange(0.68F, 0.92F, 0.14F, 0.42F, 0.0F, 0.05F)),
        PLASMA(new ColorRange(0.82F, 1.0F, 0.34F, 0.62F, 0.85F, 1.0F), new ColorRange(0.54F, 0.86F, 0.10F, 0.30F, 0.64F, 1.0F), new ColorRange(0.28F, 0.58F, 0.03F, 0.14F, 0.38F, 0.82F)),
        POISON(new ColorRange(0.34F, 0.62F, 0.82F, 1.0F, 0.10F, 0.28F), new ColorRange(0.16F, 0.42F, 0.52F, 0.80F, 0.04F, 0.16F), new ColorRange(0.04F, 0.20F, 0.24F, 0.54F, 0.01F, 0.08F)),
        TOXIN(new ColorRange(0.74F, 1.0F, 0.22F, 0.50F, 0.78F, 1.0F), new ColorRange(0.46F, 0.78F, 0.06F, 0.24F, 0.54F, 0.92F), new ColorRange(0.22F, 0.48F, 0.02F, 0.12F, 0.28F, 0.68F));

        private final ColorRange light;
        private final ColorRange mid;
        private final ColorRange dark;

        Palette(ColorRange light, ColorRange mid, ColorRange dark) {
            this.light = light;
            this.mid = mid;
            this.dark = dark;
        }

        private FireAuraEffectSettings.EmberStyle style(Band band, FireAuraEffectSettings settings) {
            FireAuraEffectSettings.EmberStyle shared = band.fireStyle(settings);
            if (this == FIRE) return shared;
            ColorRange color = switch (band) {
                case LIGHT -> light;
                case MID -> mid;
                case DARK -> dark;
            };
            return new FireAuraEffectSettings.EmberStyle(shared.minScale(), shared.maxScale(), shared.minAlpha(), shared.maxAlpha(),
                    color.minRed(), color.maxRed(), color.minGreen(), color.maxGreen(), color.minBlue(), color.maxBlue());
        }
    }

    private record ColorRange(float minRed, float maxRed, float minGreen, float maxGreen, float minBlue, float maxBlue) {
    }
}
