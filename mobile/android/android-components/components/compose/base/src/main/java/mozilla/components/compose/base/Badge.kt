package mozilla.components.compose.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import mozilla.components.compose.base.theme.AcornTheme

private val BADGE_ROUNDED_CORNER = 100.dp

enum class BadgeState {
    DEFAULT, ACTIVE, WARNING
}

@Composable
fun Badge(
    text: String,
    state: BadgeState = BadgeState.DEFAULT,
    backgroundColor: Color = AcornTheme.colors.layer2,
) {
    Column(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(BADGE_ROUNDED_CORNER))
            .background(
                color = backgroundColor,
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = text,
            color = getLabelTextColor(state),
            overflow = TextOverflow.Ellipsis,
            style = AcornTheme.typography.subtitle2,
            maxLines = 1,
        )
    }
}

@Composable
private fun getLabelTextColor(state: BadgeState): Color {
    return when (state) {
        BadgeState.ACTIVE -> AcornTheme.colors.textAccent
        BadgeState.WARNING -> AcornTheme.colors.textCritical
        else -> AcornTheme.colors.textPrimary
    }
}
