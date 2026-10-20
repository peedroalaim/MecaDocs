package com.softworld.mecadocs.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.softworld.mecadocs.data.local.dao.MecaDocsDao
import com.softworld.mecadocs.data.local.entity.CachedComponentEntity
import com.softworld.mecadocs.data.local.entity.FavoriteComponentEntity
import com.softworld.mecadocs.data.local.entity.SearchHistoryEntity

/**
 * Banco de Dados do Room para persistência offline de favoritos e histórico na aplicação MecaDocs.
 */
@Database(
    entities = [FavoriteComponentEntity::class, SearchHistoryEntity::class, CachedComponentEntity::class],
    version = 2,
    exportSchema = false,
)
@TypeConverters(MecaDocsTypeConverters::class)
abstract class MecaDocsDatabase : RoomDatabase() {

    abstract fun mecaDocsDao(): MecaDocsDao

    companion object {
        @Volatile
        private var INSTANCE: MecaDocsDatabase? = null

        fun getInstance(context: Context): MecaDocsDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MecaDocsDatabase::class.java,
                    "mecadocs_db",
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
