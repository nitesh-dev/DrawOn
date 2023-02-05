package com.flaxstudio.drawon.utils

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


}

data class Size(var width: Int = 0, var height: Int = 0)
data class WhiteBoardData(var pos: Vector2, var zoom: Float, var size: Size)

data class ProjectData(
    var fileName: String,
    var dateTime: String,
    var isFav: Boolean,
    var thumbnailLoc: String,

    )