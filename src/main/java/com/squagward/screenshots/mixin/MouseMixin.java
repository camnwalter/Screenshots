package com.squagward.screenshots.mixin;

import com.squagward.screenshots.event.EventsKt;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Inject(method = "method_1602", at = @At("HEAD"))
    private void screenshots$mouseDragged(Screen screen, double mx, double my, double dx, double dy, CallbackInfo ci) {
        EventsKt.ScreenDragEvent.invoker().drag(screen, mx, my, dx, dy);
    }
}
