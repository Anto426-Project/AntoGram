package org.telegram.ui.Components.compose

import androidx.compose.runtime.Composable
import org.telegram.ui.ActionBar.Theme

@Composable
fun TelegramMaterialTheme(
    darkTheme: Boolean = Theme.isCurrentThemeDark(),
    resourcesProvider: Theme.ResourcesProvider? = null,
    content: @Composable () -> Unit,
) {
    TelegramMainTheme(
        darkTheme = darkTheme,
        resourcesProvider = resourcesProvider,
        content = content,
    )
}
