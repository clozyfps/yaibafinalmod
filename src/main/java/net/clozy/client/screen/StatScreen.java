package net.clozy.client.screen;

import net.clozy.network.YaibaNetworking;
import net.clozy.registry.Clan;
import net.clozy.registry.BreathingStyle;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class StatScreen extends Screen {

    // Sync Data
    public static int vitality, strength, swordsmanship, agility, level, exp, sp;
    public static Clan clan = Clan.NONE;
    public static BreathingStyle style = BreathingStyle.NONE;

    private boolean showStatsTab = true; // Toggle between Stats and Status

    // We store the buttons in a list so we can show/hide them easily
    private final List<ButtonWidget> statButtons = new ArrayList<>();

    public StatScreen() {
        super(Text.literal("Character Menu"));
    }

    @Override
    protected void init() {
        statButtons.clear(); // Clear old buttons on re-init
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Tab Buttons
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Attributes"), button -> {
                    showStatsTab = true;
                    updateButtonVisibility();
                })
                .dimensions(centerX - 100, centerY - 80, 95, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Status"), button -> {
                    showStatsTab = false;
                    updateButtonVisibility();
                })
                .dimensions(centerX + 5, centerY - 80, 95, 20).build());

        // Stat Upgrades (Added to our list to manage visibility)
        addStatButton(centerX + 60, centerY - 30, "Vitality");
        addStatButton(centerX + 60, centerY - 5, "Strength");
        addStatButton(centerX + 60, centerY + 20, "Swordsmanship");
        addStatButton(centerX + 60, centerY + 45, "Agility");

        // Ensure initial state is correct
        updateButtonVisibility();
    }

    private void addStatButton(int x, int y, String statName) {
        // Create the button normally
        ButtonWidget btn = ButtonWidget.builder(Text.literal("+"), button -> {
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeString(statName);
                    ClientPlayNetworking.send(YaibaNetworking.UPGRADE_STAT_PACKET, buf);
                })
                .dimensions(x, y, 20, 20)
                .build();

        // Add to screen
        this.addDrawableChild(btn);

        // Add to our list so we can control it
        this.statButtons.add(btn);
    }

    private void updateButtonVisibility() {
        // If we are on the Stats tab, show buttons. Otherwise, hide them.
        for (ButtonWidget btn : statButtons) {
            btn.visible = showStatsTab;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Background Panel
        context.fill(centerX - 110, centerY - 90, centerX + 110, centerY + 90, 0xFF101010);

        // Draw Border manually
        int borderX = centerX - 110;
        int borderY = centerY - 90;
        int borderW = 220;
        int borderH = 180;
        int color = 0xFF333333;

        context.fill(borderX, borderY, borderX + borderW, borderY + 1, color); // Top
        context.fill(borderX, borderY + borderH - 1, borderX + borderW, borderY + borderH, color); // Bottom
        context.fill(borderX, borderY, borderX + 1, borderY + borderH, color); // Left
        context.fill(borderX + borderW - 1, borderY, borderX + borderW, borderY + borderH, color); // Right

        // Header
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Level " + level).formatted(Formatting.GOLD), centerX, centerY - 105, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("SP: " + sp).formatted(Formatting.AQUA), centerX, centerY - 115, 0xFFFFFF);

        // EXP Bar
        int expBarWidth = 200;
        int expReq = level * 100;
        if (expReq == 0) expReq = 100; // Prevent divide by zero error

        int expFill = (int) (((float)exp / (float)expReq) * expBarWidth);
        context.fill(centerX - 100, centerY + 95, centerX + 100, centerY + 100, 0xFF555555);
        context.fill(centerX - 100, centerY + 95, centerX - 100 + expFill, centerY + 100, 0xFF00AA00);

        if (showStatsTab) {
            renderStatsTab(context, centerX, centerY);
        } else {
            renderStatusTab(context, centerX, centerY);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderStatsTab(DrawContext context, int cx, int cy) {
        drawStatRow(context, cx, cy - 30, "Vitality", vitality);
        drawStatRow(context, cx, cy - 5, "Strength", strength);
        drawStatRow(context, cx, cy + 20, "Swordsmanship", swordsmanship);
        drawStatRow(context, cx, cy + 45, "Agility", agility);
    }

    private void drawStatRow(DrawContext context, int cx, int y, String name, int val) {
        context.drawTextWithShadow(this.textRenderer, name, cx - 90, y + 6, 0xFFAAAAAA);
        context.drawTextWithShadow(this.textRenderer, String.valueOf(val), cx, y + 6, 0xFFFFFFFF);
    }

    private void renderStatusTab(DrawContext context, int cx, int cy) {
        // Clan Info
        context.drawTextWithShadow(this.textRenderer, Text.literal("Clan:").formatted(Formatting.GRAY), cx - 90, cy - 40, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, Text.literal(clan.getDisplayName()).formatted(clan.getColor()), cx - 90, cy - 30, 0xFFFFFF);

        // Breathing Info
        context.drawTextWithShadow(this.textRenderer, Text.literal("Breathing Style:").formatted(Formatting.GRAY), cx - 90, cy, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, Text.literal(style.getDisplayName()).formatted(style.getColor()), cx - 90, cy + 10, 0xFFFFFF);
    }
}