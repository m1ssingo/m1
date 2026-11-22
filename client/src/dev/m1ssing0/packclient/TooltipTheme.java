package dev.m1ssing0.packclient;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

@EventBusSubscriber(modid = "m1packclient", value = Dist.CLIENT)
public final class TooltipTheme {
    private TooltipTheme() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void tint(RenderTooltipEvent.Color event) {
        if (event.getBackgroundStart() == event.getOriginalBackgroundStart()
            && event.getBackgroundEnd() == event.getOriginalBackgroundEnd()) {
            event.setBackgroundStart(0xf2271b15);
            event.setBackgroundEnd(0xf20f0d0c);
        }
        if (event.getBorderStart() == event.getOriginalBorderStart()
            && event.getBorderEnd() == event.getOriginalBorderEnd()) {
            event.setBorderStart(SteelButtonStyle.ORANGE);
            event.setBorderEnd(0xff864019);
        }
    }
}
