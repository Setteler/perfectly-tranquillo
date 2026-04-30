# Sub-project #6: Local notifications — Spec

**Parent:** Perfectly Tranquillo. Designs at `docs/design/README.md` § "Notifications" + prototype `docs/design/prototype/src/notifications.jsx`.

**Job:** Schedule local reminders for any habit with a `remindAt` time. When the alarm fires, post a notification matching the prototype's translucent dark card style. Tapping the notification opens the Habits tab.

## Approach

Use **WorkManager** rather than raw `AlarmManager` — already a dep, survives boot automatically, handles periodic scheduling without a custom `BOOT_COMPLETED` receiver.

- **Daily habits** → `PeriodicWorkRequest`, repeat every 24 h, initial delay = (now → next `HH:MM`).
- **Weekly habits** → `OneTimeWorkRequest` with 7-day delay. The worker re-enqueues a fresh OneTimeWorkRequest for the next occurrence when it fires.
- **Cancellation/replacement:** each habit has a unique work tag (`habit-reminder-<id>`). Replace existing work via `ExistingPeriodicWorkPolicy.REPLACE` / `ExistingWorkPolicy.REPLACE`.

## Scope (in)

- **Notification channel:** "habit_reminders" with default importance + sound + vibration. Created on `Application.onCreate` (idempotent).
- **POST_NOTIFICATIONS permission:** request once on first launch (Android 13+). Pre-13 devices: no runtime permission.
- **Deep link:** notification `PendingIntent` → `MainActivity` with `?route=habits` extra. `MainActivity` reads it on create + new-intent and navigates Root nav to Habits.
- **HabitReminderWorker:** posts a notification when fired. Reads habit by ID, skips if `done` for today.
- **HabitReminderScheduler:** `schedule(habit)`, `cancel(habitId)`, `rescheduleAll()`. Called from `HabitsRepository` after add/edit/remove/setReminder + on app start.
- **Demo trigger:** `AppViewModel.fireDemoReminder()` posts a sample notification immediately — for QA wiring before Settings ships.
- **Manifest:** `POST_NOTIFICATIONS` permission. (No `RECEIVE_BOOT_COMPLETED` needed thanks to WorkManager.)

## Out of scope (defer)

- `notifMode` setting (silent / sound / vibrate) — added in #7 with the Settings UI.
- Per-habit notification channels.
- Snooze / dismiss-action / reply-action.
- Custom large icon / wave-glyph asset (Material Symbols default for now).

## File map

**Create:**
- `notifications/NotificationChannelSetup.kt`
- `notifications/HabitReminderWorker.kt`
- `notifications/HabitReminderScheduler.kt`

**Modify:**
- `app/src/main/AndroidManifest.xml` — `POST_NOTIFICATIONS` permission; configure `MainActivity` to handle the deep-link intent (`launchMode="singleTask"`).
- `PerfectlyTranquilloApp.kt` — create channel; on app start, kick a `rescheduleAll` (one-shot Worker safer than blocking onCreate).
- `MainActivity.kt` — read intent extra `route=habits` and route Root nav.
- `nav/RootNavHost.kt` — accept an optional `pendingDeepLinkRoute` and consume it once.
- `data/HabitsRepository.kt` — invoke scheduler on add/edit/remove/setReminder/toggle.
- `data/AppViewModel.kt` — add `fireDemoReminder()`.
- `app/build.gradle.kts` — bump versionCode 7, versionName 0.7.0.

## Verification

- assembleDebug clean
- Manual: set a habit's `remindAt` to 1 minute from now → notification fires; tap → Habits tab open. Toggling the habit done before the alarm should suppress the notification.
- Demo button (when wired in #7) posts a sample notification immediately.
