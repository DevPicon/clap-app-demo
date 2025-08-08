package pe.devpicon.android.clapapp.ui.compose

import android.content.res.Configuration
import android.graphics.Paint
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Suppress("MagicNumber", "LongParameterList")
@Composable
fun RangeSlider(
    modifier: Modifier = Modifier,
    rangeStart: Float,
    rangeEnd: Float,
    rangeColor: Color = Color(0xFFFFA500),
    onRangeChange: (Float, Float) -> Unit,
    minimumDistanceBetweenHandles: Float = 27f
) {
    var start by remember { mutableFloatStateOf(rangeStart) }
    var end by remember { mutableFloatStateOf(rangeEnd) }
    var size by remember { mutableStateOf(Offset.Zero) }
    var draggingStart by remember { mutableStateOf(false) }

    val strokeWidth = 16.dp.value
    val strokeThinWidth = 2.dp.value
    val pointRadius = 32.dp.value
    val textPaint = rememberTextPaint(
        textSizeSp = 16.sp, density = Density(LocalDensity.current.density)
    )

    Canvas(modifier = modifier
        .fillMaxWidth()
        .height(56.dp)
        .pointerInput(Unit) {
            detectDragGestures(onDragStart = { offset ->
                val startDistance = abs(offset.x - start)
                val endDistance = abs(offset.x - end)
                draggingStart = startDistance < endDistance
            }, onDrag = { change, dragAmount ->
                change.consume()
                if (draggingStart) {
                    val newStart = (start + dragAmount.x).coerceIn(0f, size.x)
                    start = if (newStart > end - minimumDistanceBetweenHandles) {
                        end - minimumDistanceBetweenHandles
                    } else {
                        newStart
                    }
                } else {
                    val newEnd = (end + dragAmount.x).coerceIn(0f, size.x)
                    end = if (newEnd < start + minimumDistanceBetweenHandles) {
                        start + minimumDistanceBetweenHandles
                    } else {
                        newEnd
                    }
                }

                onRangeChange(min(start, end) / size.x, max(start, end) / size.x)
            })
        }
        .onGloballyPositioned { coordinates ->
            val newSize = Offset(
                coordinates.size.width.toFloat(), coordinates.size.height.toFloat()
            )
            if (size != newSize) {
                size = newSize
                val (initialStart, initialEnd) = calculateInitialPositions(
                    newSize, rangeStart, rangeEnd, minimumDistanceBetweenHandles
                )
                start = initialStart
                end = initialEnd
            }
        }) {
        drawSliderElements(
            start, end, size, strokeWidth, strokeThinWidth, pointRadius, textPaint, rangeColor
        )
    }
}

@Suppress("MagicNumber")
@Composable
private fun rememberTextPaint(textSizeSp: TextUnit, density: Density): Paint {
    return remember {
        Paint().apply {
            isAntiAlias = true
            color = Color(0xFFFFA500).toArgb()
            textSize = with(density) { textSizeSp.toPx() }
        }
    }
}

private fun calculateInitialPositions(
    size: Offset, rangeStart: Float, rangeEnd: Float, minDistance: Float
): Pair<Float, Float> {
    val initialStart = rangeStart * size.x
    var initialEnd = rangeEnd * size.x
    var start = initialStart
    var end = initialEnd

    if (initialEnd - initialStart < minDistance) {
        end = initialStart + minDistance
        if (end > size.x) {
            end = size.x
            start = max(0f, end - minDistance)
        }
    }

    return start to end
}

@Suppress("MagicNumber", "LongParameterList")
private fun DrawScope.drawSliderElements(
    start: Float,
    end: Float,
    size: Offset,
    strokeWidth: Float,
    strokeThinWidth: Float,
    pointRadius: Float,
    textPaint: Paint,
    rangeColor: Color
) {
    drawLine(
        color = Color.Black,
        start = Offset(0f, size.y / 2),
        end = Offset(size.x, size.y / 2),
        strokeWidth = strokeThinWidth
    )

    drawLine(
        color = rangeColor,
        start = Offset(min(start, end), size.y / 2),
        end = Offset(max(start, end), size.y / 2),
        strokeWidth = strokeWidth
    )

    drawCircle(
        color = rangeColor, center = Offset(min(start, end), size.y / 2), radius = pointRadius
    )
    drawCircle(
        color = rangeColor, center = Offset(max(start, end), size.y / 2), radius = pointRadius
    )

    val minValue = (min(start, end) / size.x) * 100
    val maxValue = (max(start, end) / size.x) * 100

    drawIntoCanvas {
        it.nativeCanvas.drawText(
            "%.0f".format(minValue), min(start, end), size.y / 2 - 20.dp.toPx(), textPaint
        )
        it.nativeCanvas.drawText(
            "%.0f".format(maxValue), max(start, end), size.y / 2 - 20.dp.toPx(), textPaint
        )
    }
}


@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun RangeSliderPreview() {
    MaterialTheme {
        // Padding is added for better visualization in the preview
        RangeSlider(
            modifier = Modifier
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
