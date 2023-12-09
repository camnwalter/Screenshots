package com.squagward.screenshots.hud

import com.squagward.screenshots.Screenshots
import com.squagward.screenshots.config.ScreenshotsConfig
import com.squagward.screenshots.event.ScreenDragCallback
import com.squagward.screenshots.screen.ScreenshotScreen
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.client.util.ScreenshotRecorder
import net.minecraft.client.util.Window
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW
import kotlin.math.max
import kotlin.math.min

object ScreenshotHud {
    private var startCorner = 0.0 to 0.0
    private var stopCorner = 0.0 to 0.0
    private val mc: MinecraftClient = MinecraftClient.getInstance()
    private val window: Window
        get() = mc.window
    private var image: NativeImage? = null
    private var texture: NativeImageBackedTexture? = null
    private val id = Identifier("screenshots", "textures/background.png")

    init {
        val outside = 0x99111111.toInt()

        ScreenEvents.AFTER_INIT.register outer@{ _, screen: Screen, _, _ ->
            destroy()

            ScreenEvents.afterRender(screen).register { _, context: DrawContext, _, _, _ ->
                if (!Screenshots.displayScreenshotHud) return@register

                if (ScreenshotsConfig.CONFIG.instance().pauseGameWhileCropping) {
                    renderPausedBackground(context)
                }

                val left = getLeft()
                val right = getRight()
                val top = getTop()
                val bottom = getBottom()

                context.matrices.push()
                context.matrices.translate(0f, 0f, 400f)

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

            ScreenMouseEvents.allowMouseClick(screen).register { _, mx, my, _ ->
                if (Screenshots.displayScreenshotHud) {
                    startCorner = mx to my
                    stopCorner = mx to my

                    false
                } else {
                    true
                }
            }

            ScreenDragCallback.EVENT.register { _, mx, my, _, _ ->
                if (!Screenshots.displayScreenshotHud) return@register

                stopCorner = mx to my
            }

            ScreenMouseEvents.afterMouseRelease(screen).register { _, _, _, _ ->
                if (!Screenshots.displayScreenshotHud) return@register
                if (getBottom() - getTop() < 5 || getRight() - getLeft() < 5) return@register

                ScreenshotRecorder.saveScreenshot(
                    mc.runDirectory,
                    mc.framebuffer
                ) { message: Text ->
                    if (ScreenshotsConfig.CONFIG.instance().saveScreenshotFile) {
                        mc.execute { mc.inGameHud.chatHud.addMessage(message) }
                    }
                }

                if (screen is ScreenshotScreen) {
                    screen.close()
                    Screenshots.displayScreenshotScreen = false
                }
                Screenshots.displayScreenshotHud = false
                destroy()
            }

            ScreenKeyboardEvents.allowKeyPress(screen).register { _, key, _, _ ->
                if (Screenshots.displayScreenshotHud) {
                    if (key == GLFW.GLFW_KEY_ESCAPE) {
                        Screenshots.displayScreenshotHud = false
                        destroy()
                        return@register true
                    }

                    false
                } else {
                    true
                }
            }
        }
    }

    private fun destroy() {
        texture?.close()
        texture = null
        image = null
    }

    fun updateBackgroundImage(fb: Framebuffer) {
        destroy()

        image = ScreenshotRecorder.takeScreenshot(fb)
        texture = NativeImageBackedTexture(image)
        mc.textureManager.registerTexture(id, texture)
    }

    private fun renderPausedBackground(ctx: DrawContext) {
        if (image == null) return

        ctx.matrices.push()
        ctx.matrices.translate(0f, 0f, 400f)
        ctx.matrices.scale(
            (1 / window.scaleFactor).toFloat(),
            (1 / window.scaleFactor).toFloat(),
            1f
        )

        ctx.drawTexture(
            id,
            0,
            0,
            0f,
            0f,
            window.width,
            window.height,
            image!!.width,
            image!!.height
        )
        ctx.matrices.pop()
    }

    fun cropImage(original: NativeImage): NativeImage {
        val left = getLeft().coerceIn(0.0, window.width.toDouble() - 1)
        val right = getRight().coerceIn(0.0, window.width.toDouble() - 1)
        val top = getTop().coerceIn(0.0, window.height.toDouble() - 1)
        val bottom = getBottom().coerceIn(0.0, window.height.toDouble() - 1)

        var width = ((right - left) * window.scaleFactor).toInt()
        var height = ((bottom - top) * window.scaleFactor).toInt()

        // Now that these are scaled, the width & height can actually go out of bounds. So we make
        // the stopping coords at most be the right and bottom of the original image,
        // and update the width & height accordingly. We don't have to do this with the starting
        // coords, as those will always be a minimum of 0.

        val startingX = (left * window.scaleFactor).toInt()
        val startingY = (top * window.scaleFactor).toInt()

        val stopX = min(original.width - 1, startingX + width)
        val stopY = min(original.height - 1, startingY + height)

        width = stopX - startingX
        height = stopY - startingY

        val croppedImage = NativeImage(width, height, false)
        original.copyRect(croppedImage, startingX, startingY, 0, 0, width, height, false, false)

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
