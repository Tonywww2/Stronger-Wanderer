package com.tonywww.strongerwanderer.platform;

//? if forge {
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.client.event.EntityRenderersEvent;
//?} else {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
*///?}
public final class ClientBootstrap {
    public static void register(IEventBus bus) { bus.addListener(ClientBootstrap::renderers); }
    private static void renderers(EntityRenderersEvent.RegisterRenderers event) { event.registerEntityRenderer(Entities.PHANTOM.get(), PhantomRenderer::new); }
    private ClientBootstrap() {}
}
