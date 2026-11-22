package net.revilodev.aura.client;

import com.revilo.levelup.api.LevelUpApi;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.revilodev.aura.abilities.AbilitiesAttachments;
import net.revilodev.aura.abilities.AbilityElement;
import net.revilodev.aura.abilities.AbilityConfig;
import net.revilodev.aura.abilities.AbilityId;
import net.revilodev.aura.abilities.PlayerAbilities;
import net.revilodev.aura.abilities.logic.AbilityScaling;
import net.revilodev.aura.attributes.CodexAttributes;
import net.revilodev.aura.client.abilities.AbilityKeybinds;
import net.revilodev.aura.skills.PlayerSkills;
import net.revilodev.aura.skills.SkillBalance;
import net.revilodev.aura.skills.SkillId;
import net.revilodev.aura.skills.SkillsAttachments;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("resource")
public final class PlayerStatsRenderer {
    private static final ResourceKey<Enchantment> ABILITY_POWER_ENCHANT = enchantmentKey("ability_power");
    private static final ResourceKey<Enchantment> LOOTING_ENCHANT = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.withDefaultNamespace("looting"));
    private static final ResourceKey<Enchantment> FORTUNE_ENCHANT = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.withDefaultNamespace("fortune"));
    private static final int CONTENT_TOP = 42;
    private static final int CONTENT_BOTTOM = 6;
    private static final int ROW_HEIGHT = 10;
    private static final int CATEGORY_HEIGHT = 13;

    private PlayerStatsRenderer() {
    }

    public static Result render(GuiGraphics graphics, int panelX, int panelY, int panelWidth, int panelHeight,
                                int scroll, int mouseX, int mouseY) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return new Result(0, null, null, null);

        Player player = minecraft.player;
        int level = LevelUpApi.levelForTotalXp(LevelUpApi.getXp(player));
        drawOutlinedLevel(graphics, minecraft, Integer.toString(level), panelX + panelWidth / 2, panelY + 16);
        int xpX = panelX + 19;
        int xpY = panelY + 34;
        int xpWidth = panelWidth - 38;
        long xpCurrent = LevelUpApi.getXpIntoCurrentLevel(player);
        long xpMaximum = Math.max(1L, LevelUpApi.getXpNeededForNextLevel(player));
        graphics.fill(xpX, xpY, xpX + xpWidth, xpY + 6, 0xFF151515);
        graphics.fill(xpX + 1, xpY + 1, xpX + xpWidth - 1, xpY + 5, 0xFF384552);
        graphics.fill(xpX + 2, xpY + 2, xpX + 2 + Math.round((xpWidth - 4) * LevelUpApi.getProgressToNextLevel(player)), xpY + 4, 0xFF76B8FF);
        boolean xpBarHovered = mouseX >= xpX && mouseX <= xpX + xpWidth && mouseY >= xpY && mouseY <= xpY + 6;

        List<Line> lines = lines(player);
        int contentX = panelX + 8;
        int contentY = panelY + CONTENT_TOP;
        int contentRight = panelX + panelWidth - 8;
        int contentBottom = panelY + panelHeight - CONTENT_BOTTOM;
        int maxScroll = Math.max(0, contentHeight(lines) - (contentBottom - contentY));
        int clampedScroll = Mth.clamp(scroll, 0, maxScroll);
        String hoveredKey = null;
        AbilityId hoveredAbility = null;
        SkillId hoveredSkill = null;

        graphics.enableScissor(contentX, contentY, contentRight, contentBottom);
        int y = contentY - clampedScroll;
        for (Line line : lines) {
            if (line.category()) {
                if (y + CATEGORY_HEIGHT > contentY && y < contentBottom) drawCategory(graphics, minecraft, contentX, contentRight, y, line.label());
                y += CATEGORY_HEIGHT;
                continue;
            }
            if (y + ROW_HEIGHT > contentY && y < contentBottom) {
                boolean hovered = mouseX >= contentX && mouseX <= contentRight && mouseY >= y && mouseY < y + ROW_HEIGHT;
                if (hovered) {
                    hoveredKey = line.key();
                    hoveredAbility = line.ability();
                    hoveredSkill = line.skill();
                }
                drawRow(graphics, minecraft, contentX, contentRight, y, line, hovered);
            }
            y += ROW_HEIGHT;
        }
        graphics.disableScissor();
        drawScrollbar(graphics, contentRight, contentY, contentBottom, clampedScroll, maxScroll, contentHeight(lines));
        if (xpBarHovered) graphics.renderTooltip(minecraft.font, Component.literal(xpCurrent + " / " + xpMaximum + " XP"), mouseX, mouseY);
        return new Result(maxScroll, hoveredKey, hoveredAbility, hoveredSkill);
    }

    public static boolean contains(int panelX, int panelY, int panelWidth, int panelHeight, double mouseX, double mouseY) {
        return mouseX >= panelX + 8 && mouseX <= panelX + panelWidth - 8
                && mouseY >= panelY + CONTENT_TOP && mouseY <= panelY + panelHeight - CONTENT_BOTTOM;
    }

    private static List<Line> lines(Player player) {
        PlayerSkills skills = player.getData(SkillsAttachments.PLAYER_SKILLS.get());
        PlayerAbilities abilities = player.getData(AbilitiesAttachments.PLAYER_ABILITIES.get());
        List<Line> lines = new ArrayList<>();

        lines.add(Line.category("Progression"));
        lines.add(Line.stat("skills_spent", "Skill Points Spent", Integer.toString(skillPointsSpent(skills)), 0xFF6AB2FF, "Allocated skill levels"));
        lines.add(Line.stat("abilities_spent", "Ability Points Spent", Integer.toString(abilityPointsSpent(abilities)), 0xFFC78CFF, "Total ability upgrade cost paid"));
        lines.add(Line.stat("ability_power", "Ability Power", fmt(effectiveAbilityPower(player)), 0xFFC78CFF, "Armor, held-item, and enchantment ability power"));
        lines.add(Line.stat("attack_damage", "Attack Damage", fmt(player.getAttributeValue(Attributes.ATTACK_DAMAGE)), 0xFF8D8D8D, "Attack damage attribute"));
        lines.add(Line.stat("defence", "Defence", Integer.toString(player.getArmorValue()), 0xFF8D8D8D, "Equipped armor"));
        lines.add(Line.stat("health", "Max Health", fmt(player.getMaxHealth()), 0xFFFFB3B3, "Maximum health attribute"));
        lines.add(Line.stat("speed", "Movement Speed", fmt(player.getAttributeValue(Attributes.MOVEMENT_SPEED)), modified(skills, SkillId.AGILITY) ? 0xFF99FFB6 : 0xFF8D8D8D, "Movement speed attribute"));

        lines.add(Line.category("Selected Abilities"));
        for (AbilityElement element : AbilityElement.values()) {
            AbilityId selected = abilities.selectedSpecialization(element);
            if (selected != null) lines.add(Line.ability(selected, abilities.rank(selected.core()) > 0));
        }

        lines.add(Line.category("Skills"));
        for (SkillId id : SkillId.values()) {
            lines.add(Line.skill(id, skills));
        }

        lines.add(Line.category("Combat"));
        lines.add(Line.stat("strength", "Strength", "+" + fmt(SkillBalance.strengthDamage(skills.level(SkillId.STRENGTH))), 0xFFE8B0A0, "Strength skill"));
        lines.add(Line.stat("power", "Power", "+" + fmt(SkillBalance.powerDamage(skills.level(SkillId.POWER))), 0xFFE8B0A0, "Power skill"));
        lines.add(Line.stat("crit", "Critical Power", "+" + fmt(SkillBalance.critPowerDamage(skills.level(SkillId.CRIT_POWER))), 0xFFFFD580, "Critical Power skill"));
        lines.add(Line.stat("attack_speed", "Attack Speed", percent(SkillBalance.attackSpeed(skills.level(SkillId.ATTACK_SPEED))), 0xFF99FFB6, "Attack Speed skill"));
        lines.add(Line.stat("haste", "Mining Speed", "+" + fmt(SkillBalance.hasteBreakSpeed(skills.level(SkillId.HASTE))), 0xFF99FFB6, "Haste skill"));
        lines.add(Line.stat("leech", "Life Leech", percent(skills.level(SkillId.HEALTH_BOOST) > 0 ? SkillBalance.lifeLeach(skills.level(SkillId.LUCK)) : 0.0D), 0xFFFF9CC8, "Health Boost and Luck skills"));
        lines.add(Line.stat("regeneration", "Regeneration", fmt(SkillBalance.regenHeartsPerSecond(skills.level(SkillId.REGENERATION))) + " hearts/s", 0xFFFF9CC8, "Regeneration skill"));

        lines.add(Line.category("Resistances & Effects"));
        lines.add(Line.stat("resistance", "General Resistance", percent(SkillBalance.resistance(skills.level(SkillId.RESISTANCE))), 0xFF8CC7FF, "Resistance skill"));
        lines.add(Line.stat("fire_resistance", "Fire Resistance", percent(SkillBalance.fireResistance(skills.level(SkillId.FIRE_RESISTANCE))), 0xFF8CC7FF, "Fire Resistance skill"));
        lines.add(Line.stat("projectile_resistance", "Projectile Resistance", percent(SkillBalance.projectileResistance(skills.level(SkillId.PROJECTILE_RESISTANCE))), 0xFF8CC7FF, "Projectile Resistance skill"));
        lines.add(Line.stat("blast_resistance", "Blast Resistance", percent(SkillBalance.blastResistance(skills.level(SkillId.BLAST_RESISTANCE))), 0xFF8CC7FF, "Blast Resistance skill"));
        lines.add(Line.stat("knockback_resistance", "Knockback Resistance", percent(SkillBalance.knockbackResistance(skills.level(SkillId.KNOCKBACK_RESISTANCE))), 0xFF8CC7FF, "Knockback Resistance skill"));
        lines.add(Line.stat("effect_avoidance", "Negative Effect Avoidance", percent(SkillBalance.cleanseImmunities(skills.level(SkillId.CLEANSE))), 0xFFFF9CC8, "Cleanse skill reduces negative effects"));

        lines.add(Line.category("Luck & Gathering"));
        lines.add(Line.stat("luck", "Luck", "+" + fmt(SkillBalance.luck(skills.level(SkillId.LUCK))), 0xFFFFE08A, "Luck skill"));
        lines.add(Line.stat("looting", "Looting Bonus", percent(SkillBalance.lootingChance(skills.level(SkillId.LOOTING))), 0xFFFFE08A, "Looting skill"));
        lines.add(Line.stat("looting_enchant", "Looting", Integer.toString(toolEnchantmentLevel(player, LOOTING_ENCHANT)), 0xFFFFE08A, "Main-hand tool enchantment"));
        lines.add(Line.stat("fortune", "Fortune", Integer.toString(toolEnchantmentLevel(player, FORTUNE_ENCHANT)), 0xFFFFE08A, "Main-hand tool enchantment"));
        lines.add(Line.stat("fishing", "Fishing Luck", "+" + fmt(SkillBalance.luckOfTheSea(skills.level(SkillId.LUCK_OF_THE_SEA))), 0xFFFFE08A, "Luck of the Sea skill"));
        lines.add(Line.stat("xp", "Experience Gain", "+" + skills.level(SkillId.XP_FORTUNE) * 50 + "%", 0xFFFFE08A, "XP Fortune skill"));
        lines.add(Line.stat("swimming", "Swimming Speed", percent(SkillBalance.swimmingSpeed(skills.level(SkillId.SWIMMING_SPEED))), 0xFF99FFB6, "Swimming Speed skill"));
        lines.add(Line.stat("leaping", "Leap Height", "+" + fmt(SkillBalance.leapingBonus(skills.level(SkillId.LEAPING))), 0xFF99FFB6, "Leaping skill"));

        lines.add(Line.category("Ability Affinities"));
        for (AbilityElement element : AbilityElement.values()) {
            AbilityId core = AbilityId.valueOf(element.name());
            int rank = abilities.rank(core);
            int color = rank > 0 ? elementColor(element) : 0xFF8D8D8D;
            String name = elementName(element);
            double resistance = AbilityScaling.masteryResistance(core, rank);
            lines.add(Line.stat("affinity_resistance_" + element.name(), name + " Immunity", resistance >= 1.0D ? "Immune" : percent(resistance), color, "Affinity mastery resistance"));
            lines.add(Line.stat("affinity_damage_" + element.name(), name + " Average Damage", fmt(averageAbilityDamage(abilities, core, effectiveAbilityPower(player))), color, "Average damage of unlocked " + name.toLowerCase(Locale.ROOT) + " abilities"));
        }
        return lines;
    }

    private static int skillPointsSpent(PlayerSkills skills) {
        int total = 0;
        for (SkillId id : SkillId.values()) total += skills.level(id);
        return total;
    }

    private static int abilityPointsSpent(PlayerAbilities abilities) {
        int total = 0;
        for (AbilityId id : AbilityId.values()) {
            int rank = abilities.rank(id);
            total += rank * (rank + 1) / 2;
        }
        return total;
    }

    private static double averageAbilityDamage(PlayerAbilities abilities, AbilityId core, double abilityPower) {
        double total = 0.0D;
        int count = 0;
        int coreRank = abilities.rank(core);
        if (coreRank <= 0) return 0.0D;
        for (AbilityId id : AbilityId.values()) {
            if (id.element() == core.element() && id.isSpecialization()) {
                total += AbilityScaling.damage(id, Math.max(1, coreRank), abilityPower);
                count++;
            }
        }
        return count == 0 ? 0.0D : total / count;
    }

    private static boolean modified(PlayerSkills skills, SkillId id) {
        return skills.level(id) > 0;
    }

    private static double effectiveAbilityPower(Player player) {
        int level = allEquipmentEnchantmentLevel(player, ABILITY_POWER_ENCHANT);
        return CodexAttributes.baseAbilityPower(player) * (1.0D + AbilityConfig.abilityPowerEnchantScale(level));
    }

    private static ResourceKey<Enchantment> enchantmentKey(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("aura", name));
    }

    private static int allEquipmentEnchantmentLevel(Player player, ResourceKey<Enchantment> enchantment) {
        int level = toolEnchantmentLevel(player, enchantment);
        Holder<Enchantment> holder = enchantmentHolder(player, enchantment);
        if (holder == null) return level;
        for (var stack : player.getArmorSlots()) level = Math.max(level, EnchantmentHelper.getItemEnchantmentLevel(holder, stack));
        return level;
    }

    private static int toolEnchantmentLevel(Player player, ResourceKey<Enchantment> enchantment) {
        Holder<Enchantment> holder = enchantmentHolder(player, enchantment);
        if (holder == null) return 0;
        return EnchantmentHelper.getItemEnchantmentLevel(holder, player.getMainHandItem());
    }

    private static Holder<Enchantment> enchantmentHolder(Player player, ResourceKey<Enchantment> enchantment) {
        return player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolder(enchantment).orElse(null);
    }

    private static int contentHeight(List<Line> lines) {
        int height = 0;
        for (Line line : lines) height += line.category() ? CATEGORY_HEIGHT : ROW_HEIGHT;
        return height;
    }

    private static void drawCategory(GuiGraphics graphics, Minecraft minecraft, int x, int right, int y, String title) {
        drawScaled(graphics, minecraft, title, x + 2, y + 2, 0xA0000000, 0.65F);
        drawScaled(graphics, minecraft, title, x + 1, y + 1, 0xFFFFD879, 0.65F);
        int lineX = x + 5 + Math.round(minecraft.font.width(title) * 0.65F);
        graphics.fillGradient(lineX, y + 5, right - 2, y + 6, 0xC0D39B45, 0x00D39B45);
    }

    private static void drawRow(GuiGraphics graphics, Minecraft minecraft, int x, int right, int y, Line line, boolean hovered) {
        if (hovered) graphics.fill(x, y, right, y + ROW_HEIGHT - 1, 0x406D9CD0);
        int textY = hovered ? y : y + 1;
        int labelColor = line.color() == 0xFF8D8D8D ? 0xFF707070 : 0xFFC2C2C2;
        int labelX = x + 2;
        if (line.icon() != null) {
            drawIcon(graphics, line.icon(), labelX, y + 1);
            labelX += 10;
        }
        drawScaled(graphics, minecraft, line.label(), labelX, textY, labelColor, 0.65F);
        int valueX = right - 2 - Math.round(minecraft.font.width(line.value()) * 0.65F);
        drawScaled(graphics, minecraft, line.value(), valueX, textY, line.color(), 0.65F);
    }

    private static void drawScrollbar(GuiGraphics graphics, int right, int top, int bottom, int scroll, int maxScroll, int contentHeight) {
        if (maxScroll <= 0) return;
        int viewportHeight = bottom - top;
        int thumbHeight = Math.max(12, viewportHeight * viewportHeight / contentHeight);
        int thumbY = top + Math.round((viewportHeight - thumbHeight) * (scroll / (float) maxScroll));
        graphics.fill(right - 2, top, right - 1, bottom, 0x55303030);
        graphics.fill(right - 2, thumbY, right, thumbY + thumbHeight, 0xFFA0A0A0);
    }

    private static void drawOutlinedLevel(GuiGraphics graphics, Minecraft minecraft, String level, int centerX, int y) {
        float scale = 1.65F;
        int x = centerX - Math.round(minecraft.font.width(level) * scale) / 2;
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetY = -1; offsetY <= 1; offsetY++) {
                if (offsetX != 0 || offsetY != 0) drawScaled(graphics, minecraft, level, x + offsetX, y + offsetY, 0xFF5A4200, scale);
            }
        }
        drawScaled(graphics, minecraft, level, x, y, 0xFF72C7FF, scale);
    }

    private static void drawScaled(GuiGraphics graphics, Minecraft minecraft, String text, int x, int y, int color, float scale) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(minecraft.font, text, 0, 0, color, false);
        graphics.pose().popPose();
    }

    private static void drawIcon(GuiGraphics graphics, ResourceLocation icon, int x, int y) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(0.5F, 0.5F, 1.0F);
        graphics.blit(icon, 0, 0, 0, 0, 16, 16, 16, 16);
        graphics.pose().popPose();
    }

    private static int elementColor(AbilityElement element) {
        return switch (element) {
            case FIRE -> 0xFFFF8A66;
            case ICE -> 0xFF8CD3FF;
            case LIGHTNING -> 0xFFFFE36B;
            case POISON -> 0xFF96E07A;
            case FORCE -> 0xFFC4A5FF;
            case BLOOD -> 0xFFD94A5C;
            case WIND -> 0xFFA9FFD8;
        };
    }

    private static String elementName(AbilityElement element) {
        String raw = element.name().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
    }

    private static String percent(double value) {
        return fmt(value * 100.0D) + "%";
    }

    private static String fmt(double value) {
        String out = String.format(Locale.ROOT, "%.2f", value);
        while (out.contains(".") && (out.endsWith("0") || out.endsWith("."))) out = out.substring(0, out.length() - 1);
        return out;
    }

    private static String skillEffect(SkillId id, PlayerSkills skills) {
        int level = skills.level(id);
        return switch (id) {
            case STRENGTH -> "+" + fmt(SkillBalance.strengthDamage(level));
            case POWER -> "+" + fmt(SkillBalance.powerDamage(level));
            case CRIT_POWER -> "+" + fmt(SkillBalance.critPowerDamage(level));
            case HASTE -> "+" + fmt(SkillBalance.hasteBreakSpeed(level)) + " blocks/s";
            case RESISTANCE -> "+" + percent(SkillBalance.resistance(level)) + " resistance";
            case FIRE_RESISTANCE -> "+" + percent(SkillBalance.fireResistance(level)) + " fire resistance";
            case PROJECTILE_RESISTANCE -> "+" + percent(SkillBalance.projectileResistance(level)) + " projectile resistance";
            case KNOCKBACK_RESISTANCE -> "+" + percent(SkillBalance.knockbackResistance(level)) + " knockback resistance";
            case BLAST_RESISTANCE -> "+" + percent(SkillBalance.blastResistance(level)) + " blast resistance";
            case AGILITY -> "+" + percent(SkillBalance.agilitySpeed(level)) + " speed";
            case LEAPING -> "+" + fmt(SkillBalance.leapingBonus(level)) + " jump height";
            case ATTACK_SPEED -> "+" + percent(SkillBalance.attackSpeed(level));
            case SWIMMING_SPEED -> "+" + percent(SkillBalance.swimmingSpeed(level));
            case VITALITY -> "+" + fmt(SkillBalance.vitalityHearts(level)) + " hearts";
            case REGENERATION -> "+" + fmt(SkillBalance.regenHeartsPerSecond(level)) + " hearts/s";
            case HEALTH_BOOST -> "+" + percent(SkillBalance.lifeLeach(skills.level(SkillId.LUCK))) + " leech";
            case CLEANSE -> "+" + percent(SkillBalance.cleanseImmunities(level)) + " effect avoidance";
            case LUCK -> "+" + fmt(SkillBalance.luck(level)) + " luck";
            case LOOTING -> "+" + percent(SkillBalance.lootingChance(level)) + " looting";
            case FORTUNE -> "+" + SkillBalance.fortuneBonus(level) + " fortune";
            case LUCK_OF_THE_SEA -> "+" + fmt(SkillBalance.luckOfTheSea(level)) + " fishing luck";
            case XP_FORTUNE -> "+" + level * 50 + "% XP";
        };
    }

    private static int skillColor(SkillId id) {
        return switch (id.category()) {
            case STRENGTH -> 0xFFFF8E8E;
            case RESISTANCE -> 0xFF8CC7FF;
            case AGILITY -> 0xFF99FFB6;
            case VITALITY -> 0xFFFF9CC8;
            case LUCK -> 0xFFFFE08A;
        };
    }

    public record Result(int maxScroll, String hoveredKey, AbilityId hoveredAbility, SkillId hoveredSkill) {
    }

    private record Line(String key, String label, String value, int color, String source, boolean category,
                        ResourceLocation icon, AbilityId ability, SkillId skill) {
        static Line category(String title) {
            return new Line("category_" + title, title, "", 0, "", true, null, null, null);
        }

        static Line stat(String key, String label, String value, int color, String source) {
            return new Line(key, label, value, color, source, false, null, null, null);
        }

        static Line ability(AbilityId id, boolean unlocked) {
            return new Line("selected_ability_" + id.name(), id.title(), AbilityKeybinds.keyName(id), unlocked ? elementColor(id.element()) : 0xFF8D8D8D,
                    "Open this ability", false, id.iconTexture(), id, null);
        }

        static Line skill(SkillId id, PlayerSkills skills) {
            int level = skills.level(id);
            return new Line("selected_skill_" + id.name(), id.title(), skillEffect(id, skills),
                    level > 0 ? skillColor(id) : 0xFF8D8D8D, "Open this skill", false, id.icon(), null, id);
        }
    }
}
