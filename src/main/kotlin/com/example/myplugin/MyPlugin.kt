package com.example.storytellerdemo

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.CallbackContext
import org.json.JSONArray
import org.json.JSONException
import androidx.activity.compose.setContent
import com.storyteller.Storyteller
import com.storyteller.data.StorytellerClipsDataModel
import com.storyteller.data.StorytellerStoriesDataModel
import com.storyteller.domain.entities.Error
import com.storyteller.domain.entities.UserInput
import com.storyteller.ui.compose.components.lists.grid.StorytellerClipsGrid
import com.storyteller.ui.compose.components.lists.grid.StorytellerStoriesGrid
import com.storyteller.ui.compose.components.lists.row.StorytellerClipsRow
import com.storyteller.ui.compose.components.lists.row.StorytellerStoriesRow
import com.storyteller.ui.compose.components.lists.row.rememberStorytellerRowState
import com.storyteller.ui.list.StorytellerListViewDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MyPlugin : CordovaPlugin() {

    override fun execute(action: String, args: JSONArray, callbackContext: CallbackContext): Boolean {
        return when (action) {
            "showMainScreen" -> {
                val url = args.getString(0) // URL to load in the WebView
                val apiKey = args.getString(1) // API key
                cordova.activity.runOnUiThread {
                    showMainScreen(url, apiKey, callbackContext)
                }
                true
            }
            else -> false
        }
    }

    private fun showMainScreen(apiKey: String, callbackContext: CallbackContext) {
        val activity = cordova.activity as? AppCompatActivity ?: return

        if (isNetworkAvailable(activity)) {
            Storyteller.initialize(
                apiKey = BuildConfig.API_KEY,
                userInput = UserInput("unique-user-id"),
                onSuccess = {
                    activity.setContent {
                        StoryTellerDemoTheme {
                            Surface(modifier = Modifier.fillMaxSize()) {
                                MainScreen()
                            }
                        }
                    }
                    callbackContext.success("Success Initializing and showing MainScreen")
                },
                onFailure = { error ->
                    callbackContext.error("Error Initializing Storyteller: $error")
                }
            )
        } else {
            callbackContext.error("No internet connection available")
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

@Composable
fun MainScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Stories",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
        // Stories Row
        StorytellerStoriesRow(
            modifier = Modifier
                .fillMaxSize()
                .height(120.dp),
            dataModel = StorytellerStoriesDataModel(categories = listOf("benfica-top-row,benfica-singleton,benfica-moments")),
            delegate = remember { mutableStateOf(object : StorytellerListViewDelegate {
                override fun onPlayerDismissed() {}
                override fun onDataLoadStarted() {}
                override fun onDataLoadComplete(success: Boolean, error: Error?, dataCount: Int) {}
            }) }.value,
            state = rememberStorytellerRowState()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Stories Grid
        StorytellerStoriesGrid(
            modifier = Modifier
                .fillMaxSize()
                .height(200.dp),
            dataModel = StorytellerStoriesDataModel(categories = listOf("benblica-top-row,benfica-singleton,benfica-moments")),
            delegate = rememberStorytellerGridState().delegate ?: remember { mutableStateOf(object : StorytellerListViewDelegate {
                override fun onPlayerDismissed() {}
                override fun onDataLoadStarted() {}
                override fun onDataLoadComplete(success: Boolean, error: Error?, dataCount: Int) {}
            }) }.value,
            state = rememberStorytellerGridState()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Clips",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )

        // Clips Row
        StorytellerClipsRow(
            modifier = Modifier
                .fillMaxSize()
                .height(120.dp),
            dataModel = StorytellerClipsDataModel(collection = "benfica-moments"),
            delegate = remember { mutableStateOf(object : StorytellerListViewDelegate {
                override fun onPlayerDismissed() {}
                override fun onDataLoadStarted() {}
                override fun onDataLoadComplete(success: Boolean, error: Error?, dataCount: Int) {}
            }) }.value,
            state = rememberStorytellerRowState()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Clips Scrollable Grid
        StorytellerClipsGrid(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            dataModel = StorytellerClipsDataModel(collection = "benfica-moments"),
            delegate = rememberStorytellerGridState().delegate ?: remember { mutableStateOf(object : StorytellerListViewDelegate {
                override fun onPlayerDismissed() {}
                override fun onDataLoadStarted() {}
                override fun onDataLoadComplete(success: Boolean, error: Error?, dataCount: Int) {}
            }) }.value,
            state = rememberStorytellerGridState(),
            isScrollable = true,
            isEnabled = true
        )
    }
}
