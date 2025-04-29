package com.alim.greennote.data.model

import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import com.alim.greennote.data.model.SerializablePathPoint.Companion.toPathPoint
import com.alim.greennote.data.model.SerializablePathPoint.Companion.toSerializable
import com.alim.greennote.ui.views.DrawingView

/**
 * Serializable version of the DrawingAction class for storing in Room database
 */
data class SerializableDrawingAction(
    val color: Int,
    val strokeWidth: Float,
    val isErase: Boolean,
    val points: List<SerializablePathPoint>
) {
    companion object {
        // Extension function to convert DrawingView.DrawingAction to SerializableDrawingAction
        fun DrawingView.DrawingAction.toSerializable(): SerializableDrawingAction {
            return SerializableDrawingAction(
                color = paint.color,
                strokeWidth = paint.strokeWidth,
                isErase = isErase,
                points = points.map { it.toSerializable() }
            )
        }

        // Extension function to convert SerializableDrawingAction back to DrawingView.DrawingAction
        fun SerializableDrawingAction.toDrawingAction(): DrawingView.DrawingAction {
            // Create paint object with the stored properties
            val paint = Paint().apply {
                color = this@toDrawingAction.color
                strokeWidth = this@toDrawingAction.strokeWidth
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
                if (isErase) {
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                }
            }

            // Convert the points back
            val pathPoints = points.map { it.toPathPoint() }

            // Create path from points
            val path = Path()
            if (pathPoints.isNotEmpty()) {
                val firstPoint = pathPoints[0]
                path.moveTo(firstPoint.x, firstPoint.y)

                for (i in 1 until pathPoints.size) {
                    val point = pathPoints[i]
                    if (point.isQuadTo && i > 1) {
                        path.quadTo(
                            pathPoints[i-1].x,
                            pathPoints[i-1].y,
                            point.x,
                            point.y
                        )
                    } else {
                        path.lineTo(point.x, point.y)
                    }
                }
            }

            return DrawingView.DrawingAction(path, paint, isErase).apply {
//                this.points = pathPoints
            }
        }
    }
}