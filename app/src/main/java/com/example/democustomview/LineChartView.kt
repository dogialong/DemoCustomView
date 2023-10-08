package com.example.democustomview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.RelativeLayout

class LineChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val chartRect = Rect() // store coordinate of chart
    private val xLabels = mutableListOf<String>()
    private val yLabels = mutableListOf<Float>()
    private val points = mutableListOf<Point>()
    private val mutableData = mutableListOf<Temperature>()


    // declare Paints
    private val labelPaint = Paint()
    private val linePaint = Paint()
    private val backgroundPaint = Paint()
    private val curvePaint = Paint()
    // curvePath
    private val curvePath = Path()
    private val fillCurvePath = Path()
    private val controlPoint1s = mutableListOf<Point>()
    private val controlPoint2s = mutableListOf<Point>()

    private var yLabelSize = Pair(0, 0)
    private var xLabelSize = Pair(0, 0)

    init {
        labelPaint.strokeWidth = 3f
        labelPaint.color = Color.BLACK
        labelPaint.style = Paint.Style.FILL
        labelPaint.isAntiAlias = true
        labelPaint.textSize = context.resources.getDimension(R.dimen._12dp)

        linePaint.style = Paint.Style.STROKE
        linePaint.strokeCap = Paint.Cap.ROUND
        linePaint.strokeWidth = 3f
        linePaint.color = Color.LTGRAY
        linePaint.isAntiAlias = true
        linePaint.alpha = 200

        curvePaint.style = Paint.Style.STROKE
        curvePaint.strokeCap = Paint.Cap.ROUND
        curvePaint.strokeWidth = 7f
        curvePaint.color = Color.parseColor("#53944E")
        curvePaint.isAntiAlias = true
        curvePaint.alpha = 200

        backgroundPaint.style = Paint.Style.FILL
        backgroundPaint.color = Color.BLUE
        backgroundPaint.alpha = 150
        backgroundPaint.isAntiAlias = true

        val colorFrom = Color.parseColor("#63B128")
        val colorTo = Color.parseColor("#1A63B128")
        val linearGradient = LinearGradient(
            0f, height.toFloat(), 0f, 0f,
            colorTo, colorFrom, Shader.TileMode.CLAMP
        )
        backgroundPaint.shader = linearGradient

        setWillNotDraw(false)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initShader()
    }
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        initData()
        prepareData()
        getDrawingRect()
        preparePoints()
        getCurvePath(curvePath, false)
        getCurvePath(fillCurvePath, true)
        canvas?.let {
            drawLine(it)
            drawCurvePath(it)
            drawFillPath(it)
        }
    }

    private fun initShader() {
        val colorFrom = Color.parseColor("#63B128")
        val colorTo = Color.parseColor("#1A63B128")
        val linearGradient = LinearGradient(
            0f, height.toFloat(), 0f, 0f,
            colorTo, colorFrom, Shader.TileMode.CLAMP
        )
        backgroundPaint.shader = linearGradient
    }

    private fun getDrawingRect() {
        getDrawingRect(chartRect)
        val padding = context.resources.getDimension(R.dimen._8dp)
        chartRect.left = chartRect.left + padding.toInt()
        chartRect.top = chartRect.top + padding.toInt()
        chartRect.right = chartRect.right - padding.toInt()
        chartRect.bottom = chartRect.bottom - padding.toInt() - xLabelSize.second
    }

    private fun drawLine(canvas: Canvas) {
        val spaceLine = (chartRect.bottom - chartRect.top) / 4
        // draw y labels and line
        for (i in 0 until INTERNAL_CHART_LINE_NUMBER) {
            val label = yLabels[i].toString()
            val y = i * spaceLine.toFloat()
            canvas.drawText(label, yLabelsPosition[i].x, yLabelsPosition[i].y, labelPaint)
            canvas.drawLine(
                chartRect.left.toFloat() + yLabelSize.first, y + yLabelSize.second / 2,
                width.toFloat(), y + yLabelSize.second / 2, linePaint
            )
        }

        //draw x labels
        for(i in 0 until xLabels.size) {
            canvas.drawText(xLabels[i], xLabelsPosition[i].x, xLabelsPosition[i].y, labelPaint)
        }
    }

    private fun getControlPoints() {
        controlPoint1s.clear()
        controlPoint2s.clear()
        for (i in 1 until points.size) {
            controlPoint1s.add(Point((points[i].x + points[i - 1].x)/2, points[i-1].y))
            controlPoint2s.add(Point((points[i].x + points[i - 1].x)/2, points[i].y))
        }
    }
    private fun drawCurvePath(canvas: Canvas) {
        canvas.drawPath(curvePath, curvePaint)
    }

    private fun drawFillPath(canvas: Canvas) {
        canvas.drawPath(fillCurvePath, backgroundPaint)
    }

    private val yLabelsPosition = mutableListOf<Point>()
    private fun getYLabelsPosition() {
        val x = chartRect.left.toFloat()
        val y = chartRect.top.toFloat()
        val gap = (chartRect.bottom - chartRect.top)/ (INTERNAL_CHART_LINE_NUMBER - 1)
        yLabelsPosition.add(Point(x, y))
        for (i in 1 until yLabels.size) {
            yLabelsPosition.add(Point(x, gap * i + y))
        }
    }

    private val xLabelsPosition = mutableListOf<Point>()
    private fun getXLabelsPosition() {
        val x = yLabelsPosition[0].x + yLabelSize.second + MUTABLE_LABEL_PADDING
        val y = this.height.toFloat()
        val startX = x
        val endX = chartRect.right - xLabelSize.first
        val gap = (endX - startX)/ (xLabels.size - 1)
        xLabelsPosition.add(Point(x, y))
        for (i in 1 until xLabels.size) {
            xLabelsPosition.add(Point(gap * i + x, y))
        }
    }
    private fun initData() {
        mutableData.add(Temperature("06:00", 50f))
        mutableData.add(Temperature("07:00", 10f))
        mutableData.add(Temperature("08:00", 20f))
        mutableData.add(Temperature("09:00", 40f))
        mutableData.add(Temperature("10:00", 30f))
    }

    private fun prepareData() {
        if (mutableData.isEmpty()) return
        // add date which is display on x pivot
        getXLabels()
        xLabelSize = getXLabelSize()
        // add label ( temp ) which is display on y pivot
        getYLabels()
        yLabelSize = getYLabelSize()
    }

    private fun getXLabels() {
        mutableData.map {
            xLabels.add(it.date)
        }
    }

    private fun getXLabelSize(): Pair<Int, Int> {
        val rect = Rect()
        val text = xLabels[0]
        labelPaint.getTextBounds(text, 0, text.length, rect)
        return Pair(rect.width(), rect.height())
    }

    private fun getYLabels() {
        val maxTemp = mutableData.maxByOrNull { it.temp }?.temp ?: 0f
        val minTemp = mutableData.minByOrNull { it.temp }?.temp ?: 0f
        var maxValue = maxTemp
        maxValue = maxTemp / 10 * 10 + 10

        var minValue = minTemp
        minValue = minTemp / 10 * 10 - 10
        val gap = (maxValue - minValue) / (INTERNAL_CHART_LINE_NUMBER - 1)
        for (i in 0 until INTERNAL_CHART_LINE_NUMBER) {
            yLabels.add(i * gap)
        }
        yLabels.sortByDescending {
            it
        }
    }
    private fun getYLabelSize(): Pair<Int, Int> {
        val rect = Rect()
        val text = yLabels[0].toString()
        labelPaint.getTextBounds(text, 0, text.length, rect)
        return Pair(rect.width() + MUTABLE_LABEL_PADDING, rect.height())
    }
    // 20, 10, 50, 40, 30
    private fun preparePoints() {
        getDataPoints()
        getControlPoints()
        getYLabelsPosition()
        getXLabelsPosition()
    }

    private fun drawPoints(canvas: Canvas) {
        curvePath.moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            curvePath.lineTo(points[i].x, points[i].y)
        }
        canvas.drawPath(curvePath, curvePaint)
    }
    private fun getDataPoints() {
        val maxTemp = yLabels.maxBy { it }
        val dataSize = mutableData.size
        val heightChart = chartRect.bottom - chartRect.top
        val widthChart = chartRect.right - chartRect.left
        var xPoint = chartRect.left.toFloat() + yLabelSize.first
        mutableData.forEachIndexed { index, temperature ->
            xPoint += index.toFloat() * widthChart / dataSize
            val yPoint = (maxTemp - temperature.temp) * heightChart / maxTemp
            points.add(Point(xPoint, yPoint))
        }
    }
    private fun getCurvePath(curvePath: Path, fill: Boolean) {
        curvePath.reset()
        curvePath.moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            curvePath.cubicTo(
                controlPoint1s[i -1].x,
                controlPoint1s[i-1].y,
                controlPoint2s[i-1].x,
                controlPoint2s[i-1].y,
                points[i].x,
                points[i].y
            )
        }
        curvePath.quadTo(points.last().x, points.last().y,points.last().x, points.last().y)
        if (fill) {
            curvePath.lineTo(
                chartRect.right.toFloat(),
                chartRect.bottom.toFloat() - yLabelSize.second/2
            )
            curvePath.lineTo(
                chartRect.left.toFloat() + yLabelSize.first,
                chartRect.bottom.toFloat() - yLabelSize.second/2
            )
            curvePath.lineTo(
                chartRect.left.toFloat() + yLabelSize.first,
                points.first().y
            )
            curvePath.close()
        }
    }

    companion object {
        private const val INTERNAL_CHART_LINE_NUMBER = 5
        private const val MUTABLE_LABEL_PADDING = 10
    }
}

data class Temperature(val date: String, val temp: Float)

data class Point(val x: Float = 0f, val y: Float = 0f) {

    override fun toString(): String {
        return "($x, $y)"
    }

    companion object {
        val comparator: Comparator<Point> =
            Comparator<Point> { lhs, rhs -> (lhs.x * 1000 - rhs.x * 1000).toInt() }
    }
}