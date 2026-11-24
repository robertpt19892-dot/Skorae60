package com.example.bmwe60idrive

import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
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

    private var isDemoRunning = false
    private var lightsOn = true
    private var doorsOpen = false
    private var easyEntryEnabled = true
    private val handler = Handler(Looper.getMainLooper())
    private var demoPhase = 0.0

    private val demoRunnable = object : Runnable {
        override fun run() {
            if (!isDemoRunning) return
            demoPhase += 0.08
            val wave = (sin(demoPhase) + 1.0) / 2.0
            val rpm = (820 + wave * 3400 + Random.nextInt(-25, 25)).toInt()
            val torque = (60 + wave * 360).toInt()
            val power = ((torque * rpm) / 7023.0).toInt()
            val coolant = (88 + wave * 4).toInt()
            val oil = (92 + wave * 8).toInt()
            val voltage = 14.1 + (wave * 0.3)
            updateLiveTelemetry(rpm, power, torque, coolant, oil, voltage)
            handler.postDelayed(this, 50)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        setContentView(buildIdrive8Layout())
        updateLiveTelemetry(810, 12, 45, 89, 94, 14.2)
    }

    private fun buildIdrive8Layout(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#080A0F"))
            setPadding(24, 20, 24, 20)
        }
        val sideDock = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(12, 16, 12, 16)
            background = createGlassCard("#111622", "#26324A", 28f)
        }
        sideDock.addView(BmwRoundelView(this).apply {
            layoutParams = LinearLayout.LayoutParams(86, 86).apply { bottomMargin = 18 }
        })
        val menuItems = listOf(
            Pair("NAWI", "Otwieranie Nawigacji GPS...") to "#00A3E0",
            Pair("MUZYKA", "Odtwarzacz Media / Spotify") to "#1E50A2",
            Pair("BMW CCC", "Przełączanie na oryginalne menu BMW...") to "#E21B22",
            Pair("FOTEL", "Easy Entry") to "#00A3E0"
        )
        menuItems.forEach { (item, accentColor) ->
            val btn = Button(this).apply {
                text = item.first
                setTextColor(Color.WHITE)
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
                background = createGlassCard("#1A2234", accentColor, 18f)
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f).apply { topMargin = 8; bottomMargin = 8 }
                setOnClickListener {
                    if (item.first == "FOTEL") {
                        easyEntryEnabled = !easyEntryEnabled
                        tvStatusBanner.text = if (easyEntryEnabled) "E60 EASY ENTRY: AKTYWNE" else "E60 EASY ENTRY: WYŁĄCZONE"
                    } else {
                        tvStatusBanner.text = item.second
                    }
                }
            }
            sideDock.addView(btn)
        }
        root.addView(sideDock, LinearLayout.LayoutParams(150, LinearLayout.LayoutParams.MATCH_PARENT).apply { marginEnd = 20 })

        val centerPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = createGlassCard("#0E131D", "#1F293D", 32f)
            setPadding(24, 20, 24, 20)
        }
        val headerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        headerRow.addView(MStripesView(this).apply { layoutParams = LinearLayout.LayoutParams(90, 28).apply { marginEnd = 14 } })
        headerRow.addView(TextView(this).apply {
            text = "BMW 525d  |  E60 M57"
            setTextColor(Color.WHITE)
            textSize = 18f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        })
        centerPanel.addView(headerRow)

        tvStatusBanner = TextView(this).apply {
            text = "PT-CAN / K-CAN: POŁĄCZONO  •  MAPA 2 (STAGE 1)"
            setTextColor(Color.parseColor("#38BDF8"))
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 8, 0, 8)
        }
        centerPanel.addView(tvStatusBanner)

        e60CarView = BmwE60VisualizerView(this).apply { layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f) }
        centerPanel.addView(e60CarView)

        val testControls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        testControls.addView(createControlChip("▶ SYMULACJA", "#0284C7") {
            isDemoRunning = !isDemoRunning
            (it as Button).text = if (isDemoRunning) "⏹ STOP" else "▶ SYMULACJA"
            if (isDemoRunning) handler.post(demoRunnable) else updateLiveTelemetry(810, 12, 45, 89, 94, 14.2)
        })
        testControls.addView(createControlChip("RINGI: WŁ", "#1E293B") {
            lightsOn = !lightsOn
            e60CarView.setLights(lightsOn)
            (it as Button).text = if (lightsOn) "RINGI: WŁ" else "RINGI: WYŁ"
        })
        testControls.addView(createControlChip("DRZWI: OK", "#1E293B") {
            doorsOpen = !doorsOpen
            e60CarView.setDoorsOpen(doorsOpen)
            (it as Button).text = if (doorsOpen) "DRZWI: OTWARTE" else "DRZWI: OK"
            tvStatusBanner.text = if (doorsOpen) "UWAGA: OTWARTE DRZWI KIEROWCY (K-CAN)" else "PT-CAN / K-CAN: GOTOWY"
        })
        centerPanel.addView(testControls)
        root.addView(centerPanel, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.35f).apply { marginEnd = 20 })

        val rightPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = createGlassCard("#0E131D", "#1F293D", 32f)
            setPadding(24, 20, 24, 20)
        }
        rightPanel.addView(TextView(this).apply {
            text = "SPORT DISPLAYS  •  LIVE CAN"
            setTextColor(Color.parseColor("#94A3B8"))
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 14)
        })

        tvPower = createMetricLabel("12 KM", "#38BDF8")
        progressPower = createSportProgressBar("#00A3E0", 250)
        rightPanel.addView(createMetricCard("MOC SILNIKA", tvPower, progressPower))

        tvTorque = createMetricLabel("45 Nm", "#F43F5E")
        progressTorque = createSportProgressBar("#E21B22", 500)
        rightPanel.addView(createMetricCard("MOMENT OBROTOWY", tvTorque, progressTorque))

        tvRpm = createMetricLabel("810 RPM", "#FFFFFF")
        rightPanel.addView(createSimpleRow("OBROTY SILNIKA (0x0AA)", tvRpm))

        val bottomGrid = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f).apply { topMargin = 10 }
        }
        tvCoolant = createTileValue("89°C", "#38BDF8")
        tvOil = createTileValue("94°C", "#F59E0B")
        tvVoltage = createTileValue("14.2V", "#10B981")
        bottomGrid.addView(createMiniTile("CIECZ (ECT)", tvCoolant))
        bottomGrid.addView(createMiniTile("OLEJ (EOT)", tvOil))
        bottomGrid.addView(createMiniTile("ŁADOWANIE", tvVoltage))
        rightPanel.addView(bottomGrid)
        root.addView(rightPanel, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f))
        return root
    }

    private fun updateLiveTelemetry(rpm: Int, power: Int, torque: Int, coolant: Int, oil: Int, voltage: Double) {
        tvRpm.text = rpm.toString() + " RPM"
        tvPower.text = power.toString() + " KM"
        tvTorque.text = torque.toString() + " Nm"
        tvCoolant.text = coolant.toString() + "°C"
        tvOil.text = oil.toString() + "°C"
        tvVoltage.text = String.format("%.1fV", voltage)
        progressPower.progress = power.coerceIn(0, 250)
        progressTorque.progress = torque.coerceIn(0, 500)
    }

    private fun createGlassCard(bgHex: String, borderHex: String, radius: Float) = GradientDrawable().apply {
        setColor(Color.parseColor(bgHex)); setStroke(2, Color.parseColor(borderHex)); cornerRadius = radius
    }

    private fun createControlChip(label: String, bgHex: String, onClick: (View) -> Unit) = Button(this).apply {
        text = label; setTextColor(Color.WHITE); textSize = 10f; typeface = Typeface.DEFAULT_BOLD
        background = createGlassCard(bgHex, "#334155", 16f); setPadding(16, 8, 16, 8)
