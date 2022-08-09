package com.gps.chat.ui.splash.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.gps.chat.R
import com.gps.chat.ui.home.view.HomeActivity
import com.gps.chat.ui.welcome.view.WelcomeActivity
import com.gps.chat.utils.CONSTANTS.IS_ADD
import com.gps.chat.utils.CONSTANTS.IS_LOGIN
import com.gps.chat.utils.SharedPreferencesEditor

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            if (SharedPreferencesEditor.getDataBoolean(IS_LOGIN)) {
                SharedPreferencesEditor.storeValueBoolean(IS_ADD, true)
                startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this@SplashActivity, WelcomeActivity::class.java))
                finish()
            }
        }, 1000)
    }
}
