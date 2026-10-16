package com.tonywww.strongerwanderer.platform;

//? if forge {
import net.minecraftforge.common.ForgeConfigSpec;
//?} else {
/*import net.neoforged.neoforge.common.ModConfigSpec;
*///?}
public final class Settings {
    //? if forge {
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.Builder B = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.BooleanValue ENABLED, DEBUG;
    public static final ForgeConfigSpec.DoubleValue PHASE_HEALTH, DAMAGE, RANGE, RECOVERY, FOLLOW_CHANCE;
    public static final ForgeConfigSpec.IntValue COOLDOWN, PARTICLES;
    //?} else {
    /*public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();
    public static final ModConfigSpec.BooleanValue ENABLED, DEBUG;
    public static final ModConfigSpec.DoubleValue PHASE_HEALTH, DAMAGE, RANGE, RECOVERY, FOLLOW_CHANCE;
    public static final ModConfigSpec.IntValue COOLDOWN, PARTICLES;
    *///?}
    static {
        ENABLED = B.comment("启用新增技能 / Enable additional Warden skills").define("enabled", true);
        PHASE_HEALTH = B.comment("重力劈跃解锁生命比例 / Health fraction unlocking Gravity Leap").defineInRange("phaseTwoHealth", 0.5, 0.0, 1.0);
        DAMAGE = B.comment("技能伤害倍率，以原版攻击力为基准 / Skill damage scale relative to vanilla attack damage").defineInRange("damageScale", 1.0, 0.0, 10.0);
        RANGE = B.comment("技能范围与追击距离倍率 / Hit range and pursuit distance scale").defineInRange("rangeScale", 1.0, 0.25, 4.0);
        RECOVERY = B.comment("技能间隔秒数，近战时至多 0.6 秒 / Seconds between skills; capped at 0.6 in melee").defineInRange("recoverySeconds", 1.5, 0.5, 30.0);
        COOLDOWN = B.comment("同一招式冷却秒数，突进使用一半 / Per-skill cooldown seconds; rushes use half").defineInRange("skillCooldownSeconds", 12, 1, 120);
        FOLLOW_CHANCE = B.comment("普通狮子斩追加概率 / Follow-up chance after Lion's Claw").defineInRange("followupChance", 0.4, 0.0, 1.0);
        PARTICLES = B.comment("特效粒子百分比，0 为关闭 / Particle density percent; zero disables particles").defineInRange("particles", 100, 0, 200);
        DEBUG = B.comment("播报技能 / Announce skills").define("announceSkills", false);
        SPEC = B.build();
    }
    private Settings() {}
}
