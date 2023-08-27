package com.squagward.screenshotutils.hud

import com.squagward.screenshotutils.ScreenshotUtils
import com.squagward.screenshotutils.event.ScreenDragEvent
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.util.ScreenshotRecorder
import net.minecraft.client.util.Window
import net.minecraft.util.Util
import java.io.File
import java.io.IOException
import kotlin.math.max
import kotlin.math.min

object ScreenshotHud {
    private var startCorner = 0.0 to 0.0
    private var stopCorner = 0.0 to 0.0
    private val mc: MinecraftClient = MinecraftClient.getInstance()

    init {
        val outside = 0x55555555

        ScreenEvents.AFTER_INIT.register outer@{ _, screen: Screen, scaledWidth, scaledHeight ->
            ScreenEvents.afterRender(screen).register { _, context: DrawContext, mx, my, _ ->
                if (!ScreenshotUtils.displayScreenshotHud) return@register

                val left = getLeft()
                val right = getRight()
                val top = getTop()
                val bottom = getBottom()

                val window = mc.window

                if (top != bottom && left != right) {
                    context.fill(0, 0, window.width, top.toInt(), outside)
                    context.fill(0, top.toInt(), left.toInt(), bottom.toInt(), outside)
                    context.fill(right.toInt(), top.toInt(), window.width, bottom.toInt(), outside)
                    context.fill(0, bottom.toInt(), window.width, window.height, outside)
                } else {
                    context.fill(0, 0, window.width, window.height, outside)
                }
            }

            ScreenMouseEvents.afterMouseClick(screen).register { _, mx, my, btn ->
                if (!ScreenshotUtils.displayScreenshotHud) return@register

                startCorner = mx to my
                stopCorner = mx to my
            }

            ScreenDragEvent.register { _, mx, my, dx, dy ->
                if (!ScreenshotUtils.displayScreenshotHud) return@register

                stopCorner = mx to my
            }

            ScreenMouseEvents.afterMouseRelease(screen).register { _, mx, my, btn ->
                if (!ScreenshotUtils.displayScreenshotHud) return@register

                takeScreenshot()
                screen.close()
                ScreenshotUtils.displayScreenshotHud = false
            }
        }
    }

    private fun takeScreenshot() {
        val left = getLeft()
        val right = getRight()
        val top = getTop()
        val bottom = getBottom()

        if (bottom - top < 5 || right - left < 5) return

        val fullScreenshot: NativeImage = ScreenshotRecorder.takeScreenshot(mc.framebuffer)

        Util.getIoWorkerExecutor().execute {
            val window: Window = mc.window

            val leftInt = left.toInt().coerceIn(0, window.width)
            val rightInt = right.toInt().coerceIn(0, window.width)
            val topInt = top.toInt().coerceIn(0, window.height)
            val bottomInt = bottom.toInt().coerceIn(0, window.height)

            val width = ((rightInt - leftInt) * window.scaleFactor).toInt()
            val height = ((bottomInt - topInt) * window.scaleFactor).toInt()

            val croppedImage = NativeImage(width, height, false)
            fullScreenshot.copyRect(
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

            // TODO: option to save the file or not,
            //       option to copy the image to clipboard
            //       option to upload to imgur
            //       modify the chat message
            try {
                croppedImage.writeTo(File("TESTING", "${Util.getFormattedCurrentTime()}.png"))
            } catch (e: IOException) {
                ScreenshotUtils.LOGGER.error("Error taking screenshot", e)
            } finally {
                croppedImage.close()
                fullScreenshot.close()
            }
        }
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