package gegeparty.guigol.com

import android.app.Application
import gegeparty.guigol.com.notifications.Notifier
import gegeparty.guigol.com.notifications.SyncWorker

class GegePartyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Notifier.createChannels(this)
        SyncWorker.schedule(this)
    }
}
