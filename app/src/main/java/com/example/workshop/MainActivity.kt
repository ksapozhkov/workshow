@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.workshop

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.workshop.ui.theme.WorkshopTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkshopTheme {
                LoyaltyCardsApp()
            }
        }
    }
}

enum class LoyaltyCodeType(val title: String) {
    BARCODE("Barcode"),
    QR("QR"),
    AZTEC("Aztec")
}

private enum class ScreenMode {
    ADD,
    VIEW
}

@Composable
fun LoyaltyCardsApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val databaseManager = remember(context) { DatabaseManager(context) }
    val viewModel = remember(databaseManager) { CardViewModel(databaseManager) }
    var currentScreen by remember { mutableStateOf(ScreenMode.ADD) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Loyalty cards") },
                actions = {
                    TextButton(onClick = { currentScreen = ScreenMode.ADD }) {
                        Text("Add")
                    }
                    TextButton(onClick = {
                        viewModel.loadCards()
                        currentScreen = ScreenMode.VIEW
                    }) {
                        Text("View")
                    }
                }
            )
        }
    ) { innerPadding ->
        when (currentScreen) {
            ScreenMode.ADD -> AddLoyaltyCardScreen(
                databaseManager = databaseManager,
                onCardSaved = {
                    viewModel.loadCards()
                    currentScreen = ScreenMode.VIEW
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )

            ScreenMode.VIEW -> ViewLoyaltyCardScreen(
                viewModel = viewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

@Composable
fun AddLoyaltyCardScreen(
    databaseManager: DatabaseManager,
    onCardSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var serviceName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var selectedCodeType by remember { mutableStateOf(LoyaltyCodeType.BARCODE) }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = serviceName,
            onValueChange = { serviceName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Service / Store") },
            singleLine = true
        )

        OutlinedTextField(
            value = cardNumber,
            onValueChange = { cardNumber = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Card number") },
            singleLine = true
        )

        Text(text = "Code type")

        LoyaltyCodeType.entries.forEach { type ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedCodeType = type }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedCodeType == type,
                    onClick = { selectedCodeType = type }
                )
                Text(
                    text = type.title,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Button(
            onClick = {
                val insertedId = databaseManager.insertLoyaltyCard(
                    serviceName = serviceName.trim(),
                    cardNumber = cardNumber.trim(),
                    codeType = selectedCodeType.title
                )

                if (insertedId > 0) {
                    Toast.makeText(context, "Card saved", Toast.LENGTH_SHORT).show()
                    serviceName = ""
                    cardNumber = ""
                    selectedCodeType = LoyaltyCodeType.BARCODE
                    onCardSaved()
                } else {
                    Toast.makeText(context, "Save failed", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = serviceName.isNotBlank() && cardNumber.isNotBlank()
        ) {
            Text("Save card")
        }
    }
}

@Composable
fun ViewLoyaltyCardScreen(
    viewModel: CardViewModel,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        viewModel.loadCards()
    }

    val cards = viewModel.cards
    val selectedCard = viewModel.selectedCard

    if (cards.isEmpty()) {
        Column(
            modifier = modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("No cards yet")
            Button(onClick = {
                viewModel.loadCards()
            }) {
                Text("Refresh")
            }
        }
        return
    }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Saved cards")

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cards, key = { it.id }) { card ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectCard(card) },
                    colors = CardDefaults.cardColors()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(card.serviceName)
                        Text("**** ${card.cardNumber.takeLast(4)}")
                    }
                }
            }
        }

        selectedCard?.let { card ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Card details")
                    Text("Service: ${card.serviceName}")
                    Text("Number: ${card.cardNumber}")
                    Text("Code type: ${card.codeType}")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoyaltyCardsAppPreview() {
    WorkshopTheme {
        LoyaltyCardsApp()
    }
}
