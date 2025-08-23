package io.composeflow.ui.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.composeflow.Res
import io.composeflow.ic_google
import org.jetbrains.compose.resources.painterResource

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Sign in with Google",
    fontSize: TextUnit = 14.sp,
    iconSize: Dp = 20.dp,
    height: Dp = 40.dp,
    backgroundColor: Color = Color.White,
    contentColor: Color = Color(0xFF3C4043),
    borderColor: Color = Color(0xFFDADCE0),
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(height),
        enabled = enabled,
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.6f),
            disabledContentColor = contentColor.copy(alpha = 0.6f),
        ),
        border = BorderStroke(1.dp, borderColor),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_google),
                contentDescription = "Google logo",
                modifier = Modifier.size(iconSize),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = fontSize,
                fontWeight = FontWeight.Medium,
                color = contentColor,
            )
        }
    }
}

@Composable
fun GoogleSignInButtonIconOnly(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 20.dp,
    buttonSize: Dp = 40.dp,
    backgroundColor: Color = Color.White,
    borderColor: Color = Color(0xFFDADCE0),
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.size(buttonSize),
        enabled = enabled,
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.6f),
        ),
        border = BorderStroke(1.dp, borderColor),
        contentPadding = PaddingValues(0.dp),
    ) {
        Image(
            painter = painterResource(Res.drawable.ic_google),
            contentDescription = "Google logo",
            modifier = Modifier.size(iconSize),
        )
    }
}
