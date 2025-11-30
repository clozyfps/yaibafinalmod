package net.clozy.util;

import net.clozy.registry.Clan;
import net.clozy.registry.BreathingStyle;

public interface IClanAccessor {
    // Clan
    Clan getClan();
    void setClan(Clan clan);
    boolean hasSelectedClan();

    // Breathing
    BreathingStyle getBreathingStyle();
    void setBreathingStyle(BreathingStyle style);
    int getSelectedAbilityIndex();
    void cycleAbility();

    // Stats
    int getStat(String statName);
    void upgradeStat(String statName);
    int getLevel();
    int getExp();
    int getSp(); // Stat Points
    void addExp(int amount);

    // Breathing Mechanics
    float getBreathingProgress();
    void setBreathingCharging(boolean charging);
}