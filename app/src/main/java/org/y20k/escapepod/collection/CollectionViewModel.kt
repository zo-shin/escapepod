/*
 * CollectionViewModel.kt
 * Implements the CollectionViewModel class
 * A CollectionViewModel stores the podcast collection as live data
 *
 * This file is part of
 * ESCAPEPOD - Free and Open Podcast App
 *
 * Copyright (c) 2018-20 - Y20K.org
 * Licensed under the MIT-License
 * http://opensource.org/licenses/MIT
 */


package org.y20k.escapepod.collection

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.*
import org.y20k.escapepod.Keys
import org.y20k.escapepod.core.Collection
import org.y20k.escapepod.database.CollectionDatabase
import org.y20k.escapepod.database.EpisodeEntity
import org.y20k.escapepod.database.PodcastDataEntity
import org.y20k.escapepod.database.PodcastWrapperEntity
import org.y20k.escapepod.helpers.DatabaseHelper
import org.y20k.escapepod.helpers.FileHelper
import org.y20k.escapepod.helpers.LogHelper
import java.util.*


/*
 * CollectionViewModel.class
 */
class CollectionViewModel(application: Application) : AndroidViewModel(application) {

    /* Define log tag */
    private val TAG: String = LogHelper.makeLogTag(CollectionViewModel::class.java)


    /* Main class variables */
    val legacyCollectionLiveData: MutableLiveData<Collection> = MutableLiveData<Collection>()
    lateinit var collectionLiveData: LiveData<List<PodcastWrapperEntity>>

    //val collectionEpisodesLiveData: MutableLiveData<List<PodcastEntity>> = MutableLiveData<List<EpisodeEntity>>()
    private var modificationDateViewModel: Date = Date()
    private var collectionChangedReceiver: BroadcastReceiver
    private val backgroundJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + backgroundJob)
    private var collectionDatabase: CollectionDatabase


    /* Init constructor */
    init {
        // load collection
        loadCollection(application)
        // create and register collection changed receiver
        collectionChangedReceiver = createCollectionChangedReceiver()
        LocalBroadcastManager.getInstance(application).registerReceiver(collectionChangedReceiver, IntentFilter(Keys.ACTION_COLLECTION_CHANGED))
        // get instance of collection database // todo remove: just a database test
        collectionDatabase = CollectionDatabase.getInstance(application)

        uiScope.launch {
            // initialize podcast live data
            val deferred: Deferred<LiveData<List<PodcastWrapperEntity>>> = async(Dispatchers.Default) { collectionDatabase.podcastDao().getCollection() }
            collectionLiveData = deferred.await()
        }

    }


    /* Overrides onCleared */
    override fun onCleared() {
        super.onCleared()
        backgroundJob.cancel()
        LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(collectionChangedReceiver)
    }


    /* Creates the collectionChangedReceiver - handles Keys.ACTION_COLLECTION_CHANGED */
    private fun createCollectionChangedReceiver(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.hasExtra(Keys.EXTRA_COLLECTION_MODIFICATION_DATE)) {
                    val date: Date = Date(intent.getLongExtra(Keys.EXTRA_COLLECTION_MODIFICATION_DATE, 0L))
                    // check if reload is necessary
                    if (date.after(modificationDateViewModel)) {
                        LogHelper.v(TAG, "CollectionViewModel - reload collection after broadcast received.")
                        loadCollection(context)
                    }
                }
            }
        }
    }


    /* Reads podcast collection from storage using GSON */
    private fun loadCollection(context: Context) {
        LogHelper.v(TAG, "Loading podcast collection from storage")
        uiScope.launch {
            // load collection on background thread
            val deferred: Deferred<Collection> = async(Dispatchers.Default) { FileHelper.readCollectionSuspended(getApplication()) }
            // wait for result
            val collection: Collection = deferred.await()
            // get updated modification date
            modificationDateViewModel = collection.modificationDate
            // update collection view model
            legacyCollectionLiveData.value = collection

            // Todo remove: just a database test
            //insertPodcasts(collection)
        }


    }


    /* Todo remove: just a database test */
    private fun insertPodcasts(collection: Collection) {
        val backgroundJob = Job()
        val uiScope = CoroutineScope(Dispatchers.IO + backgroundJob)
        uiScope.launch {
            collectionDatabase.clearAllTables()

            val result: Pair<List<PodcastDataEntity>, List<EpisodeEntity>> = DatabaseHelper.convertToPodcastEntityList(collection)
            collectionDatabase.podcastDao().insertAll(result.first)
            collectionDatabase.episodeDao().insertAll(result.second)

            val numberOfEpisodes = collectionDatabase.episodeDao().getSize()
            val numberOfPodcasts = collectionDatabase.podcastDao().getSize()
        }
        backgroundJob.cancel()
    }

}