package com.kohs.optimizer.client.gui;

/**
 * Futuristic dark-purple color palette for KoHs Player Optimizer GUI.
 * All colors are ARGB packed integers.
 */
public final class KohsGuiColors {
    private KohsGuiColors() {}

    // ── Backgrounds ────────────────────────────────────────────
    /** Full-screen dim overlay */
    public static final int SCREEN_DIM         = 0xD9080012;
    /** Main panel background */
    public static final int PANEL_BG           = 0xE60D0518;
    /** Secondary/inner panel background */
    public static final int PANEL_BG_LIGHT     = 0xCC1A0A2E;
    /** Tab content area */
    public static final int CONTENT_BG         = 0xB3110822;

    // ── Borders & accents ──────────────────────────────────────
    /** Default border */
    public static final int BORDER             = 0xFF2D0850;
    /** Highlighted / hovered border */
    public static final int BORDER_GLOW        = 0xFFA855F7;
    /** Active accent (selected tab underline, active toggle) */
    public static final int ACCENT             = 0xFFA855F7;
    /** Secondary accent (gradient end) */
    public static final int ACCENT_SECONDARY   = 0xFFEC4899;

    // ── Text ──────────────────────────────────────────────────
    /** Primary text */
    public static final int TEXT_PRIMARY        = 0xFFDAB3FF;
    /** Secondary / tooltip text */
    public static final int TEXT_SECONDARY      = 0xFF9A7BBF;
    /** Bright highlight text */
    public static final int TEXT_BRIGHT         = 0xFFF0E6FF;
    /** Disabled / inactive text */
    public static final int TEXT_DISABLED       = 0xFF5A4570;

    // ── Widgets ───────────────────────────────────────────────
    /** Toggle track OFF */
    public static final int TOGGLE_TRACK_OFF   = 0xFF1E0E35;
    /** Toggle track ON */
    public static final int TOGGLE_TRACK_ON    = 0xFF6B21A8;
    /** Toggle thumb */
    public static final int TOGGLE_THUMB       = 0xFFE9D5FF;
    /** Toggle thumb glow when ON */
    public static final int TOGGLE_THUMB_GLOW  = 0xFFA855F7;

    /** Slider track background */
    public static final int SLIDER_TRACK       = 0xFF1E0E35;
    /** Slider fill start (left) */
    public static final int SLIDER_FILL_START  = 0xFF7C3AED;
    /** Slider fill end (right) — gradient */
    public static final int SLIDER_FILL_END    = 0xFFEC4899;
    /** Slider thumb */
    public static final int SLIDER_THUMB       = 0xFFE9D5FF;

    /** Scrollbar track */
    public static final int SCROLL_TRACK       = 0x40FFFFFF;
    /** Scrollbar thumb */
    public static final int SCROLL_THUMB       = 0xFFA855F7;

    // ── Tab bar ───────────────────────────────────────────────
    /** Tab background (inactive) */
    public static final int TAB_INACTIVE       = 0x00000000;
    /** Tab background (hovered) */
    public static final int TAB_HOVER          = 0x33A855F7;
    /** Tab underline (active) */
    public static final int TAB_UNDERLINE      = 0xFFA855F7;

    // ── Utility ───────────────────────────────────────────────
    /** Button background */
    public static final int BUTTON_BG          = 0xFF2D0850;
    /** Button hovered */
    public static final int BUTTON_HOVER       = 0xFF4A1A6B;
    /** Success / green accent */
    public static final int SUCCESS            = 0xFF22C55E;
    /** Warning accent */
    public static final int WARNING            = 0xFFF59E0B;

    /**
     * Interpolate between two ARGB colors.
     */
    public static int lerp(int c1, int c2, float t) {
        if (t <= 0) return c1;
        if (t >= 1) return c2;

        int a1 = (c1 >> 24) & 0xFF, r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
        int a2 = (c2 >> 24) & 0xFF, r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Apply alpha to an existing color.
     */
    public static int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }
}
