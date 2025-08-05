package pe.devpicon.android.clapapp.ui.compose

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun RangeSlider(
    modifier: Modifier = Modifier,
    rangeStart: Float,
    rangeEnd: Float,
    rangeColor: Color = Color(0xFFFFA500), // Orange color
    onRangeChange: (Float, Float) -> Unit,
    minimumDistanceBetweenHandles: Float = 27f // <<--- here is the problem
) {
    // We declare the start and end of the range as mutable state
    var start by remember { mutableFloatStateOf(rangeStart) }
    var end by remember { mutableFloatStateOf(rangeEnd) }
    // We declare the width of the lines and points
    val strokeWidth = 16.dp.value
    val strokeThinWidth = 2.dp.value
    val pointRadius = 32.dp.value
    // We declare the size of the canvas
    var size by remember { mutableStateOf(Offset.Zero) }
    // We declare a boolean to determine if the user is dragging the start or end point
    var draggingStart by remember { mutableStateOf(false) }
    // We get the density of the screen
    val density = LocalDensity.current

    // We declare the paint for the text
    val textSizeSp = 16.sp
    val textPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = Color(0xFFFFA500).toArgb() // Use Color.Black or any other color you prefer
        // Convert Sp to Px using LocalDensity
        textSize = with(density) { textSizeSp.toPx() }
    }
    // Declare and initialize hasBeenDragged
    var hasBeenDragged by remember { mutableStateOf(false) }


    Canvas(modifier = modifier
        .fillMaxWidth()
        .height(56.dp)
        .pointerInput(Unit) {
            detectDragGestures(onDragStart = { offset ->
                // Remember initial positions at the start of the drag
                val initialStart = start
                val initialEnd = end

                // Determine which handle is being dragged based on proximity
                val startDistance = abs(offset.x - start)
                val endDistance = abs(offset.x - end)
                draggingStart = startDistance < endDistance
            }, onDrag = { change, dragAmount ->
                change.consume()
                // Temporary variables to calculate potential new positions
                val newStart: Float
                val newEnd: Float

                if (draggingStart) {
                    newStart = (start + dragAmount.x).coerceIn(0f, size.x)
                    // Check if the new position violates the minimum distance constraint
                    start = if (newStart > end - minimumDistanceBetweenHandles) {
                        // Option 1: Revert to initial position
                        // start = initialStart

                        // Option 2: Stick at the minimum allowable distance
                        end - minimumDistanceBetweenHandles
                    } else {
                        newStart
                    }
                } else {
                    newEnd = (end + dragAmount.x).coerceIn(0f, size.x)
                    // Check if the new position violates the minimum distance constraint
                    end = if (newEnd < start + minimumDistanceBetweenHandles) {
                        // Option 1: Revert to initial position
                        // end = initialEnd

                        // Option 2: Stick at the minimum allowable distance
                        start + minimumDistanceBetweenHandles
                    } else {
                        newEnd
                    }
                }

                onRangeChange(min(start, end) / size.x, max(start, end) / size.x)
            })

        }
        .onGloballyPositioned { coordinates ->
            // Update the size of the drawable area
            val newSize = Offset(coordinates.size.width.toFloat(), coordinates.size.height.toFloat())
            if (size != newSize) {
                size = newSize

                // Initialize or re-initialize start and end positions based on rangeStart and rangeEnd
                // This calculation converts the provided range values (as fractions of the total width) into actual pixel positions
                val initialStart = rangeStart * size.x
                val initialEnd = rangeEnd * size.x

                // Check to ensure the initial positions respect the minimum distance constraint
                if (initialEnd - initialStart < minimumDistanceBetweenHandles) {
                    // Adjust initialEnd or initialStart as needed to enforce the minimum distance
                    // This example adjusts initialEnd, but you could adjust initialStart instead, depending on your requirements
                    end = initialStart + minimumDistanceBetweenHandles

                    // Ensure 'end' does not exceed the drawable area's bounds
                    if (end > size.x) {
                        end = size.x
                        start = max(0f, end - minimumDistanceBetweenHandles)
                    } else {
                        start = initialStart
                    }
                } else {
                    // If the initial positions already respect the minimum distance constraint, use them directly
                    start = initialStart
                    end = initialEnd
                }
            }
        }
    ) {
        // Draw the base line
        drawLine(
            color = Color.Black,
            start = Offset(0f, size.y / 2),
            end = Offset(size.x, size.y / 2),
            strokeWidth = strokeThinWidth
        )

        // Draw the selected range line
        drawLine(
            color = rangeColor,
            start = Offset(min(start, end), size.y / 2),
            end = Offset(max(start, end), size.y / 2),
            strokeWidth = strokeWidth
        )

        // Draw the draggable points
        drawCircle(
            color = rangeColor,
            center = Offset(min(start, end), size.y / 2),
            radius = pointRadius
        )
        drawCircle(
            color = rangeColor,
            center = Offset(max(start, end), size.y / 2),
            radius = pointRadius
        )

        // Calculate the values to display
        val minValue = (min(start, end) / size.x) * 100
        val maxValue = (max(start, end) / size.x) * 100

        // Draw the labels
        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawText(
                "%.0f".format(minValue),
                min(start, end),
                size.y / 2 - 20.dp.toPx(), // Adjust the position as needed
                textPaint
            )
            canvas.nativeCanvas.drawText(
                "%.0f".format(maxValue),
                max(start, end),
                size.y / 2 - 20.dp.toPx(), // Adjust the position as needed
                textPaint
            )
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun RangeSliderPreview() {
    MaterialTheme {
        // Padding is added for better visualization in the preview
        RangeSlider(modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
            rangeStart = 0.2f,  // Starting point of the range as a fraction of the total width
            rangeEnd = 0.8f,    // Ending point of the range as a fraction of the total width
            onRangeChange = { _, _ ->
                // This is a dummy handler for the preview.
                // In actual use, you would update the state with these values.
            })
    }
}
