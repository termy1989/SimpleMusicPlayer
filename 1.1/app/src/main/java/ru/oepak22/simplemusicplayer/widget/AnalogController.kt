package ru.oepak22.simplemusicplayer.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

// класс тумблеров
class AnalogController : View {

    private var midx: Float = 0F
    private var midy: Float = 0F

    private var currdeg: Float = 0F
    private var downdeg: Float = 0F
    private var deg: Float = 3F

    private var angle: String = ""
    var label = ""

    private lateinit var textPaint: Paint
    private lateinit var circlePaint: Paint
    lateinit var circlePaint2: Paint
    lateinit var linePaint: Paint

    private var mListener: onProgressChangedListener? = null

    constructor(context: Context) : super(context) { init() }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { init() }
    constructor(context: Context,
                attrs: AttributeSet,
                defStyle: Int) : super(context, attrs, defStyle) { init() }

    interface onProgressChangedListener {
        fun onProgressChanged(progress: Int)
    }

    fun setOnProgressChangedListener(listener: onProgressChangedListener?) {
        mListener = listener
    }

    private fun init() {

        textPaint = Paint()
        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 33F
        textPaint.isFakeBoldText = true
        textPaint.textAlign = Paint.Align.CENTER

        circlePaint = Paint()
        circlePaint.color = Color.parseColor("#222222")
        circlePaint.style = Paint.Style.FILL

        circlePaint2 = Paint()
        circlePaint2.color = Color.parseColor("#B24242")//EqualizerFragment.themeColor
        //circlePaint2.setColor(Color.parseColor("#FFA036"));
        circlePaint2.style = Paint.Style.FILL

        linePaint = Paint()
        linePaint.color = Color.parseColor("#B24242")
        //linePaint.setColor(EqualizerFragment.themeColor);
        //linePaint.setColor(Color.parseColor("#FFA036"));
        linePaint.strokeWidth = 10F

        angle = "0.0"
        label = "Label"
    }

    @SuppressLint("CanvasSize")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        midx = canvas!!.width / 2.toFloat()
        midy = canvas.height / 2.toFloat()

        val radius: Int = (min(midx, midy) * (14.5 / 16)).toInt()
        var x: Float
        var y: Float
        val deg2: Float = max(3F, deg)
        val deg3: Float = min(deg, 21F)

        for (i in deg2.toInt()..21) {
            val tmp: Float = i.toFloat() / 24
            x = midx + (radius * sin(2 * Math.PI * (1.0 - tmp))).toFloat()
            y = midy + (radius * cos(2 * Math.PI * (1.0 - tmp))).toFloat()
            circlePaint.color = Color.parseColor("#111111")
            canvas.drawCircle(x, y, (radius.toFloat() / 15), circlePaint)
        }

        for (i in 3..deg3.toInt()) {
            val tmp: Float = i.toFloat() / 24
            x = midx + (radius * sin(2 * Math.PI * (1.0 - tmp))).toFloat()
            y = midy + (radius * cos(2 * Math.PI * (1.0 - tmp))).toFloat()
            canvas.drawCircle(x, y, (radius.toFloat() / 15), circlePaint2)
        }

        val tmp2 = deg / 24
        val x1 = midx + (radius * (2f / 5) * sin(2 * Math.PI * (1.0 - tmp2))).toFloat()
        val y1 = midy + (radius * (2f / 5) * cos(2 * Math.PI * (1.0 - tmp2))).toFloat()
        val x2 = midx + (radius * (3f / 5) * sin(2 * Math.PI * (1.0 - tmp2))).toFloat()
        val y2 = midy + (radius * (3f / 5) * cos(2 * Math.PI * (1.0 - tmp2))).toFloat()

        circlePaint.color = Color.parseColor("#222222")
        canvas.drawCircle(midx, midy, radius * (13f / 15), circlePaint)
        circlePaint.color = Color.parseColor("#FF3700B3")
        canvas.drawCircle(midx, midy, radius * (11f / 15), circlePaint)
        canvas.drawText(label, midx, midy + (radius * 1.1).toFloat(), textPaint)
        canvas.drawLine(x1, y1, x2, y2, linePaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        mListener!!.onProgressChanged((deg - 2).toInt())

        if (event!!.action == MotionEvent.ACTION_DOWN) {
            val dx: Double = (event.x - midx).toDouble()
            val dy: Double = (event.y - midy).toDouble()
            downdeg = ((atan2(dy, dx) * 180) / Math.PI).toFloat()
            downdeg -= 90
            if (downdeg < 0) downdeg += 360
            downdeg = floor((downdeg / 15).toDouble()).toFloat()
            return true
        }

        if (event.action == MotionEvent.ACTION_MOVE) {
            val dx: Double = (event.x - midx).toDouble()
            val dy: Double = (event.y - midy).toDouble()
            currdeg = ((atan2(dy, dx) * 180) / Math.PI).toFloat()
            currdeg -= 90
            if (currdeg < 0) currdeg += 360
            currdeg = floor((currdeg / 15).toDouble()).toFloat()

            if (currdeg == 0F && downdeg == 23F) {
                deg++
                if (deg > 21) deg = 21F
                downdeg = currdeg
            }
            else if (currdeg == 23F && downdeg == 0F) {
                deg--
                if (deg < 3) deg = 3F
                downdeg = currdeg
            }
            else {
                deg += (currdeg - downdeg)
                if (deg > 21) deg = 21F
                if (deg < 3) deg = 3F
                downdeg = currdeg
            }

            angle = deg.toString()
            invalidate()
            return true
        }

        return event.action == MotionEvent.ACTION_UP || super.onTouchEvent(event)
    }

    fun setProgress(param: Int) {
        deg = (param + 2).toFloat()
    }
}

