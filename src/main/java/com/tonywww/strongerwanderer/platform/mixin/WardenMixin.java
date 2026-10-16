package com.tonywww.strongerwanderer.platform.mixin;

import com.tonywww.strongerwanderer.combat.*;
import com.tonywww.strongerwanderer.platform.PhantomWarden;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.monster.warden.Warden;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Warden.class)
public abstract class WardenMixin implements WardenAccess {
    @Unique private static final EntityDataAccessor<Integer> SW_SKILL = SynchedEntityData.defineId(Warden.class, EntityDataSerializers.INT);
    @Unique private static final EntityDataAccessor<Long> SW_STARTED = SynchedEntityData.defineId(Warden.class, EntityDataSerializers.LONG);
    @Unique private WardenCombat sw$controller;
    //? if forge {
    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void sw$define(CallbackInfo ci) {
        var data = ((Warden)(Object)this).getEntityData();
        data.define(SW_SKILL, 0); data.define(SW_STARTED, 0L);
    }
    //?} else {
    /*@Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void sw$define(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(SW_SKILL, 0); builder.define(SW_STARTED, 0L);
    }
    *///?}
    @Override public Skill sw$skill() { return Skill.byId(((Warden)(Object)this).getEntityData().get(SW_SKILL)); }
    @Override public long sw$started() { return ((Warden)(Object)this).getEntityData().get(SW_STARTED); }
    @Override public void sw$sync(Skill skill, long started) {
        var data = ((Warden)(Object)this).getEntityData();
        data.set(SW_STARTED, started); data.set(SW_SKILL, skill.ordinal());
    }
    @Override public WardenCombat sw$combat() {
        if (sw$controller == null) sw$controller = new WardenCombat((Warden)(Object)this);
        return sw$controller;
    }
    @Inject(method = "tick", at = @At("TAIL"))
    private void sw$tick(CallbackInfo ci) {
        Warden w = (Warden)(Object)this;
        if (w.level() instanceof net.minecraft.server.level.ServerLevel && !(w instanceof PhantomWarden)) sw$combat().tick();
    }
    @Inject(method = "customServerAiStep", at = @At("HEAD"), cancellable = true)
    private void sw$pauseBrain(CallbackInfo ci) {
        sw$combat().beforeBrain();
        if (sw$skill() != Skill.NONE) ci.cancel();
    }
}
