package com.methoda.tranquillo.data

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

/**
 * A single mandala entry — one resource, one phase (am/pm), one kind
 * (resource = good thing noticed; challenge = small ache noticed).
 *
 * Keyed by the triple (date, key, phase, kind) — we upsert on that tuple
 * so the user can re-open a petal and rewrite their entry.
 */
@Entity(tableName = "mandala_entries")
data class MandalaEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    // ISO date yyyy-MM-dd
    @ColumnInfo(name = "date") val date: String,
    // ResourceKey.name (Physical, Intellectual, ...)
    @ColumnInfo(name = "key") val key: String,
    // "am" | "pm"
    @ColumnInfo(name = "phase") val phase: String,
    // "resource" | "challenge"
    @ColumnInfo(name = "kind") val kind: String,
    @ColumnInfo(name = "text") val text: String
)

@Dao
interface MandalaEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MandalaEntryEntity): Long

    @Query("DELETE FROM mandala_entries WHERE date = :date AND `key` = :key AND phase = :phase AND kind = :kind")
    suspend fun deleteForKeyPhaseKind(date: String, key: String, phase: String, kind: String)

    @Query("SELECT * FROM mandala_entries WHERE date = :date AND `key` = :key AND phase = :phase AND kind = :kind LIMIT 1")
    suspend fun findForKeyPhaseKind(date: String, key: String, phase: String, kind: String): MandalaEntryEntity?

    /** Upsert: delete any existing row for (date,key,phase,kind) then insert fresh. */
    suspend fun upsertForKeyPhaseKind(date: String, key: String, phase: String, kind: String, text: String) {
        deleteForKeyPhaseKind(date, key, phase, kind)
        if (text.isNotBlank()) {
            insert(
                MandalaEntryEntity(
                    date = date, key = key, phase = phase, kind = kind, text = text
                )
            )
        }
    }

    @Query("SELECT * FROM mandala_entries WHERE date = :date")
    fun entriesForDate(date: String): Flow<List<MandalaEntryEntity>>
}

@Database(
    entities = [MandalaEntryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mandalaEntryDao(): MandalaEntryDao

    companion object {
        private const val DB_NAME = "perfectly_tranquillo.db"

        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
