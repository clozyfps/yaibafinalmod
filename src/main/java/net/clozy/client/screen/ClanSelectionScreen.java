package net.clozy.client.screen;

import net.clozy.network.YaibaNetworking;
import net.clozy.registry.Clan;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.List;

public class ClanSelectionScreen extends Screen {
    private Clan selectedClan = Clan.KAMADO; // Default selection
    private float animationProgress = 0f;
    private final List<Clan> availableClans;

    public ClanSelectionScreen() {
        super(Text.literal("Select Your Lineage"));
        // Filter out NONE
        this.availableClans = Arrays.stream(Clan.values())
                .filter(c -> c != Clan.NONE)
                .toList();
    }

    @Override
    protected void init() {
        int listWidth = 100;
        int startY = 40;

        // Create buttons for each clan on the left side
        for (int i = 0; i < availableClans.size(); i++) {
            Clan clan = availableClans.get(i);
            int yPos = startY + (i * 22);

            this.addDrawableChild(ButtonWidget.builder(Text.literal(clan.getDisplayName()), button -> {
                        this.selectedClan = clan;
                        this.animationProgress = 0f; // Reset animation on switch
                    })
                    .dimensions(20, yPos, listWidth, 20)
                    .build());
        }

        // Confirm Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Confirm Selection").formatted(Formatting.BOLD), button -> {
                    confirmSelection();
                })
                .dimensions(this.width - 140, this.height - 40, 120, 20)
                .build());
    }

    private void confirmSelection() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(selectedClan.name());
        ClientPlayNetworking.send(YaibaNetworking.SELECT_CLAN_PACKET, buf);

        // Removed "this.close()" here.
        // We want the screen to persist for the split second until the server
        // sends the "Open Breathing UI" packet, which will automatically replace this screen.
        // This makes the transition smoother (no flash of the world in between).
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);

        // Darken background
        context.fill(0, 0, this.width, this.height, 0xDD000000);

        // Sidebar Background
        context.fill(0, 0, 140, this.height, 0xFF101010);

        // Sidebar Border (Vertical Line)
        context.fill(140, 0, 141, this.height, 0xFF333333);

        // Title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, 70, 15, 0xFFFFFF);

        // Render Details Area
        renderDetails(context, delta);

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderDetails(DrawContext context, float delta) {
        int detailX = 160;
        int detailY = 40;
        int contentWidth = this.width - detailX - 20;

        // Smooth animation
        if (animationProgress < 1.0f) {
            animationProgress += delta * 0.05f;
            if (animationProgress > 1.0f) animationProgress = 1.0f;
        }

        int alpha = (int) (animationProgress * 255);
        int textColor = (alpha << 24) | 0xFFFFFF;

        Integer clanColorVal = selectedClan.getColor().getColorValue();
        int safeClanColor = (clanColorVal != null) ? clanColorVal : 0xFFFFFF;
        int headerColor = (alpha << 24) | safeClanColor;

        // Clan Name (Large)
        context.getMatrices().push();
        context.getMatrices().translate(detailX, detailY, 0);
        context.getMatrices().scale(2.0f, 2.0f, 2.0f);
        context.drawTextWithShadow(this.textRenderer, selectedClan.getDisplayName(), 0, 0, headerColor);
        context.getMatrices().pop();

        // Description Header
        detailY += 30;
        context.drawTextWithShadow(this.textRenderer, Text.literal("History:").formatted(Formatting.GRAY), detailX, detailY, textColor);
        detailY += 12;

        // Description Body (Manually Wrapped)
        List<OrderedText> descriptionLines = this.textRenderer.wrapLines(Text.literal(selectedClan.getDescription()), contentWidth);
        for (OrderedText line : descriptionLines) {
            context.drawTextWithShadow(this.textRenderer, line, detailX, detailY, textColor);
            detailY += 12;
        }

        // Boons
        detailY += 15; // Extra spacing after description
        context.drawTextWithShadow(this.textRenderer, Text.literal("Boons (Advantages):").formatted(Formatting.GREEN), detailX, detailY, textColor);
        detailY += 15;
        for (String boon : selectedClan.getBoons()) {
            context.drawTextWithShadow(this.textRenderer, Text.literal("+" + boon), detailX, detailY, textColor);
            detailY += 12;
        }

        // Flaws
        detailY += 15;
        context.drawTextWithShadow(this.textRenderer, Text.literal("Flaws (Disadvantages):").formatted(Formatting.RED), detailX, detailY, textColor);
        detailY += 15;
        for (String flaw : selectedClan.getFlaws()) {
            context.drawTextWithShadow(this.textRenderer, Text.literal("-" + flaw), detailX, detailY, textColor);
            detailY += 12;
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false; // Force selection
    }
}