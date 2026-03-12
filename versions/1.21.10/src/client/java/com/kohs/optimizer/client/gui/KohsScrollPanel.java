package com.kohs.optimizer.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Scrollable panel with clipping, smooth scroll animation, and custom thin purple scrollbar.
 * Manages a list of widgets arranged vertically with padding.
 */
public class KohsScrollPanel {
    private final int x, y, width, height;
    private final int padding = 6;
    private final int itemSpacing = 4;

    private final List<ClickableWidget> widgets = new ArrayList<>();
    private float scrollOffset;
    private float targetScrollOffset;
    private int totalContentHeight;

    // Scrollbar
    private boolean scrollbarDragging;
    private double scrollbarDragStartY;
    private float scrollbarDragStartOffset;
    private static final int SCROLLBAR_WIDTH = 3;

    public KohsScrollPanel(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void clearWidgets() {
        widgets.clear();
        scrollOffset = 0;
        targetScrollOffset = 0;
        totalContentHeight = 0;
    }

    public <T extends ClickableWidget> T addWidget(T widget) {
        widgets.add(widget);
        recalculateLayout();
        return widget;
    }

    private void recalculateLayout() {
        int currentY = 0;
        for (ClickableWidget widget : widgets) {
            widget.setPosition(x + padding, y + padding + currentY);
            currentY += widget.getHeight() + itemSpacing;
        }
        totalContentHeight = currentY;
    }

    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Smooth scroll animation
        float scrollSpeed = 0.25f;
        float diff = targetScrollOffset - scrollOffset;
        if (Math.abs(diff) > 0.5f) {
            scrollOffset += diff * scrollSpeed;
        } else {
            scrollOffset = targetScrollOffset;
        }

        // Background
        ctx.fill(x, y, x + width, y + height, KohsGuiColors.CONTENT_BG);
        KohsGuiUtils.drawBorder(ctx, x, y, width, height, KohsGuiColors.BORDER);

        // Clipping via scissor
        ctx.enableScissor(x + 1, y + 1, x + width - 1, y + height - 1);

        // Render widgets with scroll offset
        int offsetY = (int) -scrollOffset;
        for (ClickableWidget widget : widgets) {
            int originalY = widget.getY();
            widget.setY(originalY + offsetY);

            // Only render visible widgets
            if (widget.getY() + widget.getHeight() > y && widget.getY() < y + height) {
                widget.render(ctx, mouseX, mouseY, delta);
            }

            widget.setY(originalY); // restore
        }

        ctx.disableScissor();

        // Scrollbar
        if (totalContentHeight > height) {
            drawScrollbar(ctx);
        }
    }

    private void drawScrollbar(DrawContext ctx) {
        int trackX = x + width - SCROLLBAR_WIDTH - 2;
        int trackY = y + 2;
        int trackH = height - 4;

        // Track
        ctx.fill(trackX, trackY, trackX + SCROLLBAR_WIDTH, trackY + trackH, KohsGuiColors.SCROLL_TRACK);

        // Thumb
        float viewRatio = (float) height / totalContentHeight;
        int thumbH = Math.max(12, (int) (trackH * viewRatio));
        float scrollRatio = scrollOffset / Math.max(1, totalContentHeight - height);
        int thumbY = trackY + (int) ((trackH - thumbH) * MathHelper.clamp(scrollRatio, 0, 1));

        int thumbColor = scrollbarDragging ? KohsGuiColors.ACCENT : KohsGuiColors.SCROLL_THUMB;
        ctx.fill(trackX, thumbY, trackX + SCROLLBAR_WIDTH, thumbY + thumbH, thumbColor);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            targetScrollOffset -= (float) (verticalAmount * 20);
            clampScroll();
            return true;
        }
        return false;
    }

    public boolean mouseClicked(Click click, boolean playSound) {
        double mouseX = click.x();
        double mouseY = click.y();

        // Check scrollbar
        if (totalContentHeight > height) {
            int trackX = x + width - SCROLLBAR_WIDTH - 2;
            if (mouseX >= trackX && mouseX <= trackX + SCROLLBAR_WIDTH + 2) {
                scrollbarDragging = true;
                scrollbarDragStartY = mouseY;
                scrollbarDragStartOffset = scrollOffset;
                return true;
            }
        }

        // Forward to widgets with scroll offset applied
        int offsetY = (int) -scrollOffset;
        for (ClickableWidget widget : widgets) {
            int originalY = widget.getY();
            int wy = widget.getY() + offsetY;
            if (mouseX >= widget.getX() && mouseX <= widget.getX() + widget.getWidth()
                && mouseY >= wy && mouseY <= wy + widget.getHeight()
                && wy + widget.getHeight() > y && wy < y + height) {
                widget.setY(wy);
                boolean result = widget.mouseClicked(click, playSound);
                widget.setY(originalY);
                return result;
            }
        }
        return false;
    }

    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        double mouseY = click.y();
        if (scrollbarDragging) {
            int trackH = height - 4;
            float viewRatio = (float) height / totalContentHeight;
            int thumbH = Math.max(12, (int) (trackH * viewRatio));
            float scrollableTrack = trackH - thumbH;

            double dragDelta = mouseY - scrollbarDragStartY;
            float scrollDelta = (float) (dragDelta / Math.max(1, scrollableTrack)) * (totalContentHeight - height);
            targetScrollOffset = scrollbarDragStartOffset + scrollDelta;
            clampScroll();
            return true;
        }

        // Forward drag to widgets
        int offsetY = (int) -scrollOffset;
        for (ClickableWidget widget : widgets) {
            int originalY = widget.getY();
            widget.setY(originalY + offsetY);
            boolean result = widget.mouseDragged(click, deltaX, deltaY);
            widget.setY(originalY);
            if (result) return true;
        }
        return false;
    }

    public boolean mouseReleased(Click click) {
        scrollbarDragging = false;

        int offsetY = (int) -scrollOffset;
        for (ClickableWidget widget : widgets) {
            int originalY = widget.getY();
            widget.setY(originalY + offsetY);
            widget.mouseReleased(click);
            widget.setY(originalY);
        }
        return false;
    }

    private void clampScroll() {
        float maxScroll = Math.max(0, totalContentHeight - height + padding * 2);
        targetScrollOffset = MathHelper.clamp(targetScrollOffset, 0, maxScroll);
    }

    public List<? extends Element> children() {
        return widgets;
    }

    public int getWidgetAreaWidth() {
        return width - padding * 2 - SCROLLBAR_WIDTH - 4;
    }
}
