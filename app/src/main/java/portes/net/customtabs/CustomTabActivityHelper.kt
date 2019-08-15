package portes.net.customtabs

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.browser.customtabs.*

class CustomTabActivityHelper : ServiceConnectionCallback {
    private var mCustomTabsSession: CustomTabsSession? = null
    private var mClient: CustomTabsClient? = null
    private var mConnection: CustomTabsServiceConnection? = null
    private var mConnectionCallback: ConnectionCallback? = null
    private val isConnected = false

    val session: CustomTabsSession?
        get() {
            if (mClient == null) {
                println("**** mClient is null no session created!!!")
                mCustomTabsSession = null
            } else if (mCustomTabsSession == null) {
                println("**** initializing custom tab session!")
                mCustomTabsSession = mClient!!.newSession(object : CustomTabsCallback() {
                    override fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
                        super.onNavigationEvent(navigationEvent, extras)
                        println("**** onNavigationEnvent: !$navigationEvent Extras: $extras")
                    }

                    override fun extraCallback(callbackName: String?, args: Bundle?) {
                        super.extraCallback(callbackName, args)

                        println("**** extraCallback: !$callbackName Extras: $args")
                    }
                })
            }
            return mCustomTabsSession
        }

    /**
     * Unbinds the Activity from the Custom Tabs Service.
     *
     * @param activity the activity that is connected to the service.
     */
    fun unbindCustomTabsService(activity: Activity) {
        if (mConnection == null) return
        activity.unbindService(mConnection!!)
        mClient = null
        mCustomTabsSession = null
        mConnection = null
    }

    fun setConnectionCallback(connectionCallback: ConnectionCallback) {
        println("**** setConnectionCallback!!")
        this.mConnectionCallback = connectionCallback
    }

    fun bindCustomTabsService(activity: Activity) {
        if (mClient != null) return

        val packageName = CustomTabsHelper.getPackageNameToUse(activity) ?: return
        mConnection = ServiceConnection(this)
        CustomTabsClient.bindCustomTabsService(activity, packageName, mConnection)
    }

    fun mayLaunchUrl(uri: Uri, extras: Bundle, otherLikelyBundles: List<Bundle>): Boolean {
        if (mClient == null) return false

        val session = session ?: return false
        return session.mayLaunchUrl(uri, extras, otherLikelyBundles)
    }

    override fun onServiceConnected(client: CustomTabsClient) {
        mClient = client
        mClient!!.warmup(0L)
        if (mConnectionCallback != null) mConnectionCallback!!.onCustomTabsConnected()
    }

    override fun onServiceDisconnected() {
        mClient = null
        mCustomTabsSession = null
        if (mConnectionCallback != null) mConnectionCallback!!.onCustomTabsDisconnected()
    }

    interface ConnectionCallback {

        fun onCustomTabsConnected()

        fun onCustomTabsDisconnected()
    }

    interface CustomTabFallback {

        fun openUri(activity: Activity, uri: Uri)
    }

    companion object {

        /**
         * Opens the URL on a Custom Tab if possible. Otherwise fallsback to opening it on a WebView.
         *
         * @param activity         The host activity.
         * @param customTabsIntent a CustomTabsIntent to be used if Custom Tabs is available.
         * @param uri              the Uri to be opened.
         * @param fallback         a CustomTabFallback to be used if Custom Tabs is not available.
         */
        fun openCustomTab(
            activity: Activity,
            customTabsIntent: CustomTabsIntent,
            uri: Uri,
            fallback: CustomTabFallback?
        ) {
            val packageName = CustomTabsHelper.getPackageNameToUse(activity)

            //If we cant find a package name, it means theres no browser that supports
            //Chrome Custom Tabs installed. So, we fallback to the webview
            if (packageName == null) {
                if (fallback != null) {
                    fallback.openUri(activity, uri)
                } else {
                    Toast.makeText(
                        activity.applicationContext,
                        "Unable to launch a custom tab, please provide webview fallback",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                customTabsIntent.intent.setPackage(packageName)
                customTabsIntent.launchUrl(activity, uri)
            }
        }
    }

}
