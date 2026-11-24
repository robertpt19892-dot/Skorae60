package com.example.bmwe60idrive

import android.content.pm.ActivityInfo
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.sin
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var e60CarView: BmwE60VisualizerView
    private lateinit var tvRpm: TextView
    private lateinit var tvPower: TextView
    private lateinit var tvTorque: TextView
    private lateinit var tvCoolant: TextView
    private lateinit var tvOil: TextView
    private lateinit var tvVoltage: TextView
    private lateinit var tvStatusBanner: TextView
    private lateinit var progressTorque: ProgressBar
    private lateinit var progressPower: ProgressBar
    private var isDemoRunning = false; private var lightsOn = true; private var doorsOpen = false; private var easyEntryEnabled = true
    private val handler = Handler(Looper.getMainLooper()); private var demoPhase = 0.0

    private val demoRunnable = object : Runnable {
        override fun run() {
            if (!isDemoRunning) return
            demoPhase += 0.08
            val wave = (sin(demoPhase) + 1.0) / 2.0
            val rpm = (820 + wave * 3400 + Random.nextInt(-25, 25)).toInt()
            val torque = (60 + wave * 360).toInt()
            val power = ((torque * rpm) / 7023.0).toInt()
            updateLiveTelemetry(rpm, power, torque, (88 + wave * 4).toInt(), (92 + wave * 8).toInt(), 14.1 + wave * 0.3)
            handler.postDelayed(this, 50)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        setContentView(buildLayout())
        updateLiveTelemetry(810, 12, 45, 89, 94, 14.2)
    }

    private fun buildLayout(): View {
        val root = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setBackgroundColor(Color.parseColor("#080A0F")); setPadding(24, 20, 24, 20) }
        val dock = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL; setPadding(12, 16, 12, 16); background = card("#111622", "#26324A", 28f) }
        dock.addView(BmwRoundelView(this).apply { layoutParams = LinearLayout.LayoutParams(86, 86).apply { bottomMargin = 18 } })
        listOf(Pair("NAWI", "Nawigacja GPS") to "#00A3E0", Pair("MUZYKA", "Odtwarzacz Media") to "#1E50A2", Pair("BMW CCC", "Oryginalne menu BMW") to "#E21B22", Pair("FOTEL", "Easy Entry") to "#00A3E0").forEach { (itPair, col) ->
            dock.addView(Button(this).apply {
                text = itPair.first; setTextColor(Color.WHITE); textSize = 11f; typeface = Typeface.DEFAULT_BOLD; background = card("#1A2234", col, 18f)
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f).apply { topMargin = 8; bottomMargin = 8 }
                setOnClickListener {
                    if (itPair.first == "FOTEL") { easyEntryEnabled = !easyEntryEnabled; tvStatusBanner.text = if (easyEntryEnabled) "E60 EASY ENTRY: AKTYWNE" else "E60 EASY ENTRY: WYŁĄCZONE" }
                    else tvStatusBanner.text = itPair.second
                }
            })
        }
        root.addView(dock, LinearLayout.LayoutParams(150, LinearLayout.LayoutParams.MATCH_PARENT).apply { marginEnd = 20 })

        val center = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = card("#0E131D", "#1F293D", 32f); setPadding(24, 20, 24, 20) }
        val head = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        head.addView(MStripesView(this).apply { layoutParams = LinearLayout.LayoutParams(90, 28).apply { marginEnd = 14 } })
        head.addView(TextView(this).apply { text = "BMW 525d  |  E60 M57"; setTextColor(Color.WHITE); textSize = 18f; typeface = Typeface.DEFAULT_BOLD })
        center.addView(head)
        tvStatusBanner = TextView(this).apply { text = "PT-CAN / K-CAN: POŁĄCZONO  •  MAPA 2 (STAGE 1)"; setTextColor(Color.parseColor("#38BDF8")); textSize = 12f; typeface = Typeface.DEFAULT_BOLD; setPadding(0, 8, 0, 8) }
        center.addView(tvStatusBanner)
        e60CarView = BmwE60VisualizerView(this).apply { layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f) }
        center.addView(e60CarView)

        val ctrls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        ctrls.addView(chip("▶ SYMULACJA", "#0284C7") { isDemoRunning = !isDemoRunning; (it as Button).text = if (isDemoRunning) "⏹ STOP" else "▶ SYMULACJA"; if (isDemoRunning) handler.post(demoRunnable) else updateLiveTelemetry(810, 12, 45, 89, 94, 14.2) })
        ctrls.addView(chip("RINGI: WŁ", "#1E293B") { lightsOn = !lightsOn; e60CarView.setLights(lightsOn); (it as Button).text = if (lightsOn) "RINGI: WŁ" else "RINGI: WYŁ" })
        ctrls.addView(chip("DRZWI: OK", "#1E293B") { doorsOpen = !doorsOpen; e60CarView.setDoorsOpen(doorsOpen); (it as Button).text = if (doorsOpen) "DRZWI: OTWARTE" else "DRZWI: OK"; tvStatusBanner.text = if (doorsOpen) "OTWARTE DRZWI KIEROWCY (K-CAN)" else "PT-CAN / K-CAN: GOTOWY" })
        center.addView(ctrls)
        root.addView(center, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.35f).apply { marginEnd = 20 })

        val right = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = card("#0E131D", "#1F293D", 32f); setPadding(24, 20, 24, 20) }
        right.addView(TextView(this).apply { text = "SPORT DISPLAYS  •  LIVE CAN"; setTextColor(Color.parseColor("#94A3B8")); textSize = 12f; typeface = Typeface.DEFAULT_BOLD; setPadding(0, 0, 0, 14) })
        tvPower = lbl("12 KM", "#38BDF8"); progressPower = bar("#00A3E0", 250); right.addView(mCard("MOC SILNIKA", tvPower, progressPower))
        tvTorque = lbl("45 Nm", "#F43F5E"); progressTorque = bar("#E21B22", 500); right.addView(mCard("MOMENT OBROTOWY", tvTorque, progressTorque))
        tvRpm = lbl("810 RPM", "#FFFFFF"); right.addView(row("OBROTY SILNIKA (0x0AA)", tvRpm))
        val grid = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f).apply { topMargin = 10 } }
        tvCoolant = tVal("89°C", "#38BDF8"); tvOil = tVal("94°C", "#F59E0B"); tvVoltage = tVal("14.2V", "#10B981")
        grid.addView(mTile("CIECZ (ECT)", tvCoolant)); grid.addView(mTile("OLEJ (EOT)", tvOil)); grid.addView(mTile("ŁADOWANIE", tvVoltage))
        right.addView(grid)
        root.addView(right, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f))
        return root
    }

    private fun updateLiveTelemetry(rpm: Int, power: Int, torque: Int, coolant: Int, oil: Int, voltage: Double) {
        tvRpm.text = rpm.toString() + " RPM"; tvPower.text = power.toString() + " KM"; tvTorque.text = torque.toString() + " Nm"
        tvCoolant.text = coolant.toString() + "°C"; tvOil.text = oil.toString() + "°C"; tvVoltage.text = String.format("%.1fV", voltage)
        progressPower.progress = power.coerceIn(0, 250); progressTorque.progress = torque.coerceIn(0, 500)
    }

    private fun card(bg: String, brd: String, r: Float) = GradientDrawable().apply { setColor(Color.parseColor(bg)); setStroke(2, Color.parseColor(brd)); cornerRadius = r }
    private fun chip(txt: String, bg: String, fn: (View) -> Unit) = Button(this).apply { text = txt; setTextColor(Color.WHITE); textSize = 10f; typeface = Typeface.DEFAULT_BOLD; background = card(bg, "#334155", 16f); setPadding(16, 8, 16, 8); layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { marginEnd = 10 }; setOnClickListener(fn) }
    private fun lbl(txt: String, col: String) = TextView(this).apply { text = txt; setTextColor(Color.parseColor(col)); textSize = 20f; typeface = Typeface.DEFAULT_BOLD }
    private fun bar(col: String, maxV: Int) = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply { max = maxV; progressDrawable.setColorFilter(Color.parseColor(col), PorterDuff.Mode.SRC_IN); layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 18).apply { topMargin = 6 } }
    private fun mCard(t: String, v: TextView, b: ProgressBar) = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = card("#151C2B", "#26324A", 20f); setPadding(18, 12, 18, 14); layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 10 }; addView(LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL; addView(TextView(context).apply { text = t; setTextColor(Color.parseColor("#CBD5E1")); textSize = 11f; typeface = Typeface.DEFAULT_BOLD; layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) }); addView(v) }); addView(b) }
    private fun row(t: String, v: TextView) = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; background = card("#151C2B", "#26324A", 20f); setPadding(18, 10, 18, 10); addView(TextView(context).apply { text = t; setTextColor(Color.parseColor("#CBD5E1")); textSize = 11f; layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) }); addView(v.apply { textSize = 16f }) }
