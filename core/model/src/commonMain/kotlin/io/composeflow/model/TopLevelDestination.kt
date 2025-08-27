package io.composeflow.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.ui.graphics.vector.ImageVector
import io.composeflow.custom.ComposeFlowIcons
import io.composeflow.custom.composeflowicons.AssetsDark
import io.composeflow.custom.composeflowicons.CloudFirestore
import io.composeflow.custom.composeflowicons.Colors
import io.composeflow.custom.composeflowicons.DatacolumnDark
import io.composeflow.custom.composeflowicons.DbmsDark
import io.composeflow.custom.composeflowicons.EditfolderDark
import io.composeflow.custom.composeflowicons.HttpRequestsFiletypeDark
import io.composeflow.custom.composeflowicons.SettingsDark
import io.composeflow.custom.composeflowicons.TranslateDark

const val UI_BUILDER_ROUTE = "ui_builder_route"
const val DATA_TYPE_EDITOR_ROUTE = "datatype_editor_route"
const val APP_STATE_EDITOR_ROUTE = "appstate_editor_route"
const val FIRESTORE_EDITOR_ROUTE = "firestore_editor_route"
const val API_EDITOR_ROUTE = "api_editor_route"
const val THEME_EDITOR_ROUTE = "theme_editor_route"
const val ASSET_EDITOR_ROUTE = "asset_editor_route"
const val STRING_EDITOR_ROUTE = "string_editor_route"
const val SETTINGS_ROUTE = "settings_route"

enum class TopLevelDestination(
    val icon: ImageVector,
    val label: String,
    val route: String,
) {
    UiBuilder(
        ComposeFlowIcons.EditfolderDark,
        "UI Builder",
        UI_BUILDER_ROUTE,
    ),
    DataTypeEditor(
        ComposeFlowIcons.DatacolumnDark,
        "Data type",
        DATA_TYPE_EDITOR_ROUTE,
    ),
    AppStateEditor(
        ComposeFlowIcons.DbmsDark,
        "App State",
        APP_STATE_EDITOR_ROUTE,
    ),
    FirestoreEditor(
        ComposeFlowIcons.CloudFirestore,
        "Firestore",
        FIRESTORE_EDITOR_ROUTE,
    ),
    ApiEditor(
        ComposeFlowIcons.HttpRequestsFiletypeDark,
        "API editor",
        API_EDITOR_ROUTE,
    ),
    ThemeEditor(
        ComposeFlowIcons.Colors,
        "Theme editor",
        THEME_EDITOR_ROUTE,
    ),
    AssetEditor(
        ComposeFlowIcons.AssetsDark,
        "Assets",
        ASSET_EDITOR_ROUTE,
    ),
    StringEditor(
        ComposeFlowIcons.TranslateDark,
        "Strings",
        STRING_EDITOR_ROUTE,
    ),
    Settings(
        ComposeFlowIcons.SettingsDark,
        "Settings",
        SETTINGS_ROUTE,
    ),
}

enum class InspectorTabDestination(
    val imageVector: ImageVector,
    val contentDesc: String,
) {
    Inspector(Icons.Outlined.ZoomIn, "Inspector"),
    Action(Icons.Outlined.FlashOn, "Action"),
    Code(Icons.Outlined.Code, "Code"),
}
