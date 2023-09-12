package com.squagward.screenshots.mixin;

import com.squagward.screenshots.Screenshots;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void screenshots$ignoreKeyPress(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (Screenshots.INSTANCE.getDisplayScreenshotHud() && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            cir.setReturnValue(true);
        }
    }
}
