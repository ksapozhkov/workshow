package com.example.workshop

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class LoyaltyCard(
    val id: Long,
    val serviceName: String,
    val cardNumber: String,
    val codeType: String
)

class DatabaseManager(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_LOYALTY_CARDS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_SERVICE_NAME TEXT NOT NULL,
                $COLUMN_CARD_NUMBER TEXT NOT NULL,
                $COLUMN_CODE_TYPE TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOYALTY_CARDS")
        onCreate(db)
    }

    fun insertLoyaltyCard(serviceName: String, cardNumber: String, codeType: String): Long {
        val values = ContentValues().apply {
            put(COLUMN_SERVICE_NAME, serviceName)
            put(COLUMN_CARD_NUMBER, cardNumber)
            put(COLUMN_CODE_TYPE, codeType)
        }

        return writableDatabase.insert(TABLE_LOYALTY_CARDS, null, values)
    }

    fun getAllLoyaltyCards(): List<LoyaltyCard> {
        val cards = mutableListOf<LoyaltyCard>()
        val cursor = readableDatabase.query(
            TABLE_LOYALTY_CARDS,
            arrayOf(COLUMN_ID, COLUMN_SERVICE_NAME, COLUMN_CARD_NUMBER, COLUMN_CODE_TYPE),
            null,
            null,
            null,
            null,
            "$COLUMN_ID DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                cards.add(
                    LoyaltyCard(
                        id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID)),
                        serviceName = it.getString(it.getColumnIndexOrThrow(COLUMN_SERVICE_NAME)),
                        cardNumber = it.getString(it.getColumnIndexOrThrow(COLUMN_CARD_NUMBER)),
                        codeType = it.getString(it.getColumnIndexOrThrow(COLUMN_CODE_TYPE))
                    )
                )
            }
        }

        return cards
    }

    companion object {
        private const val DATABASE_NAME = "loyalty_cards.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_LOYALTY_CARDS = "loyalty_cards"
        private const val COLUMN_ID = "id"
        private const val COLUMN_SERVICE_NAME = "service_name"
        private const val COLUMN_CARD_NUMBER = "card_number"
        private const val COLUMN_CODE_TYPE = "code_type"
    }
}
