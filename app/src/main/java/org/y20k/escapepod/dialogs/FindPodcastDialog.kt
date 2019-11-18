package org.y20k.escapepod.dialogs

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.google.gson.GsonBuilder
import org.y20k.escapepod.R
import org.y20k.escapepod.helpers.LogHelper
import org.y20k.escapepod.search.GpodderResult


/*
 * FindPodcastDialog class
 */
class FindPodcastDialog (private var findPodcastDialogListener: FindPodcastDialogListener) {

    /* Interface used to communicate back to activity */
    interface FindPodcastDialogListener {
        fun onFindPodcastDialog(remotePodcastFeedLocation: String) {
        }
    }

    /* Define log tag */
    private val TAG = LogHelper.makeLogTag(FindPodcastDialog::class.java.simpleName)


    /* Main class variables */
    private lateinit var dialog: AlertDialog
    private lateinit var podcastSearchBoxView: SearchView
    private lateinit var searchRequestProgressBar: ProgressBar
    private lateinit var noSearchResultsTextView: MaterialTextView
    private lateinit var requestQueue: RequestQueue
    private var podcastFeedLocation: String = String()


    /* Construct and show dialog */
    fun show(context: Context) {

        // prepare dialog builder
        val builder: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(context)

        // set title
        builder.setTitle(R.string.dialog_find_podcast_title)

        // get views
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_find_podcast, null)
        podcastSearchBoxView = view.findViewById(R.id.podcast_search_box_view)
        searchRequestProgressBar = view.findViewById(R.id.search_request_progress_bar)
        noSearchResultsTextView = view.findViewById(R.id.no_results_text_view)
        noSearchResultsTextView.visibility = View.GONE

        // add okay ("import") button
        builder.setPositiveButton(R.string.dialog_find_podcast_button_add) { _, _ ->
            // listen for click on add button
            findPodcastDialogListener.onFindPodcastDialog(podcastFeedLocation)
        }
        // add cancel button
        builder.setNegativeButton(R.string.dialog_generic_button_cancel) { _, _ ->
            // listen for click on cancel button
            requestQueue.stop()
        }
        // handle outside-click as "no"
        builder.setOnCancelListener(){
            requestQueue.stop()
        }

        // listen for input
        podcastSearchBoxView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(query: String): Boolean {
                handleSearchBoxLiveInput(context, query)
                return true
            }
            override fun onQueryTextSubmit(query: String): Boolean {
                handleSearchBoxInput(context, query)
                return true
            }
        })

        // set dialog view
        builder.setView(view)

        // create and display dialog
        dialog = builder.create()
        dialog.show()

        // initially disable "Add" button
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

    }



    private fun handleSearchBoxInput(context: Context, query: String) {
        when {
            query.isEmpty() -> resetLayout()
            query.startsWith("http") -> activateAddButton(query)
            else -> search(context, query)
        }
    }


    private fun handleSearchBoxLiveInput(context: Context, query: String) {
        when {
            query.contains(" ") || query.length > 4 -> search(context, query)
            query.startsWith("http") -> activateAddButton(query)
            else -> resetLayout()
        }
    }


    private fun activateAddButton(query: String) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
        searchRequestProgressBar.visibility = View.GONE
        noSearchResultsTextView.visibility = View.GONE
        podcastFeedLocation = query
    }


    private fun resetLayout() {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        searchRequestProgressBar.visibility = View.GONE
        noSearchResultsTextView.visibility = View.GONE
    }


    private fun showNoResultsError() {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        searchRequestProgressBar.visibility = View.GONE
        noSearchResultsTextView.visibility = View.VISIBLE
    }


    private fun showProgressIndicator() {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        searchRequestProgressBar.visibility = View.VISIBLE
        noSearchResultsTextView.visibility = View.GONE
    }


    private fun search(context: Context, query: String) {
        // create queue and request and show progress indicator
        showProgressIndicator()
        requestQueue = Volley.newRequestQueue(context)
        val requestUrl = "https://gpodder.net/search.json?q=${query.replace(" ", "+")}"

        // request data from request URL
        val stringRequest = object: StringRequest(Request.Method.GET, requestUrl, responseListener, errorListener) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["User-Agent"] = context.getString(R.string.app_name)
                return params
            }
        }

        // override retry policy
        stringRequest.retryPolicy = object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 30000
            }
            override fun getCurrentRetryCount(): Int {
                return 30000
            }
            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {
                LogHelper.w(TAG, "Error: $error")
            }
        }

        // add to RequestQueue.
        requestQueue.add(stringRequest)
    }


    private fun createGpodderResult(result: String): Array<GpodderResult> {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.setDateFormat("M/d/yy hh:mm a")
        val gson = gsonBuilder.create()
        return gson.fromJson(result, Array<GpodderResult>::class.java)
    }


    private val responseListener: Response.Listener<String> = Response.Listener<String> { response ->
        if (response != null && response.isNotBlank()) {
            val result: Array<GpodderResult> = createGpodderResult(response)
            if (result.isNotEmpty()) {
                Toast.makeText(findPodcastDialogListener as Context, "Best result = ${result[0].title}", Toast.LENGTH_LONG).show()
            }
        }
        resetLayout()
    }


    private val errorListener: Response.ErrorListener = Response.ErrorListener { error ->
        showNoResultsError()
        LogHelper.w(TAG, "Error: $error") }

}