package org.telegram.ui.Components.compose

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.Theme

class GoogleSignInMaterialButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AbstractComposeView(context, attrs, defStyleAttr) {

    private var label by mutableStateOf("")
    private var fillContainerWidthState by mutableStateOf(false)
    private var enabledState by mutableStateOf(isEnabled)
    private var themeVersion by mutableIntStateOf(0)

    fun setLabel(value: CharSequence?) {
        label = value?.toString().orEmpty()
    }

    fun setFillContainerWidth(value: Boolean) {
        fillContainerWidthState = value
    }

    fun syncTheme() {
        themeVersion++
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        enabledState = enabled
    }

    @Composable
    override fun Content() {
        themeVersion
        TelegramMaterialTheme {
            OutlinedButton(
                onClick = { performClick() },
                enabled = enabledState,
                modifier = Modifier
                    .then(if (fillContainerWidthState) Modifier.fillMaxWidth() else Modifier)
                    .defaultMinSize(minHeight = 52.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(Theme.getColor(Theme.key_windowBackgroundWhite)),
                    contentColor = Color(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4)),
                ),
                border = ButtonDefaults.outlinedButtonBorder(enabled = enabledState).copy(
                    brush = SolidColor(Color(Theme.getColor(Theme.key_windowBackgroundWhiteInputField))),
                ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                ) {
                    AndroidView(
                        factory = { androidContext ->
                            ImageView(androidContext).apply {
                                setImageResource(R.drawable.googleg_standard_color_18)
                            }
                        },
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "  $label",
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
