package com.tonywww.strongerwanderer.platform.mixin;
import com.tonywww.strongerwanderer.combat.Skill;
import com.tonywww.strongerwanderer.combat.WardenAccess;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LivingEntity.class)
public abstract class TravelMixin {
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void sw$skillMotion(Vec3 input, CallbackInfo ci) {
        if ((Object)this instanceof WardenAccess w && w.sw$skill() != Skill.NONE) ci.cancel();
    }
}
