package com.methoda.tranquillo.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * One stone (visualized in the Garden as a seashell) earned from a ritual
 * completion — a Morning intention, an Evening reflection, a finished Breath
 * session, a Focus block, a Take-a-break, or a habit completion.
 *
 * `kind` is `StoneKind.name` (Moon, Jade, Shell, Coral, Sand, Deep).
 * `source` is a free-form tag — "morning", "evening", "breath", "focus",
 * "break", "habit", etc. — used by the legend to count by kind.
 */
@Entity(tableName = "stones")
data class StoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "kind") val kind: String,
    @ColumnInfo(name = "source") val source: String,
    @ColumnInfo(name = "awarded_at") val awardedAt: Long
)

@Dao
interface StoneDao {
    @Insert
    suspend fun insert(entity: StoneEntity): Long

    @Query("SELECT * FROM stones ORDER BY awarded_at ASC")
    fun all(): Flow<List<StoneEntity>>

    @Query("SELECT COUNT(*) FROM stones")
    fun count(): Flow<Int>

    @Query("DELETE FROM stones")
    suspend fun clear()
}
