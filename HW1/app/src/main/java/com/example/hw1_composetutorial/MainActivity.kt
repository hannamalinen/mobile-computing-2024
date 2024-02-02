package com.example.hw1_composetutorial

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.hw1_composetutorial.ui.theme.HW1ComposeTutorialTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import android.content.res.Configuration
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.material3.Button
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavController
import androidx.compose.ui.Alignment

import android.net.Uri
import android.util.Log
// import androidx.activity.compose.registerForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.TextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hw1_composetutorial.ui.theme.AppDatabase
import com.example.hw1_composetutorial.ui.theme.UserProfile
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.AndroidViewModel
import androidx.room.Room
import androidx.core.net.toUri
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HW1ComposeTutorialTheme {
                AppNavigation()
                // A surface container using the 'background' color from the theme
                // Surface(
                //    modifier = Modifier.fillMaxSize(),
                //    color = MaterialTheme.colorScheme.background
                //) {
                // Greeting("Android")
            }
            // Conversation(SampleData.conversationSample)
        }
        // Text("Hello world!")
        // MessageCard(Message("Hanna", "Hello what's up"))
    }
}
//}

data class Message(val author: String, val body: String)

@Composable
fun MessageCard(msg: Message) {
    // let's add padding around the message
    Row (modifier = Modifier.padding(all = 8.dp)) {
        Image(
            painter = painterResource(R.drawable.profiilikuva_jpg),
            contentDescription = "Contact profile picture",
            modifier = Modifier
                // set image size to 40 dp
                .size(40.dp)
                // clip image to be shaped as a circle
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )

        // add a horizontal space between the image and the column
        Spacer(modifier = Modifier.width(8.dp))

        // we keep track if the message is expanded or not in this variable
        var isExpanded by remember { mutableStateOf(false) }
        // surfaceColor will be updated gradually from one color to the other
        val surfaceColor by animateColorAsState(
            if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            label = "",
        )

        // we toggle the isExpanded variable when we click on this Column
        Column (modifier = Modifier.clickable { isExpanded = !isExpanded }) {
            Text(
                text = msg.author,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall
            )
            // add a vertical space between the author and message texts
            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp,
                // surfaceColor color will be changing gradually from primary to surface
                color = surfaceColor,
                // animateContentSize will change the Surface size gradually
                modifier = Modifier.animateContentSize().padding(1.dp)
            ) {
                Text(
                    text = msg.body,
                    modifier = Modifier.padding(all = 4.dp),
                    // if the message is expanded, we display all its content
                    // otherwise we only display the first line
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

    }
}

@Composable
fun Conversation(messages: List<Message>) {
    LazyColumn {
        items(messages) { message ->
            MessageCard(message)
        }
    }
}

@Preview
@Composable
fun PreviewConversation() {
    HW1ComposeTutorialTheme {
        Conversation(SampleData.conversationSample)
    }
}

@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)

@Preview
@Composable
fun PreviewMessageCard() {
    HW1ComposeTutorialTheme {
        Surface {
            MessageCard(
                msg = Message("Lexi", "Take a look at Jetpack Compose, it's great!")
            )
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HW1ComposeTutorialTheme {
        Greeting("Android")
    }
}

// luokka user profile -nakyman hallintaan
class UserProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val db: AppDatabase by lazy {
        // alusta tietokanta
        Room.databaseBuilder(application, AppDatabase::class.java, "user_profile_db")
            .fallbackToDestructiveMigration() // kehitysvaihe
            .build()
    }

    val userProfile = MutableLiveData<UserProfile?>()

    init {
        loadUserProfile()
    }
// tallenna kayttajan profiili
    fun saveUserProfile(username: String, imageUri: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newUserProfile = UserProfile(username = username, imageUri = imageUri ?: "")
                db.userProfileDao().insertOrUpdateUserProfile(newUserProfile)
                userProfile.postValue(newUserProfile)

            // log errorien virheiden nappaamiseen
            } catch (e: Exception) {
                Log.e("UserProfileViewModel", "Error saving user profile", e)
            }
        }
    }
// lataa kayttajan profiili
    private fun loadUserProfile() {
        viewModelScope.launch {
            userProfile.postValue(db.userProfileDao().getLastUserProfile())
        }
    }
}

// navigointi
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: UserProfileViewModel = viewModel()

    NavHost(navController, startDestination = "first") {
        composable("first") { FirstView(navController, viewModel) }
        composable("second") { SecondView(navController, viewModel) }
    }
}

@Composable
fun FirstView(navController: NavController, viewModel: UserProfileViewModel) {
    val userProfile by viewModel.userProfile.observeAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // ylareunan layout kayttäjan tervehdykselle, profiilikuvalle ja asetukset-napille
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // vasemmalla puolella profiilikuva ja tervehdys
            Row(verticalAlignment = Alignment.CenterVertically) {
                userProfile?.imageUri?.let { imageUri ->
                    if (imageUri.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(model = imageUri),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Hello, ${userProfile?.username ?: "Guest"}!",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            // oikealla puolella asetukset-nappi
            Button(onClick = { navController.navigate("second") }) {
                Text("Settings")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // keskusteluosio
        Conversation(SampleData.conversationSample)

    }
}

@Composable
fun SecondView(navController: NavController, viewModel: UserProfileViewModel) {
    // liveDatan seuranta
    val userProfile by viewModel.userProfile.observeAsState()

    // alusta tilamuuttujat
    var username by remember { mutableStateOf(userProfile?.username ?: "") }
    var imageUri by remember { mutableStateOf(userProfile?.imageUri ?: "") }

    // kuvan valinta profiilikuvaan
    val context = LocalContext.current
    val openDocumentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                val contentResolver = context.contentResolver
                contentResolver.takePersistableUriPermission(uri, takeFlags)
                imageUri = uri.toString()
            // log errorien virheiden nappaamiseen
            } catch (e: Exception) {
                Log.e("MainActivity", "Error granting persistable permission", e)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        // Text(text = "my profile", style = MaterialTheme.typography.titleLarge)
        // paivita otsikko kayttajanimen mukaan
        val title = userProfile?.username?.let { "$it's profile" } ?: "my profile"
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        if (imageUri.isNotEmpty()) {
            val painter = rememberAsyncImagePainter(model = imageUri)
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
        } else {
            Image(
                painter = painterResource(R.drawable.profiilikuva_jpg),
                contentDescription = "Profiilikuva",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Text(text = "My profile", style = MaterialTheme.typography.bodyLarge)
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("my name") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        // aseta uusi profiilikuva
        Button(onClick = { openDocumentLauncher.launch(arrayOf("image/*")) }) {
            Text("set a new profile picture")
        }
        Spacer(modifier = Modifier.height(16.dp))
        // tallenna profiili
        Button(onClick = {
            if (username.isNotEmpty() && imageUri.isNotEmpty()) {
                viewModel.saveUserProfile(username, imageUri)
            }
        }) {
            Text("save")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("first") }) {
            Text("back 2 conversations")
        }
    }
}

