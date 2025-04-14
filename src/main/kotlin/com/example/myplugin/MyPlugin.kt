package com.example.storytellerdemo

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import com.storyteller.Storyteller
import com.storyteller.data.StorytellerClipsDataModel
import com.storyteller.data.StorytellerStoriesDataModel
import com.storyteller.domain.entities.Error
import com.storyteller.domain.entities.UserInput
import com.storyteller.ui.compose.components.lists.grid.StorytellerClipsGrid
import com.storyteller.ui.compose.components.lists.grid.StorytellerStoriesGrid
import com.storyteller.ui.compose.components.lists.grid.rememberStorytellerGridState
import com.storyteller.ui.compose.components.lists.row.StorytellerClipsRow
import com.storyteller.ui.compose.components.lists.row.StorytellerStoriesRow
import com.storyteller.ui.compose.components.lists.row.rememberStorytellerRowState
import com.storyteller.ui.list.StorytellerListViewDelegate
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.json.JSONArray

class MyPlugin : CordovaPlugin() {

    override fun execute(action: String, args: JSONArray, callbackContext: CallbackContext): Boolean {
        return when (action) {
            "showMainScreen" -> {
                val apiKey = args.getString(1) // API key
                cordova.activity.runOnUiThread {
                    showMainScreen(apiKey, callbackContext)
                }
                true
            }
            "dismissDialog" -> {
                cordova.activity.runOnUiThread {
                    dismissDialog(callbackContext)
                }
                true
            }
            else -> false
        }
    }

    private var storytellerDialog: StorytellerDialogFragment? = null

    private fun showMainScreen(apiKey: String, callbackContext: CallbackContext) {
        val activity = cordova.activity as? AppCompatActivity ?: return

        if (isNetworkAvailable(activity)) {
            Storyteller.initialize(
                apiKey = apiKey,
                userInput = UserInput("unique-user-id"),
                onSuccess = {
                    // Use DialogFragment instead of replacing the entire activity content
                    storytellerDialog = StorytellerDialogFragment()
                    storytellerDialog?.show(activity.supportFragmentManager, "StorytellerDialog")
                    callbackContext.success("Success Initializing and showing Storyteller Dialog")
                },
                onFailure = { error ->
                    callbackContext.error("Error Initializing Storyteller: $error")
                }
            )
        } else {
            callbackContext.error("No internet connection available")
        }
    }

    private fun dismissDialog(callbackContext: CallbackContext) {
        storytellerDialog?.dismiss()
        storytellerDialog = null
        callbackContext.success("Dialog dismissed")
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

// DialogFragment implementation to show the Compose UI
class StorytellerDialogFragment : DialogFragment() {
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Create a ComposeView to host our Compose UI
        return ComposeView(requireContext()).apply {
            setContent {
                // Use MaterialTheme or your custom theme
                MaterialTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        MainScreen(onClose = {
                            dismiss()
                        })
                    }
                }
            }
        }
    }
    
    override fun onStart() {
        super.onStart()
        // Make the dialog fullscreen or adjust size as needed
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }
}

@Composable
fun MainScreen(onClose: () -> Unit) {
    val listViewDelegate by remember("your_item_id") {
        val value = object : StorytellerListViewDelegate {
            override fun onPlayerDismissed() {
                // You could trigger onClose here if needed
            }

            override fun onDataLoadStarted() {
                // handle loading started event
            }

            override fun onDataLoadComplete(success: Boolean, error: Error?, dataCount: Int) {
                // handle data load complete event
            }
        }
        mutableStateOf(value)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Optional: Add a close button
        // Button(onClick = onClose) { Text("Close") }
        
        Text(
            text = "Stories",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
        
        // Stories Row
        StorytellerStoriesRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            dataModel = StorytellerStoriesDataModel(categories = listOf("benfica-top-row,benfica-singleton,benfica-moments")),
            delegate = listViewDelegate,
            state = rememberStorytellerRowState()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Stories Grid
        StorytellerStoriesGrid(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            dataModel = StorytellerStoriesDataModel(categories = listOf("benfica-top-row,benfica-singleton,benfica-moments")),
            delegate = listViewDelegate,
            state = rememberStorytellerGridState(),
            isScrollable = true,
            isEnabled = true
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
                .fillMaxWidth()
                .height(120.dp),
            dataModel = StorytellerClipsDataModel(collection = "benfica-moments"),
            delegate = listViewDelegate,
            state = rememberStorytellerRowState()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Clips Scrollable Grid
        StorytellerClipsGrid(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            dataModel = StorytellerClipsDataModel(collection = "benfica-moments"),
            delegate = listViewDelegate,
            state = rememberStorytellerGridState(),
            isScrollable = true,
            isEnabled = true
        )
    }
}