package com.example.workshop

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class CardViewModel(
    private val databaseManager: DatabaseManager
) {
    var cards by mutableStateOf<List<LoyaltyCard>>(emptyList())
        private set

    var selectedCard by mutableStateOf<LoyaltyCard?>(null)
        private set

    fun loadCards() {
        cards = databaseManager.getAllLoyaltyCards()
        if (selectedCard == null && cards.isNotEmpty()) {
            selectedCard = cards.first()
        }
    }

    fun selectCard(card: LoyaltyCard) {
        selectedCard = card
    }
}
