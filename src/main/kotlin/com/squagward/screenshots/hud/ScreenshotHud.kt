package com.squagward.screenshots.hud

import com.squagward.screenshots.Screenshots
import com.squagward.screenshots.event.ScreenDragEvent
import com.squagward.screenshots.screen.ScreenshotScreen
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.util.ScreenshotRecorder
import net.minecraft.client.util.Window
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW
import kotlin.math.max
import kotlin.math.min

object ScreenshotHud {
    private var startCorner = 0.0 to 0.0
    private var stopCorner = 0.0 to 0.0
    private val mc: MinecraftClient = MinecraftClient.getInstance()

    init {
        val outside = 0x99111111.toInt()

        ScreenEvents.AFTER_INIT.register outer@{ _, screen: Screen, _, _ ->
            ScreenEvents.afterRender(screen).register { _, context: DrawContext, _, _, _ ->
                if (!Screenshots.displayScreenshotHud) return@register

                val left = getLeft()
                val right = getRight()
                val top = getTop()
                val bottom = getBottom()

                val window = mc.window

                context.matrices.push()
                context.matrices.translate(0.0, 0.0, 100.0)
                if (top != bottom && left != right) {
                    context.fill(0, 0, window.width, top.toInt(), outside)
                    context.fill(0, top.toInt(), left.toInt(), bottom.toInt(), outside)
                    context.fill(right.toInt(), top.toInt(), window.width, bottom.toInt(), outside)
                    context.fill(0, bottom.toInt(), window.width, window.height, outside)
                } else {
                    context.fill(0, 0, window.width, window.height, outside)
                }
                context.matrices.pop()
            }

            ScreenMouseEvents.afterMouseClick(screen).register { _, mx, my, _ ->
                if (!Screenshots.displayScreenshotHud) return@register

                startCorner = mx to my
                stopCorner = mx to my
            }

            ScreenDragEvent.register { _, mx, my, _, _ ->
                if (!Screenshots.displayScreenshotHud) return@register

                stopCorner = mx to my
            }

            ScreenMouseEvents.afterMouseRelease(screen).register { _, _, _, _ ->
                if (!Screenshots.displayScreenshotHud) return@register
                if (getBottom() - getTop() < 5 || getRight() - getLeft() < 5) return@register

                ScreenshotRecorder.saveScreenshot(mc.runDirectory, mc.framebuffer) { message: Text? ->
                    mc.execute { mc.inGameHud.chatHud.addMessage(message) }
                }

                if (screen is ScreenshotScreen) {
                    screen.close()
                }
                Screenshots.displayScreenshotHud = false
            }

            ScreenKeyboardEvents.afterKeyPress(screen).register { _, key, _, _ ->
                if (Screenshots.displayScreenshotHud && key == GLFW.GLFW_KEY_ESCAPE) {
                    Screenshots.displayScreenshotHud = false
                }
            }
        }
    }

    // TODO: modify the chat message to allow uploading to imgur,
    //       maybe adding an image previewer?

    fun cropImage(original: NativeImage): NativeImage {
        val window: Window = mc.window

        val leftInt = getLeft().toInt().coerceIn(0, window.width)
        val rightInt = getRight().toInt().coerceIn(0, window.width)
        val topInt = getTop().toInt().coerceIn(0, window.height)
        val bottomInt = getBottom().toInt().coerceIn(0, window.height)

        val width = ((rightInt - leftInt) * window.scaleFactor).toInt()
        val height = ((bottomInt - topInt) * window.scaleFactor).toInt()

        val croppedImage = NativeImage(width, height, false)
        original.copyRect(
            croppedImage,
            (leftInt * window.scaleFactor).toInt(),
            (topInt * window.scaleFactor).toInt(),
            0,
            0,
            width,
            height,
            false,
            false
        )

        return croppedImage
    }

    fun reset() {
        startCorner = 0.0 to 0.0
        stopCorner = 0.0 to 0.0
    }

    private fun getLeft(): Double = min(startCorner.first, stopCorner.first)

    private fun getRight(): Double = max(startCorner.first, stopCorner.first)

    private fun getTop(): Double = min(startCorner.second, stopCorner.second)

    private fun getBottom(): Double = max(startCorner.second, stopCorner.second)

    fun init() {}
}