package net.revilodev.aura.abilities.event;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.revilodev.aura.abilities.AbilityId;

public final class AbilityCooldownCalculationEvent extends Event {
    private final Player player;
    private final AbilityId ability;
    private final int baseCooldownTicks;
    private int cooldownTicks;

    public AbilityCooldownCalculationEvent(Player player, AbilityId ability, int baseCooldownTicks, int cooldownTicks) {
        this.player = player;
        this.ability = ability;
        this.baseCooldownTicks = baseCooldownTicks;
        this.cooldownTicks = cooldownTicks;
    }

    public Player getPlayer() { return player; }
    public AbilityId getAbility() { return ability; }
    public int getBaseCooldownTicks() { return baseCooldownTicks; }
    public int getCooldownTicks() { return cooldownTicks; }
    public void setCooldownTicks(int cooldownTicks) { this.cooldownTicks = Math.max(0, cooldownTicks); }
}
