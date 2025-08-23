package io.composeflow.ui.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.composeflow.Res
import io.composeflow.font.FontFamilyWrapper
import io.composeflow.ic_google
import org.jetbrains.compose.resources.painterResource

enum class GoogleButtonMode {
    Light,
    Dark,
    Neutral,
}

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier.height(44.dp),
    mode: GoogleButtonMode = GoogleButtonMode.Light,
    text: String = "Sign in with Google",
    shape: Shape = ButtonDefaults.shape,
    fontSize: TextUnit = 14.sp,
    enabled: Boolean = true,
) {
    val backgroundColor =
        when (mode) {
            GoogleButtonMode.Light -> Color.White
            GoogleButtonMode.Dark -> Color(0xFF131314)
            GoogleButtonMode.Neutral -> Color(0xFFF2F2F2)
        }

    val contentColor =
        when (mode) {
            GoogleButtonMode.Light -> Color(0xFF3C4043)
            GoogleButtonMode.Dark -> Color(0xFFE3E3E3)
            GoogleButtonMode.Neutral -> Color(0xFF3C4043)
        }

    val borderStroke =
        when (mode) {
            GoogleButtonMode.Light ->
                BorderStroke(
                    width = 1.dp,
                    color = Color(0xFF747775),
                )

            GoogleButtonMode.Dark ->
                BorderStroke(
                    width = 1.dp,
                    color = Color(0xFF8E918F),
                )

            GoogleButtonMode.Neutral -> null
        }

    val horizontalPadding = 16.dp
    val iconTextPadding = 12.dp

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = backgroundColor,
                contentColor = contentColor,
                disabledContainerColor = backgroundColor.copy(alpha = 0.6f),
                disabledContentColor = contentColor.copy(alpha = 0.6f),
            ),
        border = borderStroke,
        contentPadding = PaddingValues(horizontal = horizontalPadding),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_google),
                contentDescription = "Google logo",
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(iconTextPadding))
            Text(
                text = text,
                maxLines = 1,
                fontSize = fontSize,
                fontFamily = FontFamilyWrapper.Roboto.asFontFamily(),
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
fun GoogleSignInButtonIconOnly(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    mode: GoogleButtonMode = GoogleButtonMode.Light,
    iconSize: Dp = 18.dp,
    buttonSize: Dp = 40.dp,
    enabled: Boolean = true,
) {
    val backgroundColor =
        when (mode) {
            GoogleButtonMode.Light -> Color.White
            GoogleButtonMode.Dark -> Color(0xFF131314)
            GoogleButtonMode.Neutral -> Color(0xFFF2F2F2)
        }

    val borderStroke =
        when (mode) {
            GoogleButtonMode.Light ->
                BorderStroke(
                    width = 1.dp,
                    color = Color(0xFF747775),
                )

            GoogleButtonMode.Dark ->
                BorderStroke(
                    width = 1.dp,
                    color = Color(0xFF8E918F),
                )

            GoogleButtonMode.Neutral -> null
        }

    Button(
        onClick = onClick,
        modifier = modifier.size(buttonSize),
        enabled = enabled,
        shape = CircleShape,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = backgroundColor,
                disabledContainerColor = backgroundColor.copy(alpha = 0.6f),
            ),
        border = borderStroke,
        contentPadding = PaddingValues(0.dp),
    ) {
        Image(
            painter = painterResource(Res.drawable.ic_google),
            contentDescription = "Google logo",
            modifier = Modifier.size(iconSize),
        )
    }
}
