package de.domjos.cloudapp.features.stream

import android.content.Context
import android.content.res.Resources
import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.TextureView
import dagger.hilt.android.UnstableApi
import org.webrtc.EglBase
import org.webrtc.EglRenderer
import org.webrtc.GlRectDrawer
import org.webrtc.RendererCommon
import org.webrtc.ThreadUtils
import org.webrtc.VideoFrame
import org.webrtc.VideoSink
import java.util.concurrent.CountDownLatch

/**
 * Custom [TextureView] used to render local/incoming videos on the screen.
 */
@UnstableApi
open class VideoTextureViewRenderer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : TextureView(context, attrs), VideoSink, TextureView.SurfaceTextureListener {

    private val resourceName: String = getResourceName()

    private val eglRenderer: EglRenderer = EglRenderer(resourceName)

    private var rendererEvents: RendererCommon.RendererEvents? = null

    private val uiThreadHandler = Handler(Looper.getMainLooper())

    private var isFirstFrameRendered = false

    private var rotatedFrameWidth = 0

    private var rotatedFrameHeight = 0

    private var frameRotation = 0

    init {
        surfaceTextureListener = this
    }

    /**
     * Called when a new frame is received. Sends the frame to be rendered.
     *
     * @param videoFrame The [VideoFrame] received from WebRTC connection to draw on the screen.
     */
    override fun onFrame(videoFrame: VideoFrame) {
        eglRenderer.onFrame(videoFrame)
        updateFrameData(videoFrame)
    }

    /**
     * Updates the frame data and notifies [rendererEvents] about the changes.
     */
    private fun updateFrameData(videoFrame: VideoFrame) {
        if (isFirstFrameRendered) {
            rendererEvents?.onFirstFrameRendered()
            isFirstFrameRendered = true
        }

        if (videoFrame.rotatedWidth != rotatedFrameWidth ||
            videoFrame.rotatedHeight != rotatedFrameHeight ||
            videoFrame.rotation != frameRotation
        ) {
            rotatedFrameWidth = videoFrame.rotatedWidth
            rotatedFrameHeight = videoFrame.rotatedHeight
            frameRotation = videoFrame.rotation

            uiThreadHandler.post {
                rendererEvents?.onFrameResolutionChanged(
                    rotatedFrameWidth,
                    rotatedFrameHeight,
                    frameRotation
                )
            }
        }
    }

    /**
     * After the view is laid out we need to set the correct layout aspect ratio to the renderer so that the image
     * is scaled correctly.
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        eglRenderer.setLayoutAspectRatio((right - left) / (bottom.toFloat() - top))
    }

    /**
     * Initialise the renderer. Should be called from the main thread.
     *
     * @param sharedContext [EglBase.Context]
     * @param rendererEvents Sets the render event listener.
     */
    fun init(
        sharedContext: EglBase.Context,
        rendererEvents: RendererCommon.RendererEvents
    ) {
        ThreadUtils.checkIsOnMainThread()
        this.rendererEvents = rendererEvents
        eglRenderer.init(sharedContext, EglBase.CONFIG_PLAIN, GlRectDrawer())
    }

    /**
     * [SurfaceTextureListener] callback that lets us know when a surface texture is ready and we can draw on it.
     */
    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
        eglRenderer.createEglSurface(surfaceTexture)
    }

    /**
     * [SurfaceTextureListener] callback that lets us know when a surface texture is destroyed we need to stop drawing
     * on it.
     */
    override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
        val completionLatch = CountDownLatch(1)
        eglRenderer.releaseEglSurface { completionLatch.countDown() }
        ThreadUtils.awaitUninterruptibly(completionLatch)
        return true
    }

    override fun onSurfaceTextureSizeChanged(
        surfaceTexture: SurfaceTexture,
        width: Int,
        height: Int
    ) {
    }

    override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}

    override fun onDetachedFromWindow() {
        eglRenderer.release()
        super.onDetachedFromWindow()
    }

    private fun getResourceName(): String {
        return try {
            resources.getResourceEntryName(id) + ": "
        } catch (e: Resources.NotFoundException) {
            ""
        }
    }
}