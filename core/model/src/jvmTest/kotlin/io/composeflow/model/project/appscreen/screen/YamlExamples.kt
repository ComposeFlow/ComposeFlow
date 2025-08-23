package io.composeflow.model.project.appscreen.screen

/**
 * An example yaml with IntScreenState which failed before adding IntScreenState
 */
const val SCREEN_YAML_WITH_INT_SCREEN_STATE = """
id: "editProfileRoot"
name: "Edit Profile"
rootNode:
    id: "editProfileScreen"
    trait: !<ScreenTrait> { }
    label: "Edit Profile"
    children:
        -   id: "mainScrollColumn"
            trait: !<ColumnTrait> { }
            label: "Main Column"
            level: 1
            modifierList:
                - !<FillMaxSize> { }
                - !<VerticalScroll> { }
            children:
                -   id: "coverPhotoSection"
                    trait: !<BoxTrait>
                        contentAlignment: "Center"
                    label: "Cover Photo Section"
                    level: 2
                    modifierList:
                        - !<FillMaxWidth> { }
                        - !<Height>
                            height: 200.0
                        - !<Background>
                            colorWrapper: !<ColorIntrinsicValue>
                                value:
                                    themeColor: "SurfaceVariant"
                    children:
                        -   id: "coverPhotoImage"
                            trait: !<ImageTrait>
                                url: !<StringIntrinsicValue>
                                    value: "https://picsum.photos/400/200"
                                contentScaleWrapper: !<EnumProperty>
                                    value: !<ContentScaleWrapper> "Crop"
                            label: "Cover Photo"
                            level: 3
                            modifierList:
                                - !<FillMaxSize> { }
                            lazyListChildParams: !<FixedNumber>
                                numOfItems: 1
                            dynamicItems: null
                            actionHandler: !<ActionHandlerImpl>
                                actionsMap:
                                    "OnClick":
                                        - !<Simple>
                                            id: "coverPhotoPickerAction"
                                            action: !<ShowInformationDialog>
                                                id: "coverPhotoDialog"
                                                title: !<StringIntrinsicValue>
                                                    value: "Change Cover Photo"
                                                message: !<StringIntrinsicValue>
                                                    value: "Select a new cover photo from your gallery. You can crop and resize it after selection."
                                                confirmText: !<StringIntrinsicValue>
                                                    value: "Select Photo"
                                    "OnDoubleClick": [ ]
                                    "OnLongClick": [ ]
                            visibilityParams: { }
                        -   id: "coverPhotoEditIcon"
                            trait: !<IconTrait>
                                imageVectorHolder: !<Outlined> "Edit"
                                contentDescription: "Edit cover photo"
                                tint: !<ColorIntrinsicValue>
                                    value:
                                        themeColor: "OnPrimary"
                            label: "Edit Cover Icon"
                            level: 3
                            modifierList:
                                - !<Background>
                                    colorWrapper: !<ColorIntrinsicValue>
                                        value:
                                            themeColor: "Primary"
                                    shapeWrapper: !<Circle> { }
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
                                    "OnClick":
                                        - !<Simple>
                                            id: "coverPhotoEditAction"
                                            action: !<ShowInformationDialog>
                                                id: "coverEditDialog"
                                                title: !<StringIntrinsicValue>
                                                    value: "Edit Cover Photo"
                                                message: !<StringIntrinsicValue>
                                                    value: "Crop and resize your cover photo to fit perfectly."
                                                confirmText: !<StringIntrinsicValue>
                                                    value: "Edit"
                                    "OnDoubleClick": [ ]
                                    "OnLongClick": [ ]
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
                -   id: "profilePhotoSection"
                    trait: !<BoxTrait>
                        contentAlignment: "Center"
                    label: "Profile Photo Section"
                    level: 2
                    modifierList:
                        - !<FillMaxWidth> { }
                        - !<Height>
                            height: 120.0
                        - !<Offset>
                            x: 0.0
                            y: -60.0
                    children:
                        -   id: "profilePhotoImage"
                            trait: !<ImageTrait>
                                url: !<StringIntrinsicValue>
                                    value: "https://picsum.photos/120"
                                contentScaleWrapper: !<EnumProperty>
                                    value: !<ContentScaleWrapper> "Crop"
                            label: "Profile Photo"
                            level: 3
                            modifierList:
                                - !<Size>
                                    width: 120.0
                                    height: 120.0
                                - !<Clip>
                                    shapeWrapper: !<Circle> { }
                                - !<Border>
                                    width: 4.0
                                    colorWrapper: !<ColorIntrinsicValue>
                                        value:
                                            themeColor: "Surface"
                                    shapeWrapper: !<Circle> { }
                            lazyListChildParams: !<FixedNumber>
                                numOfItems: 1
                            dynamicItems: null
                            actionHandler: !<ActionHandlerImpl>
                                actionsMap:
                                    "OnClick":
                                        - !<Simple>
                                            id: "profilePhotoPickerAction"
                                            action: !<ShowInformationDialog>
                                                id: "profilePhotoDialog"
                                                title: !<StringIntrinsicValue>
                                                    value: "Change Profile Photo"
                                                message: !<StringIntrinsicValue>
                                                    value: "Select a new profile photo from your gallery. You can crop it to a perfect circle after selection."
                                                confirmText: !<StringIntrinsicValue>
                                                    value: "Select Photo"
                                    "OnDoubleClick": [ ]
                                    "OnLongClick": [ ]
                            visibilityParams: { }
                        -   id: "profilePhotoEditIcon"
                            trait: !<IconTrait>
                                imageVectorHolder: !<Outlined> "CameraAlt"
                                contentDescription: "Edit profile photo"
                                tint: !<ColorIntrinsicValue>
                                    value:
                                        themeColor: "OnPrimary"
                            label: "Edit Profile Icon"
                            level: 3
                            modifierList:
                                - !<Background>
                                    colorWrapper: !<ColorIntrinsicValue>
                                        value:
                                            themeColor: "Primary"
                                    shapeWrapper: !<Circle> { }
                                - !<Padding>
                                    start: 6.0
                                    top: 6.0
                                    end: 6.0
                                    bottom: 6.0
                                - !<Offset>
                                    x: 35.0
                                    y: 35.0
                            lazyListChildParams: !<FixedNumber>
                                numOfItems: 1
                            dynamicItems: null
                            actionHandler: !<ActionHandlerImpl>
                                actionsMap:
                                    "OnClick":
                                        - !<Simple>
                                            id: "profilePhotoEditAction"
                                            action: !<ShowInformationDialog>
                                                id: "profileEditDialog"
                                                title: !<StringIntrinsicValue>
                                                    value: "Edit Profile Photo"
                                                message: !<StringIntrinsicValue>
                                                    value: "Crop and resize your profile photo to create the perfect avatar."
                                                confirmText: !<StringIntrinsicValue>
                                                    value: "Edit"
                                    "OnDoubleClick": [ ]
                                    "OnLongClick": [ ]
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
                -   id: "formFieldsColumn"
                    trait: !<ColumnTrait> { }
                    label: "Form Fields"
                    level: 2
                    modifierList:
                        - !<FillMaxWidth> { }
                        - !<Padding>
                            start: 16.0
                            top: 16.0
                            end: 16.0
                            bottom: 16.0
                    children:
                        -   id: "displayNameField"
                            trait: !<TextFieldTrait>
                                value: !<ValueFromCompanionState> { }
                                label: !<StringIntrinsicValue>
                                    value: "Display Name"
                                textFieldType: "Outlined"
                                enableValidator: true
                                textFieldValidator: !<StringValidator>
                                    minLength: 1
                                    maxLength: 50
                                    allowBlank: false
                            label: "Display Name"
                            level: 3
                            modifierList:
                                - !<FillMaxWidth> { }
                                - !<Padding>
                                    bottom: 16.0
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
                        -   id: "usernameField"
                            trait: !<TextFieldTrait>
                                value: !<ValueFromCompanionState> { }
                                label: !<StringIntrinsicValue>
                                    value: "Username"
                                placeholder: !<StringIntrinsicValue>
                                    value: "@username"
                                textFieldType: "Outlined"
                                enableValidator: true
                                textFieldValidator: !<StringValidator>
                                    minLength: 3
                                    maxLength: 30
                                    allowBlank: false
                            label: "Username"
                            level: 3
                            modifierList:
                                - !<FillMaxWidth> { }
                                - !<Padding>
                                    bottom: 8.0
                            lazyListChildParams: !<FixedNumber>
                                numOfItems: 1
                            dynamicItems: null
                            actionHandler: !<ActionHandlerImpl>
                                actionsMap:
                                    "OnChange":
                                        - !<Simple>
                                            id: "usernameValidationAction"
                                            action: !<SetAppStateValue>
                                                id: "validateUsername"
                                                setValueToStates:
                                                    -   writeToStateId: "usernameValidationState"
                                                        operation: !<SetValue>
                                                            readProperty: !<StringIntrinsicValue>
                                                                value: "Checking availability..."
                                    "OnUnfocused": [ ]
                                    "OnSubmit": [ ]
                                    "OnFocused": [ ]
                            visibilityParams: { }
                        -   id: "usernameValidationText"
                            trait: !<TextTrait>
                                text: !<ValueFromState>
                                    readFromStateId: "usernameValidationState"
                                colorWrapper: !<ColorIntrinsicValue>
                                    value:
                                        themeColor: "Secondary"
                                textStyleWrapper: !<EnumProperty>
                                    value: !<TextStyleWrapper> "BodySmall"
                            label: "Username Validation"
                            level: 3
                            modifierList:
                                - !<Padding>
                                    start: 16.0
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
                        -   id: "bioField"
                            trait: !<TextFieldTrait>
                                value: !<ValueFromCompanionState> { }
                                label: !<StringIntrinsicValue>
                                    value: "Bio"
                                placeholder: !<StringIntrinsicValue>
                                    value: "Tell us about yourself..."
                                textFieldType: "Outlined"
                                maxLines: 4
                                enableValidator: true
                                textFieldValidator: !<StringValidator>
                                    minLength: 0
                                    maxLength: 150
                                    allowBlank: true
                            label: "Bio"
                            level: 3
                            modifierList:
                                - !<FillMaxWidth> { }
                                - !<Padding>
                                    bottom: 8.0
                            lazyListChildParams: !<FixedNumber>
                                numOfItems: 1
                            dynamicItems: null
                            actionHandler: !<ActionHandlerImpl>
                                actionsMap:
                                    "OnChange":
                                        - !<Simple>
                                            id: "bioCharacterCountAction"
                                            action: !<SetAppStateValue>
                                                id: "updateBioCount"
                                                setValueToStates:
                                                    -   writeToStateId: "bioCharacterCountState"
                                                        operation: !<SetValue>
                                                            readProperty: !<ValueFromState>
                                                                readFromStateId: "bioField-companionState"
                                                                propertyTransformers:
                                                                    - !<length> { }
                                    "OnUnfocused": [ ]
                                    "OnSubmit": [ ]
                                    "OnFocused": [ ]
                            visibilityParams: { }
                        -   id: "bioCharacterCountRow"
                            trait: !<RowTrait>
                                horizontalArrangement: "End"
                            label: "Bio Character Count"
                            level: 3
                            modifierList:
                                - !<FillMaxWidth> { }
                                - !<Padding>
                                    start: 16.0
                                    bottom: 16.0
                            children:
                                -   id: "bioCharacterCountText"
                                    trait: !<TextTrait>
                                        text: !<ValueFromState>
                                            readFromStateId: "bioCharacterCountState"
                                            propertyTransformers:
                                                - !<AddAfter>
                                                    value: !<StringIntrinsicValue>
                                                        value: "/150"
                                        colorWrapper: !<ColorIntrinsicValue>
                                            value:
                                                themeColor: "Secondary"
                                        textStyleWrapper: !<EnumProperty>
                                            value: !<TextStyleWrapper> "BodySmall"
                                    label: "Character Count"
                                    level: 4
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
                        -   id: "websiteField"
                            trait: !<TextFieldTrait>
                                value: !<ValueFromCompanionState> { }
                                label: !<StringIntrinsicValue>
                                    value: "Website"
                                placeholder: !<StringIntrinsicValue>
                                    value: "https://yourwebsite.com"
                                leadingIcon: !<Outlined> "Language"
                                textFieldType: "Outlined"
                            label: "Website"
                            level: 3
                            modifierList:
                                - !<FillMaxWidth> { }
                                - !<Padding>
                                    bottom: 16.0
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
                        -   id: "locationField"
                            trait: !<TextFieldTrait>
                                value: !<ValueFromCompanionState> { }
                                label: !<StringIntrinsicValue>
                                    value: "Location"
                                placeholder: !<StringIntrinsicValue>
                                    value: "City, Country"
                                leadingIcon: !<Outlined> "LocationOn"
                                textFieldType: "Outlined"
                            label: "Location"
                            level: 3
                            modifierList:
                                - !<FillMaxWidth> { }
                                - !<Padding>
                                    bottom: 32.0
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
            "OnInitialLoad":
                - !<Simple>
                    id: "loadProfileDataAction"
                    action: !<SetAppStateValue>
                        id: "loadProfileData"
                        setValueToStates:
                            -   writeToStateId: "usernameValidationState"
                                operation: !<SetValue>
                                    readProperty: !<StringIntrinsicValue>
                                        value: "Username is available"
                            -   writeToStateId: "bioCharacterCountState"
                                operation: !<SetValue>
                                    readProperty: !<IntIntrinsicValue>
                                        value: 0
    visibilityParams: { }
showOnNavigation: false
title: "Edit Profile"
label: "Edit Profile"
icon: !<Outlined> "Edit"
isDefault: false
isSelected: false
topAppBarNode:
    id: "editProfileTopAppBar"
    trait: !<TopAppBarTrait>
        title: !<StringIntrinsicValue>
            value: "Edit Profile"
        topAppBarType: "CenterAligned"
    label: "TopAppBar"
    children:
        -   id: "cancelButton"
            trait: !<IconTrait>
                imageVectorHolder: !<Outlined> "Close"
                contentDescription: "Cancel editing"
            label: "Cancel Button"
            level: 1
            lazyListChildParams: !<FixedNumber>
                numOfItems: 1
            dynamicItems: null
            actionHandler: !<ActionHandlerImpl>
                actionsMap:
                    "OnClick":
                        - !<Simple>
                            id: "cancelEditAction"
                            action: !<ShowConfirmationDialog>
                                id: "cancelConfirmDialog"
                                title: !<StringIntrinsicValue>
                                    value: "Discard Changes?"
                                message: !<StringIntrinsicValue>
                                    value: "Are you sure you want to discard your changes? All unsaved changes will be lost."
                                positiveText: !<StringIntrinsicValue>
                                    value: "Discard"
                                negativeText: !<StringIntrinsicValue>
                                    value: "Keep Editing"
                                dialogOpenVariableName: "cancelConfirmDialog"
                    "OnDoubleClick": [ ]
                    "OnLongClick": [ ]
            visibilityParams: { }
        -   id: "saveButton"
            trait: !<IconTrait>
                imageVectorHolder: !<Outlined> "Check"
                contentDescription: "Save changes"
                tint: !<ColorIntrinsicValue>
                    value:
                        themeColor: "Primary"
            label: "Save Button"
            level: 1
            lazyListChildParams: !<FixedNumber>
                numOfItems: 1
            dynamicItems: null
            actionHandler: !<ActionHandlerImpl>
                actionsMap:
                    "OnClick":
                        - !<Simple>
                            id: "saveProfileAction"
                            action: !<Snackbar>
                                id: "saveSuccessSnackbar"
                                message: !<StringIntrinsicValue>
                                    value: "Profile updated successfully!"
                    "OnDoubleClick": [ ]
                    "OnLongClick": [ ]
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
    states:
        - !<StringScreenState>
            id: "usernameValidationState"
            name: "usernameValidation"
            defaultValue: "Username is available"
            companionNodeId: ""
            userWritable: true
        - !<IntScreenState>
            id: "bioCharacterCountState"
            name: "bioCharacterCount"
            defaultValue: 0
            companionNodeId: ""
            userWritable: true
parameters: [ ]
"""
