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
 * "Looking forward to" / Ser emot från — one tiny note per day. Stored in
 * its own table so it doesn't pollute the Spiritual mandala category.
 *
 * Keyed by ISO date so re-saves on the same day overwrite (matching the
 * single-line UI on Home).
 */
@Entity(tableName = "good_things")
data class GoodThingEntity(
    @PrimaryKey @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "text") val text: String
)

@Dao
interface GoodThingDao {
    @Query("SELECT * FROM good_things WHERE date = :date LIMIT 1")
    suspend fun forDate(date: String): GoodThingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: GoodThingEntity)

    @Query("DELETE FROM good_things WHERE date = :date")
    suspend fun deleteForDate(date: String)

    @Query("SELECT * FROM good_things WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun inRange(startDate: String, endDate: String): Flow<List<GoodThingEntity>>
}
