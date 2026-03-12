package com.kohs.optimizer.client.gui;

import net.minecraft.client.gui.DrawContext;

/**
 * Drawing utilities for the futuristic KoHs GUI.
 * Provides gradient fills, glow borders, and animated rendering helpers.
 */
public final class KohsGuiUtils {
    private KohsGuiUtils() {}

    /**
     * Draw a horizontal gradient rectangle.
     */
    public static void fillHorizontalGradient(DrawContext ctx, int x1, int y1, int x2, int y2, int colorLeft, int colorRight) {
        ctx.fillGradient(x1, y1, x2, y2, colorLeft, colorRight);
    }

    /**
     * Draw a vertical gradient rectangle.
     */
    public static void fillVerticalGradient(DrawContext ctx, int x1, int y1, int x2, int y2, int colorTop, int colorBottom) {
        ctx.fillGradient(x1, y1, x2, y2, colorTop, colorBottom);
    }

    /**
     * Draw a rectangle border (1px outline).
     */
    public static void drawBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        // Top
        ctx.fill(x, y, x + w, y + 1, color);
        // Bottom
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        // Left
        ctx.fill(x, y + 1, x + 1, y + h - 1, color);
        // Right
        ctx.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    /**
     * Draw a glow border (multi-layer expanding border with decreasing alpha).
     */
    public static void drawGlowBorder(DrawContext ctx, int x, int y, int w, int h, int baseColor, int layers) {
        for (int i = 0; i < layers; i++) {
            int alpha = Math.max(10, 80 - i * 25);
            int color = KohsGuiColors.withAlpha(baseColor, alpha);
            drawBorder(ctx, x - i, y - i, w + i * 2, h + i * 2, color);
        }
    }

    /**
     * Draw a filled rectangle with border.
     */
    public static void drawPanel(DrawContext ctx, int x, int y, int w, int h, int bgColor, int borderColor) {
        ctx.fill(x, y, x + w, y + h, bgColor);
        drawBorder(ctx, x, y, w, h, borderColor);
    }

    /**
     * Draw a horizontal line.
     */
    public static void drawHLine(DrawContext ctx, int x1, int x2, int y, int color) {
        ctx.fill(Math.min(x1, x2), y, Math.max(x1, x2), y + 1, color);
    }

    /**
     * Smooth step easing (for animations).
     */
    public static float smoothStep(float t) {
        t = Math.max(0, Math.min(1, t));
        return t * t * (3.0f - 2.0f * t);
    }

    /**
     * Ease out cubic.
     */
    public static float easeOutCubic(float t) {
        float f = 1.0f - t;
        return 1.0f - f * f * f;
    }

    /**
     * Ease in-out quad.
     */
    public static float easeInOutQuad(float t) {
        return t < 0.5f ? 2.0f * t * t : 1.0f - (-2.0f * t + 2.0f) * (-2.0f * t + 2.0f) / 2.0f;
    }

    /**
     * Draw a gradient slider fill between two colors.
     */
    public static void drawGradientFill(DrawContext ctx, int x, int y, int width, int height, int colorStart, int colorEnd) {
        if (width <= 0) return;
        // Use Minecraft's built-in gradient (top-to-bottom), we rotate via horizontal segments
        int steps = Math.min(width, 32);
        int segWidth = Math.max(1, width / steps);

        for (int i = 0; i < steps; i++) {
            float t = (float) i / Math.max(1, steps - 1);
            int color = KohsGuiColors.lerp(colorStart, colorEnd, t);
            int sx = x + i * segWidth;
            int ex = (i == steps - 1) ? x + width : sx + segWidth;
            ctx.fill(sx, y, ex, y + height, color);
        }
    }
}
