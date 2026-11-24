package com.org.serb.ecudla;

import javafx.scene.paint.Color;

/**
 * Visual theme only. Instrument values and the link to the ECU are unchanged.
 */
public final class DashboardTheme {
    public final String id;
    public final String name;
    public final String styleClass;
    public final Color ink;
    public final Color muted;
    public final Color accent;
    public final Color surface;
    public final Color field;
    public final Color line;

    private DashboardTheme(String id, String name, String styleClass,
                           Color ink, Color muted, Color accent,
                           Color surface, Color field, Color line) {
        this.id = id;
        this.name = name;
        this.styleClass = styleClass;
        this.ink = ink;
        this.muted = muted;
        this.accent = accent;
        this.surface = surface;
        this.field = field;
        this.line = line;
    }

    public static final DashboardTheme NIGHT = new DashboardTheme(
            "night", "Night", "theme-night",
            Color.web("#e8eef7"), Color.web("#8ea0b8"), Color.web("#4cc9f0"),
            Color.web("#151d2b"), Color.web("#0e1622"), Color.web("#2c3b52"));

    public static final DashboardTheme DAY = new DashboardTheme(
            "day", "Day", "theme-day",
            Color.web("#1c2430"), Color.web("#5c6b7e"), Color.web("#0b6bcb"),
            Color.web("#ffffff"), Color.web("#f4f7fb"), Color.web("#d5dee8"));

    public static final DashboardTheme AMBER = new DashboardTheme(
            "amber", "Cockpit", "theme-amber",
            Color.web("#ffe0a8"), Color.web("#c9a36a"), Color.web("#ffb000"),
            Color.web("#2a1d0c"), Color.web("#1a1208"), Color.web("#5c4320"));

    public static final DashboardTheme[] ALL = {NIGHT, DAY, AMBER};

    public static DashboardTheme byId(String id) {
        if (id == null) {
            return NIGHT;
        }
        for (DashboardTheme theme : ALL) {
            if (theme.id.equals(id)) {
                return theme;
            }
        }
        return NIGHT;
    }

    @Override
    public String toString() {
        return name;
    }
}
