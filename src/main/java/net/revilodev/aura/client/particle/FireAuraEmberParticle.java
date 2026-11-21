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
                                  SpriteSet sprites, Band band) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        FireAuraEffectSettings currentSettings = settings;
        FireAuraEffectSettings.EmberStyle style = band.style(currentSettings);
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
        if (this.random.nextFloat() < currentSettings.redAccentChance()) {
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
        renderVertex(buffer, rotation, x, y, z, -size, -size, this.getU1(), this.getV1(), alpha, light);
        renderVertex(buffer, rotation, x, y, z, -size, size, this.getU1(), this.getV0(), alpha, light);
        renderVertex(buffer, rotation, x, y, z, size, size, this.getU0(), this.getV0(), alpha, light);
        renderVertex(buffer, rotation, x, y, z, size, -size, this.getU0(), this.getV1(), alpha, light);
    }

    private void renderVertex(VertexConsumer buffer, Quaternionf rotation, float x, float y, float z,
                              float xOffset, float yOffset, float u, float v, float alpha, int light) {
        Vector3f position = new Vector3f(xOffset, yOffset, 0.0F).rotate(rotation).add(x, y, z);
        buffer.addVertex(position.x(), position.y(), position.z())
                .setUv(u, v)
                .setColor(this.rCol, this.gCol, this.bCol, alpha)
                .setLight(light);
    }

    private abstract static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final Band band;

        private Provider(SpriteSet sprites, Band band) {
            this.sprites = sprites;
            this.band = band;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new FireAuraEmberParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites, this.band);
        }
    }

    public static final class LightProvider extends Provider {
        public LightProvider(SpriteSet sprites) {
            super(sprites, Band.LIGHT);
        }
    }

    public static final class MidProvider extends Provider {
        public MidProvider(SpriteSet sprites) {
            super(sprites, Band.MID);
        }
    }

    public static final class DarkProvider extends Provider {
        public DarkProvider(SpriteSet sprites) {
            super(sprites, Band.DARK);
        }
    }

    private enum Band {
        LIGHT,
        MID,
        DARK;

        private FireAuraEffectSettings.EmberStyle style(FireAuraEffectSettings settings) {
            return switch (this) {
                case LIGHT -> settings.light();
                case MID -> settings.mid();
                case DARK -> settings.dark();
            };
        }
    }
}
