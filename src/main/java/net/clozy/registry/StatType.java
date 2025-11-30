package net.clozy.registry;

public enum StatType {
    VITALITY("Vitality", "Increases Max Health"),
    STRENGTH("Strength", "Increases Unarmed Damage"),
    SWORDSMANSHIP("Swordsmanship", "Increases Sword Damage & Unlocks Forms"),
    AGILITY("Agility", "Increases Speed & Attack Speed");

    public final String name;
    public final String description;

    StatType(String name, String description) {
        this.name = name;
        this.description = description;
    }
}