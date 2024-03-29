package com.flaxstudio.drawon.utils

import android.graphics.Color
import android.graphics.Path
import android.util.Log
import androidx.core.graphics.PathParser

data class Vector2(var x: Float = 0f, var y: Float = 0f) {
    operator fun minus(vector: Vector2): Vector2 {
        return Vector2(this.x - vector.x, this.y - vector.y)
    }

    fun minPointVector(vector: Vector2): Vector2{
        return Vector2(this.x - (this.x - vector.x)/2, this.y - (this.y - vector.y)/2)
    }

    fun addOffset(x: Float, y: Float){
        this.x += x
        this.y += y
    }

    operator fun plus(vector: Vector2): Vector2 {
        return Vector2(this.x + vector.x, this.y + vector.y)
    }

    fun setValue(vector: Vector2){
        this.x = vector.x
        this.y = vector.y
    }

    fun setValue(x: Float, y:Float){
        this.x = x
        this.y = y
    }
    fun copy(): Vector2{
        return Vector2(this.x, this.y)
    }


}

data class Size(var width: Int = 0, var height: Int = 0)






// -------------------- canvas shape ---------------------------
open class Shape{
    var shapeType = ShapeType.Rectangle
    var fillColor: Int = Color.RED
    var strokeColor: Int = Color.BLACK
    var strokeWidth: Float = 2f
}

class Rectangle: Shape() {
    var startPos = Vector2()
    var endPos = Vector2()
    init {
        shapeType = ShapeType.Rectangle
    }
}


class Line: Shape() {
    var startPos = Vector2()
    var endPos = Vector2()
    init {
        shapeType = ShapeType.Line
    }
}

class Oval: Shape() {
    var startPos = Vector2()
    var endPos = Vector2()
    init {
        shapeType = ShapeType.Oval
    }
}

class Triangle: Shape() {
    var startPos = Vector2()
    var endPos = Vector2()
    init {
        shapeType = ShapeType.Triangle
    }
}

class Heart: Shape() {
    var startPos = Vector2()
    var endPos = Vector2()
    init {
        shapeType = ShapeType.Heart
    }
}



class Brush: Shape(){
    var path = Path()
    var pathString = "M"
    init {
        shapeType = ShapeType.Brush
    }

    fun addMoveTo(startX: Float, startY: Float){
        pathString += "$startX,$startY"

    }
    fun addQuadTo(cx: Float, cy: Float, endX: Float, endY: Float){
        pathString += " Q$cx,$cy $endX,$endY"
    }

    fun toBrushRaw(): BrushRaw{
        val brushRaw = BrushRaw()
        brushRaw.pathString = pathString
        return brushRaw
    }
}

class BrushRaw: Shape(){
    var pathString = ""
    init {
        shapeType = ShapeType.Brush
    }

    fun toBrush(): Brush{
        val brush = Brush()
        brush.path = PathParser.createPathFromPathData(pathString)
        brush.pathString = pathString
        return brush
    }
}


class Eraser: Shape(){
    var path = Path()
    var pathString = "M"
    init {
        shapeType = ShapeType.Eraser
        strokeColor = Color.WHITE
    }

    fun addMoveTo(startX: Float, startY: Float){
        pathString += "$startX,$startY"

    }
    fun addQuadTo(cx: Float, cy: Float, endX: Float, endY: Float){
        pathString += " Q$cx,$cy $endX,$endY"
    }

    fun toEraserRaw(): EraserRaw{
        val eraserRaw = EraserRaw()
        eraserRaw.pathString = pathString
        eraserRaw.strokeWidth = strokeWidth
        return eraserRaw
    }
}

class EraserRaw: Shape(){
    var pathString = ""
    init {
        shapeType = ShapeType.Eraser
        strokeColor = Color.WHITE
    }

    fun toEraser(): Eraser{
        val eraser = Eraser()
        eraser.path = PathParser.createPathFromPathData(pathString)
        eraser.strokeWidth = strokeWidth
        eraser.strokeColor = strokeColor
        eraser.pathString = pathString
        return eraser
    }
}




data class ToolProperties(
    var shapeType: ShapeType = ShapeType.Rectangle,
    var fillColor: Int = Color.RED,
    var strokeColor: Int = Color.BLACK,
    var strokeWidth: Float = 4f
)

enum class ShapeType{
    Rectangle,
    Oval,
    Line,
    Brush,
    Eraser,
    Triangle,
    Heart
}

enum class FragmentType{
    All,
    Today,
    Week,
    Month
}