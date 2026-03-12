package com.kohs.optimizer.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;

import java.util.function.Consumer;

/**
 * Animated toggle switch widget with sliding thumb and glow effect.
 * Renders a pill-shaped track with a circular thumb that slides ON/OFF.
 */
public class KohsToggleWidget extends ClickableWidget {
    private static final int TRACK_WIDTH = 32;
    private static final int TRACK_HEIGHT = 14;
    private static final int THUMB_SIZE = 10;

    private boolean value;
    private float animationProgress; // 0 = OFF, 1 = ON
    private final Consumer<Boolean> onChanged;
    private final String label;

    public KohsToggleWidget(int x, int y, int totalWidth, String label, boolean initialValue, Consumer<Boolean> onChanged) {
        super(x, y, totalWidth, TRACK_HEIGHT + 2, Text.literal(label));
        this.label = label;
        this.value = initialValue;
        this.animationProgress = initialValue ? 1.0f : 0.0f;
        this.onChanged = onChanged;
    }

    @Override
    public void onClick(Click click, boolean playSound) {
        this.value = !this.value;
        this.onChanged.accept(this.value);
    }

    @Override
    protected void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Animate
        float target = value ? 1.0f : 0.0f;
        float speed = 0.15f;
        if (animationProgress < target) {
            animationProgress = Math.min(animationProgress + speed, target);
        } else if (animationProgress > target) {
            animationProgress = Math.max(animationProgress - speed, target);
        }

        float eased = KohsGuiUtils.smoothStep(animationProgress);

        // Label
        int labelColor = this.isHovered() ? KohsGuiColors.TEXT_BRIGHT : KohsGuiColors.TEXT_PRIMARY;
        ctx.drawTextWithShadow(client.textRenderer, label, getX(), getY() + 3, labelColor);

        // Track position (right-aligned within widget)
        int trackX = getX() + getWidth() - TRACK_WIDTH - 2;
        int trackY = getY() + 1;

        // Track background
        int trackColor = KohsGuiColors.lerp(KohsGuiColors.TOGGLE_TRACK_OFF, KohsGuiColors.TOGGLE_TRACK_ON, eased);
        ctx.fill(trackX, trackY, trackX + TRACK_WIDTH, trackY + TRACK_HEIGHT, trackColor);

        // Track border
        int borderColor = KohsGuiColors.lerp(KohsGuiColors.BORDER, KohsGuiColors.BORDER_GLOW, eased);
        KohsGuiUtils.drawBorder(ctx, trackX, trackY, TRACK_WIDTH, TRACK_HEIGHT, borderColor);

        // Glow on hover
        if (this.isHovered()) {
            KohsGuiUtils.drawGlowBorder(ctx, trackX, trackY, TRACK_WIDTH, TRACK_HEIGHT, KohsGuiColors.ACCENT, 2);
        }

        // Thumb
        int thumbTravel = TRACK_WIDTH - THUMB_SIZE - 4;
        int thumbX = trackX + 2 + (int) (thumbTravel * eased);
        int thumbY = trackY + 2;

        int thumbColor = value ? KohsGuiColors.TOGGLE_THUMB_GLOW : KohsGuiColors.TOGGLE_THUMB;
        ctx.fill(thumbX, thumbY, thumbX + THUMB_SIZE, thumbY + THUMB_SIZE, thumbColor);

        // Thumb glow when ON
        if (eased > 0.5f) {
            int glowAlpha = (int) (40 * eased);
            ctx.fill(thumbX - 1, thumbY - 1, thumbX + THUMB_SIZE + 1, thumbY + THUMB_SIZE + 1,
                KohsGuiColors.withAlpha(KohsGuiColors.ACCENT, glowAlpha));
        }
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        // Silent — futuristic feel
    }

    @Override
    protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }
}
