package io.composeflow.custom

import androidx.compose.ui.graphics.vector.ImageVector
import io.composeflow.custom.composeflowicons.AllIcons
import io.composeflow.custom.composeflowicons.Android
import io.composeflow.custom.composeflowicons.Androiddevice
import io.composeflow.custom.composeflowicons.AndroiddeviceDark
import io.composeflow.custom.composeflowicons.Assets
import io.composeflow.custom.composeflowicons.AssetsDark
import io.composeflow.custom.composeflowicons.Colors
import io.composeflow.custom.composeflowicons.ColorsDark
import io.composeflow.custom.composeflowicons.ComposeLogo
import io.composeflow.custom.composeflowicons.Composeflowicons
import io.composeflow.custom.composeflowicons.Datacolumn
import io.composeflow.custom.composeflowicons.DatacolumnDark
import io.composeflow.custom.composeflowicons.Dbms
import io.composeflow.custom.composeflowicons.DbmsDark
import io.composeflow.custom.composeflowicons.Editfolder
import io.composeflow.custom.composeflowicons.EditfolderDark
import io.composeflow.custom.composeflowicons.HttpRequestsFiletype
import io.composeflow.custom.composeflowicons.HttpRequestsFiletypeDark
import io.composeflow.custom.composeflowicons.Iphonedevice
import io.composeflow.custom.composeflowicons.IphonedeviceDark
import io.composeflow.custom.composeflowicons.Placeholder
import io.composeflow.custom.composeflowicons.Settings
import io.composeflow.custom.composeflowicons.SettingsDark
import io.composeflow.custom.composeflowicons.Web
import io.composeflow.custom.composeflowicons.WebDark
import kotlin.collections.List as ____KtList

public object ComposeFlowIcons

private var __AllIcons: ____KtList<ImageVector>? = null

public val ComposeFlowIcons.AllIcons: ____KtList<ImageVector>
  get() {
    if (__AllIcons != null) {
      return __AllIcons!!
    }
    __AllIcons= Composeflowicons.AllIcons + listOf(Editfolder, WebDark, SettingsDark, Android,
        IphonedeviceDark, AndroiddeviceDark, Settings, Iphonedevice, Colors, Androiddevice,
        HttpRequestsFiletype, Assets, ColorsDark, HttpRequestsFiletypeDark, Datacolumn, DbmsDark,
        EditfolderDark, Dbms, DatacolumnDark, Web, AssetsDark, ComposeLogo, Placeholder)
    return __AllIcons!!
  }
