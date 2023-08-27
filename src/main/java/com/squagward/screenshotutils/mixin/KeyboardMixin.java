package com.squagward.screenshotutils.mixin;

import com.squagward.screenshotutils.ScreenshotUtils;
import com.squagward.screenshotutils.hud.ScreenshotHud;
import com.squagward.screenshotutils.screen.ScreenshotScreen;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.util.function.Consumer;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Redirect(
            method = "onKey",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/ScreenshotRecorder;saveScreenshot(Ljava/io/File;Lnet/minecraft/client/gl/Framebuffer;Ljava/util/function/Consumer;)V"
            )
    )
    private void screenshotutils$openhud(File gameDirectory, Framebuffer framebuffer, Consumer<Text> messageReceiver) {
        ScreenshotUtils.INSTANCE.setDisplayScreenshotHud(true);
        ScreenshotHud.INSTANCE.reset();

        if (client.currentScreen == null) {
            client.send(() -> client.setScreen(new ScreenshotScreen()));
        }
    }
}
