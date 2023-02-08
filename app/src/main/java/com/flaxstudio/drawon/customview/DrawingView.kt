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
    init {
        toolsData.add(ToolProperties())

        val t = ToolProperties()
        t.shapeType = ShapeType.Brush
        t.strokeWidth = 4f
        toolsData.add(t)
    }

    private var isCurrentShapeDrawing = false
    private var currentDrawingShape = Shape()
    private var currentSelectedTool = ShapeType.Brush

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
                Log.e("hhh", "dd")
                if(!isColorTransparent(shape.strokeColor)){

                    shapePaint.color = shape.strokeColor
                    shapePaint.strokeWidth = shape.strokeWidth
                    shapePaint.style = Paint.Style.STROKE
                    canvas.drawPath(shape.path, shapePaint)
                }
            }

            ShapeType.Eraser -> {

            }

            ShapeType.Line -> {

            }

            ShapeType.Oval -> {

            }

            else -> {}
        }
    }


//    private fun actionDown(x: Float, y: Float) {
//        mPath.reset()
//        mPath.moveTo(x, y)
//        mCurX = x
//        mCurY = y
//    }
//
//    private fun actionMove(x: Float, y: Float) {
//        mPath.quadTo(mCurX, mCurY, (x + mCurX) / 2, (y + mCurY) / 2)
//        mCurX = x
//        mCurY = y
//    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val touchPos = Vector2(event.x, event.y)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {

                val selectedTool = getSelectedTool(currentSelectedTool)

                if(selectedTool != null){

                    previousTouch = touchPos
                    isCurrentShapeDrawing = true

                    if(currentSelectedTool == ShapeType.Brush){
                        currentDrawingShape = Brush()
                        currentDrawingShape.fillColor = selectedTool.fillColor
                        currentDrawingShape.strokeColor = selectedTool.strokeColor
                        currentDrawingShape.strokeWidth = selectedTool.strokeWidth

                        val pos = screenToCanvas(touchPos)
                        (currentDrawingShape as Brush).path.moveTo(pos.x, pos.y)
                        (currentDrawingShape as Brush).addMoveTo(pos.x, pos.y)

                    }else if(currentSelectedTool == ShapeType.Rectangle){
                        currentDrawingShape = Rectangle()
                        currentDrawingShape.fillColor = selectedTool.fillColor
                        currentDrawingShape.strokeColor = selectedTool.strokeColor
                        currentDrawingShape.strokeWidth = selectedTool.strokeWidth

                        (currentDrawingShape as Rectangle).startPos.setValue(screenToCanvas(touchPos))
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
                    }else if(currentDrawingShape.shapeType == ShapeType.Rectangle){

                        (currentDrawingShape as Rectangle).endPos.setValue(screenToCanvas(touchPos))
                    }

                    invalidate()
                }

            }

            MotionEvent.ACTION_UP -> {
                if(isCurrentShapeDrawing){
                    currentDrawingShape.fillColor = Color.RED

                    if(currentDrawingShape.shapeType == ShapeType.Brush){
                        allShape.add(currentDrawingShape)

                    }else if(currentDrawingShape.shapeType == ShapeType.Rectangle){
                        allShape.add(currentDrawingShape)
                    }

                    isCurrentShapeDrawing = false
                    invalidate()
                }

            }

            else -> return false

        }

        return true
    }


    private fun getSelectedTool(selectedTool: ShapeType): ToolProperties?{
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

    fun getThumbnail(): Bitmap {
        val bitmap = Bitmap.createBitmap(canvasSize.width, canvasSize.height, Bitmap.Config.ARGB_8888)
        val bitmapCanvas = Canvas(bitmap)

        bitmapCanvas.clipRect(whiteBoardRect)
        bitmapCanvas.drawRect(whiteBoardRect, whiteBoardPaint)
        for (shape in allShape) drawShape(bitmapCanvas, shape)

        return bitmapToThumbnail(bitmap, 256)
    }

}


