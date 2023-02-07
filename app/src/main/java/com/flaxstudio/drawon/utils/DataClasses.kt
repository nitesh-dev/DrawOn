package com.flaxstudio.drawon.utils

import android.graphics.Color
import android.graphics.Path

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

data class ShapeData(
    val startPos: Vector2 = Vector2(),
    val endPos: Vector2 = Vector2(),
    var fillColor: Int? = null,
    var strokeColor: Int? = Color.BLACK,
    val path: Path = Path(),
    var bitmapCode: Int = 0,                     // default value | no bitmap present | 6 digit code
    var shapeType: ShapeType = ShapeType.Rectangle
){
    fun copy(): ShapeData{
        return ShapeData(this.startPos.copy(), this.endPos.copy(), this.fillColor, this.strokeColor, this.path, this.bitmapCode, this.shapeType)
    }
}

data class ProjectData(
    var name: String = "Untitled",
    var isFavourite: Boolean = false,
    var dateTime: String = "",
    var thumbnailCode: Int = 0,              // 0 means no thumbnail present
    val allSavedShapes: ArrayList<ShapeData> = ArrayList(),
    val whiteBoardSize: Size = Size(720, 1280),
    var toolsData: ArrayList<ToolProperties> = ArrayList()

)

data class ToolProperties(
    var shapeType: ShapeType,
    var fillColor: Int,
    var strokeColor: Int,
    var strokeWidth: Float
)

enum class ShapeType{
    Rectangle,
    Oval,
    Line,
    Brush,
    Eraser
}