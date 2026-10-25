package au.edu.jcu.assessment.utilityapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import au.edu.jcu.assessment.utilityapp.data.AppContainer
import au.edu.jcu.assessment.utilityapp.ui.theme.MobileTech_UtilityAppTheme
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MobileTech_UtilityAppTheme { UtilityApp() } }
    }
}

@Composable
fun UtilityApp() {
    val model: ExchangeViewModel = viewModel(factory = ExchangeViewModel.Factory(AppContainer.repository))
    val state by model.state.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    Scaffold(bottomBar = {
        NavigationBar {
            NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                icon = { Icon(Icons.Default.Home, contentDescription = null) }, label = { Text("Convert") })
            NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                icon = { Icon(Icons.Default.Settings, contentDescription = null) }, label = { Text("Settings") })
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
            .padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            if (selectedTab == 0) UtilityScreen(state, model::refresh)
            else SettingsScreen(state, model::setAmount, model::setCurrency, model::setUnitRate)
        }
    }
}

private fun money(value: Double): String = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
    minimumFractionDigits = 2
    maximumFractionDigits = 2
}.format(value)

@Composable
fun UtilityScreen(state: ExchangeUiState, onRefresh: () -> Unit) {
    val rate = state.data?.rates?.get(state.currency)
    val converted = convertAmount(state.amount, rate)
    Text("Pocket Exchange", style = MaterialTheme.typography.headlineLarge)
    Text("Your AUD, at a glance", style = MaterialTheme.typography.titleMedium)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(24.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("${state.amount.ifBlank { "--" }} AUD", style = MaterialTheme.typography.titleLarge)
            Text(converted?.let { "${money(it)} ${state.currency}" } ?: "Rate unavailable",
                style = MaterialTheme.typography.headlineMedium)
            if (state.showUnitRate && rate != null && rate.isFinite() && rate > 0)
                Text("1 AUD = ${money(rate)} ${state.currency}")
        }
    }
    if (state.loading) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        Text("Updating rates...")
    }
    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    if (state.data != null) {
        Text("Rate date: ${state.data.date}")
        if (state.error != null) Text("Showing previously loaded rates; these may be out of date.")
        if (rate == null) Text("This currency is not available in the latest response.")
    } else if (!state.loading) Text("Connect to the internet and refresh to load rates.")
    Button(onClick = onRefresh, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) {
        Text("Refresh rates")
    }
    Text("Change your amount and destination currency in Settings.")
    HorizontalDivider()
    Text("Source: ExchangeRate-API (exchangerate-api.com). Indicative daily rates; bank fees and transfer margins are excluded.",
        style = MaterialTheme.typography.bodySmall)
}

@Composable
fun SettingsScreen(state: ExchangeUiState, onAmount: (String) -> Unit,
                   onCurrency: (String) -> Unit, onUnitRate: (Boolean) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val invalid = convertAmount(state.amount, 1.0) == null
    Text("Settings", style = MaterialTheme.typography.headlineLarge)
    Text("Make your quick conversion useful for you.")
    OutlinedTextField(value = state.amount, onValueChange = onAmount,
        label = { Text("Amount in AUD") }, singleLine = true, isError = invalid,
        supportingText = { Text(if (invalid) "Enter a finite amount of zero or more (e.g. 100.50)." else "Use a decimal point for cents.") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
    Box {
        OutlinedButton(onClick = { expanded = true }) { Text("Destination: ${state.currency}") }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            supportedCurrencies.forEach { code ->
                DropdownMenuItem(text = { Text(code) }, onClick = { onCurrency(code); expanded = false })
            }
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Show rate for 1 AUD", modifier = Modifier.weight(1f).padding(top = 12.dp))
        Switch(checked = state.showUnitRate, onCheckedChange = onUnitRate)
    }
    Text("Changes apply immediately. Settings survive screen rotation but reset when the app process closes.",
        style = MaterialTheme.typography.bodySmall)
}
