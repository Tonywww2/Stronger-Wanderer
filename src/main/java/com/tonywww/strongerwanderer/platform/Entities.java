package com.tonywww.strongerwanderer.platform;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.warden.Warden;
//? if forge {
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
//?} else {
/*import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
*///?}

public final class Entities {
    public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(Registries.ENTITY_TYPE, StrongerWanderer.ID);
    public static final Supplier<EntityType<PhantomWarden>> PHANTOM = REGISTER.register("phantom", () -> {
        var builder = EntityType.Builder.<PhantomWarden>of(PhantomWarden::new, MobCategory.MISC)
            .sized(0.9f, 2.9f).clientTrackingRange(12).updateInterval(1).noSave().noSummon();
        //? if >=26.1 {
        /*return builder.build(net.minecraft.resources.ResourceKey.create(Registries.ENTITY_TYPE,
            net.minecraft.resources.Identifier.fromNamespaceAndPath(StrongerWanderer.ID, "phantom")));
        *///?} else {
        return builder.build(StrongerWanderer.ID + ":phantom");
        //?}
    });
    public static void attributes(EntityAttributeCreationEvent event) { event.put(PHANTOM.get(), Warden.createAttributes().build()); }
    private Entities() {}
}
