package net.revilodev.aura.entity.projectile;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.revilodev.aura.abilities.AbilityElement;
import net.revilodev.aura.abilities.logic.AbilityLogic;
import net.revilodev.aura.entity.ModEntities;
import net.revilodev.aura.particle.ModParticles;

import java.util.HashSet;
import java.util.Set;

// burst cube projectile
public final class BurstCubeProjectile extends ThrowableProjectile {
    private static final EntityDataAccessor<Integer> ELEMENT = SynchedEntityData.defineId(BurstCubeProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(BurstCubeProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DURATION = SynchedEntityData.defineId(BurstCubeProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FINAL_FORM = SynchedEntityData.defineId(BurstCubeProjectile.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> REMAINING_HITS = SynchedEntityData.defineId(BurstCubeProjectile.class, EntityDataSerializers.INT);
    private final Set<Integer> hitEntityIds = new HashSet<>();

    public BurstCubeProjectile(EntityType<? extends BurstCubeProjectile> type, Level level) {
        super(type, level);
    }

    public BurstCubeProjectile(Level level, LivingEntity owner, AbilityElement element, float damage, int duration, boolean finalForm) {
        this(level, owner, element, damage, duration, finalForm, 1);
    }

    public BurstCubeProjectile(Level level, LivingEntity owner, AbilityElement element, float damage, int duration, boolean finalForm, int remainingHits) {
        this(ModEntities.BURST_CUBE.get(), level);
        setOwner(owner);
        setPos(owner.getX(), owner.getEyeY() - 0.1D, owner.getZ());
        setElement(element);
        setDamage(damage);
        setDuration(duration);
        setFinalForm(finalForm);
        setRemainingHits(Math.max(1, remainingHits));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ELEMENT, AbilityElement.FIRE.ordinal());
        builder.define(DAMAGE, 1.0F);
        builder.define(DURATION, 40);
        builder.define(FINAL_FORM, false);
        builder.define(REMAINING_HITS, 1);
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount > 30) {
            discard();
            return;
        }
        if (level() instanceof ServerLevel level && element() != AbilityElement.POISON) {
            var particle = switch (element()) {
                case ICE -> finalForm() ? ModParticles.FROST_AURA_EMBER_LIGHT.get() : ModParticles.ICE_AURA_EMBER_LIGHT.get();
                case POISON -> finalForm() ? ModParticles.TOXIN_AURA_EMBER_LIGHT.get() : ModParticles.POISON_AURA_EMBER_LIGHT.get();
                case LIGHTNING -> finalForm() ? ModParticles.PLASMA_AURA_EMBER_LIGHT.get() : ModParticles.LIGHTNING_AURA_EMBER_LIGHT.get();
                case FIRE -> finalForm() ? ModParticles.SOUL_FIRE_AURA_EMBER_LIGHT.get() : ModParticles.FIRE_AURA_EMBER_LIGHT.get();
                case BLOOD -> ModParticles.FIRE_AURA_EMBER_DARK.get();
                default -> net.minecraft.core.particles.ParticleTypes.END_ROD;
            };
            level.sendParticles(particle, getX(), getY(), getZ(), 1, 0.025D, 0.025D, 0.025D, 0.002D);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!(getOwner() instanceof ServerPlayer player)) return;
        if (!(result.getEntity() instanceof LivingEntity living) || !hitEntityIds.add(living.getId())) return;
        AbilityLogic.applyElementHit(player, element(), living, damage(), duration(), finalForm());
        int hitsLeft = remainingHits() - 1;
        setRemainingHits(hitsLeft);
        if (hitsLeft <= 0) discard();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (result.getType() != HitResult.Type.ENTITY) {
            discard();
        }
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0D;
    }

    public AbilityElement element() {
        int ord = entityData.get(ELEMENT);
        AbilityElement[] values = AbilityElement.values();
        if (ord < 0 || ord >= values.length) return AbilityElement.FIRE;
        return values[ord];
    }

    public void setElement(AbilityElement element) {
        entityData.set(ELEMENT, element == null ? AbilityElement.FIRE.ordinal() : element.ordinal());
    }

    public float damage() {
        return entityData.get(DAMAGE);
    }

    public void setDamage(float damage) {
        entityData.set(DAMAGE, Math.max(0.0F, damage));
    }

    public int duration() {
        return entityData.get(DURATION);
    }

    public void setDuration(int duration) {
        entityData.set(DURATION, Math.max(1, duration));
    }

    public boolean finalForm() {
        return entityData.get(FINAL_FORM);
    }

    public void setFinalForm(boolean finalForm) {
        entityData.set(FINAL_FORM, finalForm);
    }

    public int remainingHits() {
        return entityData.get(REMAINING_HITS);
    }

    public void setRemainingHits(int remainingHits) {
        entityData.set(REMAINING_HITS, Math.max(0, remainingHits));
    }
}
