package com.squagward.screenshots.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.squagward.screenshots.Screenshots;
import com.squagward.screenshots.config.Config;
import com.squagward.screenshots.hud.ScreenshotHud;
import com.squagward.screenshots.screen.ScreenshotScreen;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import java.io.File;
import java.util.function.Consumer;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @WrapOperation(
            method = "onKey",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/ScreenshotRecorder;saveScreenshot(Ljava/io/File;Lnet/minecraft/client/gl/Framebuffer;Ljava/util/function/Consumer;)V"
            )
    )
    private void screenshots$openhud(File gameDirectory, Framebuffer framebuffer, Consumer<Text> messageReceiver, Operation<Void> original) {
        Config config = Config.INSTANCE.getConfig();
        if (!config.getEnabled() || !config.getCropImage()) {
            original.call(gameDirectory, framebuffer, messageReceiver);
            return;
        }

        Screenshots.INSTANCE.setDisplayScreenshotHud(true);
        ScreenshotHud.INSTANCE.reset();

        if (client.currentScreen == null) {
            client.send(() -> {
                client.setScreen(new ScreenshotScreen());
                Screenshots.INSTANCE.setDisplayScreenshotScreen(true);
            });
        }
    }

    @WrapWithCondition(method = "onKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;openGameMenu(Z)V"))
    private boolean screenshots$dontOpenPauseMenu(MinecraftClient client, boolean pause) {
        boolean shouldOpenPauseMenu = !Screenshots.INSTANCE.getDisplayScreenshotScreen();
        Screenshots.INSTANCE.setDisplayScreenshotScreen(false);

        return shouldOpenPauseMenu;
    }
}
