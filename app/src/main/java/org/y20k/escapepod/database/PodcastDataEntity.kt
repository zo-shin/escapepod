package org.y20k.escapepod.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.y20k.escapepod.xml.RssHelper
import java.util.*


@Entity(tableName = "podcasts", indices = arrayOf(Index(value = ["remote_podcast_feed_location"], unique = true)))
data class PodcastDataEntity(

        @PrimaryKey
        @ColumnInfo (name = "remote_podcast_feed_location") val remotePodcastFeedLocation: String,

        @ColumnInfo (name = "name") val name: String,
        @ColumnInfo (name = "description") val description: String,
        @ColumnInfo (name = "website") val website: String,
        @ColumnInfo (name = "cover") val cover: String,
        @ColumnInfo (name = "small_cover") val smallCover: String,
        @ColumnInfo (name = "last_update") val lastUpdate: Date,
        @ColumnInfo (name = "remote_image_file_location") val remoteImageFileLocation: String

) {

    /* Constructor that uses output from RssHelper*/
    constructor(rssPodcast: RssHelper.RssPodcast) : this (
            name = rssPodcast.name,
            description = rssPodcast.description,
            website = rssPodcast.website,
            cover = rssPodcast.cover,
            smallCover = rssPodcast.smallCover,
            lastUpdate = rssPodcast.lastUpdate,
            remoteImageFileLocation = rssPodcast.remoteImageFileLocation,
            remotePodcastFeedLocation = rssPodcast.remotePodcastFeedLocation
    )
}