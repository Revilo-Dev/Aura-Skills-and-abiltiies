package net.revilodev.aura.entity.effect;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.revilodev.aura.entity.ModEntities;
import net.revilodev.aura.particle.ModParticles;

public final class SmallLightningStrikeEntity extends Entity {
    private static final EntityDataAccessor<Boolean> PLASMA = SynchedEntityData.defineId(SmallLightningStrikeEntity.class, EntityDataSerializers.BOOLEAN);

    public SmallLightningStrikeEntity(EntityType<? extends SmallLightningStrikeEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static void spawn(ServerLevel level, double x, double y, double z, boolean plasma) {
        SmallLightningStrikeEntity strike = new SmallLightningStrikeEntity(ModEntities.SMALL_LIGHTNING_STRIKE.get(), level);
        strike.setPos(x, y, z);
        strike.entityData.set(PLASMA, plasma);
        level.addFreshEntity(strike);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(PLASMA, false);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(PLASMA, tag.getBoolean("Plasma"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("Plasma", entityData.get(PLASMA));
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel serverLevel) {
            ParticleOptions particle = entityData.get(PLASMA) ? ModParticles.PLASMA_STRIKE.get() : ModParticles.LIGHTNING_STRIKE.get();
            for (int segment = 0; segment < 7; segment++) {
                double progress = segment / 6.0D;
                double spread = 0.08D * (1.0D - progress);
                double x = getX() + (random.nextDouble() - 0.5D) * spread;
                double z = getZ() + (random.nextDouble() - 0.5D) * spread;
                serverLevel.sendParticles(particle, x, getY() + 1.8D - progress * 1.8D, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
        if (tickCount >= 5) discard();
    }
}
