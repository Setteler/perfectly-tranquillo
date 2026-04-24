package com.methoda.tranquillo.data

/**
 * Seeds the seven daily + seven weekly habits from the prototype's
 * `INITIAL_STATE` (`docs/design/prototype/src/app.jsx`).
 *
 * Ids, labels, hints, streaks, and reminder times are copied verbatim so the
 * Android build matches the design — only `done` is elided (we derive "done
 * today" from `lastDoneDate == today`).
 */
object HabitSeeder {

    private data class DailySeed(
        val id: String, val label: String, val hint: String,
        val streak: Int, val remindAt: String?
    )

    private data class WeeklySeed(
        val id: String, val label: String, val hint: String,
        val day: Int, val streak: Int, val remindAt: String?
    )

    private val DAILY: List<DailySeed> = listOf(
        DailySeed("no-phone",  "Don't open phone first thing", "15 minutes past waking", 4,  "07:00"),
        DailySeed("water",     "Drink water",                   "First glass by 10am",   7,  "09:30"),
        DailySeed("gratitude", "Three good things ahead",       "Any time in morning",   11, "08:15"),
        DailySeed("break",     "Take a mindful break",          "Between tasks",         2,  "14:00"),
        DailySeed("workout",   "Move your body",                "20 minutes, any kind",  3,  "17:30"),
        DailySeed("eat",       "Eat a real meal",               "Slow, unhurried",       5,  null),
        DailySeed("sleep",     "Sleep by 11:00",                "Phone away by 10:30",   8,  "22:30")
    )

    private val WEEKLY: List<WeeklySeed> = listOf(
        WeeklySeed("therapy",    "Therapy session",   "Mondays, 6pm",      1, 6, "17:45"),
        WeeklySeed("long-walk",  "A long slow walk",  "Tuesdays, outside", 2, 3, "16:00"),
        WeeklySeed("call-mom",   "Call mom",          "Tuesdays",          2, 4, "19:00"),
        WeeklySeed("deep-clean", "Tidy one room",     "Wednesdays",        3, 2, null),
        WeeklySeed("read",       "Read, just for me", "Thursday evenings", 4, 1, "21:00"),
        WeeklySeed("meal-prep",  "Meal prep",         "Sundays, midday",   0, 5, null),
        WeeklySeed("date-night", "Date night",        "Friday evenings",   5, 2, "19:30")
    )

    suspend fun seedAllIfEmpty(db: AppDatabase) {
        seedDailyIfEmpty(db.habitDao())
        seedWeeklyIfEmpty(db.weeklyHabitDao())
    }

    private suspend fun seedDailyIfEmpty(dao: HabitDao) {
        if (dao.count() > 0) return
        DAILY.forEachIndexed { index, s ->
            dao.insertIfAbsent(
                HabitEntity(
                    id = s.id,
                    label = s.label,
                    hint = s.hint,
                    streak = s.streak,
                    remindAt = s.remindAt,
                    lastDoneDate = null,
                    position = index
                )
            )
        }
    }

    private suspend fun seedWeeklyIfEmpty(dao: WeeklyHabitDao) {
        if (dao.count() > 0) return
        WEEKLY.forEachIndexed { index, s ->
            dao.insertIfAbsent(
                WeeklyHabitEntity(
                    id = s.id,
                    label = s.label,
                    hint = s.hint,
                    dayOfWeek = s.day,
                    streak = s.streak,
                    remindAt = s.remindAt,
                    lastDoneDate = null,
                    position = index
                )
            )
        }
    }
}
