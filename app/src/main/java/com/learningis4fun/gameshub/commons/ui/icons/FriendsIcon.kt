package com.learningis4fun.gameshub.commons.ui.icons

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

val FriendsIcon: ImageVector
    get() {
        if (_FontAwesomeUserFriends != null) return _FontAwesomeUserFriends!!

        _FontAwesomeUserFriends = ImageVector.Builder(
            name = "user-friends",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 640f,
            viewportHeight = 512f
        ).apply {
            path(
                fill = SolidColor(Color.Black)
            ) {
                moveTo(192f, 256f)
                curveToRelative(61.9f, 0f, 112f, -50.1f, 112f, -112f)
                reflectiveCurveTo(253.9f, 32f, 192f, 32f)
                reflectiveCurveTo(80f, 82.1f, 80f, 144f)
                reflectiveCurveToRelative(50.1f, 112f, 112f, 112f)
                close()
                moveToRelative(76.8f, 32f)
                horizontalLineToRelative(-8.3f)
                curveToRelative(-20.8f, 10f, -43.9f, 16f, -68.5f, 16f)
                reflectiveCurveToRelative(-47.6f, -6f, -68.5f, -16f)
                horizontalLineToRelative(-8.3f)
                curveTo(51.6f, 288f, 0f, 339.6f, 0f, 403.2f)
                verticalLineTo(432f)
                curveToRelative(0f, 26.5f, 21.5f, 48f, 48f, 48f)
                horizontalLineToRelative(288f)
                curveToRelative(26.5f, 0f, 48f, -21.5f, 48f, -48f)
                verticalLineToRelative(-28.8f)
                curveToRelative(0f, -63.6f, -51.6f, -115.2f, -115.2f, -115.2f)
                close()
                moveTo(480f, 256f)
                curveToRelative(53f, 0f, 96f, -43f, 96f, -96f)
                reflectiveCurveToRelative(-43f, -96f, -96f, -96f)
                reflectiveCurveToRelative(-96f, 43f, -96f, 96f)
                reflectiveCurveToRelative(43f, 96f, 96f, 96f)
                close()
                moveToRelative(48f, 32f)
                horizontalLineToRelative(-3.8f)
                curveToRelative(-13.9f, 4.8f, -28.6f, 8f, -44.2f, 8f)
                reflectiveCurveToRelative(-30.3f, -3.2f, -44.2f, -8f)
                horizontalLineTo(432f)
                curveToRelative(-20.4f, 0f, -39.2f, 5.9f, -55.7f, 15.4f)
                curveToRelative(24.4f, 26.3f, 39.7f, 61.2f, 39.7f, 99.8f)
                verticalLineToRelative(38.4f)
                curveToRelative(0f, 2.2f, -0.5f, 4.3f, -0.6f, 6.4f)
                horizontalLineTo(592f)
                curveToRelative(26.5f, 0f, 48f, -21.5f, 48f, -48f)
                curveToRelative(0f, -61.9f, -50.1f, -112f, -112f, -112f)
                close()
            }
        }.build()

        return _FontAwesomeUserFriends!!
    }

private var _FontAwesomeUserFriends: ImageVector? = null