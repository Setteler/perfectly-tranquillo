package com.methoda.tranquillo.data

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(tableName = "schema_marker")
internal data class SchemaMarker(
    @PrimaryKey val id: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long = 0L
)

@Dao
internal interface SchemaMarkerDao {
    @Query("SELECT COUNT(*) FROM schema_marker")
    suspend fun count(): Int
}

@Database(
    entities = [SchemaMarker::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    internal abstract fun schemaMarkerDao(): SchemaMarkerDao

    companion object {
        private const val DB_NAME = "perfectly_tranquillo.db"

        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                ).build().also { instance = it }
            }
    }
}
