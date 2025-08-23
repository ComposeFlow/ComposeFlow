package io.composeflow.template

/**
 * Constants containing the YAML content for screen templates.
 * These are used for WASM compatibility where resource loading might not be available.
 */
object ScreenTemplateConstants {
    const val BLANK_SCREEN_TEMPLATE = """id: "blankScreen"
name: "Blank"
rootNode:
  id: "blankScreen"
  trait: !<ScreenTrait> { }
  label: "Blank"
  children:
    - id: "rootColumn"
      trait: !<ColumnTrait> { }
      label: "Root"
      level: 1
      modifierList:
        - !<FillMaxWidth> { }
        - !<Weight> { }
      lazyListChildParams: !<FixedNumber>
        numOfItems: 1
      dynamicItems: null
      actionHandler: !<ActionHandlerImpl>
        actionsMap:
          "OnDoubleClick": [ ]
          "OnLongClick": [ ]
          "OnClick": [ ]
      inspectable: false
      visibilityParams: { }
  lazyListChildParams: !<FixedNumber>
    numOfItems: 1
  dynamicItems: null
  actionHandler: !<ActionHandlerImpl>
    actionsMap:
      "OnInitialLoad": [ ]
  visibilityParams: { }
showOnNavigation: false
title: "Blank"
label: "Blank"
icon: !<Filled> "ExitToApp"
isDefault: false
isSelected: false
topAppBarNode:
  id: "topAppBar"
  trait: !<TopAppBarTrait>
    title: !<StringIntrinsicValue>
      value: "Blank"
    topAppBarType: "CenterAligned"
  label: "TopAppBar"
  children:
    - id: "navIcon"
      trait: !<IconTrait>
        imageVectorHolder: null
      label: "Nav Icon"
      level: 1
      lazyListChildParams: !<FixedNumber>
        numOfItems: 1
      dynamicItems: null
      actionHandler: !<ActionHandlerImpl>
        actionsMap:
          "OnDoubleClick": [ ]
          "OnLongClick": [ ]
          "OnClick": [ ]
      visibilityParams: { }
  lazyListChildParams: !<FixedNumber>
    numOfItems: 1
  dynamicItems: null
  actionHandler: !<ActionHandlerImpl>
    actionsMap:
      "OnDoubleClick": [ ]
      "OnLongClick": [ ]
      "OnClick": [ ]
  visibilityParams: { }
bottomAppBarNode: null
navigationDrawerNode: null
fabNode: null"""

    const val MESSAGES_SCREEN_TEMPLATE = """id: "messagesRoot"
name: "Messages"
rootNode:
  id: "messagesScreen"
  trait: !<ScreenTrait> { }
  label: "Messages"
  children:
    - id: "column_1"
      trait: !<ColumnTrait> { }
      label: "Column"
      level: 1
      modifierList:
        - !<FillMaxWidth> { }
        - !<Weight> { }
      children:
        - id: "searchTextField"
          trait: !<TextFieldTrait>
            value: !<ValueFromCompanionState> { }
            placeholder: !<StringIntrinsicValue>
              value: "Search replies"
            leadingIcon: !<Outlined> "Search"
            trailingIcon: !<Outlined> "Close"
            singleLine: null
            shapeWrapper: !<Circle> { }
            transparentIndicator: true
          label: "TextField"
          level: 2
          modifierList:
            - !<Padding>
              start: 16.0
              top: 16.0
              end: 16.0
              bottom: 16.0
            - !<FillMaxWidth> { }
          lazyListChildParams: !<FixedNumber>
            numOfItems: 1
          dynamicItems: null
          actionHandler: !<ActionHandlerImpl>
            actionsMap:
              "OnChange": [ ]
              "OnUnfocused": [ ]
              "OnSubmit": [ ]
              "OnFocused": [ ]
          visibilityParams: { }
        - id: "messagesList"
          trait: !<LazyColumnTrait>
            contentPadding: 1.0
            defaultChildNumOfItems: 1
          label: "LazyColumn"
          level: 2
          modifierList:
            - !<Padding>
              start: 16.0
              end: 16.0
            - !<FillMaxSize> { }
          children:
            - id: "messageCardColumn"
              trait: !<ColumnTrait> { }
              label: "Column"
              level: 3
              modifierList:
                - !<Padding>
                  bottom: 16.0
                - !<FillMaxWidth> { }
                - !<Height>
                  height: 180.0
                - !<Background>
                  colorWrapper: !<ColorIntrinsicValue>
                    value:
                      themeColor: "SurfaceVariant"
                  shapeWrapper: !<RoundedCorner>
                    topStart: 16.0
                    topEnd: 16.0
                    bottomEnd: 16.0
                    bottomStart: 16.0
              children:
                - id: "messageHeaderRow"
                  trait: !<RowTrait>
                    horizontalArrangement: "Start"
                    verticalAlignment: "Top"
                  label: "Row"
                  level: 4
                  modifierList:
                    - !<WrapContentHeight> { }
                    - !<FillMaxWidth> { }
                    - !<Padding>
                      start: 16.0
                      top: 16.0
                      end: 16.0
                      bottom: 8.0
                  children:
                    - id: "senderAvatarImage"
                      trait: !<ImageTrait>
                        url: !<StringIntrinsicValue>
                          value: "https://picsum.photos/64"
                      label: "Image"
                      level: 5
                      modifierList:
                        - !<Size>
                          width: 64.0
                          height: 64.0
                        - !<Clip>
                          shapeWrapper: !<Circle> { }
                      lazyListChildParams: !<FixedNumber>
                        numOfItems: 1
                      dynamicItems: null
                      actionHandler: !<ActionHandlerImpl>
                        actionsMap:
                          "OnDoubleClick": [ ]
                          "OnLongClick": [ ]
                          "OnClick": [ ]
                      visibilityParams: { }
                    - id: "senderInfoColumn"
                      trait: !<ColumnTrait> { }
                      label: "Column"
                      level: 5
                      modifierList:
                        - !<Size>
                          width: 120.0
                        - !<Padding>
                          start: 16.0
                          top: 8.0
                          end: 16.0
                          bottom: 8.0
                        - !<Weight> { }
                      children:
                        - id: "senderNameText"
                          trait: !<TextTrait>
                            text: !<StringIntrinsicValue>
                              value: "David"
                            colorWrapper: !<ColorIntrinsicValue>
                              value:
                                themeColor: "OnSurface"
                          label: "Text"
                          level: 6
                          lazyListChildParams: !<FixedNumber>
                            numOfItems: 1
                          dynamicItems: null
                          actionHandler: !<ActionHandlerImpl>
                            actionsMap:
                              "OnDoubleClick": [ ]
                              "OnLongClick": [ ]
                              "OnClick": [ ]
                          visibilityParams: { }
                        - id: "timestampText"
                          trait: !<TextTrait>
                            text: !<StringIntrinsicValue>
                              value: "20 mins ago"
                            colorWrapper: !<ColorIntrinsicValue>
                              value:
                                themeColor: "Secondary"
                            textStyleWrapper: !<EnumProperty>
                              value: !<TextStyleWrapper> "BodySmall"
                          label: "Text"
                          level: 6
                          lazyListChildParams: !<FixedNumber>
                            numOfItems: 1
                          dynamicItems: null
                          actionHandler: !<ActionHandlerImpl>
                            actionsMap:
                              "OnDoubleClick": [ ]
                              "OnLongClick": [ ]
                              "OnClick": [ ]
                          visibilityParams: { }
                      lazyListChildParams: !<FixedNumber>
                        numOfItems: 1
                      dynamicItems: null
                      actionHandler: !<ActionHandlerImpl>
                        actionsMap:
                          "OnDoubleClick": [ ]
                          "OnLongClick": [ ]
                          "OnClick": [ ]
                      visibilityParams: { }
                    - id: "starIcon"
                      trait: !<IconTrait>
                        imageVectorHolder: !<Outlined> "StarBorder"
                        contentDescription: "Icon for Add"
                        tint: !<ColorIntrinsicValue>
                          value:
                            themeColor: "Secondary"
                      label: "Icon"
                      level: 5
                      modifierList:
                        - !<Background>
                          colorWrapper: !<ColorIntrinsicValue>
                            value:
                              themeColor: "Background"
                          shapeWrapper: !<Circle> { }
                        - !<Padding>
                          start: 16.0
                          top: 16.0
                          end: 16.0
                          bottom: 16.0
                      lazyListChildParams: !<FixedNumber>
                        numOfItems: 1
                      dynamicItems: null
                      actionHandler: !<ActionHandlerImpl>
                        actionsMap:
                          "OnDoubleClick": [ ]
                          "OnLongClick": [ ]
                          "OnClick": [ ]
                      visibilityParams: { }
                  lazyListChildParams: !<FixedNumber>
                    numOfItems: 1
                  dynamicItems: null
                  actionHandler: !<ActionHandlerImpl>
                    actionsMap:
                      "OnDoubleClick": [ ]
                      "OnLongClick": [ ]
                      "OnClick": [ ]
                  visibilityParams: { }
                - id: "messageBodyColumn"
                  trait: !<ColumnTrait> { }
                  label: "Column"
                  level: 4
                  modifierList:
                    - !<FillMaxHeight> { }
                    - !<FillMaxWidth> { }
                    - !<Size>
                      width: 120.0
                    - !<Padding>
                      start: 16.0
                      top: 8.0
                      end: 16.0
                      bottom: 8.0
                  children:
                    - id: "messageTitleText"
                      trait: !<TextTrait>
                        text: !<StringIntrinsicValue>
                          value: "Package delivered!"
                        colorWrapper: !<ColorIntrinsicValue>
                          value:
                            themeColor: "OnSurface"
                        textStyleWrapper: !<EnumProperty>
                          value: !<TextStyleWrapper> "TitleSmall"
                      label: "Text"
                      level: 5
                      lazyListChildParams: !<FixedNumber>
                        numOfItems: 1
                      dynamicItems: null
                      actionHandler: !<ActionHandlerImpl>
                        actionsMap:
                          "OnDoubleClick": [ ]
                          "OnLongClick": [ ]
                          "OnClick": [ ]
                      visibilityParams: { }
                    - id: "messageContentText"
                      trait: !<TextTrait>
                        text: !<StringIntrinsicValue>
                          value: "Great news! Your package has arrived safely and is now waiting for you at your doorstep. Please check it at your earliest convenience and let us know if everything is in order. Enjoy your purchase!"
                        colorWrapper: !<ColorIntrinsicValue>
                          value:
                            themeColor: "OnSurface"
                        overflow: !<EnumProperty>
                          value: !<TextOverflowWrapper> "Ellipsis"
                        maxLines: 2
                        textStyleWrapper: !<EnumProperty>
                          value: !<TextStyleWrapper> "BodyMedium"
                      label: "Text"
                      level: 5
                      modifierList:
                        - !<Padding>
                          top: 8.0
                          bottom: 8.0
                      lazyListChildParams: !<FixedNumber>
                        numOfItems: 1
                      dynamicItems: null
                      actionHandler: !<ActionHandlerImpl>
                        actionsMap:
                          "OnDoubleClick": [ ]
                          "OnLongClick": [ ]
                          "OnClick": [ ]
                      visibilityParams: { }
                  lazyListChildParams: !<FixedNumber>
                    numOfItems: 1
                  dynamicItems: null
                  actionHandler: !<ActionHandlerImpl>
                    actionsMap:
                      "OnDoubleClick": [ ]
                      "OnLongClick": [ ]
                      "OnClick": [ ]
                  visibilityParams: { }
              lazyListChildParams: !<FixedNumber>
                numOfItems: 10
              dynamicItems: null
              actionHandler: !<ActionHandlerImpl>
                actionsMap:
                  "OnDoubleClick": [ ]
                  "OnLongClick": [ ]
                  "OnClick": [ ]
              visibilityParams: { }
          lazyListChildParams: !<FixedNumber>
            numOfItems: 1
          dynamicItems: null
          actionHandler: !<ActionHandlerImpl>
            actionsMap:
              "OnDoubleClick": [ ]
              "OnLongClick": [ ]
              "OnClick": [ ]
          visibilityParams: { }
      lazyListChildParams: !<FixedNumber>
        numOfItems: 1
      dynamicItems: null
      actionHandler: !<ActionHandlerImpl>
        actionsMap:
          "OnDoubleClick": [ ]
          "OnLongClick": [ ]
          "OnClick": [ ]
      visibilityParams: { }
  lazyListChildParams: !<FixedNumber> { }
  dynamicItems: null
  actionHandler: !<ActionHandlerImpl>
    actionsMap:
      "OnInitialLoad": [ ]
  visibilityParams: { }
showOnNavigation: false
title: "Messages"
label: "Messages"
icon: !<Outlined> "Message"
isDefault: false
isSelected: false
topAppBarNode:
  id: "topAppBar"
  trait: !<TopAppBarTrait>
    title: !<StringIntrinsicValue>
      value: "Messages"
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
          "OnDoubleClick": [ ]
          "OnLongClick": [ ]
          "OnClick": [ ]
      visibilityParams: { }
  lazyListChildParams: !<FixedNumber> { }
  dynamicItems: null
  actionHandler: !<ActionHandlerImpl>
    actionsMap:
      "OnDoubleClick": [ ]
      "OnLongClick": [ ]
      "OnClick": [ ]
  visibilityParams: { }
bottomAppBarNode: null
navigationDrawerNode: null
fabNode: null
stateHolderImpl:
  states: [ ]"""
}
