package io.composeflow.template

/**
 * Additional screen template constants for WASM compatibility.
 * Split into a separate file due to size constraints.
 */
object ScreenTemplateConstants2 {
    
    const val LOGIN_SCREEN_TEMPLATE = """id: "loginRoot"
name: "Login"
rootNode:
  id: "loginScreen"
  trait: !<ScreenTrait> { }
  label: "Login"
  children:
    - id: "rootColumn"
      trait: !<ColumnTrait> { }
      label: "Root"
      level: 1
      modifierList:
        - !<FillMaxWidth> { }
        - !<Weight> { }
      children:
        - id: "mainContentColumn"
          trait: !<ColumnTrait> { }
          label: "Column"
          level: 2
          modifierList:
            - !<FillMaxHeight> { }
            - !<FillMaxWidth> { }
          children:
            - id: "topSpacer"
              trait: !<SpacerTrait> { }
              label: "Spacer"
              level: 3
              modifierList:
                - !<Width>
                  width: 48.0
                - !<Height>
                  height: 48.0
              lazyListChildParams: !<FixedNumber>
                numOfItems: 1
              dynamicItems: null
              actionHandler: !<ActionHandlerImpl>
                actionsMap:
                  "OnDoubleClick": [ ]
                  "OnLongClick": [ ]
                  "OnClick": [ ]
              visibilityParams: { }
            - id: "titleRow"
              trait: !<RowTrait>
                horizontalArrangement: "Center"
                verticalAlignment: "CenterVertically"
              label: "Row"
              level: 3
              modifierList:
                - !<FillMaxWidth> { }
                - !<Height>
                  height: 120.0
                - !<Padding>
                  start: 8.0
                  top: 8.0
                  end: 8.0
                  bottom: 8.0
              children:
                - id: "titleText"
                  trait: !<TextTrait>
                    text: !<StringIntrinsicValue>
                      value: "App template"
                    colorWrapper: !<ColorIntrinsicValue>
                      value:
                        themeColor: "OnSurface"
                    textStyleWrapper: !<EnumProperty>
                      value: !<TextStyleWrapper> "DisplayMedium"
                  label: "Text"
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
            - id: "authTabs"
              trait: !<TabsTrait> { }
              label: "Tabs"
              level: 3
              modifierList:
                - !<FillMaxSize> { }
              children:
                - id: "authTabRow"
                  trait: !<TabRowTrait> { }
                  label: "TabRow"
                  level: 4
                  children:
                    - id: "signInTab"
                      trait: !<TabTrait>
                        text: !<StringIntrinsicValue>
                          value: "Sign In"
                        index: 1
                      label: "Tab"
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
                    - id: "signUpTab"
                      trait: !<TabTrait>
                        text: !<StringIntrinsicValue>
                          value: "Sign Up"
                        index: 1
                      label: "Tab"
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
                  lazyListChildParams: !<FixedNumber>
                    numOfItems: 1
                  dynamicItems: null
                  actionHandler: !<ActionHandlerImpl>
                    actionsMap:
                      "OnDoubleClick": [ ]
                      "OnLongClick": [ ]
                      "OnClick": [ ]
                  visibilityParams: { }
                - id: "signInTabContent"
                  trait: !<TabContentTrait> { }
                  label: "TabContent"
                  level: 4
                  modifierList:
                    - !<FillMaxSize> { }
                  children:
                    - id: "signInColumn"
                      trait: !<ColumnTrait>
                        horizontalAlignmentWrapper: "CenterHorizontally"
                      label: "Column"
                      level: 5
                      modifierList:
                        - !<FillMaxHeight> { }
                        - !<FillMaxWidth> { }
                      children:
                        - id: "signInSpacer"
                          trait: !<SpacerTrait> { }
                          label: "Spacer"
                          level: 6
                          modifierList:
                            - !<Width>
                              width: 48.0
                            - !<Height>
                              height: 48.0
                          lazyListChildParams: !<FixedNumber>
                            numOfItems: 1
                          dynamicItems: null
                          actionHandler: !<ActionHandlerImpl>
                            actionsMap:
                              "OnDoubleClick": [ ]
                              "OnLongClick": [ ]
                              "OnClick": [ ]
                          visibilityParams: { }
                        - id: "signInEmailTextField"
                          trait: !<TextFieldTrait>
                            value: !<ValueFromCompanionState> { }
                            label: !<StringIntrinsicValue>
                              value: "email"
                            shapeWrapper: !<Circle> { }
                            textFieldType: "Outlined"
                            enableValidator: true
                            textFieldValidator: !<EmailValidator> { }
                          label: "email"
                          level: 6
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
                              "OnChange": [ ]
                              "OnUnfocused": [ ]
                              "OnSubmit": [ ]
                              "OnFocused": [ ]
                          visibilityParams: { }
                        - id: "signInPasswordTextField"
                          trait: !<TextFieldTrait>
                            value: !<ValueFromCompanionState> { }
                            label: !<StringIntrinsicValue>
                              value: "password"
                            shapeWrapper: !<Circle> { }
                            textFieldType: "Outlined"
                            visualTransformation: !<PasswordVisualTransformation> { }
                          label: "password"
                          level: 6
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
                              "OnChange": [ ]
                              "OnUnfocused": [ ]
                              "OnSubmit": [ ]
                              "OnFocused": [ ]
                          visibilityParams: { }
                        - id: "signInSpacer2"
                          trait: !<SpacerTrait> { }
                          label: "Spacer"
                          level: 6
                          modifierList:
                            - !<Width>
                              width: 8.0
                            - !<Height>
                              height: 8.0
                          lazyListChildParams: !<FixedNumber>
                            numOfItems: 1
                          dynamicItems: null
                          actionHandler: !<ActionHandlerImpl>
                            actionsMap:
                              "OnDoubleClick": [ ]
                              "OnLongClick": [ ]
                              "OnClick": [ ]
                          visibilityParams: { }
                        - id: "signInButton"
                          trait: !<ButtonTrait>
                            contentText: !<StringIntrinsicValue>
                              value: "Login"
                          label: "Button"
                          level: 6
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
                - id: "signUpTabContent"
                  trait: !<TabContentTrait> { }
                  label: "TabContent"
                  level: 4
                  modifierList:
                    - !<FillMaxSize> { }
                  children:
                    - id: "signUpColumn"
                      trait: !<ColumnTrait>
                        horizontalAlignmentWrapper: "CenterHorizontally"
                      label: "Column"
                      level: 5
                      modifierList:
                        - !<FillMaxHeight> { }
                        - !<FillMaxWidth> { }
                      children:
                        - id: "signUpSpacer"
                          trait: !<SpacerTrait> { }
                          label: "Spacer"
                          level: 6
                          modifierList:
                            - !<Width>
                              width: 48.0
                            - !<Height>
                              height: 48.0
                          lazyListChildParams: !<FixedNumber>
                            numOfItems: 1
                          dynamicItems: null
                          actionHandler: !<ActionHandlerImpl>
                            actionsMap:
                              "OnDoubleClick": [ ]
                              "OnLongClick": [ ]
                              "OnClick": [ ]
                          visibilityParams: { }
                        - id: "signUpEmailTextField"
                          trait: !<TextFieldTrait>
                            value: !<ValueFromCompanionState> { }
                            label: !<StringIntrinsicValue>
                              value: "email"
                            shapeWrapper: !<Circle> { }
                            textFieldType: "Outlined"
                            enableValidator: true
                            textFieldValidator: !<EmailValidator> { }
                          label: "email"
                          level: 6
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
                              "OnChange": [ ]
                              "OnUnfocused": [ ]
                              "OnSubmit": [ ]
                              "OnFocused": [ ]
                          visibilityParams: { }
                        - id: "signUpNameTextField"
                          trait: !<TextFieldTrait>
                            value: !<ValueFromCompanionState> { }
                            label: !<StringIntrinsicValue>
                              value: "name"
                            shapeWrapper: !<Circle> { }
                            textFieldType: "Outlined"
                          label: "name"
                          level: 6
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
                              "OnChange": [ ]
                              "OnUnfocused": [ ]
                              "OnSubmit": [ ]
                              "OnFocused": [ ]
                          visibilityParams: { }
                        - id: "signUpPasswordTextField"
                          trait: !<TextFieldTrait>
                            value: !<ValueFromCompanionState> { }
                            label: !<StringIntrinsicValue>
                              value: "password"
                            shapeWrapper: !<Circle> { }
                            textFieldType: "Outlined"
                            visualTransformation: !<PasswordVisualTransformation> { }
                          label: "password"
                          level: 6
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
                              "OnChange": [ ]
                              "OnUnfocused": [ ]
                              "OnSubmit": [ ]
                              "OnFocused": [ ]
                          visibilityParams: { }
                        - id: "signUpSpacer2"
                          trait: !<SpacerTrait> { }
                          label: "Spacer"
                          level: 6
                          modifierList:
                            - !<Width>
                              width: 8.0
                            - !<Height>
                              height: 8.0
                          lazyListChildParams: !<FixedNumber>
                            numOfItems: 1
                          dynamicItems: null
                          actionHandler: !<ActionHandlerImpl>
                            actionsMap:
                              "OnDoubleClick": [ ]
                              "OnLongClick": [ ]
                              "OnClick": [ ]
                          visibilityParams: { }
                        - id: "signUpButton"
                          trait: !<ButtonTrait>
                            contentText: !<StringIntrinsicValue>
                              value: "Sign up"
                          label: "Button"
                          level: 6
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
      "OnInitialLoad": [ ]
  visibilityParams: { }
showOnNavigation: false
title: "Login"
label: "Login"
icon: !<Outlined> "Login"
isDefault: false
isSelected: false
topAppBarNode:
  id: "topAppBar"
  trait: !<TopAppBarTrait>
    title: !<StringIntrinsicValue>
      value: "Login"
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
fabNode: null
stateHolderImpl:
  states: [ ]"""
}