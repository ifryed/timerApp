package com.ifryed.gymtimer

import android.app.ActivityManager
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.gymtimer.R
import com.ifryed.gymtimer.Common.Common
import java.lang.Math.floorDiv

fun secToString(sec: Int): String {
    val m = "%02d".format(floorDiv(sec, 60))
    val s = "%02d".format(sec % 60)

    return "$m:$s"
}

fun stringToSec(str: String): Int {
    if (str.isEmpty()) {
        return 0
    }
    val m_s: List<String> = str.split(':')
    if (m_s.size < 2) {
        return m_s[0].toInt()
    }
    val sec = m_s[0].toInt() * 60 + m_s[1].toInt()
    return sec
}


class MainActivity : AppCompatActivity() {
    // The reference variables for the
    // Button, AlertDialog, EditText
    // classes are created
    private var mBgView: View? = null
    private var minimizeBtn: Button? = null
    private var dialog: AlertDialog? = null
    private var startBtn: Button? = null
    private var pauseBtn: Button? = null
    private var resetBtn: Button? = null
    private var dispTimer: EditText? = null

    private var minBtn: Button? = null
    private var twominBtn: Button? = null
    private var halfBtn: Button? = null

    private var mTimer: CountDownTimer? = null
    private var mTimerRunning: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mBgView = findViewById<View>(R.id.screenCont)
        mBgView!!.setBackgroundColor(Color.WHITE)

        // The Buttons and the EditText are connected with
        // the corresponding component id used in layout file
        minimizeBtn = findViewById(R.id.buttonMinimize)
        dispTimer = findViewById(R.id.timer)
        startBtn = findViewById(R.id.startBtn)
//        pauseBtn = findViewById(R.id.pauseBtn)
        resetBtn = findViewById(R.id.resetBtn)

        minBtn = findViewById(R.id.min)
        halfBtn = findViewById(R.id.halfmin)
        twominBtn = findViewById(R.id.min2)

        // If the app is started again while the
        // floating window service is running
        // then the floating window service will stop
        if (isMyServiceRunning) {
            // onDestroy() method in FloatingWindowGFG
            // class will be called here
            stopService(Intent(this@MainActivity, FloatingWindowGFG::class.java))
        }

        // currentDesc String will be empty
        // at first time launch
        // but the text written in floating
        // window will not gone
        dispTimer!!.setText(secToString(Common.savedTime))
        dispTimer!!.setSelection(dispTimer!!.getText().toString().length)
        setTime(Common.savedTime)

        // The EditText string will be stored in
        // currentDesc while writing
        dispTimer!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                // Not Necessary
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                Common.currentTime = stringToSec(dispTimer!!.getText().toString())
            }

            override fun afterTextChanged(editable: Editable) {
                // Not Necessary
            }
        })

        resetBtn!!.setOnClickListener(View.OnClickListener {
            if (mTimer != null) {
                mTimer!!.cancel()
                reset()
            }
        })

        minBtn!!.setOnClickListener(View.OnClickListener {
            setTime(60)
            it.setBackgroundColor(ContextCompat.getColor(this, R.color.btnSelectColor))
            Common.savedTime = 60
        })

        twominBtn!!.setOnClickListener(View.OnClickListener {
            setTime(120)
            it.setBackgroundColor(ContextCompat.getColor(this, R.color.btnSelectColor))
            Common.savedTime = 120
        })

        halfBtn!!.setOnClickListener(View.OnClickListener {
            setTime(30)
            it.setBackgroundColor(ContextCompat.getColor(this, R.color.btnSelectColor))
            Common.savedTime = 30
        })

        startBtn!!.setOnClickListener(View.OnClickListener {
            if (!mTimerRunning) {
                mTimer = object : CountDownTimer((Common.currentTime.toLong()) * 1000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        Common.currentTime = Math.floorDiv(
                            millisUntilFinished.toInt(),
                            1000
                        )
                        dispTimer!!.setText(
                            secToString(
                                Common.currentTime
                            )
                        )
                    }

                    override fun onFinish() {
                        reset()
                        Toast.makeText(
                            this@MainActivity,
                            "Time's finished!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                mBgView!!.setBackgroundColor(Color.RED)
                mTimerRunning = true
                mTimer!!.start()
                startBtn!!.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_pause,0,0,0)
            } else {
                mTimerRunning = false
                mTimer!!.cancel()
                mBgView!!.setBackgroundColor(Color.WHITE)
                startBtn!!.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_play,0,0,0)
            }
        })

        // The Main Button that helps to minimize the app
        minimizeBtn!!.setOnClickListener(View.OnClickListener {
            // First it confirms whether the
            // 'Display over other apps' permission in given
            if (checkOverlayDisplayPermission()) {
                // FloatingWindowGFG service is started
                val serviceIntent: Intent = Intent(this, FloatingWindowGFG::class.java)
                serviceIntent.putExtra("timerRun", mTimerRunning)
                startService(serviceIntent)
                // The MainActivity closes here
                finish()
            } else {
                // If permission is not given,
                // it shows the AlertDialog box and
                // redirects to the Settings
                requestOverlayDisplayPermission()
            }
        })

        minBtn!!.callOnClick()
    }// If this service is found as a running,

    // it will return true or else false.
    // The ACTIVITY_SERVICE is needed to retrieve a
    // ActivityManager for interacting with the global system
    // It has a constant String value "activity".
    private val isMyServiceRunning:

    // A loop is needed to get Service information that
    // are currently running in the System.
    // So ActivityManager.RunningServiceInfo is used.
    // It helps to retrieve a
    // particular service information, here its this service.
    // getRunningServices() method returns a list of the
    // services that are currently running
    // and MAX_VALUE is 2147483647. So at most this many services
    // can be returned by this method.
            Boolean
        private get() {
            // The ACTIVITY_SERVICE is needed to retrieve a
            // ActivityManager for interacting with the global system
            // It has a constant String value "activity".
            val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager

            // A loop is needed to get Service information that
            // are currently running in the System.
            // So ActivityManager.RunningServiceInfo is used.
            // It helps to retrieve a
            // particular service information, here its this service.
            // getRunningServices() method returns a list of the
            // services that are currently running
            // and MAX_VALUE is 2147483647. So at most this many services
            // can be returned by this method.
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                // If this service is found as a running,
                // it will return true or else false.
                if (FloatingWindowGFG::class.java.name == service.service.className) {
                    return true
                }
            }
            return false
        }

    private fun reset() {
        mTimer!!.cancel()
        mTimerRunning = false
        mBgView!!.setBackgroundColor(Color.WHITE)
        dispTimer!!.setText(secToString(Common.savedTime))
        startBtn!!.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_play,0,0,0)
    }

    private fun setTime(newTime: Int) {
        if (mTimerRunning) {
            return
        }
        dispTimer!!.setText(secToString(newTime))

        minBtn!!.setBackgroundColor(ContextCompat.getColor(this, R.color.btnColor))
        twominBtn!!.setBackgroundColor(ContextCompat.getColor(this, R.color.btnColor))
        halfBtn!!.setBackgroundColor(ContextCompat.getColor(this, R.color.btnColor))
    }

    private fun requestOverlayDisplayPermission() {
        // An AlertDialog is created
        val builder = AlertDialog.Builder(this)

        // This dialog can be closed, just by taping
        // anywhere outside the dialog-box
        builder.setCancelable(true)

        // The title of the Dialog-box is set
        builder.setTitle("Screen Overlay Permission Needed")

        // The message of the Dialog-box is set
        builder.setMessage("Enable 'Display over other apps' from System Settings.")

        // The event of the Positive-Button is set
        builder.setPositiveButton(
            "Open Settings"
        ) { dialog, which -> // The app will redirect to the 'Display over other apps' in Settings.
            // This is an Implicit Intent. This is needed when any Action is needed
            // to perform, here it is
            // redirecting to an other app(Settings).
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )

            // This method will start the intent. It takes two parameter, one is the Intent and the other is
            // an requestCode Integer. Here it is -1.
            startActivityForResult(intent, RESULT_OK)
        }
        dialog = builder.create()
        // The Dialog will
        // show in the screen
        dialog!!.show()
    }

    private fun checkOverlayDisplayPermission(): Boolean {
        // Android Version is lesser than Marshmallow or
        // the API is lesser than 23
        // doesn't need 'Display over other apps' permission enabling.
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            // If 'Display over other apps' is not enabled
            // it will return false or else true
            if (!Settings.canDrawOverlays(this)) {
                false
            } else {
                true
            }
        } else {
            true
        }
    }
}