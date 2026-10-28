package com.learningis4fun.papers.ui.common.icons

/*
Font Awesome Free License
-------------------------

Font Awesome Free is free, open source, and GPL friendly. You can use it for
commercial projects, open source projects, or really almost whatever you want.
Full Font Awesome Free license: https://fontawesome.com/license/free.

# Icons: CC BY 4.0 License (https://creativecommons.org/licenses/by/4.0/)
In the Font Awesome Free download, the CC BY 4.0 license applies to all icons
packaged as SVG and JS file types.

# Fonts: SIL OFL 1.1 License (https://scripts.sil.org/OFL)
In the Font Awesome Free download, the SIL OFL license applies to all icons
packaged as web and desktop font files.

# Code: MIT License (https://opensource.org/licenses/MIT)
In the Font Awesome Free download, the MIT license applies to all non-font and
non-icon files.

# Attribution
Attribution is required by MIT, SIL OFL, and CC BY licenses. Downloaded Font
Awesome Free files already contain embedded comments with sufficient
attribution, so you shouldn't need to do anything additional when using these
files normally.

We've kept attribution comments terse, so we ask that you do not actively work
to remove them from files, especially code. They're a great way for folks to
learn about Font Awesome.

# Brand Icons
All brand icons are trademarks of their respective owners. The use of these
trademarks does not indicate endorsement of the trademark holder by Font
Awesome, nor vice versa. **Please do not use brand logos for any purpose except
to represent the company, product, or service to which they refer.**
*/
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val CheckCircle: ImageVector
    get() {
        if (_FontAwesomeCheckCircle != null) return _FontAwesomeCheckCircle!!

        _FontAwesomeCheckCircle = ImageVector.Builder(
            name = "check-circle",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 512f,
            viewportHeight = 512f
        ).apply {
            path(
                fill = SolidColor(Color.Black)
            ) {
                moveTo(504f, 256f)
                curveToRelative(0f, 136.967f, -111.033f, 248f, -248f, 248f)
                reflectiveCurveTo(8f, 392.967f, 8f, 256f)
                reflectiveCurveTo(119.033f, 8f, 256f, 8f)
                reflectiveCurveToRelative(248f, 111.033f, 248f, 248f)
                close()
                moveTo(227.314f, 387.314f)
                lineToRelative(184f, -184f)
                curveToRelative(6.248f, -6.248f, 6.248f, -16.379f, 0f, -22.627f)
                lineToRelative(-22.627f, -22.627f)
                curveToRelative(-6.248f, -6.249f, -16.379f, -6.249f, -22.628f, 0f)
                lineTo(216f, 308.118f)
                lineToRelative(-70.059f, -70.059f)
                curveToRelative(-6.248f, -6.248f, -16.379f, -6.248f, -22.628f, 0f)
                lineToRelative(-22.627f, 22.627f)
                curveToRelative(-6.248f, 6.248f, -6.248f, 16.379f, 0f, 22.627f)
                lineToRelative(104f, 104f)
                curveToRelative(6.249f, 6.249f, 16.379f, 6.249f, 22.628f, 0.001f)
                close()
            }
        }.build()

        return _FontAwesomeCheckCircle!!
    }

private var _FontAwesomeCheckCircle: ImageVector? = null