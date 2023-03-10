package com.flaxstudio.drawon.customview

import android.content.Context
import android.graphics.*
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.flaxstudio.drawon.utils.*
import java.lang.Float.min

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val allShape: ArrayList<Shape> = ArrayList()
    private val toolsData: ArrayList<ToolProperties> = ArrayList()
    private val tempPath = Path()                   // this path is used by triangle

    private var isCurrentShapeDrawing = false
    private var currentDrawingShape = Shape()
    private var currentSelectedTool = ShapeType.Rectangle

    private var canvasPosition = Vector2()
    private val whiteBoardRect = Rect(0, 0, 1080, 720)
    private val canvasSize = Size()

    private var previousTouch = Vector2()


    private var whiteBoardPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private var shapePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth = 4f
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.translate(canvasPosition.x, canvasPosition.y)
        canvas.clipRect(whiteBoardRect)
        canvas.drawRect(whiteBoardRect, whiteBoardPaint)

        for (shape in allShape){
            drawShape(canvas, shape)
        }

        if(isCurrentShapeDrawing){
            drawShape(canvas, currentDrawingShape)
        }


        // this is only used to find the canvas size, it will only called 1 time
        if(canvasSize.width == 0){
            canvasSize.width = this.width
            canvasSize.height = this.height
        }

    }

    private fun drawShape(canvas: Canvas, shape: Shape){

        when(shape.shapeType){

            ShapeType.Rectangle -> {
                shape as Rectangle

                // drawing fill shape if color is not transparent
                if(!isColorTransparent(shape.fillColor)) {
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
                if(!isColorTransparent(shape.strokeColor)){
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
                if(!isColorTransparent(shape.strokeColor)){

                    shapePaint.color = shape.strokeColor
                    shapePaint.strokeWidth = shape.strokeWidth
                    shapePaint.style = Paint.Style.STROKE
                    canvas.drawPath(shape.path, shapePaint)
                }
            }

            ShapeType.Eraser -> {
                shape as Eraser
                // drawing stroke if color is not transparent

                if(!isColorTransparent(shape.strokeColor)){

                    shapePaint.color = shape.strokeColor
                    shapePaint.strokeWidth = shape.strokeWidth
                    shapePaint.style = Paint.Style.STROKE
                    canvas.drawPath(shape.path, shapePaint)
                }

            }

            ShapeType.Line -> {
                shape as Line
                // drawing stroke if color is not transparent
                if(!isColorTransparent(shape.strokeColor)){

                    shapePaint.color = shape.strokeColor
                    shapePaint.strokeWidth = shape.strokeWidth
                    shapePaint.style = Paint.Style.STROKE
                    canvas.drawLine(shape.startPos.x, shape.startPos.y, shape.endPos.x, shape.endPos.y, shapePaint)
                }
            }

            ShapeType.Oval -> {
                shape as Oval

                // drawing fill shape if color is not transparent
                if(!isColorTransparent(shape.fillColor)) {
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
                if(!isColorTransparent(shape.strokeColor)){

                    shapePaint.color = shape.strokeColor
                    shapePaint.strokeWidth = shape.strokeWidth
                    shapePaint.style = Paint.Style.STROKE
                    canvas.drawOval(shape.startPos.x, shape.startPos.y, shape.endPos.x, shape.endPos.y, shapePaint)
                }

            }

            ShapeType.Triangle -> {
                shape as Triangle

                // Note:   (shape.endPos.x - shape.startPos.x) / 2 + shape.startPos.x is used to
                //         calculate mid-x point in rect

                tempPath.reset()        // clear path for drawing
                tempPath.moveTo((shape.endPos.x - shape.startPos.x) / 2 + shape.startPos.x, shape.startPos.y)
                tempPath.lineTo(shape.endPos.x, shape.endPos.y)
                tempPath.lineTo(shape.startPos.x, shape.endPos.y)
                tempPath.close()

                // drawing fill if color is not transparent
                if(!isColorTransparent(shape.fillColor)){

                    shapePaint.color = shape.fillColor
                    shapePaint.style = Paint.Style.FILL
                    canvas.drawPath(tempPath, shapePaint)
                }

                // drawing stroke if color is not transparent
                if(!isColorTransparent(shape.strokeColor)){

                    shapePaint.color = shape.strokeColor
                    shapePaint.strokeWidth = shape.strokeWidth
                    shapePaint.style = Paint.Style.STROKE
                    canvas.drawPath(tempPath, shapePaint)
                }

            }

            else -> {}
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val touchPos = Vector2(event.x, event.y)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {

                val selectedTool = getSelectedToolProp(currentSelectedTool)

                if(selectedTool != null){

                    previousTouch = touchPos
                    isCurrentShapeDrawing = true

                    if(currentSelectedTool == ShapeType.Brush){
                        currentDrawingShape = Brush()
                        currentDrawingShape.strokeColor = selectedTool.strokeColor
                        currentDrawingShape.strokeWidth = selectedTool.strokeWidth

                        val pos = screenToCanvas(touchPos)
                        (currentDrawingShape as Brush).path.moveTo(pos.x, pos.y)
                        (currentDrawingShape as Brush).addMoveTo(pos.x, pos.y)

                    }else if(currentSelectedTool == ShapeType.Eraser){
                        currentDrawingShape = Eraser()
                        currentDrawingShape.strokeColor = selectedTool.strokeColor
                        currentDrawingShape.strokeWidth = selectedTool.strokeWidth

                        val pos = screenToCanvas(touchPos)
                        (currentDrawingShape as Eraser).path.moveTo(pos.x, pos.y)
                        (currentDrawingShape as Eraser).addMoveTo(pos.x, pos.y)

                    } else if(currentSelectedTool == ShapeType.Rectangle){
                        currentDrawingShape = Rectangle()
                        currentDrawingShape.fillColor = selectedTool.fillColor
                        currentDrawingShape.strokeColor = selectedTool.strokeColor
                        currentDrawingShape.strokeWidth = selectedTool.strokeWidth
                        (currentDrawingShape as Rectangle).startPos.setValue(screenToCanvas(touchPos))

                    }else if(currentSelectedTool == ShapeType.Line){
                        currentDrawingShape = Line()
                        currentDrawingShape.strokeColor = selectedTool.strokeColor
                        currentDrawingShape.strokeWidth = selectedTool.strokeWidth
                        (currentDrawingShape as Line).startPos.setValue(screenToCanvas(touchPos))

                    }else if(currentSelectedTool == ShapeType.Oval){
                        currentDrawingShape = Oval()
                        currentDrawingShape.fillColor = selectedTool.fillColor
                        currentDrawingShape.strokeColor = selectedTool.strokeColor
                        currentDrawingShape.strokeWidth = selectedTool.strokeWidth
                        (currentDrawingShape as Oval).startPos.setValue(screenToCanvas(touchPos))

                    }else if(currentSelectedTool == ShapeType.Triangle){
                        currentDrawingShape = Triangle()
                        currentDrawingShape.fillColor = selectedTool.fillColor
                        currentDrawingShape.strokeColor = selectedTool.strokeColor
                        currentDrawingShape.strokeWidth = selectedTool.strokeWidth
                        (currentDrawingShape as Triangle).startPos.setValue(screenToCanvas(touchPos))
                    }

                }else{
                    return false
                }
            }

            MotionEvent.ACTION_POINTER_DOWN -> {

                // this callback is only for zoom and pan
                if(event.pointerCount > 1){
                    if(isCurrentShapeDrawing) isCurrentShapeDrawing = false
                    val touch1 = Vector2(event.getX(0), event.getY(0))
                    val touch2 = Vector2(event.getX(1), event.getY(1))
                    previousTouch = touch1.minPointVector(touch2)
                    invalidate()

                }
            }

            MotionEvent.ACTION_MOVE -> {

                // this callback is only for zoom and pan
                if(event.pointerCount > 1){
                    if(isCurrentShapeDrawing) isCurrentShapeDrawing = false
                    val touch1 = Vector2(event.getX(0), event.getY(0))
                    val touch2 = Vector2(event.getX(1), event.getY(1))
                    val currentTouch = touch1.minPointVector(touch2)
                    val deltaChange = currentTouch - previousTouch
                    canvasPosition += deltaChange
                    previousTouch = currentTouch

                    invalidate()
                    return true
                }

                if(isCurrentShapeDrawing){

                    if(currentDrawingShape.shapeType == ShapeType.Brush){
                        val ctp = screenToCanvas(previousTouch)
                        val endP = screenToCanvas(touchPos)
                        val newP = Vector2((endP.x + ctp.x) / 2, (endP.y + ctp.y) / 2)
                        (currentDrawingShape as Brush).path.quadTo(ctp.x, ctp.y, newP.x, newP.y)
                        (currentDrawingShape as Brush).addQuadTo(ctp.x, ctp.y, newP.x, newP.y)
                        previousTouch.setValue(touchPos)

                    }else if(currentDrawingShape.shapeType == ShapeType.Eraser){
                        val ctp = screenToCanvas(previousTouch)
                        val endP = screenToCanvas(touchPos)
                        val newP = Vector2((endP.x + ctp.x) / 2, (endP.y + ctp.y) / 2)
                        (currentDrawingShape as Eraser).path.quadTo(ctp.x, ctp.y, newP.x, newP.y)
                        (currentDrawingShape as Eraser).addQuadTo(ctp.x, ctp.y, newP.x, newP.y)
                        previousTouch.setValue(touchPos)

                    } else if(currentDrawingShape.shapeType == ShapeType.Rectangle){
                        (currentDrawingShape as Rectangle).endPos.setValue(screenToCanvas(touchPos))

                    }else if(currentDrawingShape.shapeType == ShapeType.Line){
                        (currentDrawingShape as Line).endPos.setValue(screenToCanvas(touchPos))

                    }else if(currentDrawingShape.shapeType == ShapeType.Oval){
                        (currentDrawingShape as Oval).endPos.setValue(screenToCanvas(touchPos))

                    }else if(currentDrawingShape.shapeType == ShapeType.Triangle){
                        (currentDrawingShape as Triangle).endPos.setValue(screenToCanvas(touchPos))

                    }

                    invalidate()
                }

            }

            MotionEvent.ACTION_UP -> {
                if(isCurrentShapeDrawing){

                    allShape.add(currentDrawingShape)

                    isCurrentShapeDrawing = false
                    invalidate()
                }
            }

            else -> return false

        }

        return true
    }


    private fun getSelectedToolProp(selectedTool: ShapeType): ToolProperties?{
        for (tool in toolsData){
            if(selectedTool == tool.shapeType){
                return tool
            }
        }

        return null
    }

    private fun screenToCanvas(screenPos: Vector2): Vector2{
        return  screenPos - canvasPosition

    }

    private fun bitmapToThumbnail(originalBitmap: Bitmap, maxSize: Int): Bitmap {

        val ratio = min(width.toFloat() / maxSize, height.toFloat() / maxSize)

        val thumbnailWidth = (width / ratio).toInt()
        val thumbnailHeight = (height / ratio).toInt()

        // returning thumbnail
        return Bitmap.createScaledBitmap(originalBitmap, thumbnailWidth, thumbnailHeight, false)
    }

    private fun isColorTransparent(color: Int): Boolean {
        val alpha = color shr 24 and 0xff
        return alpha == 0
    }





    // the below functions will be called from fragment or activity

    fun setDrawnShapes(shapeData: Shape){
        allShape.add(shapeData)
    }

    fun getDrawnShapes(): ArrayList<Shape>{
        val tempShapes = ArrayList<Shape>()

        for (shape in allShape){
            if(shape.shapeType == ShapeType.Brush){
                shape as Brush
                tempShapes.add(shape.toBrushRaw())
            }else{
                tempShapes.add(shape)
            }
        }
        return allShape
    }

    fun getToolData(): ArrayList<ToolProperties>{
        return toolsData
    }

    fun setToolData(data: ArrayList<ToolProperties>){
        toolsData.clear()
        for (prop in data){
            toolsData.add(prop)
        }
    }

    fun setSelectedTool(toolType: ShapeType){
        currentSelectedTool = toolType
    }

    fun getThumbnail(): Bitmap {
        val bitmap = Bitmap.createBitmap(canvasSize.width, canvasSize.height, Bitmap.Config.ARGB_8888)
        val bitmapCanvas = Canvas(bitmap)

        bitmapCanvas.clipRect(whiteBoardRect)
        bitmapCanvas.drawRect(whiteBoardRect, whiteBoardPaint)
        for (shape in allShape) drawShape(bitmapCanvas, shape)

        return bitmapToThumbnail(bitmap, 512)
    }

}


