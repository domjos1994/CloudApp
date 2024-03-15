package de.domjos.cloudapp.features.stream

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import dagger.hilt.android.UnstableApi
import org.webrtc.RendererCommon
import org.webrtc.VideoTrack

@OptIn(UnstableApi::class)
@Composable
fun VideoRenderer(
    videoTrack: VideoTrack,
    modifier: Modifier = Modifier
) {
    val trackState: MutableState<VideoTrack?> = remember { mutableStateOf(null) }
    var view: VideoTextureViewRenderer? by remember { mutableStateOf(null) }

    DisposableEffect(videoTrack) {
        onDispose {
            cleanTrack(view, trackState)
        }
    }


    val sessionManager = LocalWebRtcSessionManager.current
    AndroidView(
        factory = { context ->
            VideoTextureViewRenderer(context).apply {
                init(
                    sessionManager.peerConnectionFactory.eglBaseContext,
                    object : RendererCommon.RendererEvents {
                        override fun onFirstFrameRendered() = Unit

                        override fun onFrameResolutionChanged(p0: Int, p1: Int, p2: Int) = Unit
                    }
                )
                setupVideo(trackState, videoTrack, this)
                view = this
            }
        },
        update = { v -> setupVideo(trackState, videoTrack, v) },
        modifier = modifier
    )
}

@OptIn(UnstableApi::class)
private fun cleanTrack(
    view: VideoTextureViewRenderer?,
    trackState: MutableState<VideoTrack?>
) {
    view?.let { trackState.value?.removeSink(it) }
    trackState.value = null
}

@OptIn(UnstableApi::class)
private fun setupVideo(
    trackState: MutableState<VideoTrack?>,
    track: VideoTrack,
    renderer: VideoTextureViewRenderer
) {
    if (trackState.value == track) {
        return
    }

    cleanTrack(renderer, trackState)

    trackState.value = track
    track.addSink(renderer)
}