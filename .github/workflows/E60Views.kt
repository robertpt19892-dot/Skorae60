package com.example.bmwe60idrive

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.View
import android.view.animation.LinearInterpolator

class BmwRoundelView(c: Context) : View(c) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun onDraw(cv: Canvas) {
        val cx = width / 2f; val cy = height / 2f; val r = minOf(cx, cy) - 4f
        p.color = Color.parseColor("#CBD5E1"); cv.drawCircle(cx, cy, r, p)
        p.color = Color.parseColor("#0B0E14"); cv.drawCircle(cx, cy, r * 0.92f, p)
        val ir = r * 0.62f; val rc = RectF(cx - ir, cy - ir, cx + ir, cy + ir)
        p.color = Color.parseColor("#008AC9"); cv.drawArc(rc, 180f, 90f, true, p); cv.drawArc(rc, 0f, 90f, true, p)
        p.color = Color.WHITE; cv.drawArc(rc, 270f, 90f, true, p); cv.drawArc(rc, 90f, 90f, true, p)
    }
}

class MStripesView(c: Context) : View(c) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun onDraw(cv: Canvas) {
        listOf("#00A3E0", "#1E50A2", "#E21B22").forEachIndexed { i, h ->
            p.color = Color.parseColor(h); val w = width / 3.6f; val x = i * (w + 3f)
            cv.drawPath(Path().apply { moveTo(x + 12f, 0f); lineTo(x + w + 12f, 0f); lineTo(x + w, height.toFloat()); lineTo(x, height.toFloat()); close() }, p)
        }
    }
}

class BmwE60VisualizerView(c: Context) : View(c) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG); private var lights = true; private var doorOpen = false; private var pulse = 0.9f
    init { ValueAnimator.ofFloat(0.75f, 1.0f).apply { duration = 1400; repeatCount = ValueAnimator.INFINITE; repeatMode = ValueAnimator.REVERSE; interpolator = LinearInterpolator(); addUpdateListener { pulse = it.animatedValue as Float; invalidate() }; start() } }
    fun setLights(on: Boolean) { lights = on; invalidate() }
    fun setDoorsOpen(open: Boolean) { doorOpen = open; invalidate() }
    override fun onDraw(cv: Canvas) {
        val cx = width / 2f; val cy = height * 0.56f; val cw = width * 0.78f
        p.shader = RadialGradient(cx, cy + 45f, cw * 0.52f, Color.parseColor("#2500A3E0"), Color.TRANSPARENT, Shader.TileMode.CLAMP)
        cv.drawOval(cx - cw * 0.52f, cy + 15f, cx + cw * 0.52f, cy + 75f, p); p.shader = null
        p.style = Paint.Style.STROKE; p.strokeWidth = 4f; p.color = Color.parseColor("#475569")
        cv.drawPath(Path().apply { moveTo(cx - cw * 0.44f, cy + 25f); quadTo(cx - cw * 0.46f, cy - 25f, cx - cw * 0.34f, cy - 35f); lineTo(cx - cw * 0.24f, cy - 75f); quadTo(cx, cy - 88f, cx + cw * 0.24f, cy - 75f); lineTo(cx + cw * 0.34f, cy - 35f); quadTo(cx + cw * 0.46f, cy - 25f, cx + cw * 0.44f, cy + 25f); quadTo(cx, cy + 42f, cx - cw * 0.44f, cy + 25f) }, p)
        if (doorOpen) { p.color = Color.parseColor("#F43F5E"); p.strokeWidth = 6f; cv.drawLine(cx - cw * 0.35f, cy - 35f, cx - cw * 0.49f, cy - 55f, p) }
        p.style = Paint.Style.STROKE; p.strokeWidth = 4f; p.color = Color.parseColor("#94A3B8")
        val kw = cw * 0.11f; cv.drawRoundRect(cx - kw - 8f, cy - 12f, cx - 8f, cy + 22f, 12f, 12f, p); cv.drawRoundRect(cx + 8f, cy - 12f, cx + kw + 8f, cy + 22f, 12f, 12f, p)
        listOf(-cw * 0.29f, cw * 0.29f).forEachIndexed { s, ox ->
            val lx = cx + ox; val ly = cy - 2f
            p.color = if (lights) Color.parseColor("#FF8C00") else Color.parseColor("#334155"); cv.drawLine(if (s == 0) lx - 42f else lx + 42f, ly - 24f, if (s == 0) lx + 35f else lx - 35f, ly - 15f, p)
            p.color = if (lights) Color.parseColor("#E0F2FE") else Color.parseColor("#334155"); p.strokeWidth = if (lights) 4.5f * pulse else 2.5f
            cv.drawCircle(lx - 17f, ly, 13f, p); cv.drawCircle(lx + 17f, ly, 13f, p)
        }
    }
}
