package com.recorder.service

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class PlayerService : MediaLibraryService() {

    private lateinit var mediaLibrarySession: MediaLibrarySession

    private lateinit var exoPlayer: ExoPlayer

    private val callback = MediaLibraryCallback()

    inner class MediaLibraryCallback() : MediaLibrarySession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): MediaSession.ConnectionResult {
            return super.onConnect(session, controller)
        }

        override fun onDisconnected(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ) {
            super.onDisconnected(session, controller)
        }


        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String,
        ): ListenableFuture<LibraryResult<MediaItem>> {
            return super.onGetItem(session, browser, mediaId)
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
        ): ListenableFuture<MutableList<MediaItem>> {
            mediaItems.forEach {
                Timber.e(it.mediaId)
            }
            return Futures.immediateFuture(mediaItems)
        }
        override fun onGetLibraryRoot(session: MediaLibrarySession, browser: MediaSession.ControllerInfo, params: LibraryParams?): ListenableFuture<LibraryResult<MediaItem>> {
            return Futures.immediateFuture(LibraryResult.ofItem(rootItem, params))
        }
        override fun onGetChildren(
            session: MediaLibrarySession, browser: MediaSession.ControllerInfo,
            parentId: String, page: Int, pageSize: Int, params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {

            return Futures.immediateFuture(
                LibraryResult.ofItemList(
                    ImmutableList.of<MediaItem>(),
                    params
                )
            )
        }
        override fun onGetSearchResult(
            session: MediaLibrarySession, browser: MediaSession.ControllerInfo,
            query: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {

            return Futures.immediateFuture(
                LibraryResult.ofItemList(
                    ImmutableList.of<MediaItem>(),
                    params
                )
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        exoPlayer = ExoPlayer.Builder(this).build()
        mediaLibrarySession = MediaLibrarySession.Builder(
            this,
            exoPlayer,
            callback,
        )
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession {
        return mediaLibrarySession
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaLibrarySession.run {
            player.release()
            release()
        }
    }

    /** This is the root item that is parent to our playlist.
     *  It is necessary to have a parent item otherwise there is no "library" */
    val rootItem = MediaItem.Builder()
        .setMediaId("my_root_node")
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setIsBrowsable(false)
                .setIsPlayable(false)
                .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                .setTitle("MyVoieRecorderAppRootWhichIsNotVisibleToControllers")
                .build()
        )
        .build()

}