package com.tonywww.strongerwanderer.combat;

public enum Skill {
    NONE(0, 0, 0, 0), LION_CLAW(52, 10, 28, 3.5),
    FOLLOWUP_CLAW(52, 5, 23, 3.8), GRAVITY_LEAP(115, 44, 72, 5.5),
    FORWARD_PHANTOMS(68, 39, 47, 0.8), SIDE_PHANTOMS(56, 27, 37, 1.0),
    PHANTOM_FORWARD(22, 2, 10, 0), PHANTOM_SIDE(30, 10, 18, 0.8),
    PHANTOM_LEAP(26, 3, 17, 3.0);
    public final int duration, launch, impact;
    public final double height;
    Skill(int duration, int launch, int impact, double height) {
        this.duration = duration; this.launch = launch; this.impact = impact; this.height = height;
    }
    public boolean projection() { return ordinal() >= PHANTOM_FORWARD.ordinal(); }
    public boolean rush() { return this == FORWARD_PHANTOMS || this == SIDE_PHANTOMS || this == PHANTOM_FORWARD || this == PHANTOM_SIDE; }
    public static Skill byId(int id) { return id >= 0 && id < values().length ? values()[id] : NONE; }
}
