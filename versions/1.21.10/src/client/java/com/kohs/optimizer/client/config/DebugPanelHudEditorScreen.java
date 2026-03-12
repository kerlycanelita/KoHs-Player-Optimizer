package com.kohs.optimizer.client.config;

import com.kohs.optimizer.client.gui.KohsGuiColors;
import com.kohs.optimizer.client.gui.KohsGuiUtils;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public final class DebugPanelHudEditorScreen extends Screen {
    private final Screen parent;
    private final OptimizerConfig workingCopy;
    private boolean dragging;
    private int dragOffsetX;
    private int dragOffsetY;
    private final int initialX;
    private final int initialY;
    private static final int PREVIEW_W = 240;
    private static final int PREVIEW_H = 136;

    public DebugPanelHudEditorScreen(Screen parent, OptimizerConfig workingCopy) {
        super(Text.literal("Debug Panel HUD Editor"));
        this.parent = parent;
        this.workingCopy = workingCopy;
        this.initialX = workingCopy.debug.realtimePanelX;
        this.initialY = workingCopy.debug.realtimePanelY;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, KohsGuiColors.SCREEN_DIM);

        int x = clampX(workingCopy.debug.realtimePanelX);
        int y = clampY(workingCopy.debug.realtimePanelY);
        workingCopy.debug.realtimePanelX = x;
        workingCopy.debug.realtimePanelY = y;

        context.fill(x, y, x + PREVIEW_W, y + PREVIEW_H, 0xB0150824);
        KohsGuiUtils.drawBorder(context, x, y, PREVIEW_W, PREVIEW_H, KohsGuiColors.ACCENT);
        context.drawTextWithShadow(textRenderer, "Realtime Debug Panel (Preview)", x + 8, y + 8, KohsGuiColors.TEXT_BRIGHT);
        context.drawTextWithShadow(textRenderer, "|- Rendering", x + 8, y + 22, KohsGuiColors.TEXT_PRIMARY);
        context.drawTextWithShadow(textRenderer, "|  |- alpha: 0.034", x + 8, y + 32, KohsGuiColors.TEXT_SECONDARY);
        context.drawTextWithShadow(textRenderer, "|- Combat", x + 8, y + 44, KohsGuiColors.TEXT_PRIMARY);
        context.drawTextWithShadow(textRenderer, "|  |- combo: 4", x + 8, y + 54, KohsGuiColors.TEXT_SECONDARY);
        context.drawTextWithShadow(textRenderer, "|- Timings", x + 8, y + 66, KohsGuiColors.TEXT_PRIMARY);
        context.drawTextWithShadow(textRenderer, "|  |- client.BlockPlacementFeature: 0.08", x + 8, y + 76, KohsGuiColors.TEXT_SECONDARY);

        int hintColor = 0xFFDAB3FF;
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("Drag the preview panel to set position"),
            width / 2, 18, hintColor);
        context.drawCenteredTextWithShadow(textRenderer,
            Text.literal("ENTER = Save | ESC = Cancel"),
            width / 2, 30, hintColor);

        drawButtons(context, mouseX, mouseY);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawButtons(DrawContext context, int mouseX, int mouseY) {
        int buttonY = height - 26;
        drawButton(context, mouseX, mouseY, width / 2 - 110, buttonY, 64, 16, "Save", KohsGuiColors.SUCCESS);
        drawButton(context, mouseX, mouseY, width / 2 - 34, buttonY, 64, 16, "Reset", KohsGuiColors.WARNING);
        drawButton(context, mouseX, mouseY, width / 2 + 42, buttonY, 64, 16, "Cancel", KohsGuiColors.BORDER);
    }

    private void drawButton(
        DrawContext context,
        int mouseX,
        int mouseY,
        int x,
        int y,
        int w,
        int h,
        String label,
        int border
    ) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        context.fill(x, y, x + w, y + h, hover ? KohsGuiColors.BUTTON_HOVER : KohsGuiColors.BUTTON_BG);
        KohsGuiUtils.drawBorder(context, x, y, w, h, border);
        int textWidth = textRenderer.getWidth(label);
        context.drawTextWithShadow(textRenderer, label, x + (w - textWidth) / 2, y + 4, KohsGuiColors.TEXT_PRIMARY);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput keyInput) {
        if (keyInput.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER
            || keyInput.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER) {
            saveAndClose();
            return true;
        }
        if (keyInput.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            cancelAndClose();
            return true;
        }
        return super.keyPressed(keyInput);
    }

    @Override
    public boolean mouseClicked(Click click, boolean playSound) {
        double mouseX = click.x();
        double mouseY = click.y();

        int previewX = workingCopy.debug.realtimePanelX;
        int previewY = workingCopy.debug.realtimePanelY;
        if (mouseX >= previewX && mouseX <= previewX + PREVIEW_W && mouseY >= previewY && mouseY <= previewY + PREVIEW_H) {
            dragging = true;
            dragOffsetX = (int) mouseX - previewX;
            dragOffsetY = (int) mouseY - previewY;
            return true;
        }

        int buttonY = height - 26;
        if (isInside(mouseX, mouseY, width / 2 - 110, buttonY, 64, 16)) {
            saveAndClose();
            return true;
        }
        if (isInside(mouseX, mouseY, width / 2 - 34, buttonY, 64, 16)) {
            workingCopy.debug.realtimePanelX = 8;
            workingCopy.debug.realtimePanelY = 56;
            return true;
        }
        if (isInside(mouseX, mouseY, width / 2 + 42, buttonY, 64, 16)) {
            cancelAndClose();
            return true;
        }

        return super.mouseClicked(click, playSound);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (dragging) {
            int targetX = (int) click.x() - dragOffsetX;
            int targetY = (int) click.y() - dragOffsetY;
            workingCopy.debug.realtimePanelX = clampX(targetX);
            workingCopy.debug.realtimePanelY = clampY(targetY);
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        dragging = false;
        return super.mouseReleased(click);
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private int clampX(int x) {
        return MathHelper.clamp(x, 2, Math.max(2, width - PREVIEW_W - 2));
    }

    private int clampY(int y) {
        return MathHelper.clamp(y, 2, Math.max(2, height - PREVIEW_H - 28));
    }

    private void saveAndClose() {
        if (client != null) {
            client.setScreen(parent);
        }
    }

    private void cancelAndClose() {
        workingCopy.debug.realtimePanelX = initialX;
        workingCopy.debug.realtimePanelY = initialY;
        if (client != null) {
            client.setScreen(parent);
        }
    }
}
