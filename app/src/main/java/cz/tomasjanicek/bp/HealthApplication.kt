package cz.tomasjanicek.bp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import dagger.hilt.android.HiltAndroidApp
import androidx.work.Configuration
import javax.inject.Inject


@HiltAndroidApp
class HealthApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    // ZMĚNA: Místo funkce 'override fun getWorkManagerConfiguration()'
    // musíte použít 'override val workManagerConfiguration'
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}