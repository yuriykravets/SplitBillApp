package com.partitionsoft.splitbillapp.presentation.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Minimal, crisp 24dp rounded-stroke vector icons to avoid pulling in the heavy
 * material-icons-extended artifact while keeping clean Material 3 visual consistency.
 */
object AppIcons {

    val Add: ImageVector by lazy {
        icon("Add") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
            ) {
                moveTo(12f, 5f)
                lineTo(12f, 19f)
                moveTo(5f, 12f)
                lineTo(19f, 12f)
            }
        }
    }

    val Remove: ImageVector by lazy {
        icon("Remove") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
            ) {
                moveTo(5f, 12f)
                lineTo(19f, 12f)
            }
        }
    }

    val History: ImageVector by lazy {
        icon("History") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(3f, 12f)
                arcTo(9f, 9f, 0f, true, true, 12f, 21f)
                arcTo(9f, 9f, 0f, false, true, 5.6f, 18.4f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(3f, 7f)
                lineTo(3f, 12f)
                lineTo(8f, 12f)
                moveTo(12f, 7f)
                lineTo(12f, 12f)
                lineTo(15.5f, 14f)
            }
        }
    }

    val Share: ImageVector by lazy {
        icon("Share") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(12f, 3f)
                lineTo(12f, 15f)
                moveTo(8f, 7f)
                lineTo(12f, 3f)
                lineTo(16f, 7f)
                moveTo(5f, 12f)
                lineTo(5f, 19f)
                arcTo(2f, 2f, 0f, false, false, 7f, 21f)
                lineTo(17f, 21f)
                arcTo(2f, 2f, 0f, false, false, 19f, 19f)
                lineTo(19f, 12f)
            }
        }
    }

    val BookmarkCheck: ImageVector by lazy {
        icon("BookmarkCheck") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(6f, 4f)
                lineTo(18f, 4f)
                arcTo(1f, 1f, 0f, false, true, 19f, 5f)
                lineTo(19f, 20f)
                lineTo(12f, 16f)
                lineTo(5f, 20f)
                lineTo(5f, 5f)
                arcTo(1f, 1f, 0f, false, true, 6f, 4f)
                close()
            }
        }
    }

    val ArrowBack: ImageVector by lazy {
        icon("ArrowBack") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(19f, 12f)
                lineTo(5f, 12f)
                moveTo(11f, 6f)
                lineTo(5f, 12f)
                lineTo(11f, 18f)
            }
        }
    }

    val Close: ImageVector by lazy {
        icon("Close") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
            ) {
                moveTo(6f, 6f)
                lineTo(18f, 18f)
                moveTo(18f, 6f)
                lineTo(6f, 18f)
            }
        }
    }

    val Delete: ImageVector by lazy {
        icon("Delete") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(4f, 7f)
                lineTo(20f, 7f)
                moveTo(9f, 7f)
                lineTo(9f, 4f)
                lineTo(15f, 4f)
                lineTo(15f, 7f)
                moveTo(6f, 7f)
                lineTo(7f, 19f)
                arcTo(2f, 2f, 0f, false, false, 9f, 21f)
                lineTo(15f, 21f)
                arcTo(2f, 2f, 0f, false, false, 17f, 19f)
                lineTo(18f, 7f)
                moveTo(10f, 11f)
                lineTo(10f, 17f)
                moveTo(14f, 11f)
                lineTo(14f, 17f)
            }
        }
    }

    val Check: ImageVector by lazy {
        icon("Check") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(5f, 12.5f)
                lineTo(9.5f, 17f)
                lineTo(19f, 7.5f)
            }
        }
    }

    val Refresh: ImageVector by lazy {
        icon("Refresh") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(20f, 12f)
                arcTo(8f, 8f, 0f, true, true, 17.6f, 6.3f)
                moveTo(20f, 4f)
                lineTo(20f, 9f)
                lineTo(15f, 9f)
            }
        }
    }

    val ChevronDown: ImageVector by lazy {
        icon("ChevronDown") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(6f, 9f)
                lineTo(12f, 15f)
                lineTo(18f, 9f)
            }
        }
    }

    val Edit: ImageVector by lazy {
        icon("Edit") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(4f, 20f)
                lineTo(8.5f, 19f)
                lineTo(19f, 8.5f)
                lineTo(15.5f, 5f)
                lineTo(5f, 15.5f)
                close()
            }
        }
    }

    val Settings: ImageVector by lazy {
        icon("Settings") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                // Center circle
                moveTo(15f, 12f)
                arcTo(3f, 3f, 0f, true, true, 9f, 12f)
                arcTo(3f, 3f, 0f, true, true, 15f, 12f)
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(19.4f, 15f)
                arcTo(1.65f, 1.65f, 0f, false, false, 19.73f, 16.82f)
                lineTo(19.79f, 16.88f)
                arcTo(2f, 2f, 0f, false, true, 16.96f, 19.71f)
                lineTo(16.9f, 19.65f)
                arcTo(1.65f, 1.65f, 0f, false, false, 15.08f, 19.32f)
                arcTo(1.65f, 1.65f, 0f, false, false, 14.08f, 20.83f)
                lineTo(14.08f, 21f)
                arcTo(2f, 2f, 0f, false, true, 10.08f, 21f)
                lineTo(10.08f, 20.91f)
                arcTo(1.65f, 1.65f, 0f, false, false, 9f, 19.4f)
                arcTo(1.65f, 1.65f, 0f, false, false, 7.18f, 19.73f)
                lineTo(7.12f, 19.79f)
                arcTo(2f, 2f, 0f, false, true, 4.29f, 16.96f)
                lineTo(4.35f, 16.9f)
                arcTo(1.65f, 1.65f, 0f, false, false, 4.68f, 15.08f)
                arcTo(1.65f, 1.65f, 0f, false, false, 3.17f, 14.08f)
                lineTo(3f, 14.08f)
                arcTo(2f, 2f, 0f, false, true, 3f, 10.08f)
                lineTo(3.09f, 10.08f)
                arcTo(1.65f, 1.65f, 0f, false, false, 4.6f, 9f)
                arcTo(1.65f, 1.65f, 0f, false, false, 4.27f, 7.18f)
                lineTo(4.21f, 7.12f)
                arcTo(2f, 2f, 0f, false, true, 7.04f, 4.29f)
                lineTo(7.1f, 4.35f)
                arcTo(1.65f, 1.65f, 0f, false, false, 8.92f, 4.68f)
                lineTo(9f, 4.68f)
                arcTo(1.65f, 1.65f, 0f, false, false, 10f, 3.17f)
                lineTo(10f, 3f)
                arcTo(2f, 2f, 0f, false, true, 14f, 3f)
                lineTo(14f, 3.09f)
                arcTo(1.65f, 1.65f, 0f, false, false, 15f, 4.6f)
                arcTo(1.65f, 1.65f, 0f, false, false, 16.82f, 4.27f)
                lineTo(16.88f, 4.21f)
                arcTo(2f, 2f, 0f, false, true, 19.71f, 7.04f)
                lineTo(19.65f, 7.1f)
                arcTo(1.65f, 1.65f, 0f, false, false, 19.32f, 8.92f)
                lineTo(19.32f, 9f)
                arcTo(1.65f, 1.65f, 0f, false, false, 20.83f, 10f)
                lineTo(21f, 10f)
                arcTo(2f, 2f, 0f, false, true, 21f, 14f)
                lineTo(20.91f, 14f)
                arcTo(1.65f, 1.65f, 0f, false, false, 19.4f, 15f)
                close()
            }
        }
    }

    private inline fun icon(name: String, block: ImageVector.Builder.() -> Unit): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply(block).build()
}
