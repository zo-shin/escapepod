package org.y20k.escapepod.database

import androidx.room.Embedded
import androidx.room.Relation

data class PodcastWrapperEntity(
        @Embedded
        val data: PodcastDataEntity,

        @Relation(parentColumn = "remote_podcast_feed_location", entityColumn = "episode_remote_podcast_feed_location")
        val episodes: List<EpisodeEntity> = emptyList()
)