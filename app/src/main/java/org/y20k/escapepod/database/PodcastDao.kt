package org.y20k.escapepod.database

import androidx.lifecycle.LiveData
import androidx.room.*
import org.y20k.escapepod.helpers.LogHelper

@Dao
interface PodcastDao {
    @Query("SELECT COUNT(*) FROM podcasts")
    fun getSize(): Int

    @Query("SELECT * FROM podcasts")
    fun getAll(): List<PodcastDataEntity>

    @Query("SELECT * FROM podcasts WHERE name LIKE :name LIMIT 1")
    fun findByName(name: String): PodcastDataEntity

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(podcast: PodcastDataEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(podcasts: List<PodcastDataEntity>): List<Long>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(podcast: PodcastDataEntity)

    @Delete
    fun delete(user: PodcastDataEntity)

    @Transaction
    fun upsert(podcast: PodcastDataEntity): Boolean {
        LogHelper.e("TAG", "Upsert => ${podcast.name}")
        val rowId = insert(podcast)
        if (rowId == -1L) {
            update(podcast)
            return true
        }
        return false
    }

    @Transaction
    fun upsertAll(podcasts: List<PodcastDataEntity>) {
        val rowIds = insertAll(podcasts)
        val podcastsToUpdate: List<PodcastDataEntity> = rowIds.mapIndexedNotNull { index, rowId ->
            if (rowId == -1L) {
                null
            } else {
                podcasts[index]
            }
        }
        podcastsToUpdate.forEach { update(it) }
    }


    /**
     * This query will tell Room to query both the Podcast and Episode tables and handle
     * the object mapping.
     */
    @Transaction
    @Query("SELECT * FROM podcasts")
    //@Query("SELECT * FROM episodes WHERE episode_remote_podcast_feed_location IN (SELECT DISTINCT(remote_podcast_feed_location) FROM podcasts)")
    fun getCollection(): LiveData<List<PodcastWrapperEntity>>

    /**
     * This query will tell Room to query both the Podcast and Episode tables and handle
     * the object mapping.
     */
    @Transaction
    @Query("SELECT * FROM podcasts WHERE remote_podcast_feed_location LIKE :feed LIMIT 1")
    //@Query("SELECT * FROM episodes WHERE episode_remote_podcast_feed_location IN (SELECT DISTINCT(remote_podcast_feed_location) FROM podcasts)")
    fun getPodcast(feed: String): PodcastWrapperEntity



}