package io.githun.mucute.qwq.kolomitm.application

import android.app.Application
import com.google.android.material.color.DynamicColors
import io.githun.mucute.qwq.kolomitm.manager.AccountManager

class AppContext : Application() {

    companion object {

        lateinit var instance: AppContext
            private set

    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        AccountManager.fetchAccounts()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

}