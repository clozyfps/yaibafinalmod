package net.clozy.mixin;

import net.clozy.registry.Clan;
import net.clozy.registry.BreathingStyle;
import net.clozy.util.IClanAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements IClanAccessor {

    @Unique private Clan selectedClan = Clan.NONE;
    @Unique private BreathingStyle breathingStyle = BreathingStyle.NONE;
    @Unique private int selectedAbilityIndex = 0;

    // Stats
    @Unique private int vitality = 1;
    @Unique private int strength = 1;
    @Unique private int swordsmanship = 1;
    @Unique private int agility = 1;
    @Unique private int level = 1;
    @Unique private int exp = 0;
    @Unique private int sp = 0;

    // Breathing Bar
    @Unique private float breathingProgress = 0.0f;
    @Unique private boolean isBreathingCharging = false;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    // --- Accessor Methods ---
    @Override public Clan getClan() { return selectedClan; }
    @Override public void setClan(Clan clan) { this.selectedClan = clan; updateStats(); }
    @Override public boolean hasSelectedClan() { return this.selectedClan != Clan.NONE; }
    @Override public BreathingStyle getBreathingStyle() { return breathingStyle; }
    @Override public void setBreathingStyle(BreathingStyle style) { this.breathingStyle = style; this.selectedAbilityIndex = 0; }
    @Override public int getSelectedAbilityIndex() { return selectedAbilityIndex; }
    @Override public float getBreathingProgress() { return breathingProgress; }
    @Override public void setBreathingCharging(boolean charging) { this.isBreathingCharging = charging; }

    @Override
    public void cycleAbility() {
        if (breathingStyle == BreathingStyle.NONE || breathingStyle.getForms().isEmpty()) return;
        selectedAbilityIndex++;
        if (selectedAbilityIndex >= breathingStyle.getForms().size()) {
            selectedAbilityIndex = 0;
        }
    }

    // --- Stat Methods ---
    @Override
    public int getStat(String statName) {
        switch (statName) {
            case "Vitality": return vitality;
            case "Strength": return strength;
            case "Swordsmanship": return swordsmanship;
            case "Agility": return agility;
            default: return 0;
        }
    }

    @Override
    public int getLevel() { return level; }
    @Override public int getExp() { return exp; }
    @Override public int getSp() { return sp; }

    @Override
    public void addExp(int amount) {
        this.exp += amount;
        int reqExp = level * 100; // Simple curve
        if (this.exp >= reqExp) {
            this.exp -= reqExp;
            this.level++;
            this.sp += 2; // 2 SP per level

            // FIX: Cast to Object first to satisfy the compiler
            if ((Object)this instanceof ServerPlayerEntity player) {
                player.sendMessage(Text.literal("Level Up! You are now Level " + level).formatted(Formatting.GOLD), true);
            }
        }
    }

    @Override
    public void upgradeStat(String statName) {
        if (sp <= 0) return;
        switch (statName) {
            case "Vitality": vitality++; break;
            case "Strength": strength++; break;
            case "Swordsmanship": swordsmanship++; break;
            case "Agility": agility++; break;
        }
        sp--;
        updateStats();
    }

    // --- Core Logic ---

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickStats(CallbackInfo ci) {
        // Breathing Bar Logic
        if (isBreathingCharging) {
            if (breathingProgress < 100f) breathingProgress += 1.0f; // Fill speed
        } else {
            if (breathingProgress > 0f) breathingProgress -= 0.5f; // Drain speed
        }

        // Apply dynamic attribute modifiers periodically (every 20 ticks)
        if (this.age % 20 == 0) {
            updateStats();
        }
    }

    @Unique
    private void updateStats() {
        // Vitality -> Max Health (20 base + 2 per vitality level)
        EntityAttributeInstance healthAttr = this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (healthAttr != null) {
            double targetHealth = 20.0 + (vitality * 2.0);
            if (healthAttr.getBaseValue() != targetHealth) healthAttr.setBaseValue(targetHealth);
        }

        // Agility -> Speed & Attack Speed
        EntityAttributeInstance speedAttr = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.setBaseValue(0.1 + (agility * 0.005));
        }

        EntityAttributeInstance attackSpeedAttr = this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);
        if (attackSpeedAttr != null) {
            attackSpeedAttr.setBaseValue(4.0 + (agility * 0.1));
        }

        // Damage Logic (Strength vs Swordsmanship)
        EntityAttributeInstance damageAttr = this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (damageAttr != null) {
            boolean holdingSword = this.getMainHandStack().getItem() instanceof SwordItem;
            double baseDmg = 1.0;
            if (holdingSword) {
                baseDmg += (swordsmanship * 1.5);
            } else {
                baseDmg += (strength * 1.0);
            }
            // We set base value, items add on top of this naturally
            damageAttr.setBaseValue(baseDmg);
        }
    }

    // Kill Event for XP
    // FIX: Method signature updated to match Minecraft 1.20.1 (ServerWorld first, then LivingEntity)
    // The previous error occurred because it expected ServerPlayerEntity as the first arg.
    @Inject(method = "onKilledOther", at = @At("HEAD"))
    public void onKill(ServerWorld world, LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir) {
        this.addExp(10 + (livingEntity.getMaxHealth() > 20 ? 20 : 0)); // More XP for stronger mobs
    }

    // --- NBT Storage ---
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeData(NbtCompound nbt, CallbackInfo ci) {
        nbt.putString("YaibaClan", selectedClan.name());
        nbt.putString("YaibaBreathing", breathingStyle.name());
        nbt.putInt("YaibaAbilityIndex", selectedAbilityIndex);
        nbt.putInt("YaibaVitality", vitality);
        nbt.putInt("YaibaStrength", strength);
        nbt.putInt("YaibaSwordsmanship", swordsmanship);
        nbt.putInt("YaibaAgility", agility);
        nbt.putInt("YaibaLevel", level);
        nbt.putInt("YaibaExp", exp);
        nbt.putInt("YaibaSP", sp);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readData(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("YaibaClan")) selectedClan = Clan.fromString(nbt.getString("YaibaClan"));
        if (nbt.contains("YaibaBreathing")) breathingStyle = BreathingStyle.fromString(nbt.getString("YaibaBreathing"));
        if (nbt.contains("YaibaAbilityIndex")) selectedAbilityIndex = nbt.getInt("YaibaAbilityIndex");
        if (nbt.contains("YaibaVitality")) vitality = nbt.getInt("YaibaVitality");
        if (nbt.contains("YaibaStrength")) strength = nbt.getInt("YaibaStrength");
        if (nbt.contains("YaibaSwordsmanship")) swordsmanship = nbt.getInt("YaibaSwordsmanship");
        if (nbt.contains("YaibaAgility")) agility = nbt.getInt("YaibaAgility");
        if (nbt.contains("YaibaLevel")) level = nbt.getInt("YaibaLevel");
        if (nbt.contains("YaibaExp")) exp = nbt.getInt("YaibaExp");
        if (nbt.contains("YaibaSP")) sp = nbt.getInt("YaibaSP");

        updateStats(); // Apply loaded stats
    }
}