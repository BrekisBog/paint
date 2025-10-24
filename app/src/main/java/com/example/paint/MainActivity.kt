package com.example.paint

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.*
import androidx.core.content.ContextCompat
import java.io.*
import kotlin.math.abs

class MainActivity : Activity() {

    private lateinit var canvasView: CanvasView
    private lateinit var thicknessSeekBar: SeekBar
    private lateinit var thicknessText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val density = displayMetrics.density

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        canvasView = CanvasView(this)
        canvasView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        )

        val bottomPanelLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(Color.LTGRAY)
        }

        val thicknessLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setPadding((16 * density).toInt(), (8 * density).toInt(), (16 * density).toInt(), (8 * density).toInt())
            }
        }

        thicknessText = TextView(this).apply {
            text = "Толщина: 8px"
            setTextSize(14f)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, (12 * density).toInt(), 0)
            }
        }

        thicknessSeekBar = SeekBar(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            max = 50
            progress = 8
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val thickness = progress.coerceAtLeast(1)
                    thicknessText.text = "Толщина: ${thickness}px"
                    canvasView.setBrushWidth(thickness.toFloat() * density)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        thicknessLayout.addView(thicknessText)
        thicknessLayout.addView(thicknessSeekBar)

        val buttonsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setPadding((8 * density).toInt(), (4 * density).toInt(), (8 * density).toInt(), (12 * density).toInt())
            }
        }

        fun createToolButton(text: String, onClick: () -> Unit): Button {
            return Button(this).apply {
                this.text = text
                setOnClickListener { onClick() }
                setTextSize(12f)
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    setMargins((4 * density).toInt(), (4 * density).toInt(), (4 * density).toInt(), (4 * density).toInt())
                }
            }
        }

        val paletteBtn = createToolButton("🎨 Цвет") { showColorPickerDialog() }
        val saveBtn = createToolButton("💾 Сохранить") { showSaveDialog() }
        val loadBtn = createToolButton("📂 Открыть") { showLoadDialog() }
        val undoBtn = createToolButton("↩️ Отмена") { canvasView.undo() }
        val clearBtn = createToolButton("🗑️ Очистить") { canvasView.clearCanvas() }

        val firstRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val secondRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        firstRow.addView(paletteBtn)
        firstRow.addView(saveBtn)
        firstRow.addView(loadBtn)

        secondRow.addView(undoBtn)
        secondRow.addView(clearBtn)

        bottomPanelLayout.addView(thicknessLayout)
        bottomPanelLayout.addView(firstRow)
        bottomPanelLayout.addView(secondRow)

        mainLayout.addView(canvasView)
        mainLayout.addView(bottomPanelLayout)

        setContentView(mainLayout)
    }

    private fun showColorPickerDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.color_picker_dialog)
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val seekBarRed = dialog.findViewById<SeekBar>(R.id.seekBarRed)
        val seekBarGreen = dialog.findViewById<SeekBar>(R.id.seekBarGreen)
        val seekBarBlue = dialog.findViewById<SeekBar>(R.id.seekBarBlue)
        val colorPreview = dialog.findViewById<View>(R.id.colorPreview)
        val textRed = dialog.findViewById<TextView>(R.id.textRed)
        val textGreen = dialog.findViewById<TextView>(R.id.textGreen)
        val textBlue = dialog.findViewById<TextView>(R.id.textBlue)
        val btnConfirm = dialog.findViewById<Button>(R.id.btnConfirm)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

        var currentColor = Color.BLACK
        seekBarRed.progress = Color.red(currentColor)
        seekBarGreen.progress = Color.green(currentColor)
        seekBarBlue.progress = Color.blue(currentColor)

        fun updateColorPreview() {
            val red = seekBarRed.progress
            val green = seekBarGreen.progress
            val blue = seekBarBlue.progress
            currentColor = Color.rgb(red, green, blue)
            colorPreview.setBackgroundColor(currentColor)
            textRed.text = "R: $red"
            textGreen.text = "G: $green"
            textBlue.text = "B: $blue"
        }

        seekBarRed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateColorPreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarGreen.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateColorPreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarBlue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateColorPreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnConfirm.setOnClickListener {
            canvasView.setBrushColor(currentColor)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        updateColorPreview()
        dialog.show()
    }

    private fun showSaveDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.save_load_dialog)
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val title = dialog.findViewById<TextView>(R.id.dialogTitle)
        val input = dialog.findViewById<EditText>(R.id.fileNameInput)
        val btnConfirm = dialog.findViewById<Button>(R.id.btnConfirm)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

        title.text = "Сохранить рисунок"
        input.hint = "Введите название файла"

        btnConfirm.setOnClickListener {
            val fileName = input.text.toString().trim()
            if (fileName.isNotEmpty()) {
                if (canvasView.saveDrawing(fileName)) {
                    Toast.makeText(this, "Рисунок сохранен: $fileName", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Введите название файла", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showLoadDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.save_load_dialog)
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val title = dialog.findViewById<TextView>(R.id.dialogTitle)
        val input = dialog.findViewById<EditText>(R.id.fileNameInput)
        val btnConfirm = dialog.findViewById<Button>(R.id.btnConfirm)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

        title.text = "Открыть рисунок"
        input.hint = "Введите название файла"

        val files = getSavedFiles()
        if (files.isNotEmpty()) {
            val filesLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            files.forEach { file ->
                val fileBtn = Button(this).apply {
                    text = file
                    setOnClickListener {
                        input.setText(file)
                    }
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 4, 0, 4)
                    }
                }
                filesLayout.addView(fileBtn)
            }

            val scrollView = ScrollView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    200
                )
                addView(filesLayout)
            }

            (dialog.findViewById<View>(R.id.dialogLayout) as LinearLayout).addView(
                scrollView,
                1
            )
        }

        btnConfirm.setOnClickListener {
            val fileName = input.text.toString().trim()
            if (fileName.isNotEmpty()) {
                if (canvasView.loadDrawing(fileName)) {
                    Toast.makeText(this, "Рисунок загружен: $fileName", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Файл не найден: $fileName", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Введите название файла", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun getSavedFiles(): List<String> {
        return try {
            val filesDir = filesDir
            filesDir.list()?.filter { it.endsWith(".paint") }?.map { it.removeSuffix(".paint") } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    data class DrawPath(val path: Path, val paint: Paint, val color: Int, val strokeWidth: Float)

    inner class CanvasView(context: Context) : View(context) {

        private var path = Path()
        private var paint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 8f
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        private var currentX = 0f
        private var currentY = 0f

        private val paths = mutableListOf<DrawPath>()
        private var currentColor = Color.BLACK
        private var strokeWidth = 8f

        init {
            paint.color = currentColor
            paint.strokeWidth = strokeWidth
            setBackgroundColor(Color.WHITE)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            for (drawPath in paths) {
                drawPath.paint.color = drawPath.color
                drawPath.paint.strokeWidth = drawPath.strokeWidth
                canvas.drawPath(drawPath.path, drawPath.paint)
            }

            canvas.drawPath(path, paint)
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val x = event.x
            val y = event.y

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    path.moveTo(x, y)
                    currentX = x
                    currentY = y
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = abs(x - currentX)
                    val dy = abs(y - currentY)
                    if (dx >= 4 || dy >= 4) {
                        path.quadTo(currentX, currentY, (x + currentX) / 2, (y + currentY) / 2)
                        currentX = x
                        currentY = y
                    }
                    invalidate()
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    path.lineTo(currentX, currentY)
                    // Сохраняем текущий путь
                    paths.add(DrawPath(Path(path), Paint(paint), currentColor, strokeWidth))
                    path.reset()
                    invalidate()
                    return true
                }
                else -> return false
            }
        }

        fun clearCanvas() {
            paths.clear()
            invalidate()
        }

        fun setBrushColor(color: Int) {
            currentColor = color
            paint.color = color
        }

        fun setBrushWidth(width: Float) {
            strokeWidth = width
            paint.strokeWidth = width
        }

        fun undo() {
            if (paths.isNotEmpty()) {
                paths.removeAt(paths.size - 1)
                invalidate()
            }
        }

        fun saveDrawing(fileName: String): Boolean {
            return try {
                val file = File(context.filesDir, "$fileName.paint")
                ObjectOutputStream(FileOutputStream(file)).use { oos ->
                    oos.writeInt(paths.size)
                    paths.forEach { drawPath ->
                        oos.writeInt(drawPath.color)
                        oos.writeFloat(drawPath.strokeWidth)
                        val points = getPathPoints(drawPath.path)
                        oos.writeInt(points.size)
                        points.forEach { point ->
                            oos.writeFloat(point.x)
                            oos.writeFloat(point.y)
                        }
                    }
                }
                true
            } catch (e: Exception) {
                false
            }
        }

        fun loadDrawing(fileName: String): Boolean {
            return try {
                val file = File(context.filesDir, "$fileName.paint")
                if (!file.exists()) return false

                ObjectInputStream(FileInputStream(file)).use { ois ->
                    paths.clear()
                    val size = ois.readInt()
                    repeat(size) {
                        val color = ois.readInt()
                        val strokeWidth = ois.readFloat()
                        val pointsCount = ois.readInt()
                        val path = Path()
                        val points = mutableListOf<PointF>()

                        repeat(pointsCount) {
                            points.add(PointF(ois.readFloat(), ois.readFloat()))
                        }

                        if (points.size >= 2) {
                            path.moveTo(points[0].x, points[0].y)
                            for (i in 1 until points.size) {
                                path.lineTo(points[i].x, points[i].y)
                            }
                        }

                        paths.add(DrawPath(path, Paint(paint), color, strokeWidth))
                    }
                }
                invalidate()
                true
            } catch (e: Exception) {
                false
            }
        }

        private fun getPathPoints(path: Path): List<PointF> {
            val points = mutableListOf<PointF>()
            val pathMeasure = PathMeasure(path, false)
            val length = pathMeasure.length
            val step = 1f // Шаг в пикселях

            var distance = 0f
            val coords = FloatArray(2)

            while (distance < length) {
                if (pathMeasure.getPosTan(distance, coords, null)) {
                    points.add(PointF(coords[0], coords[1]))
                }
                distance += step
            }

            return points
        }
    }
}