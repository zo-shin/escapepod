package org.y20k.escapepod.helpers

import org.y20k.escapepod.core.Collection
import org.y20k.escapepod.database.EpisodeEntity
import org.y20k.escapepod.database.PodcastDataEntity

object DatabaseHelper {

    fun convertToPodcastEntityList(collection: Collection): Pair<List<PodcastDataEntity>, List<EpisodeEntity>> {
        val podcastList: MutableList<PodcastDataEntity> = mutableListOf()
        val episodeList: MutableList<EpisodeEntity> = mutableListOf()
        collection.podcasts.forEach { podcast ->
            val podcastEntity: PodcastDataEntity = PodcastDataEntity(
                    name = podcast.name,
                    description = podcast.description,
                    website = podcast.website,
                    cover = podcast.cover,
                    smallCover = podcast.smallCover,
                    lastUpdate = podcast.lastUpdate,
                    remoteImageFileLocation = podcast.remoteImageFileLocation,
                    remotePodcastFeedLocation = podcast.remotePodcastFeedLocation
            )
            podcastList.add(podcastEntity)

            podcast.episodes.forEach { episode ->
                val episodeEntity: EpisodeEntity = EpisodeEntity(
                        guid = episode.guid,
                        title = episode.title,
                        description = episode.description,
                        audio = episode.audio,
                        cover = episode.cover,
                        smallCover = episode.smallCover,
                        publicationDate = episode.publicationDate,
                        playbackState = episode.playbackState,
                        playbackPosition = episode.playbackPosition,
                        duration = episode.duration,
                        manuallyDownloaded = episode.manuallyDownloaded,
                        manuallyDeleted = episode.manuallyDeleted,
                        remoteCoverFileLocation = episode.remoteCoverFileLocation,
                        remoteAudioFileLocation = episode.remoteAudioFileLocation,
                        episodeRemotePodcastFeedLocation = podcast.remotePodcastFeedLocation
                )
                episodeList.add(episodeEntity)
            }


        }
        return Pair(podcastList, episodeList)
    }

}