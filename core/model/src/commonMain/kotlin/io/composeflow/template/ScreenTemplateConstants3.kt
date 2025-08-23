package io.composeflow.template

/**
 * Settings screen template constant for WASM compatibility.
 * Split into a separate file due to size constraints.
 */
object ScreenTemplateConstants3 {
    const val SETTINGS_SCREEN_TEMPLATE = """id: "settingsRoot"
name: "Settings"
rootNode:
  id: "settingsScreen"
  trait: !<ScreenTrait> { }
  label: "Settings"
  children:
    - id: "rootColumn"
      trait: !<ColumnTrait> { }
      label: "Column"
      level: 1
      modifierList:
        - !<FillMaxWidth> { }
        - !<Weight> { }
      children:
        - id: "settingsList"
          trait: !<LazyColumnTrait> { }
          label: "LazyColumn"
          level: 2
          modifierList:
            - !<FillMaxSize> { }
            - !<Padding>
              start: 16.0
              end: 16.0
          children:
            - id: "manageTitleRow"
              trait: !<RowTrait>
                verticalAlignment: "CenterVertically"
              label: "Row"
              level: 3
              modifierList:
                - !<FillMaxWidth> { }
                - !<Size>
                  height: 80.0
              children:
                - id: "manageTitleText"
                  trait: !<TextTrait>
                    text: !<StringIntrinsicValue>
                      value: "Manage"
                    colorWrapper: !<ColorIntrinsicValue>
                      value:
                        themeColor: "Tertiary"
                    textStyleWrapper: !<EnumProperty>
                      value: !<TextStyleWrapper> "TitleSmall"
                  label: "Text"
                  level: 4
                  lazyListChildParams: !<FixedNumber>
                    numOfItems: 1
                  dynamicItems: null
                  actionHandler: !<ActionHandlerImpl>
                    actionsMap:
                      "OnLongClick": [ ]
                      "OnClick": [ ]
                      "OnDoubleClick": [ ]
                  visibilityParams: { }
              lazyListChildParams: !<FixedNumber> { }
              dynamicItems: null
              actionHandler: !<ActionHandlerImpl>
                actionsMap:
                  "OnLongClick": [ ]
                  "OnClick": [ ]
                  "OnDoubleClick": [ ]
              visibilityParams: { }
            - id: "appNotificationsRow"
              trait: !<RowTrait>
                horizontalArrangement: "Start"
                verticalAlignment: "CenterVertically"
              label: "Row"
              level: 3
              modifierList:
                - !<Size>
                  height: 80.0
                - !<FillMaxWidth> { }
              children:
                - id: "appNotificationsTextColumn"
                  trait: !<ColumnTrait>
                    verticalArrangementWrapper: "Center"
                  label: "Column"
                  level: 4
                  modifierList:
                    - !<Height>
                      height: 80.0
                    - !<WrapContentWidth>
                      align: "Start"
                  children:
                    - id: "appNotificationsTitleText"
                      trait: !<TextTrait>
                        text: !<StringIntrinsicValue>
                          value: "App notifications"
                        colorWrapper: !<ColorIntrinsicValue>
                          value:
                            themeColor: "OnSurface"
                        textStyleWrapper: !<EnumProperty>
                          value: !<TextStyleWrapper> "TitleLarge"
                      label: "Text"
                      level: 5
                      lazyListChildParams: !<FixedNumber>
                        numOfItems: 1
                      dynamicItems: null
                      actionHandler: !<ActionHandlerImpl>
                        actionsMap:
                          "OnLongClick": [ ]
                          "OnClick": [ ]
                          "OnDoubleClick": [ ]
                      visibilityParams: { }
                    - id: "appNotificationsSubtitleText"
                      trait: !<TextTrait>
                        text: !<StringIntrinsicValue>
                          value: "Control notifications from individual apps"
                        colorWrapper: !<ColorIntrinsicValue>
                          value:
                            themeColor: "Secondary"
                        textStyleWrapper: !<EnumProperty>
                          value: !<TextStyleWrapper> "BodyMedium"
                      label: "Text"
                      level: 5
                      lazyListChildParams: !<FixedNumber>
                        numOfItems: 1
                      dynamicItems: null
                      actionHandler: !<ActionHandlerImpl>
                        actionsMap:
                          "OnLongClick": [ ]
                          "OnClick": [ ]
                          "OnDoubleClick": [ ]
                      visibilityParams: { }
                  lazyListChildParams: !<FixedNumber>
                    numOfItems: 1
                  dynamicItems: null
                  actionHandler: !<ActionHandlerImpl>
                    actionsMap:
                      "OnLongClick": [ ]
                      "OnClick": [ ]
                      "OnDoubleClick": [ ]
                  visibilityParams: { }
                - id: "appNotificationsSpacer"
                  trait: !<SpacerTrait> { }
                  label: "Spacer"
                  level: 4
                  modifierList:
                    - !<Weight> { }
                    - !<FillMaxHeight> { }
                    - !<Padding>
                      start: 8.0
                      top: 8.0
                      end: 8.0
                      bottom: 8.0
                  lazyListChildParams: !<FixedNumber>
                    numOfItems: 1
                  dynamicItems: null
                  actionHandler: !<ActionHandlerImpl>
                    actionsMap:
                      "OnLongClick": [ ]
                      "OnClick": [ ]
                      "OnDoubleClick": [ ]
                  visibilityParams: { }
              lazyListChildParams: !<FixedNumber> { }
              dynamicItems: null
              actionHandler: !<ActionHandlerImpl>
                actionsMap:
                  "OnLongClick": [ ]
                  "OnClick": [ ]
                  "OnDoubleClick": [ ]
              visibilityParams: { }
            - id: "notificationHistoryRow"
              trait: !<RowTrait>
                horizontalArrangement: "Start"
                verticalAlignment: "CenterVertically"
              label: "Row"
              level: 3
              modifierList:
                - !<Height>
                  height: 80.0
                - !<FillMaxWidth> { }
              children:
                - id: "notificationHistoryTextColumn"
                  trait: !<ColumnTrait>
                    verticalArrangementWrapper: "Center"
                  label: "Column"
                  level: 4
                  modifierList:
                    - !<Height>
                      height: 80.0
                    - !<WrapContentWidth>
                      align: "Start"
                  children:
                    - id: "notificationHistoryTitleText"
                      trait: !<TextTrait>
                        text: !<StringIntrinsicValue>
                          value: "Notification history"
                        colorWrapper: !<ColorIntrinsicValue>
                          value:
                            themeColor: "OnSurface"
                        textStyleWrapper: !<EnumProperty>
                          value: !<TextStyleWrapper> "TitleLarge"
                      label: "Text"
                      level: 5
                      lazyListChildParams: !<FixedNumber>
                        numOfItems: 1
                      dynamicItems: null
                      actionHandler: !<ActionHandlerImpl>
                        actionsMap:
                          "OnLongClick": [ ]
                          "OnClick": [ ]
                          "OnDoubleClick": [ ]
                      visibilityParams: { }
                    - id: "notificationHistorySubtitleText"
                      trait: !<TextTrait>
                        text: !<StringIntrinsicValue>
                          value: "Show sensitive content when locked"
                        colorWrapper: !<ColorIntrinsicValue>
                          value:
                            themeColor: "Secondary"
                        textStyleWrapper: !<EnumProperty>
                          value: !<TextStyleWrapper> "BodyMedium"
                      label: "Text"
                      level: 5
                      lazyListChildParams: !<FixedNumber>
                        numOfItems: 1
                      dynamicItems: null
                      actionHandler: !<ActionHandlerImpl>
                        actionsMap:
                          "OnLongClick": [ ]
                          "OnClick": [ ]
                          "OnDoubleClick": [ ]
                      visibilityParams: { }
                  lazyListChildParams: !<FixedNumber>
                    numOfItems: 1
                  dynamicItems: null
                  actionHandler: !<ActionHandlerImpl>
                    actionsMap:
                      "OnLongClick": [ ]
                      "OnClick": [ ]
                      "OnDoubleClick": [ ]
                  visibilityParams: { }
                - id: "notificationHistorySpacer"
                  trait: !<SpacerTrait> { }
                  label: "Spacer"
                  level: 4
                  modifierList:
                    - !<Weight> { }
                    - !<FillMaxHeight> { }
                    - !<Padding>
                      start: 8.0
                      top: 8.0
                      end: 8.0
                      bottom: 8.0
                  lazyListChildParams: !<FixedNumber>
                    numOfItems: 1
                  dynamicItems: null
                  actionHandler: !<ActionHandlerImpl>
                    actionsMap:
                      "OnLongClick": [ ]
                      "OnClick": [ ]
                      "OnDoubleClick": [ ]
                  visibilityParams: { }
                - id: "notificationHistoryArrowIcon"
                  trait: !<IconTrait>
                    imageVectorHolder: !<Outlined> "KeyboardArrowRight"
                    contentDescription: "Icon for Add"
                    tint: null
                  label: "Icon"
                  level: 4
                  modifierList:
                    - !<Padding>
                      start: 8.0
                      top: 8.0
                      end: 8.0
                      bottom: 8.0
                  lazyListChildParams: !<FixedNumber>
                    numOfItems: 1
                  dynamicItems: null
                  actionHandler: !<ActionHandlerImpl>
                    actionsMap:
                      "OnLongClick": [ ]
                      "OnClick": [ ]
                      "OnDoubleClick": [ ]
                  visibilityParams: { }
              lazyListChildParams: !<FixedNumber> { }
              dynamicItems: null
              actionHandler: !<ActionHandlerImpl>
                actionsMap:
                  "OnLongClick": [ ]
                  "OnClick": [ ]
                  "OnDoubleClick": [ ]
              visibilityParams: { }
            - id: "privacyTitleRow"
              trait: !<RowTrait>
                verticalAlignment: "CenterVertically"
              label: "Row"
              level: 3
              modifierList:
                - !<FillMaxWidth> { }
                - !<Size>
                  height: 80.0
              children:
                - id: "privacyTitleText"
                  trait: !<TextTrait>
                    text: !<StringIntrinsicValue>
                      value: "Privacy"
                    colorWrapper: !<ColorIntrinsicValue>
                      value:
                        themeColor: "Tertiary"
                    textStyleWrapper: !<EnumProperty>
                      value: !<TextStyleWrapper> "TitleSmall"
                  label: "Text"
                  level: 4
                  lazyListChildParams: !<FixedNumber>
                    numOfItems: 1
                  dynamicItems: null
                  actionHandler: !<ActionHandlerImpl>
                    actionsMap:
                      "OnLongClick": [ ]
                      "OnClick": [ ]
                      "OnDoubleClick": [ ]
                  visibilityParams: { }
              lazyListChildParams: !<FixedNumber> { }
              dynamicItems: null
              actionHandler: !<ActionHandlerImpl>
                actionsMap:
                  "OnLongClick": [ ]
                  "OnClick": [ ]
                  "OnDoubleClick": [ ]
              visibilityParams: { }
            - id: "sensitiveNotificationsRow"
              trait: !<RowTrait>
                horizontalArrangement: "Start"
                verticalAlignment: "CenterVertically"
              label: "Row"
              level: 3
              modifierList:
                - !<Size>
                  height: 80.0
                - !<FillMaxWidth> { }
              children:
                - id: "sensitiveNotificationsTextColumn"
                  trait: !<ColumnTrait>
                    verticalArrangementWrapper: "Center"
                  label: "Column"
                  level: 4
                  modifierList:
                    - !<Height>
                      height: 80.0
                    - !<WrapContentWidth>
                      align: "Start"
                  children:
                    - id: "sensitiveNotificationsTitleText"
                      trait: !<TextTrait>
                        text: !<StringIntrinsicValue>
                          value: "Sensitive notifications"
                        colorWrapper: !<ColorIntrinsicValue>
                          value:
                            themeColor: "OnSurface"
                        textStyleWrapper: !<EnumProperty>
                          value: !<TextStyleWrapper> "TitleLarge"
                      label: "Text"
                      level: 5
                      lazyListChildParams: !<FixedNumber>
                        numOfItems: 1
                      dynamicItems: null
                      actionHandler: !<ActionHandlerImpl>
                        actionsMap:
                          "OnLongClick": [ ]
                          "OnClick": [ ]
                          "OnDoubleClick": [ ]
                      visibilityParams: { }
                    - id: "sensitiveNotificationsSubtitleText"
                      trait: !<TextTrait>
                        text: !<StringIntrinsicValue>
                          value: "Show sensitive content when locked"
                        colorWrapper: !<ColorIntrinsicValue>
                          value:
                            themeColor: "Secondary"
                        textStyleWrapper: !<EnumProperty>
                          value: !<TextStyleWrapper> "BodyMedium"
                      label: "Text"
                      level: 5
                      lazyListChildParams: !<FixedNumber>
                        numOfItems: 1
                      dynamicItems: null
                      actionHandler: !<ActionHandlerImpl>
                        actionsMap:
                          "OnLongClick": [ ]
                          "OnClick": [ ]
                          "OnDoubleClick": [ ]
                      visibilityParams: { }
                  lazyListChildParams: !<FixedNumber> { }
                  dynamicItems: null
                  actionHandler: !<ActionHandlerImpl>
                    actionsMap:
                      "OnLongClick": [ ]
                      "OnClick": [ ]
                      "OnDoubleClick": [ ]
                  visibilityParams: { }
                - id: "sensitiveNotificationsSpacer"
                  trait: !<SpacerTrait> { }
                  label: "Spacer"
                  level: 4
                  modifierList:
                    - !<Weight> { }
                    - !<FillMaxHeight> { }
                    - !<Padding>
                      start: 8.0
                      top: 8.0
                      end: 8.0
                      bottom: 8.0
                  lazyListChildParams: !<FixedNumber>
                    numOfItems: 1
                  dynamicItems: null
                  actionHandler: !<ActionHandlerImpl>
                    actionsMap:
                      "OnLongClick": [ ]
                      "OnClick": [ ]
                      "OnDoubleClick": [ ]
                  visibilityParams: { }
                - id: "sensitiveNotificationsSwitch"
                  trait: !<SwitchTrait>
                    checked: !<ValueFromCompanionState> { }
                  label: "Switch"
                  level: 4
                  lazyListChildParams: !<FixedNumber>
                    numOfItems: 1
                  dynamicItems: null
                  actionHandler: !<ActionHandlerImpl>
                    actionsMap:
                      "OnChange": [ ]
                  visibilityParams: { }
              lazyListChildParams: !<FixedNumber> { }
              dynamicItems: null
              actionHandler: !<ActionHandlerImpl>
                actionsMap:
                  "OnLongClick": [ ]
                  "OnClick": [ ]
                  "OnDoubleClick": [ ]
              visibilityParams: { }
            - id: "lockScreenNotificationsRow"
              trait: !<RowTrait>
                horizontalArrangement: "Start"
                verticalAlignment: "CenterVertically"
              label: "Row"
              level: 3
              modifierList:
                - !<Size>
                  height: 80.0
                - !<FillMaxWidth> { }
              children:
                - id: "lockScreenNotificationsTextColumn"
                  trait: !<ColumnTrait>
                    verticalArrangementWrapper: "Center"
                  label: "Column"
                  level: 4
                  modifierList:
                    - !<Height>
                      height: 80.0
                    - !<WrapContentWidth>
                      align: "Start"
                  children:
                    - id: "lockScreenNotificationsTitleText"
                      trait: !<TextTrait>
                        text: !<StringIntrinsicValue>
                          value: "Notifications on lock screen"
                        colorWrapper: !<ColorIntrinsicValue>
                          value:
                            themeColor: "OnSurface"
                        textStyleWrapper: !<EnumProperty>
                          value: !<TextStyleWrapper> "TitleLarge"
                      label: "Text"
                      level: 5
                      lazyListChildParams: !<FixedNumber>
                        numOfItems: 1
                      dynamicItems: null
                      actionHandler: !<ActionHandlerImpl>
                        actionsMap:
                          "OnLongClick": [ ]
                          "OnClick": [ ]
                          "OnDoubleClick": [ ]
                      visibilityParams: { }
                    - id: "lockScreenNotificationsSubtitleText"
                      trait: !<TextTrait>
                        text: !<StringIntrinsicValue>
                          value: "Hide silent conversations and notifications"
                        colorWrapper: !<ColorIntrinsicValue>
                          value:
                            themeColor: "Secondary"
                        textStyleWrapper: !<EnumProperty>
                          value: !<TextStyleWrapper> "BodyMedium"
                      label: "Text"
                      level: 5
                      lazyListChildParams: !<FixedNumber>
                        numOfItems: 1
                      dynamicItems: null
                      actionHandler: !<ActionHandlerImpl>
                        actionsMap:
                          "OnLongClick": [ ]
                          "OnClick": [ ]
                          "OnDoubleClick": [ ]
                      visibilityParams: { }
                  lazyListChildParams: !<FixedNumber>
                    numOfItems: 1
                  dynamicItems: null
                  actionHandler: !<ActionHandlerImpl>
                    actionsMap:
                      "OnLongClick": [ ]
                      "OnClick": [ ]
                      "OnDoubleClick": [ ]
                  visibilityParams: { }
                - id: "lockScreenNotificationsSpacer"
                  trait: !<EmptyTrait> { }
                  label: "Empty"
                  level: 4
                  modifierList:
                    - !<Weight> { }
                    - !<FillMaxHeight> { }
                    - !<Padding>
                      start: 8.0
                      top: 8.0
                      end: 8.0
                      bottom: 8.0
                  lazyListChildParams: !<FixedNumber>
                    numOfItems: 1
                  dynamicItems: null
                  actionHandler: !<ActionHandlerImpl>
                    actionsMap:
                      "OnLongClick": [ ]
                      "OnClick": [ ]
                      "OnDoubleClick": [ ]
                  visibilityParams: { }
              lazyListChildParams: !<FixedNumber> { }
              dynamicItems: null
              actionHandler: !<ActionHandlerImpl>
                actionsMap:
                  "OnLongClick": [ ]
                  "OnClick": [ ]
                  "OnDoubleClick": [ ]
              visibilityParams: { }
            - id: "generalTitleRow"
              trait: !<RowTrait>
                verticalAlignment: "CenterVertically"
              label: "Row"
              level: 3
              modifierList:
                - !<FillMaxWidth> { }
                - !<Size>
                  height: 80.0
              children:
                - id: "generalTitleText"
                  trait: !<TextTrait>
                    text: !<StringIntrinsicValue>
                      value: "General"
                    colorWrapper: !<ColorIntrinsicValue>
                      value:
                        themeColor: "Tertiary"
                    textStyleWrapper: !<EnumProperty>
                      value: !<TextStyleWrapper> "TitleSmall"
                  label: "Text"
                  level: 4
                  lazyListChildParams: !<FixedNumber>
                    numOfItems: 1
                  dynamicItems: null
                  actionHandler: !<ActionHandlerImpl>
                    actionsMap:
                      "OnLongClick": [ ]
                      "OnClick": [ ]
                      "OnDoubleClick": [ ]
                  visibilityParams: { }
              lazyListChildParams: !<FixedNumber> { }
              dynamicItems: null
              actionHandler: !<ActionHandlerImpl>
                actionsMap:
                  "OnLongClick": [ ]
                  "OnClick": [ ]
                  "OnDoubleClick": [ ]
              visibilityParams: { }
            - id: "doNotDisturbRow"
              trait: !<RowTrait>
                horizontalArrangement: "Start"
                verticalAlignment: "CenterVertically"
              label: "Row"
              level: 3
              modifierList:
                - !<Size>
                  height: 80.0
                - !<FillMaxWidth> { }
              children:
                - id: "doNotDisturbTextColumn"
                  trait: !<ColumnTrait>
                    verticalArrangementWrapper: "Center"
                  label: "Column"
                  level: 4
                  modifierList:
                    - !<Height>
                      height: 80.0
                    - !<WrapContentWidth>
                      align: "Start"
                  children:
                    - id: "doNotDisturbTitleText"
                      trait: !<TextTrait>
                        text: !<StringIntrinsicValue>
                          value: "Do not disturb"
                        colorWrapper: !<ColorIntrinsicValue>
                          value:
                            themeColor: "OnSurface"
                        textStyleWrapper: !<EnumProperty>
                          value: !<TextStyleWrapper> "TitleLarge"
                      label: "Text"
                      level: 5
                      lazyListChildParams: !<FixedNumber>
                        numOfItems: 1
                      dynamicItems: null
                      actionHandler: !<ActionHandlerImpl>
                        actionsMap:
                          "OnLongClick": [ ]
                          "OnClick": [ ]
                          "OnDoubleClick": [ ]
                      visibilityParams: { }
                    - id: "doNotDisturbSubtitleText"
                      trait: !<TextTrait>
                        text: !<StringIntrinsicValue>
                          value: "on"
                        colorWrapper: !<ColorIntrinsicValue>
                          value:
                            themeColor: "Secondary"
                        textStyleWrapper: !<EnumProperty>
                          value: !<TextStyleWrapper> "BodyMedium"
                      label: "Text"
                      level: 5
                      lazyListChildParams: !<FixedNumber>
                        numOfItems: 1
                      dynamicItems: null
                      actionHandler: !<ActionHandlerImpl>
                        actionsMap:
                          "OnLongClick": [ ]
                          "OnClick": [ ]
                          "OnDoubleClick": [ ]
                      visibilityParams: { }
                  lazyListChildParams: !<FixedNumber>
                    numOfItems: 1
                  dynamicItems: null
                  actionHandler: !<ActionHandlerImpl>
                    actionsMap:
                      "OnLongClick": [ ]
                      "OnClick": [ ]
                      "OnDoubleClick": [ ]
                  visibilityParams: { }
                - id: "doNotDisturbSpacer"
                  trait: !<EmptyTrait> { }
                  label: "Empty"
                  level: 4
                  modifierList:
                    - !<Weight> { }
                    - !<FillMaxHeight> { }
                    - !<Padding>
                      start: 8.0
                      top: 8.0
                      end: 8.0
                      bottom: 8.0
                  lazyListChildParams: !<FixedNumber>
                    numOfItems: 1
                  dynamicItems: null
                  actionHandler: !<ActionHandlerImpl>
                    actionsMap:
                      "OnLongClick": [ ]
                      "OnClick": [ ]
                      "OnDoubleClick": [ ]
                  visibilityParams: { }
              lazyListChildParams: !<FixedNumber> { }
              dynamicItems: null
              actionHandler: !<ActionHandlerImpl>
                actionsMap:
                  "OnLongClick": [ ]
                  "OnClick": [ ]
                  "OnDoubleClick": [ ]
              visibilityParams: { }
            - id: "flashNotificationsRow"
              trait: !<RowTrait>
                horizontalArrangement: "Start"
                verticalAlignment: "CenterVertically"
              label: "Row"
              level: 3
              modifierList:
                - !<Size>
                  height: 80.0
                - !<FillMaxWidth> { }
              children:
                - id: "flashNotificationsTextColumn"
                  trait: !<ColumnTrait>
                    verticalArrangementWrapper: "Center"
                  label: "Column"
                  level: 4
                  modifierList:
                    - !<Height>
                      height: 80.0
                    - !<WrapContentWidth>
                      align: "Start"
                  children:
                    - id: "flashNotificationsTitleText"
                      trait: !<TextTrait>
                        text: !<StringIntrinsicValue>
                          value: "Flash notifications"
                        colorWrapper: !<ColorIntrinsicValue>
                          value:
                            themeColor: "OnSurface"
                        textStyleWrapper: !<EnumProperty>
                          value: !<TextStyleWrapper> "TitleLarge"
                      label: "Text"
                      level: 5
                      lazyListChildParams: !<FixedNumber>
                        numOfItems: 1
                      dynamicItems: null
                      actionHandler: !<ActionHandlerImpl>
                        actionsMap:
                          "OnLongClick": [ ]
                          "OnClick": [ ]
                          "OnDoubleClick": [ ]
                      visibilityParams: { }
                    - id: "flashNotificationsSubtitleText"
                      trait: !<TextTrait>
                        text: !<StringIntrinsicValue>
                          value: "on"
                        colorWrapper: !<ColorIntrinsicValue>
                          value:
                            themeColor: "Secondary"
                        textStyleWrapper: !<EnumProperty>
                          value: !<TextStyleWrapper> "BodyMedium"
                      label: "Text"
                      level: 5
                      lazyListChildParams: !<FixedNumber>
                        numOfItems: 1
                      dynamicItems: null
                      actionHandler: !<ActionHandlerImpl>
                        actionsMap:
                          "OnLongClick": [ ]
                          "OnClick": [ ]
                          "OnDoubleClick": [ ]
                      visibilityParams: { }
                  lazyListChildParams: !<FixedNumber>
                    numOfItems: 1
                  dynamicItems: null
                  actionHandler: !<ActionHandlerImpl>
                    actionsMap:
                      "OnLongClick": [ ]
                      "OnClick": [ ]
                      "OnDoubleClick": [ ]
                  visibilityParams: { }
                - id: "flashNotificationsSpacer"
                  trait: !<EmptyTrait> { }
                  label: "Empty"
                  level: 4
                  modifierList:
                    - !<Weight> { }
                    - !<FillMaxHeight> { }
                    - !<Padding>
                      start: 8.0
                      top: 8.0
                      end: 8.0
                      bottom: 8.0
                  lazyListChildParams: !<FixedNumber>
                    numOfItems: 1
                  dynamicItems: null
                  actionHandler: !<ActionHandlerImpl>
                    actionsMap:
                      "OnLongClick": [ ]
                      "OnClick": [ ]
                      "OnDoubleClick": [ ]
                  visibilityParams: { }
              lazyListChildParams: !<FixedNumber> { }
              dynamicItems: null
              actionHandler: !<ActionHandlerImpl>
                actionsMap:
                  "OnLongClick": [ ]
                  "OnClick": [ ]
                  "OnDoubleClick": [ ]
              visibilityParams: { }
            - id: "wirelessAlertsRow"
              trait: !<RowTrait>
                horizontalArrangement: "Start"
                verticalAlignment: "CenterVertically"
              label: "Row"
              level: 3
              modifierList:
                - !<Size>
                  height: 80.0
                - !<FillMaxWidth> { }
              children:
                - id: "wirelessAlertsTextColumn"
                  trait: !<ColumnTrait>
                    verticalArrangementWrapper: "Center"
                  label: "Column"
                  level: 4
                  modifierList:
                    - !<Height>
                      height: 80.0
                    - !<WrapContentWidth>
                      align: "Start"
                  children:
                    - id: "wirelessAlertsTitleText"
                      trait: !<TextTrait>
                        text: !<StringIntrinsicValue>
                          value: "Wireless emergency alerts"
                        colorWrapper: !<ColorIntrinsicValue>
                          value:
                            themeColor: "OnSurface"
                        textStyleWrapper: !<EnumProperty>
                          value: !<TextStyleWrapper> "TitleLarge"
                      label: "Text"
                      level: 5
                      lazyListChildParams: !<FixedNumber>
                        numOfItems: 1
                      dynamicItems: null
                      actionHandler: !<ActionHandlerImpl>
                        actionsMap:
                          "OnLongClick": [ ]
                          "OnClick": [ ]
                          "OnDoubleClick": [ ]
                      visibilityParams: { }
                  lazyListChildParams: !<FixedNumber>
                    numOfItems: 1
                  dynamicItems: null
                  actionHandler: !<ActionHandlerImpl>
                    actionsMap:
                      "OnLongClick": [ ]
                      "OnClick": [ ]
                      "OnDoubleClick": [ ]
                  visibilityParams: { }
                - id: "wirelessAlertsSpacer"
                  trait: !<EmptyTrait> { }
                  label: "Empty"
                  level: 4
                  modifierList:
                    - !<Weight> { }
                    - !<FillMaxHeight> { }
                    - !<Padding>
                      start: 8.0
                      top: 8.0
                      end: 8.0
                      bottom: 8.0
                  lazyListChildParams: !<FixedNumber>
                    numOfItems: 1
                  dynamicItems: null
                  actionHandler: !<ActionHandlerImpl>
                    actionsMap:
                      "OnLongClick": [ ]
                      "OnClick": [ ]
                      "OnDoubleClick": [ ]
                  visibilityParams: { }
              lazyListChildParams: !<FixedNumber> { }
              dynamicItems: null
              actionHandler: !<ActionHandlerImpl>
                actionsMap:
                  "OnLongClick": [ ]
                  "OnClick": [ ]
                  "OnDoubleClick": [ ]
              visibilityParams: { }
          lazyListChildParams: !<FixedNumber>
            numOfItems: 1
          dynamicItems: null
          actionHandler: !<ActionHandlerImpl>
            actionsMap:
              "OnLongClick": [ ]
              "OnClick": [ ]
              "OnDoubleClick": [ ]
          visibilityParams: { }
      lazyListChildParams: !<FixedNumber>
        numOfItems: 1
      dynamicItems: null
      actionHandler: !<ActionHandlerImpl>
        actionsMap:
          "OnLongClick": [ ]
          "OnClick": [ ]
          "OnDoubleClick": [ ]
      visibilityParams: { }
  lazyListChildParams: !<FixedNumber> { }
  dynamicItems: null
  actionHandler: !<ActionHandlerImpl>
    actionsMap:
      "OnInitialLoad": [ ]
  visibilityParams: { }
showOnNavigation: false
title: "Settings"
label: "Settings"
icon: !<Filled> "Settings"
isDefault: false
isSelected: false
topAppBarNode:
  id: "topAppBar"
  trait: !<TopAppBarTrait>
    title: !<StringIntrinsicValue>
      value: "Settings"
    topAppBarType: "CenterAligned"
  label: "TopAppBar"
  children:
    - id: "navIcon"
      trait: !<IconTrait>
        imageVectorHolder: null
      label: "Nav Icon"
      level: 1
      lazyListChildParams: !<FixedNumber> { }
      dynamicItems: null
      actionHandler: !<ActionHandlerImpl>
        actionsMap:
          "OnLongClick": [ ]
          "OnClick": [ ]
          "OnDoubleClick": [ ]
      visibilityParams: { }
  lazyListChildParams: !<FixedNumber> { }
  dynamicItems: null
  actionHandler: !<ActionHandlerImpl>
    actionsMap:
      "OnLongClick": [ ]
      "OnClick": [ ]
      "OnDoubleClick": [ ]
  visibilityParams: { }
bottomAppBarNode: null
navigationDrawerNode: null
fabNode: null
stateHolderImpl:
  states: [ ]"""
}
