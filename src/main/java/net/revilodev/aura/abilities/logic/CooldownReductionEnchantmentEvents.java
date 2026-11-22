package net.revilodev.aura.abilities.logic;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.revilodev.aura.CodexMod;

public final class CooldownReductionEnchantmentEvents {
    private static final ResourceKey<Enchantment> COOLDOWN_REDUCTION = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(CodexMod.MOD_ID, "cooldown_reduction"));

    private CooldownReductionEnchantmentEvents() {}

    public static double reduction(Player player) {
        if (player == null) return 0.0D;
        Holder<Enchantment> holder = player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(COOLDOWN_REDUCTION).orElse(null);
        if (holder == null) return 0.0D;
        int level = 0;
        for (ItemStack stack : player.getArmorSlots()) level = Math.max(level, EnchantmentHelper.getItemEnchantmentLevel(holder, stack));
        level = Math.max(level, EnchantmentHelper.getItemEnchantmentLevel(holder, player.getMainHandItem()));
        level = Math.max(level, EnchantmentHelper.getItemEnchantmentLevel(holder, player.getOffhandItem()));
        return Math.min(0.06D, Math.max(0, level) * 0.02D);
    }
}
