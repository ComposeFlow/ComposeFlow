package io.composeflow.custom.composeflowicons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import io.composeflow.custom.ComposeFlowIcons

val ComposeFlowIcons.ComposeFlowLogo: ImageVector
    get() {
        if (_ComposeFlowLogo != null) {
            return _ComposeFlowLogo!!
        }
        _ComposeFlowLogo =
            ImageVector
                .Builder(
                    name = "IconName",
                    defaultWidth = 1000.dp,
                    defaultHeight = 1000.dp,
                    viewportWidth = 1000f,
                    viewportHeight = 1000f,
                ).apply {
                    path(
                        fill = SolidColor(Color(0xFFFEFEFE)),
                        fillAlpha = 0.999f,
                        strokeAlpha = 0.999f,
                    ) {
                        moveTo(244.5f, 139.5f)
                        curveTo(414.2f, 139.3f, 583.8f, 139.5f, 753.5f, 140f)
                        curveTo(804.4f, 148.3f, 838.3f, 176.8f, 855f, 225.5f)
                        curveTo(856.8f, 232.1f, 858.2f, 238.7f, 859f, 245.5f)
                        curveTo(859.7f, 414.8f, 859.7f, 584.2f, 859f, 753.5f)
                        curveTo(850.7f, 804.4f, 822.2f, 838.3f, 773.5f, 855f)
                        curveTo(766.9f, 856.8f, 760.3f, 858.2f, 753.5f, 859f)
                        curveTo(583.8f, 859.7f, 414.2f, 859.7f, 244.5f, 859f)
                        curveTo(194.1f, 850.3f, 160.6f, 821.8f, 144f, 773.5f)
                        curveTo(142.2f, 766.9f, 140.8f, 760.3f, 140f, 753.5f)
                        curveTo(139.3f, 583.8f, 139.3f, 414.2f, 140f, 244.5f)
                        curveTo(151f, 185.6f, 185.9f, 150.6f, 244.5f, 139.5f)
                        close()
                    }
                    path(fill = SolidColor(Color(0xFF72CA91))) {
                        moveTo(381.5f, 494.5f)
                        curveTo(377f, 473.2f, 363.8f, 461.5f, 342f, 459.5f)
                        curveTo(322.2f, 461.1f, 309.1f, 471.1f, 302.5f, 489.5f)
                        curveTo(307.7f, 416.7f, 341f, 360.5f, 402.5f, 321f)
                        curveTo(437.8f, 300.6f, 475.6f, 290.7f, 516f, 291.5f)
                        curveTo(540.7f, 298.6f, 551.7f, 314.9f, 549f, 340.5f)
                        curveTo(543.3f, 360.5f, 530.3f, 370.8f, 510f, 371.5f)
                        curveTo(450.1f, 375.5f, 409.4f, 405.5f, 388f, 461.5f)
                        curveTo(385.2f, 472.5f, 383f, 483.5f, 381.5f, 494.5f)
                        close()
                    }
                    path(fill = SolidColor(Color(0xFF63C485))) {
                        moveTo(617.5f, 337.5f)
                        curveTo(634.7f, 335.3f, 648.5f, 341f, 659f, 354.5f)
                        curveTo(670.4f, 374.1f, 667.5f, 392f, 650.5f, 408f)
                        curveTo(629.9f, 421.1f, 611.1f, 418.9f, 594f, 401.5f)
                        curveTo(580f, 378.9f, 583.5f, 359.4f, 604.5f, 343f)
                        curveTo(608.8f, 340.7f, 613.1f, 338.9f, 617.5f, 337.5f)
                        close()
                    }
                    path(fill = SolidColor(Color(0xFF4C80F0))) {
                        moveTo(381.5f, 494.5f)
                        curveTo(381.5f, 495.2f, 381.5f, 495.8f, 381.5f, 496.5f)
                        curveTo(381.1f, 499.8f, 381.1f, 503f, 381.5f, 506f)
                        curveTo(375.4f, 528.9f, 359.7f, 539.6f, 334.5f, 538f)
                        curveTo(318.5f, 533.9f, 307.9f, 524f, 302.5f, 508.5f)
                        curveTo(301.3f, 502.5f, 301.3f, 496.2f, 302.5f, 489.5f)
                        curveTo(309.1f, 471.1f, 322.2f, 461.1f, 342f, 459.5f)
                        curveTo(363.8f, 461.5f, 377f, 473.2f, 381.5f, 494.5f)
                        close()
                    }
                    path(fill = SolidColor(Color(0xFF5D8CF0))) {
                        moveTo(381.5f, 496.5f)
                        curveTo(384.3f, 557.7f, 414.3f, 599.2f, 471.5f, 621f)
                        curveTo(522.2f, 634.8f, 566.4f, 622.9f, 604f, 585.5f)
                        curveTo(612.3f, 575.5f, 619.7f, 564.9f, 626f, 553.5f)
                        curveTo(642f, 537.9f, 659.8f, 535.4f, 679.5f, 546f)
                        curveTo(693.8f, 557.1f, 699.3f, 571.6f, 696f, 589.5f)
                        curveTo(682.9f, 618.6f, 663.7f, 643.1f, 638.5f, 663f)
                        curveTo(583.9f, 703.5f, 523.5f, 716.1f, 457.5f, 701f)
                        curveTo(368.9f, 673.7f, 317.4f, 614.2f, 303f, 522.5f)
                        curveTo(302.8f, 518.2f, 302.3f, 514.1f, 301.5f, 510f)
                        curveTo(302.1f, 509.6f, 302.4f, 509.1f, 302.5f, 508.5f)
                        curveTo(307.9f, 524f, 318.5f, 533.9f, 334.5f, 538f)
                        curveTo(359.7f, 539.6f, 375.4f, 528.9f, 381.5f, 506f)
                        curveTo(381.1f, 503f, 381.1f, 499.8f, 381.5f, 496.5f)
                        close()
                    }
                }.build()

        return _ComposeFlowLogo!!
    }

@Suppress("ObjectPropertyName")
private var _ComposeFlowLogo: ImageVector? = null
