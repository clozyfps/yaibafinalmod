package net.clozy.client.screen;

import net.clozy.network.YaibaNetworking;
import net.clozy.registry.BreathingStyle;
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

public class BreathingSelectionScreen extends Screen {
    private BreathingStyle selectedStyle = BreathingStyle.WATER;
    private float animationProgress = 0f;
    private final List<BreathingStyle> availableStyles;

    public BreathingSelectionScreen() {
        super(Text.literal("Select Breathing Style"));
        this.availableStyles = Arrays.stream(BreathingStyle.values())
                .filter(c -> c != BreathingStyle.NONE)
                .toList();
    }

    @Override
    protected void init() {
        int listWidth = 120;
        int startY = 40;

        for (int i = 0; i < availableStyles.size(); i++) {
            BreathingStyle style = availableStyles.get(i);
            int yPos = startY + (i * 22);

            this.addDrawableChild(ButtonWidget.builder(Text.literal(style.getDisplayName()), button -> {
                        this.selectedStyle = style;
                        this.animationProgress = 0f;
                    })
                    .dimensions(20, yPos, listWidth, 20)
                    .build());
        }

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Learn Technique").formatted(Formatting.BOLD), button -> {
                    confirmSelection();
                })
                .dimensions(this.width - 140, this.height - 40, 120, 20)
                .build());
    }

    private void confirmSelection() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(selectedStyle.name());
        ClientPlayNetworking.send(YaibaNetworking.SELECT_BREATHING_PACKET, buf);
        this.close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        context.fill(0, 0, this.width, this.height, 0xDD000000); // BG Dim
        context.fill(0, 0, 160, this.height, 0xFF101010); // Sidebar BG
        context.fill(160, 0, 161, this.height, 0xFF333333); // Sidebar Border

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, 80, 15, 0xFFFFFF);
        renderDetails(context, delta);
        super.render(context, mouseX, mouseY, delta);
    }

    private void renderDetails(DrawContext context, float delta) {
        int detailX = 180;
        int detailY = 40;
        int contentWidth = this.width - detailX - 20;

        if (animationProgress < 1.0f) {
            animationProgress += delta * 0.05f;
            if (animationProgress > 1.0f) animationProgress = 1.0f;
        }

        int alpha = (int) (animationProgress * 255);
        int textColor = (alpha << 24) | 0xFFFFFF;

        Integer styleColorVal = selectedStyle.getColor().getColorValue();
        int safeColor = (styleColorVal != null) ? styleColorVal : 0xFFFFFF;
        int headerColor = (alpha << 24) | safeColor;

        // Title
        context.getMatrices().push();
        context.getMatrices().translate(detailX, detailY, 0);
        context.getMatrices().scale(2.0f, 2.0f, 2.0f);
        context.drawTextWithShadow(this.textRenderer, selectedStyle.getDisplayName(), 0, 0, headerColor);
        context.getMatrices().pop();

        detailY += 30;
        context.drawTextWithShadow(this.textRenderer, Text.literal("Description:").formatted(Formatting.GRAY), detailX, detailY, textColor);
        detailY += 12;

        List<OrderedText> descLines = this.textRenderer.wrapLines(Text.literal(selectedStyle.getDescription()), contentWidth);
        for (OrderedText line : descLines) {
            context.drawTextWithShadow(this.textRenderer, line, detailX, detailY, textColor);
            detailY += 12;
        }

        detailY += 20;
        context.drawTextWithShadow(this.textRenderer, Text.literal("Forms (Moveset):").formatted(Formatting.GOLD), detailX, detailY, textColor);
        detailY += 15;

        for (String form : selectedStyle.getForms()) {
            context.drawTextWithShadow(this.textRenderer, Text.literal(form), detailX, detailY, textColor);
            detailY += 12;
        }
    }
}