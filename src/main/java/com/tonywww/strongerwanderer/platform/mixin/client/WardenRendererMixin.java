//? if >=26.1 {
/*package com.tonywww.strongerwanderer.platform.mixin.client;
import com.tonywww.strongerwanderer.client.PoseSnapshot;
import com.tonywww.strongerwanderer.combat.WardenAccess;
import net.minecraft.client.renderer.entity.WardenRenderer;
import net.minecraft.client.renderer.entity.state.WardenRenderState;
import net.minecraft.world.entity.monster.warden.Warden;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(WardenRenderer.class)
public abstract class WardenRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/monster/warden/Warden;Lnet/minecraft/client/renderer/entity/state/WardenRenderState;F)V", at = @At("TAIL"))
    private void sw$snapshot(Warden w, WardenRenderState state, float partialTick, CallbackInfo ci) {
        WardenAccess data = (WardenAccess)w;
        ((PoseSnapshot)state).sw$pose(data.sw$skill(), w.level().getGameTime() - data.sw$started() + partialTick);
    }
}
*///?}
