package org.telegram.ui.Components.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import org.telegram.ui.ActionBar.Theme

/**
 * Compose counterpart for the legacy XML themes:
 * - Theme.TMessages.Start
 * - Theme.TMessages
 * - Theme.TMessages.Dark
 *
 * This keeps Compose colors aligned with AntoGram's existing Theme key system,
 * so screens can migrate incrementally without forking palette logic.
 */
enum class TelegramComposeThemeVariant {
    Start,
    Main,
    Dialog,
}

@Immutable
data class TelegramComposePalette(
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val tertiary: Color,
    val outline: Color,
    val outlineVariant: Color,
    val error: Color,
    val onError: Color,
)

@Composable
fun TelegramComposeTheme(
    variant: TelegramComposeThemeVariant = TelegramComposeThemeVariant.Main,
    darkTheme: Boolean = Theme.isCurrentThemeDark() || isSystemInDarkTheme(),
    resourcesProvider: Theme.ResourcesProvider? = null,
    content: @Composable () -> Unit,
) {
    val palette = telegramComposePalette(
        variant = variant,
        darkTheme = darkTheme,
        resourcesProvider = resourcesProvider,
    )
    MaterialTheme(
        colorScheme = palette.asColorScheme(darkTheme),
        typography = TelegramExpressiveTypography,
        shapes = TelegramExpressiveShapes,
        content = content,
    )
}

@Composable
fun TelegramMainTheme(
    darkTheme: Boolean = Theme.isCurrentThemeDark() || isSystemInDarkTheme(),
    resourcesProvider: Theme.ResourcesProvider? = null,
    content: @Composable () -> Unit,
) {
    TelegramComposeTheme(
        variant = TelegramComposeThemeVariant.Main,
        darkTheme = darkTheme,
        resourcesProvider = resourcesProvider,
        content = content,
    )
}

@Composable
fun TelegramStartTheme(
    darkTheme: Boolean = Theme.isCurrentThemeDark() || isSystemInDarkTheme(),
    resourcesProvider: Theme.ResourcesProvider? = null,
    content: @Composable () -> Unit,
) {
    TelegramComposeTheme(
        variant = TelegramComposeThemeVariant.Start,
        darkTheme = darkTheme,
        resourcesProvider = resourcesProvider,
        content = content,
    )
}

@Composable
fun TelegramDialogTheme(
    darkTheme: Boolean = Theme.isCurrentThemeDark() || isSystemInDarkTheme(),
    resourcesProvider: Theme.ResourcesProvider? = null,
    content: @Composable () -> Unit,
) {
    TelegramComposeTheme(
        variant = TelegramComposeThemeVariant.Dialog,
        darkTheme = darkTheme,
        resourcesProvider = resourcesProvider,
        content = content,
    )
}

fun telegramComposePalette(
    variant: TelegramComposeThemeVariant,
    darkTheme: Boolean,
    resourcesProvider: Theme.ResourcesProvider? = null,
): TelegramComposePalette {
    val isDark = resourcesProvider?.isDark() ?: darkTheme
    val backgroundKey = when (variant) {
        TelegramComposeThemeVariant.Start -> if (isDark) Theme.key_dialogBackground else Theme.key_windowBackgroundWhite
        TelegramComposeThemeVariant.Main -> Theme.key_windowBackgroundWhite
        TelegramComposeThemeVariant.Dialog -> Theme.key_dialogBackground
    }
    val onBackgroundKey = when (variant) {
        TelegramComposeThemeVariant.Dialog -> Theme.key_dialogTextBlack
        TelegramComposeThemeVariant.Start,
        TelegramComposeThemeVariant.Main -> Theme.key_windowBackgroundWhiteBlackText
    }
    val surfaceKey = when (variant) {
        TelegramComposeThemeVariant.Dialog -> Theme.key_dialogBackground
        TelegramComposeThemeVariant.Start,
        TelegramComposeThemeVariant.Main -> Theme.key_windowBackgroundWhite
    }
    val onSurfaceKey = onBackgroundKey
    val surfaceVariantKey = when (variant) {
        TelegramComposeThemeVariant.Dialog -> Theme.key_dialogBackgroundGray
        TelegramComposeThemeVariant.Start,
        TelegramComposeThemeVariant.Main -> Theme.key_windowBackgroundWhite
    }
    val onSurfaceVariantKey = when (variant) {
        TelegramComposeThemeVariant.Dialog -> Theme.key_dialogTextGray2
        TelegramComposeThemeVariant.Start,
        TelegramComposeThemeVariant.Main -> Theme.key_windowBackgroundWhiteGrayText6
    }
    return TelegramComposePalette(
        background = themeColor(backgroundKey, resourcesProvider),
        onBackground = themeColor(onBackgroundKey, resourcesProvider),
        surface = themeColor(surfaceKey, resourcesProvider),
        onSurface = themeColor(onSurfaceKey, resourcesProvider),
        surfaceVariant = themeColor(surfaceVariantKey, resourcesProvider),
        onSurfaceVariant = themeColor(onSurfaceVariantKey, resourcesProvider),
        primary = themeColor(Theme.key_chats_actionBackground, resourcesProvider),
        onPrimary = themeColor(Theme.key_chats_actionIcon, resourcesProvider),
        secondary = themeColor(Theme.key_windowBackgroundWhiteBlueText4, resourcesProvider),
        onSecondary = themeColor(Theme.key_windowBackgroundWhite, resourcesProvider),
        tertiary = themeColor(Theme.key_windowBackgroundWhiteValueText, resourcesProvider),
        outline = themeColor(Theme.key_windowBackgroundWhiteInputField, resourcesProvider),
        outlineVariant = themeColor(Theme.key_listSelector, resourcesProvider),
        error = themeColor(Theme.key_text_RedRegular, resourcesProvider),
        onError = themeColor(Theme.key_windowBackgroundWhite, resourcesProvider),
    )
}

private fun TelegramComposePalette.asColorScheme(darkTheme: Boolean): ColorScheme {
    return if (darkTheme) {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            secondary = secondary,
            onSecondary = onSecondary,
            tertiary = tertiary,
            onTertiary = onSecondary,
            error = error,
            onError = onError,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            outlineVariant = outlineVariant,
            surfaceTint = primary,
            inversePrimary = secondary,
            inverseSurface = onSurface,
            inverseOnSurface = surface,
            scrim = Color.Black.copy(alpha = 0.42f),
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            secondary = secondary,
            onSecondary = onSecondary,
            tertiary = tertiary,
            onTertiary = onSecondary,
            error = error,
            onError = onError,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            outlineVariant = outlineVariant,
            surfaceTint = primary,
            inversePrimary = secondary,
            inverseSurface = onSurface,
            inverseOnSurface = surface,
            scrim = Color.Black.copy(alpha = 0.42f),
        )
    }
}

private fun themeColor(
    key: Int,
    resourcesProvider: Theme.ResourcesProvider?,
): Color = Color(Theme.getColor(key, resourcesProvider))
