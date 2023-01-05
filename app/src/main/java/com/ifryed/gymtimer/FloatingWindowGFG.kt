package com.ifryed.gymtimer

import com.example.gymtimer.R
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.View.OnTouchListener
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.ifryed.gymtimer.Common.Common


class FloatingWindowGFG : Service() {
    // The reference variables for the
    // ViewGroup, WindowManager.LayoutParams,
    // WindowManager, Button, EditText classes are created
    private var floatView: ViewGroup? = null
    private var LAYOUT_TYPE = 0
    private var floatWindowLayoutParam: WindowManager.LayoutParams? = null
    private var windowManager: WindowManager? = null
    private var maximizeBtn: Button? = null

    private var startBtn: Button? = null
    private var pauseBtn: Button? = null
    private var resetBtn: Button? = null
    private var dispTimer: EditText? = null

    private var mTimer: CountDownTimer? = null
    private var mTimerRunning: Boolean = false

    // As FloatingWindowGFG inherits Service class,
    // it actually overrides the onBind method
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val extras = intent!!.extras;
        if (extras != null) {
            mTimerRunning = extras.get("timerRun") as Boolean

            if (mTimerRunning){
                mTimerRunning = false
                startBtn!!.callOnClick()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()

        // The screen height and width are calculated, cause
        // the height and width of the floating window is set depending on this
        val metrics = applicationContext.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        // To obtain a WindowManager of a different Display,
        // we need a Context for that display, so WINDOW_SERVICE is used
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // A LayoutInflater instance is created to retrieve the
        // LayoutInflater for the floating_layout xml
        val inflater = baseContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // inflate a new view hierarchy from the floating_layout xml
        floatView = inflater.inflate(R.layout.floating_layout, null) as ViewGroup?
        floatView!!.setBackgroundColor(Color.WHITE)

        // The Buttons and the EditText are connected with
        // the corresponding component id used in floating_layout xml file
        maximizeBtn = floatView!!.findViewById(R.id.buttonMaximize)
        dispTimer = floatView!!.findViewById(R.id.timer)
        startBtn = floatView!!.findViewById(R.id.startBtn)
        pauseBtn = floatView!!.findViewById(R.id.pauseBtn)
        resetBtn = floatView!!.findViewById(R.id.resetBtn)

        // Just like MainActivity, the text written
        // in Maximized will stay
        dispTimer!!.setText(secToString(Common.currentTime))
        dispTimer!!.setCursorVisible(false)

        startBtn!!.setOnClickListener(View.OnClickListener {
            if (!mTimerRunning) {
                Common.savedTime = Common.currentTime
                mTimer = object : CountDownTimer(Common.currentTime.toLong() * 1000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        dispTimer!!.setText(
                            secToString(
                                Math.floorDiv(
                                    millisUntilFinished.toInt(),
                                    1000
                                )
                            )
                        )
                    }

                    override fun onFinish() {
                        reset()
                        Toast.makeText(
                            this@FloatingWindowGFG,
                            "Time's finished!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                floatView!!.setBackgroundColor(Color.RED)
                mTimerRunning = true
                mTimer!!.start()
            }else{
                mTimerRunning = true
                mTimer!!.cancel()
            }
        })

        resetBtn!!.setOnClickListener(View.OnClickListener {
            if (mTimer != null) {
                mTimer!!.cancel()
                reset()
            }
        })

        // WindowManager.LayoutParams takes a lot of parameters to set the
        // the parameters of the layout. One of them is Layout_type.
        LAYOUT_TYPE = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // If API Level is more than 26, we need TYPE_APPLICATION_OVERLAY
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            // If API Level is lesser than 26, then we can
            // use TYPE_SYSTEM_ERROR,
            // TYPE_SYSTEM_OVERLAY, TYPE_PHONE, TYPE_PRIORITY_PHONE.
            // But these are all
            // deprecated in API 26 and later. Here TYPE_TOAST works best.
            WindowManager.LayoutParams.TYPE_TOAST
        }

        // Now the Parameter of the floating-window layout is set.
        // 1) The Width of the window will be 55% of the phone width.
        // 2) The Height of the window will be 58% of the phone height.
        // 3) Layout_Type is already set.
        // 4) Next Parameter is Window_Flag. Here FLAG_NOT_FOCUSABLE is used. But
        // problem with this flag is key inputs can't be given to the EditText.
        // This problem is solved later.
        // 5) Next parameter is Layout_Format. System chooses a format that supports
        // translucency by PixelFormat.TRANSLUCENT
        floatWindowLayoutParam = WindowManager.LayoutParams(
            (width * 0.55f).toInt(), (height * 0.358f).toInt(),
            LAYOUT_TYPE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        // The Gravity of the Floating Window is set.
        // The Window will appear in the center of the screen
        floatWindowLayoutParam!!.gravity = Gravity.CENTER

        // X and Y value of the window is set
        floatWindowLayoutParam!!.x = 0
        floatWindowLayoutParam!!.y = 0

        // The ViewGroup that inflates the floating_layout.xml is
        // added to the WindowManager with all the parameters
        windowManager!!.addView(floatView, floatWindowLayoutParam)

        // The button that helps to maximize the app
        maximizeBtn!!.setOnClickListener(View.OnClickListener { // stopSelf() method is used to stop the service if
            // it was previously started
            stopSelf()

            // The window is removed from the screen
            windowManager!!.removeView(floatView)

            // The app will maximize again. So the MainActivity
            // class will be called again.
            val backToHome = Intent(this@FloatingWindowGFG, MainActivity::class.java)

            // 1) FLAG_ACTIVITY_NEW_TASK flag helps activity to start a new task on the history stack.
            // If a task is already running like the floating window service, a new activity will not be started.
            // Instead the task will be brought back to the front just like the MainActivity here
            // 2) FLAG_ACTIVITY_CLEAR_TASK can be used in the conjunction with FLAG_ACTIVITY_NEW_TASK. This flag will
            // kill the existing task first and then new activity is started.
            backToHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(backToHome)
        })


        // Another feature of the floating window is, the window is movable.
        // The window can be moved at any position on the screen.
        floatView!!.setOnTouchListener(object : OnTouchListener {
            val floatWindowLayoutUpdateParam: WindowManager.LayoutParams =
                floatWindowLayoutParam as WindowManager.LayoutParams
            var x = 0.0
            var y = 0.0
            var px = 0.0
            var py = 0.0
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        x = floatWindowLayoutUpdateParam.x.toDouble()
                        y = floatWindowLayoutUpdateParam.y.toDouble()

                        // returns the original raw X
                        // coordinate of this event
                        px = event.rawX.toDouble()

                        // returns the original raw Y
                        // coordinate of this event
                        py = event.rawY.toDouble()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        floatWindowLayoutUpdateParam.x = (x + event.rawX - px).toInt()
                        floatWindowLayoutUpdateParam.y = (y + event.rawY - py).toInt()

                        // updated parameter is applied to the WindowManager
                        windowManager!!.updateViewLayout(floatView, floatWindowLayoutUpdateParam)
                    }
                }
                return false
            }
        })

    }

    private fun reset() {
        mTimerRunning = false
        mTimer!!.cancel()
        floatView!!.setBackgroundColor(Color.WHITE)
        dispTimer!!.setText(secToString(Common.savedTime))
    }

    // It is called when stopService()
    // method is called in MainActivity
    override fun onDestroy() {
        super.onDestroy()
        stopSelf()
        // Window is removed from the screen
        windowManager!!.removeView(floatView)
    }
}
