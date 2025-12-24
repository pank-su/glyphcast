package su.pank.exhelp.app.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

object ScreenshotService {
    
    private val robot = Robot()
    
    /**
     * Создает поток скриншотов каждые 30 секунд
     */
    fun createScreenshotFlow(): Flow<ByteArray> = flow {
        while (true) {
            val screenshot = captureScreenshot()
            emit(screenshot)
            delay(30_000) // 30 секунд
        }
    }
    
    /**
     * Делает скриншот экрана и возвращает его в виде ByteArray
     */
    private fun captureScreenshot(): ByteArray {
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        val screenRect = Rectangle(screenSize)
        val screenshot: BufferedImage = robot.createScreenCapture(screenRect)
        
        return ByteArrayOutputStream().use { outputStream ->
            ImageIO.write(screenshot, "png", outputStream)
            outputStream.toByteArray()
        }
    }
}
