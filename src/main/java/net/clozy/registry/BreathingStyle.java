package net.clozy.registry;

import net.minecraft.util.Formatting;
import java.util.List;

public enum BreathingStyle {
    WATER("Water Breathing",
            "A breathing style that mimics the flow and flexibility of water.",
            List.of(
                    "1. Water Surface Slash",
                    "2. Water Wheel",
                    "3. Flowing Dance",
                    "4. Striking Tide",
                    "5. Blessed Rain After the Drought",
                    "6. Whirlpool",
                    "7. Drop Ripple Thrust",
                    "8. Waterfall Basin",
                    "9. Splashing Water Flow",
                    "10. Constant Flux",
                    "11. Dead Calm"
            ),
            Formatting.BLUE),

    THUNDER("Thunder Breathing",
            "A style that mimics lightning, focusing on blinding speed.",
            List.of(
                    "1. Thunderclap and Flash",
                    "2. Rice Spirit",
                    "3. Thunder Swarm",
                    "4. Distant Thunder",
                    "5. Heat Lightning",
                    "6. Rumble and Flash",
                    "7. Honoikazuchi no Kami"
            ),
            Formatting.YELLOW),

    WIND("Wind Breathing",
            "An offensive style that mimics tornadoes and gusts of air.",
            List.of(
                    "1. Dust Whirlwind Cutter",
                    "2. Claws-Purifying Wind",
                    "3. Clean Storm Wind Tree",
                    "4. Rising Dust Storm",
                    "5. Cold Mountain Wind",
                    "6. Black Wind Mountain Mist",
                    "7. Gale, Sudden Gusts",
                    "8. Primary Gale Slash",
                    "9. Idaten Typhoon"
            ),
            Formatting.GREEN),

    MIST("Mist Breathing",
            "A derivative of Wind that confuses opponents with obscuring movements.",
            List.of(
                    "1. Low Clouds, Distant Haze",
                    "2. Eight-Layered Mist",
                    "3. Scattering Mist Splash",
                    "4. Shifting Flow Slash",
                    "5. Sea of Clouds and Haze",
                    "6. Lunar Dispersing Mist",
                    "7. Obscuring Clouds"
            ),
            Formatting.AQUA),

    LOVE("Love Breathing",
            "A style based on intense emotions and whip-like attacks.",
            List.of(
                    "1. Shivers of First Love",
                    "2. Love Pangs",
                    "3. Catlove Shower",
                    "4. Unknown Form",
                    "5. Swaying Love, Wildclaw",
                    "6. Cat-Legged Winds of Love"
            ),
            Formatting.LIGHT_PURPLE),

    FLOWER("Flower Breathing",
            "Graceful attacks that mimic flowers and fruits.",
            List.of(
                    "1. Unknown Form",
                    "2. Honorable Shadow Plum",
                    "3. Unknown Form",
                    "4. Crimson Hanagoromo",
                    "5. Peonies of Futility",
                    "6. Whirling Peach",
                    "Final. Equinoctial Vermilion Eye"
            ),
            Formatting.RED),

    SOUND("Sound Breathing",
            "A flashy style that uses explosives and reads enemy movements like a score.",
            List.of(
                    "1. Roar",
                    "2. Unknown Form",
                    "3. Unknown Form",
                    "4. Constant Resounding Slashes",
                    "5. String Performance"
            ),
            Formatting.GOLD),

    NONE("None", "No breathing style selected.", List.of(), Formatting.GRAY);

    private final String displayName;
    private final String description;
    private final List<String> forms;
    private final Formatting color;

    BreathingStyle(String displayName, String description, List<String> forms, Formatting color) {
        this.displayName = displayName;
        this.description = description;
        this.forms = forms;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public List<String> getForms() { return forms; }
    public Formatting getColor() { return color; }

    public static BreathingStyle fromString(String name) {
        for (BreathingStyle style : values()) {
            if (style.name().equalsIgnoreCase(name)) return style;
        }
        return NONE;
    }
}