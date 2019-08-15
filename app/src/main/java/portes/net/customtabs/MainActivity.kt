package portes.net.customtabs

import android.content.ComponentName
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import java.util.logging.Logger

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    var mCustomTabsServiceConnection: CustomTabsServiceConnection? = null
    var customTabsIntent:CustomTabsIntent? = null
    var mClient: CustomTabsClient? = null
    var mCustomTabsSession:CustomTabsSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        mCustomTabsServiceConnection = object: CustomTabsServiceConnection() {
            override fun onServiceDisconnected(p0: ComponentName?) {
                mClient = null
            }

            override fun onCustomTabsServiceConnected(name: ComponentName?, client: CustomTabsClient?) {
                mClient = client
                mClient?.warmup(0)
                mCustomTabsSession = mClient?.newSession(null)
            }
        }

        CustomTabsClient.bindCustomTabsService(this, "com.android.chrome", mCustomTabsServiceConnection);


        val customTabsIntent = CustomTabsIntent.Builder(mCustomTabsSession)
            .addDefaultShareMenuItem()
            .setShowTitle(true)
            .build()

        CustomTabActivityHelper.openCustomTab(
            this, customTabsIntent,
            Uri.parse("https://github.com/saschpe/android-customtabs"),null)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val action = intent?.action

        Log.i(TAG, "action $action")
    }
}
