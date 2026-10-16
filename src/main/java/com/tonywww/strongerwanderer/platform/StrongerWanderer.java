package com.tonywww.strongerwanderer.platform;

//? if forge {
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.api.distmarker.Dist;
//?} else {
/*import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;
*///?}

@Mod(StrongerWanderer.ID)
public final class StrongerWanderer {
    public static final String ID = "strongerwanderer";
    //? if forge {
    public StrongerWanderer() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Settings.SPEC);
        MinecraftForge.EVENT_BUS.addListener(SkillCommands::register);
    //?} else {
    /*public StrongerWanderer(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, Settings.SPEC);
        NeoForge.EVENT_BUS.addListener(SkillCommands::register);
    *///?}
        Entities.REGISTER.register(bus);
        bus.addListener(Entities::attributes);
        //? if >=26.1 {
        /*if (FMLEnvironment.getDist() == Dist.CLIENT) ClientBootstrap.register(bus);
        *///?} else {
        if (FMLEnvironment.dist == Dist.CLIENT) ClientBootstrap.register(bus);
        //?}
    }
}
