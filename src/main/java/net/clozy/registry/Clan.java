package net.clozy.registry;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.List;

public enum Clan {
    KAMADO("Kamado",
            "Descendants of the charcoal sellers who carry the legacy of the Sun.",
            List.of("Hard Forehead: Reduced knockback taken.", "Sun Affinity: Regeneration in direct sunlight."),
            List.of("Self-Sacrificing: Taking damage drains hunger faster."),
            Formatting.RED),

    UZUI("Uzui",
            "A shinobi clan that values flamboyance and speed above all else.",
            List.of("Shinobi Training: Permanent Speed I.", "Poison Resistance: Immunity to Poison."),
            List.of("Flashy: Mobs detect you from further away."),
            Formatting.GOLD),

    TSUGIKUNI("Tsugikuni",
            "An ancient and powerful lineage cursed by destiny.",
            List.of("Prodigy: +15% Base Attack Damage.", "Chosen: Access to Sun/Moon breathing variants."),
            List.of("Cursed Mark: Max health reduced by 2 hearts."),
            Formatting.DARK_PURPLE),

    KANROJI("Kanroji",
            "Blessed with abnormal muscle density and flexibility.",
            List.of("Muscle Density: Permanent Strength I.", "Flexible: Jump boost I."),
            List.of("High Metabolism: Hunger depletes 2x faster."),
            Formatting.LIGHT_PURPLE),

    TOKITO("Tokito",
            "Descendants of the Tsugikuni, gifted with genius swordsmanship.",
            List.of("Mist Walker: 15% chance to dodge attacks.", "Genius: XP gain increased by 50%."),
            List.of("Forgetful: Death coordinates are never saved."),
            Formatting.AQUA),

    SHINAZUGAWA("Shinazugawa",
            "Carriers of the rare Marechi blood type.",
            List.of("Marechi Blood: Demons may hesitate to attack.", "Berserker: Strength II when below 3 hearts."),
            List.of("Targeted: Aggro range of demons doubled."),
            Formatting.WHITE),

    HIMEJIMA("Himejima",
            "A clan of immense physical stature and pious strength.",
            List.of("Stone Skin: Natural Armor +4.", "Unmovable: Immune to knockback."),
            List.of("Heavy: Cannot sprint for long durations."),
            Formatting.DARK_GRAY),

    TOMIOKA("Tomioka",
            "Stoic warriors who flow like water.",
            List.of("Flow: Blocking (Shield) reflects damage.", "Calm Mind: Immune to Nausea and Blindness."),
            List.of("Solitary: Villager trades are more expensive."),
            Formatting.BLUE),

    IGURO("Iguro",
            "A clan with a shadowed past, striking like serpents.",
            List.of("Serpent Strike: Critical hits apply Poison.", "Slither: Sneaking speed increased."),
            List.of("Light Sensitivity: Weakness I in bright daylight."),
            Formatting.DARK_GREEN),

    NONE("None", "No clan selected.", List.of(), List.of(), Formatting.GRAY);

    private final String displayName;
    private final String description;
    private final List<String> boons;
    private final List<String> flaws;
    private final Formatting color;

    Clan(String displayName, String description, List<String> boons, List<String> flaws, Formatting color) {
        this.displayName = displayName;
        this.description = description;
        this.boons = boons;
        this.flaws = flaws;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public List<String> getBoons() { return boons; }
    public List<String> getFlaws() { return flaws; }
    public Formatting getColor() { return color; }

    public static Clan fromString(String name) {
        for (Clan clan : values()) {
            if (clan.name().equalsIgnoreCase(name)) return clan;
        }
        return NONE;
    }
}