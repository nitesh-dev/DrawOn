package com.flaxstudio.drawon.utils

import android.graphics.Color
import android.graphics.Path
import androidx.core.graphics.PathParser

data class Vector2(var x: Float = 0f, var y: Float = 0f) {
    operator fun minus(vector: Vector2): Vector2 {
        return Vector2(this.x - vector.x, this.y - vector.y)
    }

    fun minPointVector(vector: Vector2): Vector2{
        return Vector2(this.x - (this.x - vector.x)/2, this.y - (this.y - vector.y)/2)
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

open class Shape{
    var shapeType = ShapeType.Rectangle
    var fillColor: Int = Color.RED
    var strokeColor: Int = Color.BLACK
    var strokeWidth: Float = 2f
}

open class Rectangle: Shape() {
    var startPos = Vector2()
    var endPos = Vector2()

    fun copy(): Rectangle{
        val clone = Rectangle()
        clone.shapeType = shapeType
        clone.fillColor = fillColor
        clone.strokeColor = strokeColor
        clone.strokeWidth = strokeWidth
        clone.startPos = startPos.copy()
        clone.endPos = endPos.copy()
        return clone
    }
}

class Brush: Shape(){
    var path = Path()
    var pathString = "M"

    // M100 100L200 200


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

open class BrushRaw: Shape(){
    var pathString = ""

    fun toBrush(): Brush{
        val brush = Brush()
        brush.path = PathParser.createPathFromPathData(pathString)
        brush.pathString = pathString
        return brush
    }
}



data class ProjectData(
    val allSavedShapes: ArrayList<Shape> = ArrayList(),
    var toolsData: ArrayList<ToolProperties> = ArrayList()
)

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
    Eraser
}