package com.softworld.mecadocs.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade do Room Database para armazenar o histórico de buscas realizadas na aplicação.
 */
@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey
    val searchTerm: String,
    val timestamp: Long = System.currentTimeMillis(),
)
