package com.squagward.screenshotutils.screen

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.util.ScreenshotRecorder
import net.minecraft.text.Text
import net.minecraft.util.Util
import java.io.File
import java.io.IOException

class ScreenshotScreen : Screen(Text.literal("Screenshot")) {
    private var top = 0.0
    private var left = 0.0
    private var bottom = 0.0
    private var right = 0.0
    private var takingScreenshot = false

    override fun onDisplayed() {
        takingScreenshot = false
        top = 0.0
        left = 0.0
        bottom = 0.0
        right = 0.0
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)

        updateBounds()

        if (!takingScreenshot) {
            context.fill(left.toInt(), top.toInt(), right.toInt(), bottom.toInt(), 0x22222222)
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        left = mouseX
        top = mouseY

        right = mouseX
        bottom = mouseY

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseDragged(
        mouseX: Double,
        mouseY: Double,
        button: Int,
        deltaX: Double,
        deltaY: Double
    ): Boolean {
        right = mouseX
        bottom = mouseY

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val result = super.mouseReleased(mouseX, mouseY, button)

        takingScreenshot = true

        val mc: MinecraftClient = MinecraftClient.getInstance()
        val image: NativeImage = ScreenshotRecorder.takeScreenshot(mc.framebuffer)

        Util.getIoWorkerExecutor().execute {
            var image2: NativeImage? = null
            try {
                val leftInt = left.toInt()
                val rightInt = right.toInt()
                val topInt = top.toInt()
                val bottomInt = bottom.toInt()

                val width = rightInt - leftInt
                val height = bottomInt - topInt

                image2 = NativeImage((width * mc.window.scaleFactor).toInt(), (height * mc.window.scaleFactor).toInt(), false)
                image.copyRect(image2,
                    (leftInt * mc.window.scaleFactor).toInt(),
                    (topInt * mc.window.scaleFactor).toInt(),
                    0,
                    0,
                    (width * mc.window.scaleFactor).toInt(),
                    (height * mc.window.scaleFactor).toInt(),
                    false,
                    false
                )

                image2.writeTo(File("TESTING", "${Util.getFormattedCurrentTime()}.png"))
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                image2?.close()
            }
        }

        close()

        return result
    }

    private fun updateBounds() {
        if (bottom < top) {
            val temp = bottom
            bottom = top
            top = temp
        }

        if (right < left) {
            val temp = right
            right = left
            left = temp
        }
    }
}
