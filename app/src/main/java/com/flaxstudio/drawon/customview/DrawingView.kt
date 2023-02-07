package com.flaxstudio.drawon.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.drawable.shapes.Shape
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.flaxstudio.drawon.utils.ShapeData
import com.flaxstudio.drawon.utils.ShapeType
import com.flaxstudio.drawon.utils.Vector2
import java.util.Vector

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    // temp data for testing purpose
    private val allShape: ArrayList<ShapeData> = ArrayList()

    private var isCurrentShapeDrawing = false
    private val currentDrawingShape = ShapeData()


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
    }




    private var canvasPosition = Vector2()
    private val whiteBoardRect = Rect(0, 0, 1080, 720)


    override fun onDraw(canvas: Canvas) {

//        canvas.scale(2f, 2f)
        canvas.translate(canvasPosition.x, canvasPosition.y)
        canvas.clipRect(whiteBoardRect)
        canvas.drawRect(whiteBoardRect, whiteBoardPaint)

        for (shape in allShape){
            addShape(canvas, shape)
        }

        if(isCurrentShapeDrawing){
            addShape(canvas, currentDrawingShape)
        }

    }

    private fun addShape(canvas: Canvas, shape: ShapeData){
        when(shape.shapeType){
            ShapeType.Rectangle -> {

                if(shape.fillColor != null){                        // drawing shape fill
                    shapePaint.color = shape.fillColor!!
                    shapePaint.style = Paint.Style.FILL
                    canvas.drawRect(shape.startPos.x, shape.startPos.y, shape.endPos.x, shape.endPos.y, shapePaint)
                }

                if(shape.strokeColor != null){                      // drawing stroke
                    shapePaint.color = shape.strokeColor!!
                    shapePaint.style = Paint.Style.STROKE
                    canvas.drawRect(shape.startPos.x, shape.startPos.y, shape.endPos.x, shape.endPos.y, shapePaint)
                }

            }
            ShapeType.Brush -> {

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



    private var previousTouch = Vector2()

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val touchPos = Vector2(event.x, event.y)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {

                previousTouch = touchPos
                isCurrentShapeDrawing = true
                currentDrawingShape.startPos.setValue(screenToCanvas(touchPos))
                Log.e("======", "down")
//                previousTouch.x = posX
//                previousTouch.y = posY
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
                    currentDrawingShape.endPos.setValue(screenToCanvas(touchPos))
                    invalidate()
                }

            }

            MotionEvent.ACTION_UP -> {
                if(isCurrentShapeDrawing){
                    currentDrawingShape.fillColor = Color.RED

                    allShape.add(currentDrawingShape.copy())
                    isCurrentShapeDrawing = false
                    invalidate()
                }

            }

            else -> return false

        }

        return true
    }


    private fun screenToCanvas(screenPos: Vector2): Vector2{
        return  screenPos - canvasPosition

    }
}

