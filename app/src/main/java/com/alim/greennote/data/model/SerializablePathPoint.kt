package com.alim.greennote.data.model

import com.alim.greennote.ui.views.DrawingView

data class SerializablePathPoint(
    val x: Float,
    val y: Float,
    val isQuadTo: Boolean
) {
    companion object {
        // Extension function to convert DrawingView.PathPoint to SerializablePathPoint
        fun DrawingView.PathPoint.toSerializable(): SerializablePathPoint {
            return SerializablePathPoint(
                x = this.x,
                y = this.y,
                isQuadTo = this.isQuadTo
            )
        }

        // Extension function to convert SerializablePathPoint back to DrawingView.PathPoint
        fun SerializablePathPoint.toPathPoint(): DrawingView.PathPoint {
            return DrawingView.PathPoint(this.x, this.y, this.isQuadTo)
        }
    }
}