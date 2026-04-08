package org.telegram.ui.ActionBar.theme

import org.telegram.tgnet.TLRPC

interface ITheme {
    fun getThemeId(): Long

    fun getThemeSettings(settingsIndex: Int): TLRPC.ThemeSettings?
    fun getThemeWallPaper(settingsIndex: Int): TLRPC.WallPaper?
}
