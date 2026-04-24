package com.methoda.tranquillo

import android.app.Application
import com.methoda.tranquillo.data.AppDatabase
import com.methoda.tranquillo.data.PrefsStore

class PerfectlyTranquilloApp : Application() {
    val db: AppDatabase by lazy { AppDatabase.get(this) }
    val prefs: PrefsStore by lazy { PrefsStore(this) }
}
