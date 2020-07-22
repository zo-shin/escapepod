package org.y20k.escapepod.database

import androidx.lifecycle.LiveData
import androidx.room.*
import org.y20k.escapepod.helpers.LogHelper

@Dao
interface PodcastDao {
    @Query("SELECT COUNT(*) FROM podcasts")
    fun getSize(): Int

    @Query("SELECT * FROM podcasts")
    fun getAll(): LiveData<List<PodcastEntity>>

    @Query("SELECT * FROM podcasts WHERE name LIKE :name LIMIT 1")
    fun findByName(name: String): PodcastEntity

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(podcast: PodcastEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(podcasts: List<PodcastEntity>): List<Long>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(podcast: PodcastEntity)

    @Delete
    fun delete(user: PodcastEntity)

    @Transaction
    fun upsert(podcast: PodcastEntity) {
        LogHelper.e("TAG", "Upsert => ${podcast.name}")
        val rowId = insert(podcast)
        if (rowId == -1L) {
            update(podcast)
        }
    }

    @Transaction
    fun upsertAll(podcasts: List<PodcastEntity>) {
        val rowIds = insertAll(podcasts)
        val podcastsToUpdate = rowIds.mapIndexedNotNull { index, rowId ->
            if (rowId == -1L) null else podcasts[index] }
        podcastsToUpdate.forEach { update(it) }
    }


    /**
     * This query will tell Room to query both the Podcast and Episode tables and handle
     * the object mapping.
     */
    @Transaction
    //@Query("SELECT * FROM episodes WHERE podcast_id IN (SELECT DISTINCT(pid) FROM podcasts)")
    @Query("SELECT * FROM podcasts")
    fun getPodcastsWithEpisodes(): List<PodcastWithEpisodesEntity>

}