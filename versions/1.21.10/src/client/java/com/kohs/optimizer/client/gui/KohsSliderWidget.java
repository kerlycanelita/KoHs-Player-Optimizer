package com.kohs.optimizer.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * Custom slider widget with gradient fill (purple → pink) and floating value label.
 * Supports both float and int modes.
 */
public class KohsSliderWidget extends ClickableWidget {
    private static final int TRACK_HEIGHT = 6;
    private static final int THUMB_WIDTH = 6;
    private static final int THUMB_HEIGHT = 12;

    private float value; // normalized 0..1
    private final float min;
    private final float max;
    private final float step;
    private final boolean intMode;
    private final Consumer<Float> onChanged;
    private final String label;
    private boolean dragging;

    public KohsSliderWidget(int x, int y, int width, String label, float min, float max, float currentValue, float step, boolean intMode, Consumer<Float> onChanged) {
        super(x, y, width, THUMB_HEIGHT + 8, Text.literal(label));
        this.label = label;
        this.min = min;
        this.max = max;
        this.step = step;
        this.intMode = intMode;
        this.onChanged = onChanged;
        this.value = (max > min) ? MathHelper.clamp((currentValue - min) / (max - min), 0, 1) : 0;
    }

    @Override
    public void onClick(Click click, boolean playSound) {
        updateValueFromMouse(click.x());
        dragging = true;
    }

    @Override
    protected void onDrag(Click click, double deltaX, double deltaY) {
        if (dragging) {
            updateValueFromMouse(click.x());
        }
    }

    @Override
    public void onRelease(Click click) {
        dragging = false;
    }

    private void updateValueFromMouse(double mouseX) {
        int trackStart = getX() + 2;
        int trackEnd = getX() + getWidth() - 2 - THUMB_WIDTH;
        float raw = (float) (mouseX - trackStart) / (float) Math.max(1, trackEnd - trackStart);
        this.value = MathHelper.clamp(raw, 0, 1);

        // Snap to step
        if (step > 0) {
            float actual = min + value * (max - min);
            actual = Math.round(actual / step) * step;
            this.value = MathHelper.clamp((actual - min) / (max - min), 0, 1);
        }

        float actual = min + value * (max - min);
        if (intMode) {
            actual = Math.round(actual);
        }
        onChanged.accept(actual);
    }

    @Override
    protected void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        float actualValue = min + value * (max - min);
        if (intMode) actualValue = Math.round(actualValue);

        // Label + value
        String valueText;
        if (intMode) {
            valueText = String.format(Locale.ROOT, "%s: %d", label, (int) actualValue);
        } else {
            valueText = String.format(Locale.ROOT, "%s: %.2f", label, actualValue);
        }

        int labelColor = this.isHovered() || dragging ? KohsGuiColors.TEXT_BRIGHT : KohsGuiColors.TEXT_PRIMARY;
        ctx.drawTextWithShadow(client.textRenderer, valueText, getX(), getY(), labelColor);

        // Track
        int trackY = getY() + 12;
        int trackX = getX();
        int trackW = getWidth();

        // Track background
        ctx.fill(trackX, trackY, trackX + trackW, trackY + TRACK_HEIGHT, KohsGuiColors.SLIDER_TRACK);

        // Gradient fill
        int fillWidth = (int) (trackW * value);
        if (fillWidth > 0) {
            KohsGuiUtils.drawGradientFill(ctx, trackX, trackY, fillWidth, TRACK_HEIGHT,
                KohsGuiColors.SLIDER_FILL_START, KohsGuiColors.SLIDER_FILL_END);
        }

        // Track border
        KohsGuiUtils.drawBorder(ctx, trackX, trackY, trackW, TRACK_HEIGHT, KohsGuiColors.BORDER);

        // Thumb
        int thumbX = trackX + (int) ((trackW - THUMB_WIDTH) * value);
        int thumbY = trackY - 3;

        int thumbColor = (this.isHovered() || dragging) ? KohsGuiColors.ACCENT : KohsGuiColors.SLIDER_THUMB;
        ctx.fill(thumbX, thumbY, thumbX + THUMB_WIDTH, thumbY + THUMB_HEIGHT, thumbColor);

        // Glow on hover/drag
        if (this.isHovered() || dragging) {
            KohsGuiUtils.drawGlowBorder(ctx, thumbX, thumbY, THUMB_WIDTH, THUMB_HEIGHT, KohsGuiColors.ACCENT, 2);
        }
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    public float getActualValue() {
        float actual = min + value * (max - min);
        return intMode ? Math.round(actual) : actual;
    }
}
