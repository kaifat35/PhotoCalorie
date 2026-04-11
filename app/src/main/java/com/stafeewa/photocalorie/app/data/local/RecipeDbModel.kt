package com.stafeewa.photocalorie.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index

@Entity(
    tableName = "recipes",
    primaryKeys = ["sourceUrl", "topic"],
    foreignKeys = [
        ForeignKey(
            entity = SubscriptionDbModel::class,
            parentColumns = ["topic"],
            childColumns = ["topic"],
            onDelete = CASCADE
        )
    ],
    indices = [Index("topic")]
)
data class RecipeDbModel(
    val id: Int,
    val image: String,
    val nutrition: String,
    val title: String,
    val sourceUrl: String,
    val topic: String
)

