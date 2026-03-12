package com.kohs.optimizer.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class KohsButtonWidget extends ClickableWidget {
    private final Supplier<String> labelSupplier;
    private final Runnable onPressed;
    private final BooleanSupplier activeSupplier;

    public KohsButtonWidget(
        int x,
        int y,
        int width,
        int height,
        Supplier<String> labelSupplier,
        Runnable onPressed
    ) {
        this(x, y, width, height, labelSupplier, onPressed, () -> false);
    }

    public KohsButtonWidget(
        int x,
        int y,
        int width,
        int height,
        Supplier<String> labelSupplier,
        Runnable onPressed,
        BooleanSupplier activeSupplier
    ) {
        super(x, y, width, height, Text.literal(labelSupplier.get()));
        this.labelSupplier = labelSupplier;
        this.onPressed = onPressed;
        this.activeSupplier = activeSupplier;
    }

    @Override
    public void onClick(Click click, boolean playSound) {
        if (active) {
            onPressed.run();
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        MinecraftClient client = MinecraftClient.getInstance();
        String text = labelSupplier.get();
        this.setMessage(Text.literal(text));

        boolean selected = activeSupplier.getAsBoolean();
        int baseColor = selected ? 0xAA7C3AED : KohsGuiColors.BUTTON_BG;
        int hoverColor = selected ? 0xCC9333EA : KohsGuiColors.BUTTON_HOVER;
        int bg = isHovered() ? hoverColor : baseColor;

        context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bg);
        KohsGuiUtils.drawBorder(context, getX(), getY(), getWidth(), getHeight(),
            selected ? KohsGuiColors.ACCENT : KohsGuiColors.BORDER);

        int textColor = selected ? KohsGuiColors.TEXT_BRIGHT : KohsGuiColors.TEXT_PRIMARY;
        int textWidth = client.textRenderer.getWidth(text);
        int textX = getX() + (getWidth() - textWidth) / 2;
        int textY = getY() + (getHeight() - 8) / 2;
        context.drawTextWithShadow(client.textRenderer, text, textX, textY, textColor);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }
}
