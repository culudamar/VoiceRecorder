package com.recorder.feature.playlist

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.core.common.model.Voice
import com.experiment.voicerecorder.rememberPlayerState
import com.recorder.core.designsystem.theme.VoiceRecorderTheme
import kotlinx.coroutines.delay
import timber.log.Timber

@Composable
fun Playlist(
    onProgressChange: (Float) -> Unit,
    onBackPressed: () -> Unit,
) {
    val viewModel = hiltViewModel<PlaylistViewModel>()
    val playerState = rememberPlayerState()
    val context = LocalContext.current
    var playingVoiceIndex by remember {
        mutableIntStateOf(-1)
    }
    val voiceList by viewModel.voices.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = Unit, block = {
        viewModel.getVoices(context)
    })
    val isPlaying by playerState.isVoicePlaying.collectAsStateWithLifecycle()
    val progress = rememberUpdatedState() {
        derivedStateOf {
            playerState.browser?.run { currentPosition }
        }
    }
    val duration = playerState.voiceDuration
    var lastProgress by remember(playerState.progress) {
        mutableFloatStateOf(playerState.progress)
    }
    LaunchedEffect(key1 = isPlaying, playerState.browser?.run { currentPosition }) {
        Timber.e("voiceduration is :${playerState.voiceDuration}")
        Timber.e("progress is :${playerState.progress}")
        viewModel.updateVoiceList(
            selectedVoiceIndex = playingVoiceIndex,
            isPlaying = isPlaying
        )
        if (isPlaying) {
            playerState.browser?.run {
                while (true) {
                    delay(1_000)
//                    progress = currentPosition.toFloat()
                    Timber.e("p$progress")
                }
            }
        } else {
//            progress = playerState.progress
        }

    }
    LaunchedEffect(key1 = lastProgress) {
//        if (progress != lastProgress) {
//            delay(50)
//            onProgressChange(lastProgress)
//        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PlaylistContent(
            voices = voiceList,
            onPlayPause = { },
            onStop = {
                playerState.browser?.run {
                    stop()
                }
            },
            onVoiceClicked = { voiceIndex, voice ->
                playingVoiceIndex = voiceIndex
//                onVoiceClicked(voiceIndex, voice)
                Timber.e("onplay")
                val metadata = MediaMetadata.Builder()
                    .setTitle(voice.title)
                    .setIsPlayable(true).build()
                val mediaitem = MediaItem.Builder()
                    .setMediaMetadata(metadata)
                    .setUri(voice.path)
                    .setMediaId(voice.title)
                    .build()
                if (playerState.browser == null) {
                    Timber.e("browsernull")
                }
                playerState.browser?.run {
                    Timber.e("item id to play:${mediaitem.mediaId}")
                    setMediaItem(mediaitem)
                    play()
                }
            },
            onBackPressed = { onBackPressed() },
            progress = lastProgress,
            duration = duration,
            onProgressChange = { desireePosition ->
                lastProgress = desireePosition
            })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistContent(
    voices: List<Voice>,
    progress: Float,
    duration: Float,
    onProgressChange: (Float) -> Unit,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onVoiceClicked: (Int, Voice) -> Unit,
    onBackPressed: () -> Unit,
) {
    var voice by remember {
        mutableStateOf(Voice())
    }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 0.dp)
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        MediumTopAppBar(
            title = {
                Text(
                    text = "Recordings",
                )
            },
            navigationIcon = {
                IconButton(onClick = { onBackPressed() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        tint = MaterialTheme.colorScheme.onBackground,
                        contentDescription = "back icon"
                    )
                }
            },
            colors = TopAppBarDefaults
                .mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
            scrollBehavior = scrollBehavior
        )
        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                count = voices.size,
                key = {
                    it
                }) { voiceIndex ->
                PlaylistItem(
                    modifier = Modifier,
                    voice = voices[voiceIndex],
                    onVoiceClicked = { clickedVoice ->
                        onVoiceClicked(voiceIndex, clickedVoice)
                        voice = clickedVoice
                    },
                    onStop = { onStop() },
                    progress = progress,
                    duration = duration,
                    onProgressChange = { progress ->
                        onProgressChange(progress)
                    }
                )
            }
        }

    }
}

@Composable
fun PlaylistItem(
    modifier: Modifier = Modifier,
    voice: Voice,
    progress: Float,
    duration: Float,
    onProgressChange: (Float) -> Unit,
    onVoiceClicked: (Voice) -> Unit,
    onStop: () -> Unit,
) {
    Surface(
        modifier = Modifier.animateContentSize(),
        onClick = { },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        val textColor = if (voice.isPlaying) MaterialTheme.colorScheme.primary
        else LocalContentColor.current
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedContent(
                targetState = voice.isPlaying,
                label = "play icon"
            ) { isPlaying ->
                if (isPlaying)
                    Icon(
                        imageVector = Icons.Default.StopCircle,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .size(60.dp)
                            .padding(all = 8.dp)
                            .clip(CircleShape)
                            .clickable { onStop() },
                        contentDescription = ""
                    )
                else
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .size(60.dp)
                            .padding(all = 8.dp)
                            .clip(CircleShape)
                            .clickable {
                                onVoiceClicked(Voice(voice.title, voice.path))
                                Timber.e("ui item: ${voice.title}")
                            },
                        contentDescription = ""
                    )
            }
            Column(
                modifier = Modifier.animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(0.dp),//janky animation if set to > 0
            ) {
                Text(
                    text = voice.title,
                    color = textColor
                )
                AnimatedVisibility(
                    voice.isPlaying,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Slider(
                        value = progress,
                        onValueChange = { onProgressChange(it) },
                        modifier = Modifier,
                        valueRange = 0f..duration,
                        steps = 0,
                        onValueChangeFinished = {},
                    )
                }
                Row {
                    Text(
                        text = voice.duration,
                        fontSize = 12.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = voice.recordTime,
                        fontSize = 12.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
            }
        }

    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Composable
fun PlaylistPagePreview() {
    VoiceRecorderTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PlaylistContent(
                listOf(
                    Voice("title", "", isPlaying = false, "00:01"),
                    Voice("title2", "", isPlaying = true, "00:10"),
                    Voice("title3", "", isPlaying = false, "02:21"),
                    Voice("title4", "", isPlaying = false, "05:01"),
                    Voice("title5", "", isPlaying = false, "00:41")
                ),
                onPlayPause = {},
                onStop = {},
                onVoiceClicked = { i, voice ->
                },
                onBackPressed = {},
                progress = 0.0f,
                duration = 0.0f,
                onProgressChange = {},

                )
        }
    }
}

@Composable
fun MediaControls(
    modifier: Modifier = Modifier,
    voice: Voice,
    onPlayPause: (Voice) -> Unit,
    onStop: () -> Unit,
) {
    var sliderInt by remember {
        mutableStateOf(0f)
    }
    Card(modifier = modifier) {
        Column() {
            Text(text = "filename:${voice.title}")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Button(onClick = { onPlayPause(voice) }) {
                    Timber.e("${voice.isPlaying}")
                    if (voice.isPlaying)
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Play/Pause Button"
                        )
                    else
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause Button"
                        )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = { onStop() }) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop Button"
                    )
                }
            }
            Slider(value = sliderInt, onValueChange = { sliderInt = it })
        }

    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PlaylistItemPreview() {
    VoiceRecorderTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PlaylistItem(
                voice =
                Voice(
                    title = "title prview",
                    path = "path",
                    isPlaying = false,
                    duration = "00:12",
                    recordTime = "just now"
                ),
                onVoiceClicked = {},
                onStop = {},
                modifier = Modifier,
                progress = 0f,
                duration = 0f,
                onProgressChange = {}
            )
        }
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun MediaControlsPreview() {
    VoiceRecorderTheme {
        Surface(color = MaterialTheme.colorScheme.background) {

        }
    }
}