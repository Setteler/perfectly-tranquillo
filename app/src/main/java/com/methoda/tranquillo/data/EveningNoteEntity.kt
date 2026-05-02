package com.methoda.tranquillo.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * "Tonight I noticed" — the optional one-line evening reflection captured in
 * EveningScreen and surfaced in the Home + Garden cards. One row per day,
 * keyed by ISO date so re-saves on the same day overwrite. Mirrors the
 * GoodThingEntity shape on purpose.
 */
@Entity(tableName = "evening_notes")
data class EveningNoteEntity(
    @PrimaryKey @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "text") val text: String
)

@Dao
interface EveningNoteDao {
    @Query("SELECT * FROM evening_notes WHERE date = :date LIMIT 1")
    suspend fun forDate(date: String): EveningNoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: EveningNoteEntity)

    @Query("DELETE FROM evening_notes WHERE date = :date")
    suspend fun deleteForDate(date: String)

    @Query("SELECT * FROM evening_notes WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun inRange(startDate: String, endDate: String): Flow<List<EveningNoteEntity>>

    @Query("DELETE FROM evening_notes WHERE date < :cutoffDate")
    suspend fun deleteOlderThan(cutoffDate: String)
}
