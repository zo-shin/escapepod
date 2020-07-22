package org.y20k.escapepod.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.y20k.escapepod.xml.RssHelper
import java.util.*

@Entity(tableName = "episodes", indices = arrayOf(Index(value = ["guid"], unique = true)))
data class EpisodeEntity (

        @PrimaryKey
        @ColumnInfo (name = "guid") val guid: String,

        @ColumnInfo (name = "title") val title: String,
        @ColumnInfo (name = "description") val description: String,
        @ColumnInfo (name = "audio") val audio: String,
        @ColumnInfo (name = "cover") val cover: String,
        @ColumnInfo (name = "small_cover") val smallCover: String,
        @ColumnInfo (name = "publication_date") val publicationDate: Date,
        @ColumnInfo (name = "playback_state") val playbackState: Int,
        @ColumnInfo (name = "playback_position") val playbackPosition: Long,
        @ColumnInfo (name = "duration") val duration: Long,
        @ColumnInfo (name = "manually_downloaded") val manuallyDownloaded: Boolean,
        @ColumnInfo (name = "manually_deleted") val manuallyDeleted: Boolean,
        @ColumnInfo (name = "remote_cover_file_location") val remoteCoverFileLocation: String,
        @ColumnInfo (name = "remote_audio_file_location") val remoteAudioFileLocation: String,

        // defines the relation between episode and podcast
        @ColumnInfo (name = "episode_remote_podcast_feed_location") val episodeRemotePodcastFeedLocation: String

        ) {

    /* Constructor that uses output from RssHelper */
    constructor(rssEpisode: RssHelper.RssEpisode) : this (
            guid = rssEpisode.guid,
            title = rssEpisode.title,
            description = rssEpisode.description,
            audio = rssEpisode.audio,
            cover = rssEpisode.cover,
            smallCover = rssEpisode.smallCover,
            publicationDate = rssEpisode.publicationDate,
            playbackState = rssEpisode.playbackState,
            playbackPosition = rssEpisode.playbackPosition,
            duration = rssEpisode.duration,
            manuallyDownloaded = rssEpisode.manuallyDownloaded,
            manuallyDeleted = rssEpisode.manuallyDeleted,
            remoteCoverFileLocation = rssEpisode.remoteCoverFileLocation,
            remoteAudioFileLocation = rssEpisode.remoteAudioFileLocation,
            episodeRemotePodcastFeedLocation = rssEpisode.episodeRemotePodcastFeedLocation

    )

}