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
 * A daily habit row.
 *
 * `lastDoneDate` (ISO yyyy-MM-dd) makes "done today" a pure function of the
 * current date so rollover is trivial — no nightly mutation required.
 */
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "label") val label: String,
    @ColumnInfo(name = "hint") val hint: String,
    @ColumnInfo(name = "streak") val streak: Int = 0,
    @ColumnInfo(name = "remind_at") val remindAt: String? = null,
    @ColumnInfo(name = "last_done_date") val lastDoneDate: String? = null,
    @ColumnInfo(name = "position") val position: Int = 0
)

/**
 * A habit tied to a specific day of the week (0..6, Sunday=0 per the prototype).
 */
@Entity(tableName = "weekly_habits")
data class WeeklyHabitEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "label") val label: String,
    @ColumnInfo(name = "hint") val hint: String,
    @ColumnInfo(name = "day_of_week") val dayOfWeek: Int,
    @ColumnInfo(name = "streak") val streak: Int = 0,
    @ColumnInfo(name = "remind_at") val remindAt: String? = null,
    @ColumnInfo(name = "last_done_date") val lastDoneDate: String? = null,
    @ColumnInfo(name = "position") val position: Int = 0
)

/**
 * A record of a habit completion that contributes `fillContribution` to the
 * mandala resource `mappedResource` on `date`. Allows reconstruction of the
 * mandala fill boost without stuffing it into `resources` state.
 */
@Entity(tableName = "habit_fills")
data class HabitFillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "habit_id") val habitId: String,
    @ColumnInfo(name = "mapped_resource") val mappedResource: String,
    @ColumnInfo(name = "fill_contribution") val fillContribution: Float
)

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY position ASC, id ASC")
    fun allDaily(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(habit: HabitEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(habit: HabitEntity): Long

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE habits SET streak = :streak WHERE id = :id")
    suspend fun setStreak(id: String, streak: Int)

    @Query("UPDATE habits SET last_done_date = :date WHERE id = :id")
    suspend fun setLastDone(id: String, date: String?)

    @Query("UPDATE habits SET remind_at = :time WHERE id = :id")
    suspend fun setRemindAt(id: String, time: String?)

    @Query("UPDATE habits SET last_done_date = NULL WHERE last_done_date = :date")
    suspend fun clearLastDoneIfDate(date: String)

    @Query("SELECT COUNT(*) FROM habits")
    suspend fun count(): Int
}

@Dao
interface WeeklyHabitDao {
    @Query("SELECT * FROM weekly_habits ORDER BY day_of_week ASC, position ASC, id ASC")
    fun allWeekly(): Flow<List<WeeklyHabitEntity>>

    @Query("SELECT * FROM weekly_habits WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): WeeklyHabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(habit: WeeklyHabitEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(habit: WeeklyHabitEntity): Long

    @Query("DELETE FROM weekly_habits WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE weekly_habits SET streak = :streak WHERE id = :id")
    suspend fun setStreak(id: String, streak: Int)

    @Query("UPDATE weekly_habits SET last_done_date = :date WHERE id = :id")
    suspend fun setLastDone(id: String, date: String?)

    @Query("UPDATE weekly_habits SET remind_at = :time WHERE id = :id")
    suspend fun setRemindAt(id: String, time: String?)

    @Query("UPDATE weekly_habits SET last_done_date = NULL WHERE last_done_date = :date")
    suspend fun clearLastDoneIfDate(date: String)

    @Query("SELECT COUNT(*) FROM weekly_habits")
    suspend fun count(): Int
}

@Dao
interface HabitFillDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fill: HabitFillEntity): Long

    @Query("DELETE FROM habit_fills WHERE date = :date AND habit_id = :habitId")
    suspend fun deleteForHabitOnDate(date: String, habitId: String)

    @Query("DELETE FROM habit_fills WHERE date = :date")
    suspend fun deleteAllForDate(date: String)

    @Query("SELECT * FROM habit_fills WHERE date = :date")
    fun fillsForDate(date: String): Flow<List<HabitFillEntity>>
}
