package com.softworld.mecadocs.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.softworld.mecadocs.data.local.entity.CachedComponentEntity
import com.softworld.mecadocs.data.local.entity.FavoriteComponentEntity
import com.softworld.mecadocs.data.local.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) do Room para gerenciamento de Favoritos e Histórico de Busca.
 */
@Dao
interface MecaDocsDao {

    // --- FAVORITOS ---

    @Query("SELECT * FROM favorite_components ORDER BY timestampFav DESC")
    fun getAllFavoritesFlow(): Flow<List<FavoriteComponentEntity>>

    @Query("SELECT * FROM favorite_components ORDER BY timestampFav DESC")
    suspend fun getAllFavorites(): List<FavoriteComponentEntity>

    @Query("SELECT * FROM favorite_components WHERE LOWER(nome) = LOWER(:nome) LIMIT 1")
    suspend fun getFavoriteByName(nome: String): FavoriteComponentEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_components WHERE LOWER(nome) = LOWER(:nome))")
    fun isFavoriteFlow(nome: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_components WHERE LOWER(nome) = LOWER(:nome))")
    suspend fun isFavorite(nome: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(entity: FavoriteComponentEntity)

    @Delete
    suspend fun deleteFavorite(entity: FavoriteComponentEntity)

    @Query("DELETE FROM favorite_components WHERE LOWER(nome) = LOWER(:nome)")
    suspend fun deleteFavoriteByName(nome: String)

    // --- CACHE DE AI ---

    @Query("SELECT * FROM cached_components WHERE LOWER(nome) = LOWER(:nome) LIMIT 1")
    suspend fun getCachedComponentByName(nome: String): CachedComponentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedComponent(entity: CachedComponentEntity)

    // --- HISTÓRICO DE BUSCA ---

    @Query("SELECT searchTerm FROM search_history ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSearchesFlow(): Flow<List<String>>

    @Query("SELECT searchTerm FROM search_history ORDER BY timestamp DESC LIMIT 10")
    suspend fun getRecentSearches(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchHistory(entity: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE searchTerm = :term")
    suspend fun deleteSearchHistory(term: String)

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()
}
