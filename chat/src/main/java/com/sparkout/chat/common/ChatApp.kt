package com.sparkout.chat.common

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.room.Room
import com.sparkout.chat.BuildConfig
import com.sparkout.chat.common.Global.USER_ID
import com.sparkout.chat.roomdb.AppDatabase
import com.sparkout.chat.socket.SocketHelper
import io.socket.client.Socket
import org.json.JSONObject
import timber.log.Timber

open class ChatApp : Application(), Application.ActivityLifecycleCallbacks, LifecycleObserver {
    private var activityReferences = 0
    private var isActivityChangingConfigurations = false
    override fun onCreate() {
        super.onCreate()
        mInstance = this
        mSocketHelper = SocketHelper(this)
        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
        mAppDatabase = initializeRoomDb(this, "GPSCOMM")
    }

    private fun initializeRoomDb(context: Context, mTableName: String): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, mTableName)
            .allowMainThreadQueries()
            .build()
    }


    override fun onTerminate() {
        if (null != mSocketHelper?.getSocketInstance()) {
            mSocketHelper?.getSocketInstance()?.off(Socket.EVENT_CONNECT, mSocketHelper?.onConnect)
            mSocketHelper?.getSocketInstance()
                ?.off(Socket.EVENT_DISCONNECT, mSocketHelper?.onDisconnect)
            mSocketHelper?.getSocketInstance()
                ?.off(Socket.EVENT_CONNECT_ERROR, mSocketHelper?.onConnectError)
           /* mSocketHelper?.getSocketInstance()
                ?.off(Socket.CON, mSocketHelper?.onConnectTimeout)*/
        }
        super.onTerminate()
    }

    companion object {
        lateinit var mInstance: ChatApp
        var mSocketHelper: SocketHelper? = null
        var mAppDatabase: AppDatabase? = null
        var wasInBackground: Boolean? = null
    }


    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStarted(activity: Activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            // App enters foreground
            try {
                if (mSocketHelper?.getSocketInstance()?.connected()!!) {
                    val jsonObject = JSONObject()
                    jsonObject.put("id", SharedPreferenceEditor.getData(USER_ID))
                    jsonObject.put("status", "ONLINE")
                    mSocketHelper?.sendOnlineOffline(jsonObject)
                }
            } catch (e: UninitializedPropertyAccessException) {
            }
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityStopped(activity: Activity) {
        try {
            isActivityChangingConfigurations = activity.isChangingConfigurations
            if (--activityReferences == 0 && !isActivityChangingConfigurations) {
                // App enters background
                if (mSocketHelper?.getSocketInstance()?.connected()!!) {
                    val jsonObject = JSONObject()
                    jsonObject.put("id", SharedPreferenceEditor.getData(USER_ID))
                    jsonObject.put("status", "OFFLINE")
                    mSocketHelper?.sendOnlineOffline(jsonObject)
                }
            }
        } catch (e: UninitializedPropertyAccessException) {
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    open fun onAppBackground() {
        wasInBackground = false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    open fun onAppForeground() {
        wasInBackground = true
    }
}