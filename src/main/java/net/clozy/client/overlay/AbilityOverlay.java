package net.clozy.client.overlay;

import net.clozy.registry.BreathingStyle;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AbilityOverlay implements HudRenderCallback {

    public static BreathingStyle currentStyle = BreathingStyle.NONE;
    public static int abilityIndex = 0;

    // Stats for unlocking
    public static int swordsmanship = 1;
    public static float breathingProgress = 0f;

    @Override
    public void onHudRender(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || currentStyle == BreathingStyle.NONE) return;

        int height = client.getWindow().getScaledHeight();
        int x = 10;
        int y = height - 40;

        // --- Breathing Bar (Vertical) ---
        int barHeight = 60;
        int barWidth = 4;
        int barX = x - 6; // To the left of text
        int barY = y - barHeight + 10;

        // Background
        context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);

        // Filled portion (grows from bottom)
        int fillHeight = (int) ((breathingProgress / 100f) * barHeight);
        int fillY = (barY + barHeight) - fillHeight;

        // Color based on charge
        int color = 0xFF00AAFF; // Blue
        if (breathingProgress >= 100f) color = 0xFFFFAA00; // Gold when full

        context.fill(barX, fillY, barX + barWidth, barY + barHeight, color);

        // --- Text Info ---
        Integer colorVal = currentStyle.getColor().getColorValue();
        int textColor = (colorVal != null) ? colorVal : 0xFFFFFF;

        context.drawTextWithShadow(client.textRenderer,
                Text.literal(currentStyle.getDisplayName()).formatted(Formatting.BOLD),
                x, y, textColor);

        y += 12;
        String formName = "None";
        if (!currentStyle.getForms().isEmpty() && abilityIndex < currentStyle.getForms().size()) {
            formName = currentStyle.getForms().get(abilityIndex);
        }

        // Logic: Form 1 unlocks at lvl 10, Form 2 at lvl 20, etc.
        int requiredLevel = (abilityIndex + 1) * 10;
        boolean unlocked = swordsmanship >= requiredLevel;

        if (unlocked) {
            context.drawTextWithShadow(client.textRenderer,
                    Text.literal(formName).formatted(Formatting.WHITE),
                    x, y, 0xFFFFFF);
        } else {
            context.drawTextWithShadow(client.textRenderer,
                    Text.literal(formName + " [Locked: " + requiredLevel + " Swd]").formatted(Formatting.GRAY),
                    x, y, 0xAAAAAA);
        }
    }
}