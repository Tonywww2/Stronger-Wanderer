package com.tonywww.strongerwanderer.platform;

import com.tonywww.strongerwanderer.combat.*;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.warden.Warden;
//? if forge {
import net.minecraftforge.event.RegisterCommandsEvent;
//?} else {
/*import net.neoforged.neoforge.event.RegisterCommandsEvent;
*///?}
public final class SkillCommands {
    public static void register(RegisterCommandsEvent event) {
        //? if >=26.1 {
        /*var root = Commands.literal("strongerwanderer").requires(source -> source.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER));
        *///?} else {
        var root = Commands.literal("strongerwanderer").requires(source -> source.hasPermission(2));
        //?}
        for (Skill skill : new Skill[]{Skill.LION_CLAW, Skill.FOLLOWUP_CLAW, Skill.GRAVITY_LEAP, Skill.FORWARD_PHANTOMS, Skill.SIDE_PHANTOMS}) {
            root.then(Commands.literal(skill.name().toLowerCase(java.util.Locale.ROOT))
                .then(Commands.argument("warden", EntityArgument.entity())
                    .then(Commands.argument("target", EntityArgument.entity()).executes(ctx -> {
                        var entity = EntityArgument.getEntity(ctx, "warden");
                        var victim = EntityArgument.getEntity(ctx, "target");
                        if (!(entity instanceof Warden w) || w instanceof PhantomWarden || !(victim instanceof net.minecraft.world.entity.LivingEntity living)) return 0;
                        boolean ok = ((WardenAccess)w).sw$combat().start(skill, living);
                        ctx.getSource().sendSuccess(() -> Component.literal(ok ? "Started " + skill.name() : "Invalid target"), false);
                        return ok ? 1 : 0;
                    }))));
        }
        event.getDispatcher().register(root);
    }
    private SkillCommands() {}
}
