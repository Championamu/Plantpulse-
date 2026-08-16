package com.plantpulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.UUID

data class Plant(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val species: String,
    val light: String,
    val water: String,
    val health: String = "Good"
)

class PlantViewModel : ViewModel() {
    var plants by mutableStateOf(
        listOf(
            Plant(name="My first plant", species="Unknown", light="Bright indirect", water="Check soil before watering")
        )
    )
        private set

    fun addPlant(name: String) {
        plants = plants + Plant(name=name, species="Unknown", light="Bright indirect", water="Check soil before watering")
    }

    fun removePlant(id: String) {
        plants = plants.filterNot { it.id == id }
    }
}

@Composable
fun PlantPulseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF2E7D32),
            secondary = Color(0xFF689F38),
            background = Color(0xFFF7FBF5),
            surface = Color.White
        ),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(vm: PlantViewModel = viewModel()) {
    var screen by remember { mutableStateOf("home") }
    var selected by remember { mutableStateOf<Plant?>(null) }
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (screen) {
                            "doctor" -> "Plant Doctor"
                            "plant" -> selected?.name ?: "Plant"
                            else -> "PlantPulse"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (screen != "home") {
                        IconButton(onClick = { screen = "home" }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (screen == "home") {
                NavigationBar {
                    NavigationBarItem(screen == "home", { screen = "home" }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
                    NavigationBarItem(screen == "doctor", { screen = "doctor" }, icon = { Icon(Icons.Default.Favorite, null) }, label = { Text("Doctor") })
                }
            }
        },
        floatingActionButton = {
            if (screen == "home") {
                FloatingActionButton(onClick = { showAdd = true }) {
                    Icon(Icons.Default.Add, "Add plant")
                }
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize().background(Color(0xFFF7FBF5))) {
            when (screen) {
                "home" -> HomeScreen(vm.plants,
                    onPlant = { selected = it; screen = "plant" },
                    onDoctor = { screen = "doctor" })
                "plant" -> selected?.let { PlantScreen(it, onDoctor = { screen = "doctor" }) }
                "doctor" -> DoctorScreen()
            }
        }
    }

    if (showAdd) {
        AddPlantDialog(
            onDismiss = { showAdd = false },
            onAdd = { name -> vm.addPlant(name); showAdd = false }
        )
    }
}

@Composable
fun HomeScreen(plants: List<Plant>, onPlant: (Plant) -> Unit, onDoctor: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE4F4E1)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().clickable { onDoctor() }
            ) {
                Column(Modifier.padding(22.dp)) {
                    Text("🩺 Plant Doctor", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Something wrong with your plant? Get a guided health check.")
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = onDoctor) { Text("Check my plant") }
                }
            }
        }
        item {
            Text("My Plants", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        items(plants) { plant ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onPlant(plant) },
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🌿", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(plant.name, fontWeight = FontWeight.Bold)
                        Text(plant.species)
                        Spacer(Modifier.height(4.dp))
                        Text("Health: ${plant.health}", color = Color(0xFF2E7D32))
                    }
                    Icon(Icons.Default.ChevronRight, null)
                }
            }
        }
    }
}

@Composable
fun PlantScreen(plant: Plant, onDoctor: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(22.dp)) {
                    Text("🌿", style = MaterialTheme.typography.displayMedium)
                    Text(plant.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(plant.species)
                    Spacer(Modifier.height(18.dp))
                    Text("🟢 ${plant.health}", fontWeight = FontWeight.Bold)
                }
            }
        }
        item { InfoCard("☀️ Light", plant.light) }
        item { InfoCard("💧 Water", plant.water) }
        item {
            Button(onClick = onDoctor, Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Favorite, null)
                Spacer(Modifier.width(8.dp))
                Text("Run Plant Doctor")
            }
        }
    }
}

@Composable
fun InfoCard(title: String, body: String) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(18.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(body)
        }
    }
}

@Composable
fun DoctorScreen() {
    var step by remember { mutableIntStateOf(0) }
    var soil by remember { mutableStateOf("") }
    var watering by remember { mutableStateOf("") }
    var light by remember { mutableStateOf("") }

    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Plant Health Check", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Answer a few questions. The first version uses transparent rule-based guidance; AI photo diagnosis can be connected next.")
        }
        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("📸 Add plant photo", fontWeight = FontWeight.Bold)
                    OutlinedButton(onClick = { step = 1 }, Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.AddAPhoto, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Choose photo")
                    }
                }
            }
        }
        item {
            Question("How does the soil feel?", listOf("Wet", "Moist", "Dry"), soil) { soil = it }
        }
        item {
            Question("When did you last water?", listOf("Today", "2–3 days ago", "1 week+", "Don't know"), watering) { watering = it }
        }
        item {
            Question("Where is the plant?", listOf("Direct sun", "Bright indirect", "Low light"), light) { light = it }
        }
        if (soil.isNotBlank() && watering.isNotBlank() && light.isNotBlank()) {
            item {
                val result = diagnose(soil, watering, light)
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("🩺 Likely issue", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(result.first, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(result.second)
                        Spacer(Modifier.height(12.dp))
                        Text("This is guidance, not a laboratory diagnosis.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun Question(title: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(18.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            options.forEach { option ->
                Row(
                    Modifier.fillMaxWidth().clickable { onSelect(option) }.padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected == option, { onSelect(option) })
                    Text(option)
                }
            }
        }
    }
}

fun diagnose(soil: String, watering: String, light: String): Pair<String, String> {
    if (soil == "Wet" && (watering == "Today" || watering == "2–3 days ago")) {
        return "Possible overwatering" to "Pause watering, check that the pot drains freely, and move the plant to suitable bright indirect light if appropriate."
    }
    if (soil == "Dry" && watering == "1 week+") {
        return "Possible underwatering" to "Check the root zone and water thoroughly if the plant's normal care requirements call for it."
    }
    if (light == "Low light") {
        return "Possible insufficient light" to "Move gradually toward brighter suitable light. Avoid sudden harsh direct sun for shade-adapted plants."
    }
    return "No obvious issue from these answers" to "Keep monitoring the plant and follow species-specific care. If symptoms persist, run another check with clear photos."
}

@Composable
fun AddPlantDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add a plant") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Plant nickname") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onAdd(name.trim()) }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlantPulseTheme { App() }
        }
    }
}
