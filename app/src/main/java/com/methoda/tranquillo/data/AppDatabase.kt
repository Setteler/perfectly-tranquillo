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
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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

    @Query("SELECT * FROM mandala_entries WHERE date >= :startDate AND date <= :endDate")
    fun entriesInRange(startDate: String, endDate: String): Flow<List<MandalaEntryEntity>>

    @Query("DELETE FROM mandala_entries WHERE date = :date")
    suspend fun deleteAllForDate(date: String)
}

@Database(
    entities = [
        MandalaEntryEntity::class,
        HabitEntity::class,
        WeeklyHabitEntity::class,
        HabitFillEntity::class,
        StoneEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mandalaEntryDao(): MandalaEntryDao
    abstract fun habitDao(): HabitDao
    abstract fun weeklyHabitDao(): WeeklyHabitDao
    abstract fun habitFillDao(): HabitFillDao
    abstract fun stoneDao(): StoneDao

    companion object {
        private const val DB_NAME = "perfectly_tranquillo.db"

        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }

        private fun buildDatabase(context: Context): AppDatabase {
            val appCtx = context.applicationContext
            lateinit var built: AppDatabase
            val seedScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            built = Room.databaseBuilder(
                appCtx,
                AppDatabase::class.java,
                DB_NAME
            )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        // Seed defaults once on first DB creation.
                        seedScope.launch {
                            HabitSeeder.seedAllIfEmpty(built)
                        }
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        // Also seed on open — handles destructive migration case
                        // (fresh schema but onCreate not re-fired for us reliably).
                        seedScope.launch {
                            HabitSeeder.seedAllIfEmpty(built)
                        }
                    }
                })
                .build()
            return built
        }
    }
}
