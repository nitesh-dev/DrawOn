package com.flaxstudio.drawon.customview

import android.content.Context
import android.graphics.*
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.abhishek.colorpicker.toPx
import com.flaxstudio.drawon.utils.*

typealias  funUndoRedo = (isUndoVisible: Boolean, isRedoVisible: Boolean) -> Unit

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val allShapeRedo: ArrayList<Shape> = ArrayList()                // redo
    private val allShape: ArrayList<Shape> = ArrayList()                    // undo

    private val toolsData: ArrayList<ToolProperties> = ArrayList()
    private val tempPath = Path()                   // this path is used by triangle, heart
    private var isCurrentShapeDrawing = false
    private var currentDrawingShape = Shape()
    private var currentSelectedTool = ShapeType.Rectangle
    private var canvasPosition = Vector2()          // store canvas translate position
    private var canvasScale = 1f                    // store canvas scale
    private var previousTouch = Vector2()
    private var previousFingerDist = 0f
    private var projectSavedBitmap: Bitmap? = null
    
    private val gridPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = Color.LTGRAY
        strokeWidth = 1f
    }

    private var shapePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth = 4f
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    // Note: Update needed
    private var totalShapeDrawn = 0
    private var undoRedoFun: funUndoRedo? = null
    private var isProjectSaved = true

    fun setUndoRedoListener(callback: funUndoRedo) {
        undoRedoFun = callback
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // 1. Draw infinite white background
        canvas.drawColor(Color.WHITE)
        
        // 2. Draw infinite grid
        drawGrid(canvas)
        
        canvas.save()
        canvas.translate(canvasPosition.x, canvasPosition.y)
        canvas.scale(canvasScale, canvasScale)

        // 3. Draw content in a layer to support Eraser (CLEAR mode)
        canvas.saveLayer(null, null)

        // Draw saved project bitmap if any
        if (projectSavedBitmap != null) {
            canvas.drawBitmap(projectSavedBitmap!!, 0F, 0F, null)
        }


        // Draw all committed shapes
        for (shape in allShape) {
            drawShape(canvas, shape)
        }

        // drawing current drawing shape
        if (isCurrentShapeDrawing) {
            drawShape(canvas, currentDrawingShape)
        }
        
        canvas.restore() // Restore layer
        canvas.restore() // Restore translate/scale

        // Update UI state
        if (totalShapeDrawn != allShape.size) {
            undoRedoFun?.invoke(allShape.isNotEmpty(), allShapeRedo.isNotEmpty())
            totalShapeDrawn = allShape.size
        }


    }

    private fun drawGrid(canvas: Canvas) {
        val gridSize = 50f * canvasScale
        val offsetX = canvasPosition.x % gridSize
        val offsetY = canvasPosition.y % gridSize
        
        for (x in 0..width step gridSize.toInt()) {
            canvas.drawLine(x + offsetX, 0f, x + offsetX, height.toFloat(), gridPaint)
        }
        for (y in 0..height step gridSize.toInt()) {
            canvas.drawLine(0f, y + offsetY, width.toFloat(), y + offsetY, gridPaint)
        }
    }

    private fun drawShape(canvas: Canvas, shape: Shape) {

        when (shape.shapeType) {

            ShapeType.Rectangle -> {
                shape as Rectangle

                // drawing fill shape if color is not transparent
                if (!isColorTransparent(shape.fillColor)) {
                    shapePaint.color = shape.fillColor
                    shapePaint.style = Paint.Style.FILL
                    canvas.drawRect(
                        shape.startPos.x,
                        shape.startPos.y,
                        shape.endPos.x,
                        shape.endPos.y,
                        shapePaint
                    )
                }

                // drawing stroke shape if color is not transparent
                if (!isColorTransparent(shape.strokeColor)) {
                    shapePaint.color = shape.strokeColor
                    shapePaint.strokeWidth = shape.strokeWidth
                    shapePaint.style = Paint.Style.STROKE
                    canvas.drawRect(
                        shape.startPos.x,
                        shape.startPos.y,
                        shape.endPos.x,
                        shape.endPos.y, shapePaint
                    )
                }

            }

            ShapeType.Brush -> {
                shape as Brush
                // drawing stroke if color is not transparent
                if (!isColorTransparent(shape.strokeColor)) {

                    shapePaint.color = shape.strokeColor
                    shapePaint.strokeWidth = shape.strokeWidth
                    shapePaint.style = Paint.Style.STROKE
                    canvas.drawPath(shape.path, shapePaint)
                }
            }

            ShapeType.Eraser -> {
                shape as Eraser
                shapePaint.strokeWidth = shape.strokeWidth
                shapePaint.style = Paint.Style.STROKE
                shapePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                canvas.drawPath(shape.path, shapePaint)
                shapePaint.xfermode = null
            }

            ShapeType.Square -> {
                shape as Square
                // drawing fill shape if color is not transparent
                if (!isColorTransparent(shape.fillColor)) {
                    shapePaint.color = shape.fillColor
                    shapePaint.style = Paint.Style.FILL
                    canvas.drawRect(
                        shape.startPos.x,
                        shape.startPos.y,
                        shape.endPos.x,
                        shape.endPos.y,
                        shapePaint
                    )
                }

                // drawing stroke shape if color is not transparent
                if (!isColorTransparent(shape.strokeColor)) {
                    shapePaint.color = shape.strokeColor
                    shapePaint.strokeWidth = shape.strokeWidth
                    shapePaint.style = Paint.Style.STROKE
                    canvas.drawRect(
                        shape.startPos.x,
                        shape.startPos.y,
                        shape.endPos.x,
                        shape.endPos.y, shapePaint
                    )
                }
            }

            ShapeType.Circle -> {
                shape as Circle
                // drawing fill shape if color is not transparent
                if (!isColorTransparent(shape.fillColor)) {
                    shapePaint.color = shape.fillColor
                    shapePaint.style = Paint.Style.FILL
                    canvas.drawOval(
                        shape.startPos.x,
                        shape.startPos.y,
                        shape.endPos.x,
                        shape.endPos.y,
                        shapePaint
                    )
                }

                // drawing stroke if color is not transparent
                if (!isColorTransparent(shape.strokeColor)) {

                    shapePaint.color = shape.strokeColor
                    shapePaint.strokeWidth = shape.strokeWidth
                    shapePaint.style = Paint.Style.STROKE
                    canvas.drawOval(
                        shape.startPos.x,
                        shape.startPos.y,
                        shape.endPos.x,
                        shape.endPos.y,
                        shapePaint
                    )
                }
            }

            ShapeType.Diamond -> {
                shape as Diamond
                tempPath.reset()
                val centerX = (shape.startPos.x + shape.endPos.x) / 2
                val centerY = (shape.startPos.y + shape.endPos.y) / 2
                tempPath.moveTo(centerX, shape.startPos.y)
                tempPath.lineTo(shape.endPos.x, centerY)
                tempPath.lineTo(centerX, shape.endPos.y)
                tempPath.lineTo(shape.startPos.x, centerY)
                tempPath.close()

                if (!isColorTransparent(shape.fillColor)) {
                    shapePaint.color = shape.fillColor
                    shapePaint.style = Paint.Style.FILL
                    canvas.drawPath(tempPath, shapePaint)
                }
                if (!isColorTransparent(shape.strokeColor)) {
                    shapePaint.color = shape.strokeColor
                    shapePaint.strokeWidth = shape.strokeWidth
                    shapePaint.style = Paint.Style.STROKE
                    canvas.drawPath(tempPath, shapePaint)
                }
            }

            ShapeType.Star -> {
                shape as Star
                tempPath.reset()
                val centerX = (shape.startPos.x + shape.endPos.x) / 2
                val centerY = (shape.startPos.y + shape.endPos.y) / 2
                val radius = Math.min(Math.abs(shape.endPos.x - shape.startPos.x), Math.abs(shape.endPos.y - shape.startPos.y)) / 2
                val innerRadius = radius / 2.5f
                
                for (i in 0 until 10) {
                    val angle = Math.toRadians((i * 36 - 90).toDouble())
                    val r = if (i % 2 == 0) radius else innerRadius
                    val x = (centerX + r * Math.cos(angle)).toFloat()
                    val y = (centerY + r * Math.sin(angle)).toFloat()
                    if (i == 0) tempPath.moveTo(x, y) else tempPath.lineTo(x, y)
                }
                tempPath.close()

                if (!isColorTransparent(shape.fillColor)) {
                    shapePaint.color = shape.fillColor
                    shapePaint.style = Paint.Style.FILL
                    canvas.drawPath(tempPath, shapePaint)
                }
                if (!isColorTransparent(shape.strokeColor)) {
                    shapePaint.color = shape.strokeColor
                    shapePaint.strokeWidth = shape.strokeWidth
                    shapePaint.style = Paint.Style.STROKE
                    canvas.drawPath(tempPath, shapePaint)
                }
            }

            ShapeType.Arrow -> {
                shape as Arrow
                tempPath.reset()
                val startX = shape.startPos.x
                val startY = shape.startPos.y
                val endX = shape.endPos.x
                val endY = shape.endPos.y
                
                val dx = endX - startX
                val dy = endY - startY
                val angle = Math.atan2(dy.toDouble(), dx.toDouble())
                val arrowHeadLen = 40f
                val arrowHeadAngle = Math.PI / 6
                
                tempPath.moveTo(startX, startY)
                tempPath.lineTo(endX, endY)
                tempPath.moveTo(endX, endY)
                tempPath.lineTo((endX - arrowHeadLen * Math.cos(angle - arrowHeadAngle)).toFloat(), (endY - arrowHeadLen * Math.sin(angle - arrowHeadAngle)).toFloat())
                tempPath.moveTo(endX, endY)
                tempPath.lineTo((endX - arrowHeadLen * Math.cos(angle + arrowHeadAngle)).toFloat(), (endY - arrowHeadLen * Math.sin(angle + arrowHeadAngle)).toFloat())

                if (!isColorTransparent(shape.strokeColor)) {
                    shapePaint.color = shape.strokeColor
                    shapePaint.strokeWidth = shape.strokeWidth
                    shapePaint.style = Paint.Style.STROKE
                    canvas.drawPath(tempPath, shapePaint)
                }
            }

            ShapeType.Pentagon -> {
                shape as Pentagon
                tempPath.reset()
                val centerX = (shape.startPos.x + shape.endPos.x) / 2
                val centerY = (shape.startPos.y + shape.endPos.y) / 2
                val radius = Math.min(Math.abs(shape.endPos.x - shape.startPos.x), Math.abs(shape.endPos.y - shape.startPos.y)) / 2
                
                for (i in 0 until 5) {
                    val angle = Math.toRadians((i * 72 - 90).toDouble())
                    val x = (centerX + radius * Math.cos(angle)).toFloat()
                    val y = (centerY + radius * Math.sin(angle)).toFloat()
                    if (i == 0) tempPath.moveTo(x, y) else tempPath.lineTo(x, y)
                }
                tempPath.close()

                if (!isColorTransparent(shape.fillColor)) {
                    shapePaint.color = shape.fillColor
                    shapePaint.style = Paint.Style.FILL
                    canvas.drawPath(tempPath, shapePaint)
                }
                if (!isColorTransparent(shape.strokeColor)) {
                    shapePaint.color = shape.strokeColor
                    shapePaint.strokeWidth = shape.strokeWidth
                    shapePaint.style = Paint.Style.STROKE
                    canvas.drawPath(tempPath, shapePaint)
                }
            }

            ShapeType.Line -> {
                shape as Line
                // drawing stroke if color is not transparent
                if (!isColorTransparent(shape.strokeColor)) {

                    shapePaint.color = shape.strokeColor
                    shapePaint.strokeWidth = shape.strokeWidth
                    shapePaint.style = Paint.Style.STROKE
                    canvas.drawLine(
                        shape.startPos.x,
                        shape.startPos.y,
                        shape.endPos.x,
                        shape.endPos.y,
                        shapePaint
                    )
                }
            }

            ShapeType.Oval -> {
                shape as Oval

                // drawing fill shape if color is not transparent
                if (!isColorTransparent(shape.fillColor)) {
                    shapePaint.color = shape.fillColor
                    shapePaint.style = Paint.Style.FILL
                    canvas.drawOval(
                        shape.startPos.x,
                        shape.startPos.y,
                        shape.endPos.x,
                        shape.endPos.y,
                        shapePaint
                    )
                }

                // drawing stroke if color is not transparent
                if (!isColorTransparent(shape.strokeColor)) {

                    shapePaint.color = shape.strokeColor
                    shapePaint.strokeWidth = shape.strokeWidth
                    shapePaint.style = Paint.Style.STROKE
                    canvas.drawOval(
                        shape.startPos.x,
                        shape.startPos.y,
                        shape.endPos.x,
                        shape.endPos.y,
                        shapePaint
                    )
                }

            }

            ShapeType.Triangle -> {
                shape as Triangle

                // Note:   (shape.endPos.x - shape.startPos.x) / 2 + shape.startPos.x is used to
                //         calculate mid-x point in rect

                tempPath.reset()        // clear path for drawing
                tempPath.moveTo(
                    (shape.endPos.x - shape.startPos.x) / 2 + shape.startPos.x,
                    shape.startPos.y
                )
                tempPath.lineTo(shape.endPos.x, shape.endPos.y)
                tempPath.lineTo(shape.startPos.x, shape.endPos.y)
                tempPath.close()

                // drawing fill if color is not transparent
                if (!isColorTransparent(shape.fillColor)) {

                    shapePaint.color = shape.fillColor
                    shapePaint.style = Paint.Style.FILL
                    canvas.drawPath(tempPath, shapePaint)
                }

                // drawing stroke if color is not transparent
                if (!isColorTransparent(shape.strokeColor)) {

                    shapePaint.color = shape.strokeColor
                    shapePaint.strokeWidth = shape.strokeWidth
                    shapePaint.style = Paint.Style.STROKE
                    canvas.drawPath(tempPath, shapePaint)
                }

            }

            ShapeType.Heart -> {
                shape as Heart
                tempPath.reset()        // clear path for drawing

                val diffVector = shape.endPos.minus(shape.startPos)   // difference of two vector
                // calculating heart top center position
                tempPath.moveTo(
                    shape.startPos.x + diffVector.x / 2,
                    shape.startPos.y + diffVector.y * 0.3f
                )

                // adding left part of heart
                tempPath.cubicTo(
                    shape.startPos.x + diffVector.x * 0.3f,
                    shape.startPos.y,
                    shape.startPos.x,
                    shape.startPos.y + diffVector.y * 0.5f,
                    shape.startPos.x + diffVector.x * 0.5f,
                    shape.endPos.y
                )

                // calculating heart top center position
                tempPath.moveTo(
                    shape.startPos.x + diffVector.x / 2,
                    shape.startPos.y + diffVector.y * 0.3f
                )

                // adding right part of heart
                tempPath.cubicTo(
                    shape.endPos.x - diffVector.x * 0.3f,
                    shape.startPos.y,
                    shape.endPos.x,
                    shape.startPos.y + diffVector.y * 0.5f,
                    shape.endPos.x - diffVector.x * 0.5f,
                    shape.endPos.y
                )

                // drawing fill if color is not transparent
                if (!isColorTransparent(shape.fillColor)) {

                    shapePaint.color = shape.fillColor
                    shapePaint.style = Paint.Style.FILL
                    canvas.drawPath(tempPath, shapePaint)
                }

                // drawing stroke if color is not transparent
                if (!isColorTransparent(shape.strokeColor)) {

                    shapePaint.color = shape.strokeColor
                    shapePaint.strokeWidth = shape.strokeWidth
                    shapePaint.style = Paint.Style.STROKE
                    canvas.drawPath(tempPath, shapePaint)
                }

            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val touchPos = Vector2(event.x, event.y)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {

                // if touch will not in limiting area then skip this step
                if (!isTouchWithinLimitingArea()) return false

                val selectedTool = getSelectedToolProp(currentSelectedTool)
                if (selectedTool != null) {

                    previousTouch = touchPos
                    isCurrentShapeDrawing = true

                    when (currentSelectedTool) {
                        ShapeType.Brush -> {
                            currentDrawingShape = Brush()
                            currentDrawingShape.strokeColor = selectedTool.strokeColor
                            currentDrawingShape.strokeWidth = selectedTool.strokeWidth

                            val pos = screenToCanvas(touchPos)
                            (currentDrawingShape as Brush).path.moveTo(pos.x, pos.y)
                            (currentDrawingShape as Brush).path.lineTo(pos.x, pos.y)
                            //(currentDrawingShape as Brush).addMoveTo(pos.x, pos.y)

                        }
                        ShapeType.Eraser -> {
                            currentDrawingShape = Eraser()
                            currentDrawingShape.strokeColor = selectedTool.strokeColor
                            currentDrawingShape.strokeWidth = selectedTool.strokeWidth

                            val pos = screenToCanvas(touchPos)
                            (currentDrawingShape as Eraser).path.moveTo(pos.x, pos.y)
                            (currentDrawingShape as Eraser).path.lineTo(pos.x, pos.y)
                            //(currentDrawingShape as Eraser).addMoveTo(pos.x, pos.y)

                        }
                        ShapeType.Rectangle -> {
                            currentDrawingShape = Rectangle()
                            currentDrawingShape.fillColor = selectedTool.fillColor
                            currentDrawingShape.strokeColor = selectedTool.strokeColor
                            currentDrawingShape.strokeWidth = selectedTool.strokeWidth
                            (currentDrawingShape as Rectangle).startPos.setValue(
                                screenToCanvas(
                                    touchPos
                                )
                            )
                            (currentDrawingShape as Rectangle).endPos.setValue(
                                screenToCanvas(
                                    touchPos
                                )
                            )

                        }
                        ShapeType.Line -> {
                            currentDrawingShape = Line()
                            currentDrawingShape.strokeColor = selectedTool.strokeColor
                            currentDrawingShape.strokeWidth = selectedTool.strokeWidth
                            (currentDrawingShape as Line).startPos.setValue(screenToCanvas(touchPos))
                            (currentDrawingShape as Line).endPos.setValue(screenToCanvas(touchPos))

                        }
                        ShapeType.Oval -> {
                            currentDrawingShape = Oval()
                            currentDrawingShape.fillColor = selectedTool.fillColor
                            currentDrawingShape.strokeColor = selectedTool.strokeColor
                            currentDrawingShape.strokeWidth = selectedTool.strokeWidth
                            (currentDrawingShape as Oval).startPos.setValue(screenToCanvas(touchPos))
                            (currentDrawingShape as Oval).endPos.setValue(screenToCanvas(touchPos))

                        }
                        ShapeType.Triangle -> {
                            currentDrawingShape = Triangle()
                            currentDrawingShape.fillColor = selectedTool.fillColor
                            currentDrawingShape.strokeColor = selectedTool.strokeColor
                            currentDrawingShape.strokeWidth = selectedTool.strokeWidth
                            (currentDrawingShape as Triangle).startPos.setValue(
                                screenToCanvas(
                                    touchPos
                                )
                            )
                            (currentDrawingShape as Triangle).endPos.setValue(
                                screenToCanvas(
                                    touchPos
                                )
                            )
                        }
                        ShapeType.Heart -> {
                            currentDrawingShape = Heart()
                            currentDrawingShape.fillColor = selectedTool.fillColor
                            currentDrawingShape.strokeColor = selectedTool.strokeColor
                            currentDrawingShape.strokeWidth = selectedTool.strokeWidth
                            (currentDrawingShape as Heart).startPos.setValue(screenToCanvas(touchPos))
                            (currentDrawingShape as Heart).endPos.setValue(screenToCanvas(touchPos))
                        }
                        ShapeType.Square -> {
                            currentDrawingShape = Square()
                            currentDrawingShape.fillColor = selectedTool.fillColor
                            currentDrawingShape.strokeColor = selectedTool.strokeColor
                            currentDrawingShape.strokeWidth = selectedTool.strokeWidth
                            (currentDrawingShape as Square).startPos.setValue(screenToCanvas(touchPos))
                            (currentDrawingShape as Square).endPos.setValue(screenToCanvas(touchPos))
                        }
                        ShapeType.Circle -> {
                            currentDrawingShape = Circle()
                            currentDrawingShape.fillColor = selectedTool.fillColor
                            currentDrawingShape.strokeColor = selectedTool.strokeColor
                            currentDrawingShape.strokeWidth = selectedTool.strokeWidth
                            (currentDrawingShape as Circle).startPos.setValue(screenToCanvas(touchPos))
                            (currentDrawingShape as Circle).endPos.setValue(screenToCanvas(touchPos))
                        }
                        ShapeType.Diamond -> {
                            currentDrawingShape = Diamond()
                            currentDrawingShape.fillColor = selectedTool.fillColor
                            currentDrawingShape.strokeColor = selectedTool.strokeColor
                            currentDrawingShape.strokeWidth = selectedTool.strokeWidth
                            (currentDrawingShape as Diamond).startPos.setValue(screenToCanvas(touchPos))
                            (currentDrawingShape as Diamond).endPos.setValue(screenToCanvas(touchPos))
                        }
                        ShapeType.Star -> {
                            currentDrawingShape = Star()
                            currentDrawingShape.fillColor = selectedTool.fillColor
                            currentDrawingShape.strokeColor = selectedTool.strokeColor
                            currentDrawingShape.strokeWidth = selectedTool.strokeWidth
                            (currentDrawingShape as Star).startPos.setValue(screenToCanvas(touchPos))
                            (currentDrawingShape as Star).endPos.setValue(screenToCanvas(touchPos))
                        }
                        ShapeType.Arrow -> {
                            currentDrawingShape = Arrow()
                            currentDrawingShape.strokeColor = selectedTool.strokeColor
                            currentDrawingShape.strokeWidth = selectedTool.strokeWidth
                            (currentDrawingShape as Arrow).startPos.setValue(screenToCanvas(touchPos))
                            (currentDrawingShape as Arrow).endPos.setValue(screenToCanvas(touchPos))
                        }
                        ShapeType.Pentagon -> {
                            currentDrawingShape = Pentagon()
                            currentDrawingShape.fillColor = selectedTool.fillColor
                            currentDrawingShape.strokeColor = selectedTool.strokeColor
                            currentDrawingShape.strokeWidth = selectedTool.strokeWidth
                            (currentDrawingShape as Pentagon).startPos.setValue(screenToCanvas(touchPos))
                            (currentDrawingShape as Pentagon).endPos.setValue(screenToCanvas(touchPos))
                        }
                    }

                } else {
                    return false
                }
            }

            MotionEvent.ACTION_POINTER_DOWN -> {

                // this callback is only for zoom and pan
                if (event.pointerCount > 1) {
                    if (isCurrentShapeDrawing) isCurrentShapeDrawing = false
                    val touch1 = Vector2(event.getX(0), event.getY(0))
                    val touch2 = Vector2(event.getX(1), event.getY(1))
                    previousTouch = touch1.minPointVector(touch2)
                    
                    val dx = event.getX(0) - event.getX(1)
                    val dy = event.getY(0) - event.getY(1)
                    previousFingerDist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                    
                    invalidate()
                    return true
                }
            }

            MotionEvent.ACTION_MOVE -> {

                // this callback is only for zoom and pan
                if (event.pointerCount > 1) {
                    if (isCurrentShapeDrawing) isCurrentShapeDrawing = false
                    val touch1 = Vector2(event.getX(0), event.getY(0))
                    val touch2 = Vector2(event.getX(1), event.getY(1))
                    val currentTouch = touch1.minPointVector(touch2)
                    
                    // Pan
                    val deltaChange = currentTouch - previousTouch
                    canvasPosition += deltaChange
                    previousTouch = currentTouch
                    
                    // Zoom
                    val dx = event.getX(0) - event.getX(1)
                    val dy = event.getY(0) - event.getY(1)
                    val currentFingerDist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                    
                    if (previousFingerDist != 0f) {
                        val scaleFactor = currentFingerDist / previousFingerDist
                        
                        val focalX = (event.getX(0) + event.getX(1)) / 2
                        val focalY = (event.getY(0) + event.getY(1)) / 2
                        
                        // Scale around focal point
                        canvasPosition.x = focalX - (focalX - canvasPosition.x) * scaleFactor
                        canvasPosition.y = focalY - (focalY - canvasPosition.y) * scaleFactor
                        
                        canvasScale *= scaleFactor
                    }
                    previousFingerDist = currentFingerDist

                    invalidate()
                    return true
                }

                // this will allow or disallow drawing new shapes
                if (isCurrentShapeDrawing) {

                    when (currentDrawingShape.shapeType) {
                        ShapeType.Brush -> {
                            val ctp = screenToCanvas(previousTouch)
                            val endP = screenToCanvas(touchPos)
                            val newP = Vector2((endP.x + ctp.x) / 2, (endP.y + ctp.y) / 2)
                            (currentDrawingShape as Brush).path.quadTo(ctp.x, ctp.y, newP.x, newP.y)
                            (currentDrawingShape as Brush).addQuadTo(ctp.x, ctp.y, newP.x, newP.y)
                            previousTouch.setValue(touchPos)

                        }
                        ShapeType.Eraser -> {
                            val ctp = screenToCanvas(previousTouch)
                            val endP = screenToCanvas(touchPos)
                            val newP = Vector2((endP.x + ctp.x) / 2, (endP.y + ctp.y) / 2)
                            (currentDrawingShape as Eraser).path.quadTo(
                                ctp.x,
                                ctp.y,
                                newP.x,
                                newP.y
                            )
                            (currentDrawingShape as Eraser).addQuadTo(ctp.x, ctp.y, newP.x, newP.y)
                            previousTouch.setValue(touchPos)

                        }
                        ShapeType.Rectangle -> {
                            (currentDrawingShape as Rectangle).endPos.setValue(
                                screenToCanvas(
                                    touchPos
                                )
                            )

                        }
                        ShapeType.Line -> {
                            (currentDrawingShape as Line).endPos.setValue(screenToCanvas(touchPos))

                        }
                        ShapeType.Oval -> {
                            (currentDrawingShape as Oval).endPos.setValue(screenToCanvas(touchPos))

                        }
                        ShapeType.Triangle -> {
                            (currentDrawingShape as Triangle).endPos.setValue(
                                screenToCanvas(
                                    touchPos
                                )
                            )
                        }
                        ShapeType.Heart -> {
                            (currentDrawingShape as Heart).endPos.setValue(screenToCanvas(touchPos))
                        }
                        ShapeType.Square -> {
                            val startP = (currentDrawingShape as Square).startPos
                            val endP = screenToCanvas(touchPos)
                            val side = Math.max(Math.abs(endP.x - startP.x), Math.abs(endP.y - startP.y))
                            val newEndX = if (endP.x > startP.x) startP.x + side else startP.x - side
                            val newEndY = if (endP.y > startP.y) startP.y + side else startP.y - side
                            (currentDrawingShape as Square).endPos.setValue(newEndX, newEndY)
                        }
                        ShapeType.Circle -> {
                            val startP = (currentDrawingShape as Circle).startPos
                            val endP = screenToCanvas(touchPos)
                            val side = Math.max(Math.abs(endP.x - startP.x), Math.abs(endP.y - startP.y))
                            val newEndX = if (endP.x > startP.x) startP.x + side else startP.x - side
                            val newEndY = if (endP.y > startP.y) startP.y + side else startP.y - side
                            (currentDrawingShape as Circle).endPos.setValue(newEndX, newEndY)
                        }
                        ShapeType.Diamond -> {
                            (currentDrawingShape as Diamond).endPos.setValue(screenToCanvas(touchPos))
                        }
                        ShapeType.Star -> {
                            (currentDrawingShape as Star).endPos.setValue(screenToCanvas(touchPos))
                        }
                        ShapeType.Arrow -> {
                            (currentDrawingShape as Arrow).endPos.setValue(screenToCanvas(touchPos))
                        }
                        ShapeType.Pentagon -> {
                            (currentDrawingShape as Pentagon).endPos.setValue(screenToCanvas(touchPos))
                        }
                    }

                    invalidate()
                    return true
                }
            }

            MotionEvent.ACTION_UP -> {
                if (isCurrentShapeDrawing) {

                    if (isProjectSaved) isProjectSaved = false

                    allShape.add(currentDrawingShape)
                    if (allShapeRedo.isNotEmpty()) allShapeRedo.clear()
                    isCurrentShapeDrawing = false
                    invalidate()
                }
            }

            else -> return false

        }

        return true
    }

    private fun isTouchWithinLimitingArea(): Boolean {
        // Return true to allow drawing anywhere, making the canvas "infinite"
        return true
    }


    fun getSelectedToolProp(selectedTool: ShapeType): ToolProperties? {
        for (tool in toolsData) {
            if (selectedTool == tool.shapeType) {
                return tool
            }
        }

        return null
    }

    private fun screenToCanvas(screenPos: Vector2): Vector2 {
        return (screenPos - canvasPosition) / canvasScale
    }


    private fun isColorTransparent(color: Int): Boolean {
        val alpha = color shr 24 and 0xff
        return alpha == 0
    }

    fun getCanvasBitmap(): Bitmap {
        // Use a default size or calculate based on content if needed
        // For now, let's use a standard 1080p size or something sensible
        val width = 1920
        val height = 1080
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val bitmapCanvas = Canvas(bitmap)

        if (projectSavedBitmap != null) {
            bitmapCanvas.drawBitmap(projectSavedBitmap!!, 0f, 0f, null)
        }
        for (shape in allShape) drawShape(bitmapCanvas, shape)
        return bitmap
    }


    // the below functions will be called from fragment or activity

    fun getToolData(): ArrayList<ToolProperties> {
        return toolsData
    }

    fun setToolData(data: ArrayList<ToolProperties>) {
        toolsData.clear()
        for (prop in data) {
            toolsData.add(prop)
        }
    }

    fun setProjectSavedBitmap(bitmap: Bitmap?) {
        if (bitmap == null) return
        projectSavedBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        invalidate()
    }

    fun updateToolData(tempToolData: ToolProperties) {
        for (index in 0..toolsData.lastIndex) {
            if (tempToolData.shapeType == toolsData[index].shapeType) {

                // updating
                toolsData[index] = tempToolData
                return
            }
        }
    }

    fun setSelectedTool(toolType: ShapeType) {
        currentSelectedTool = toolType
    }

    fun initializeView(width: Int, height: Int) {
        // calculating default translate to center the initial area
        val top = measuredHeight / 2f - height / 2
        val left = measuredWidth / 2f - width / 2
        canvasPosition.setValue(left, top)
        canvasScale = 1f
    }

    fun undo() {
        if (allShape.size == 0) return
        allShapeRedo.add(allShape.removeLast())
    }

    fun redo() {
        if (allShapeRedo.size == 0) return
        allShape.add(allShapeRedo.removeLast())
    }

    fun clearCanvas() {
        allShape.clear()
        allShapeRedo.clear()
        if (projectSavedBitmap != null) {
            projectSavedBitmap!!.eraseColor(Color.TRANSPARENT)
        }
        isProjectSaved = false
        invalidate()
    }

    suspend fun reDraw() {
        invalidate()
    }

    fun setProjectSaved() {
        isProjectSaved = true
    }

    fun isProjectSaved(): Boolean {
        return isProjectSaved
    }
}


