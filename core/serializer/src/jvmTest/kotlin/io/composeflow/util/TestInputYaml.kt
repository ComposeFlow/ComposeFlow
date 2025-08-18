package io.composeflow.util

val testInputScreenYaml = """
id: "a1b2c3d4-e5f6-4789-9012-3456789abcde"
name: "Home"
rootNode:
  id: "f0e9d8c7-b6a5-4432-b123-4567890abcde"
  paletteNode: "Column"
  label: "Root"
  level: 0
  modifierList:
    - !<io.composeflow.model.modifier.ModifierWrapper.FillMaxWidth>
      fraction: 1.0
    - !<io.composeflow.model.modifier.ModifierWrapper.Weight>
      weight: 1.0
      fill: true
  params: !<io.composeflow.model.parameter.EmptyParams> { }
  children:
    - id: "98765432-1fed-4abc-a987-6543210fedcb"
      paletteNode: "TextField"
      label: "Search Bar"
      level: 1
      modifierList:
        - !<io.composeflow.model.modifier.ModifierWrapper.Padding>
          start:
            value: 16.0
          top:
            value: 16.0
          end:
            value: 16.0
          bottom:
            value: 16.0
        - !<io.composeflow.model.modifier.ModifierWrapper.FillMaxWidth>
          fraction: 1.0
      params: !<io.composeflow.model.parameter.TextFieldParams>
        value: !<io.composeflow.model.property.ValueFromState>
          readFromStateId: "2b3c4d5e-6f7a-4890-b123-cdef01234567"
          propertyTransformers: [ ]
        enabled: null
        readOnly: null
        label: null
        placeholder: !<io.composeflow.model.property.StringProperty.StringIntrinsicValue>
          value: "Search places"
          propertyTransformers: [ ]
        leadingIcon: !<io.composeflow.materialicons.Outlined> "Search"
        trailingIcon: null
        singleLine: true
        maxLines: null
        shapeWrapper: !<io.composeflow.model.parameter.wrapper.ShapeWrapper.RoundedCorner>
          topStart:
            value: 8.0
          topEnd:
            value: 8.0
          bottomEnd:
            value: 8.0
          bottomStart:
            value: 8.0
        textFieldType: "Outlined"
        transparentIndicator: true
        enableValidator: false
        textFieldValidator: null
        passwordField: false
      children: [ ]
      lazyListChildParams: !<io.composeflow.model.parameter.lazylist.LazyListChildParams.FixedNumber>
        numOfItems: 5
      dynamicItems: null
      actionHandler: !<io.composeflow.model.project.appscreen.screen.composenode.ActionHandlerImpl>
        actionsMap:
          "OnClick": [ ]
          "OnDoubleClick": [ ]
          "OnLongClick": [ ]
          "OnChange": [ ]
          "OnFocused": [ ]
          "OnUnfocused": [ ]
          "OnSubmit": [ ]
      inspectable: true
      visibilityParams:
        nodeVisibility: !<io.composeflow.model.property.EnumProperty>
          value: !<io.composeflow.model.enumwrapper.NodeVisibility> "AlwaysVisible"
          propertyTransformers: [ ]
        visibilityCondition: !<io.composeflow.model.property.BooleanProperty.Empty> { }
        formFactorVisibility:
          visibleInCompact: true
          visibleInMedium: true
          visibleInExpanded: true
        visibleInUiBuilder: true
      companionStateId: "2b3c4d5e-6f7a-4890-b123-cdef01234567"
      componentId: null
    - id: "34567890-abcd-4ef0-9123-4567890abcde"
      paletteNode: "LazyColumn"
      label: "Places List"
      level: 1
      modifierList:
        - !<io.composeflow.model.modifier.ModifierWrapper.FillMaxSize>
          fraction: 1.0
      params: !<io.composeflow.model.parameter.LazyColumnParams>
        contentPadding: null
        reverseLayout: null
        verticalArrangement: null
        horizontalAlignment: null
        userScrollEnabled: null
        defaultChildNumOfItems: 5
      children:
        - id: "abcdef01-2345-4678-9abc-def012345678"
          paletteNode: "Card"
          label: "Place Card"
          level: 2
          modifierList:
            - !<io.composeflow.model.modifier.ModifierWrapper.Padding>
              start:
                value: 16.0
              top:
                value: 8.0
              end:
                value: 16.0
              bottom:
                value: 8.0
            - !<io.composeflow.model.modifier.ModifierWrapper.FillMaxWidth>
              fraction: 1.0
          params: !<io.composeflow.model.parameter.CardParams>
            cardType: "Elevated"
          children:
            - id: "bcdef012-3456-4789-a012-3456789abcde"
              paletteNode: "Column"
              label: "Card Content"
              level: 3
              modifierList: [ ]
              params: !<io.composeflow.model.parameter.EmptyParams> { }
              children:
                - id: "cdef0123-4567-489a-b234-567890abcdef"
                  paletteNode: "Image"
                  label: "Place Image"
                  level: 4
                  modifierList:
                    - !<io.composeflow.model.modifier.ModifierWrapper.FillMaxWidth>
                      fraction: 1.0
                    - !<io.composeflow.model.modifier.ModifierWrapper.Height>
                      height:
                        value: 200.0
                  params: !<io.composeflow.model.parameter.ImageParams>
                    url: !<io.composeflow.model.property.StringProperty.StringIntrinsicValue>
                      value: "https://picsum.photos/480"
                      propertyTransformers: [ ]
                    alignmentWrapper: null
                    contentScaleWrapper: null
                    alpha: null
                    placeholderUrl: !<io.composeflow.model.parameter.PlaceholderUrl.NoUsage> { }
                  children: [ ]
                  lazyListChildParams: !<io.composeflow.model.parameter.lazylist.LazyListChildParams.FixedNumber>
                    numOfItems: 5
                  dynamicItems: null
                  actionHandler: !<io.composeflow.model.project.appscreen.screen.composenode.ActionHandlerImpl>
                    actionsMap:
                      "OnClick": [ ]
                      "OnDoubleClick": [ ]
                      "OnLongClick": [ ]
                  inspectable: true
                  visibilityParams:
                    nodeVisibility: !<io.composeflow.model.property.EnumProperty>
                      value: !<io.composeflow.model.enumwrapper.NodeVisibility> "AlwaysVisible"
                      propertyTransformers: [ ]
                    visibilityCondition: !<io.composeflow.model.property.BooleanProperty.Empty> { }
                    formFactorVisibility:
                      visibleInCompact: true
                      visibleInMedium: true
                      visibleInExpanded: true
                    visibleInUiBuilder: true
                  companionStateId: null
                  componentId: null
                - id: "def01234-5678-49ab-c345-67890abcdef0"
                  paletteNode: "Column"
                  label: "Place Details"
                  level: 4
                  modifierList:
                    - !<io.composeflow.model.modifier.ModifierWrapper.Padding>
                      start:
                        value: 16.0
                      top:
                        value: 8.0
                      end:
                        value: 16.0
                      bottom:
                        value: 8.0
                  params: !<io.composeflow.model.parameter.EmptyParams> { }
                  children:
                    - id: "ef012345-6789-4abc-d567-890abcdef012"
                      paletteNode: "Text"
                      label: "Place Name"
                      level: 5
                      modifierList: [ ]
                      params: !<io.composeflow.model.parameter.TextParams>
                        text: !<io.composeflow.model.property.StringProperty.StringIntrinsicValue>
                          value: "Cozy Apartment in Downtown"
                          propertyTransformers: [ ]
                        colorWrapper: !<io.composeflow.model.property.ColorProperty.ColorIntrinsicValue>
                          value:
                            themeColor: "OnSurface"
                            color: null
                          propertyTransformers: [ ]
                        fontStyle: null
                        textDecoration: null
                        textAlign: null
                        overflow: null
                        softWrap: null
                        maxLines: null
                        textStyleWrapper: !<io.composeflow.model.property.EnumProperty>
                          value: !<io.composeflow.model.enumwrapper.TextStyleWrapper> "TitleMedium"
                          propertyTransformers: [ ]
                        parseHtml: null
                      children: [ ]
                      lazyListChildParams: !<io.composeflow.model.parameter.lazylist.LazyListChildParams.FixedNumber>
                        numOfItems: 5
                      dynamicItems: null
                      actionHandler: !<io.composeflow.model.project.appscreen.screen.composenode.ActionHandlerImpl>
                        actionsMap:
                          "OnClick": [ ]
                          "OnDoubleClick": [ ]
                          "OnLongClick": [ ]
                      inspectable: true
                      visibilityParams:
                        nodeVisibility: !<io.composeflow.model.property.EnumProperty>
                          value: !<io.composeflow.model.enumwrapper.NodeVisibility> "AlwaysVisible"
                          propertyTransformers: [ ]
                        visibilityCondition: !<io.composeflow.model.property.BooleanProperty.Empty> { }
                        formFactorVisibility:
                          visibleInCompact: true
                          visibleInMedium: true
                          visibleInExpanded: true
                        visibleInUiBuilder: true
                      companionStateId: null
                      componentId: null
                    - id: "f0123456-789a-4bcd-e678-90abcdef0123"
                      paletteNode: "Text"
                      label: "Place Location"
                      level: 5
                      modifierList: [ ]
                      params: !<io.composeflow.model.parameter.TextParams>
                        text: !<io.composeflow.model.property.StringProperty.StringIntrinsicValue>
                          value: "New York, NY"
                          propertyTransformers: [ ]
                        colorWrapper: !<io.composeflow.model.property.ColorProperty.ColorIntrinsicValue>
                          value:
                            themeColor: "Secondary"
                            color: null
                          propertyTransformers: [ ]
                        fontStyle: null
                        textDecoration: null
                        textAlign: null
                        overflow: null
                        softWrap: null
                        maxLines: null
                        textStyleWrapper: !<io.composeflow.model.property.EnumProperty>
                          value: !<io.composeflow.model.enumwrapper.TextStyleWrapper> "BodyMedium"
                          propertyTransformers: [ ]
                        parseHtml: null
                      children: [ ]
                      lazyListChildParams: !<io.composeflow.model.parameter.lazylist.LazyListChildParams.FixedNumber>
                        numOfItems: 5
                      dynamicItems: null
                      actionHandler: !<io.composeflow.model.project.appscreen.screen.composenode.ActionHandlerImpl>
                        actionsMap:
                          "OnClick": [ ]
                          "OnDoubleClick": [ ]
                          "OnLongClick": [ ]
                      inspectable: true
                      visibilityParams:
                        nodeVisibility: !<io.composeflow.model.property.EnumProperty>
                          value: !<io.composeflow.model.enumwrapper.NodeVisibility> "AlwaysVisible"
                          propertyTransformers: [ ]
                        visibilityCondition: !<io.composeflow.model.property.BooleanProperty.Empty> { }
                        formFactorVisibility:
                          visibleInCompact: true
                          visibleInMedium: true
                          visibleInExpanded: true
                        visibleInUiBuilder: true
                      companionStateId: null
                      componentId: null
                  lazyListChildParams: !<io.composeflow.model.parameter.lazylist.LazyListChildParams.FixedNumber>
                    numOfItems: 5
                  dynamicItems: null
                  actionHandler: !<io.composeflow.model.project.appscreen.screen.composenode.ActionHandlerImpl>
                    actionsMap:
                      "OnClick": [ ]
                      "OnDoubleClick": [ ]
                      "OnLongClick": [ ]
                  inspectable: true
                  visibilityParams:
                    nodeVisibility: !<io.composeflow.model.property.EnumProperty>
                      value: !<io.composeflow.model.enumwrapper.NodeVisibility> "AlwaysVisible"
                      propertyTransformers: [ ]
                    visibilityCondition: !<io.composeflow.model.property.BooleanProperty.Empty> { }
                    formFactorVisibility:
                      visibleInCompact: true
                      visibleInMedium: true
                      visibleInExpanded: true
                    visibleInUiBuilder: true
                  companionStateId: null
                  componentId: null
          lazyListChildParams: !<io.composeflow.model.parameter.lazylist.LazyListChildParams.FixedNumber>
            numOfItems: 5
          dynamicItems: null
          actionHandler: !<io.composeflow.model.project.appscreen.screen.composenode.ActionHandlerImpl>
            actionsMap:
              "OnClick": [ ]
              "OnDoubleClick": [ ]
              "OnLongClick": [ ]
          inspectable: true
          visibilityParams:
            nodeVisibility: !<io.composeflow.model.property.EnumProperty>
              value: !<io.composeflow.model.enumwrapper.NodeVisibility> "AlwaysVisible"
              propertyTransformers: [ ]
            visibilityCondition: !<io.composeflow.model.property.BooleanProperty.Empty> { }
            formFactorVisibility:
              visibleInCompact: true
              visibleInMedium: true
              visibleInExpanded: true
            visibleInUiBuilder: true
          companionStateId: null
          componentId: null
      lazyListChildParams: !<io.composeflow.model.parameter.lazylist.LazyListChildParams.FixedNumber>
        numOfItems: 5
      dynamicItems: null
      actionHandler: !<io.composeflow.model.project.appscreen.screen.composenode.ActionHandlerImpl>
        actionsMap:
          "OnClick": [ ]
          "OnDoubleClick": [ ]
          "OnLongClick": [ ]
      inspectable: true
      visibilityParams:
        nodeVisibility: !<io.composeflow.model.property.EnumProperty>
          value: !<io.composeflow.model.enumwrapper.NodeVisibility> "AlwaysVisible"
          propertyTransformers: [ ]
        visibilityCondition: !<io.composeflow.model.property.BooleanProperty.Empty> { }
        formFactorVisibility:
          visibleInCompact: true
          visibleInMedium: true
          visibleInExpanded: true
        visibleInUiBuilder: true
      companionStateId: null
      componentId: null
  lazyListChildParams: !<io.composeflow.model.parameter.lazylist.LazyListChildParams.FixedNumber>
    numOfItems: 5
  dynamicItems: null
  actionHandler: !<io.composeflow.model.project.appscreen.screen.composenode.ActionHandlerImpl>
    actionsMap:
      "OnInitialLoad": [ ]
  inspectable: true
  visibilityParams:
    nodeVisibility: !<io.composeflow.model.property.EnumProperty>
      value: !<io.composeflow.model.enumwrapper.NodeVisibility> "AlwaysVisible"
      propertyTransformers: [ ]
    visibilityCondition: !<io.composeflow.model.property.BooleanProperty.Empty> { }
    formFactorVisibility:
      visibleInCompact: true
      visibleInMedium: true
      visibleInExpanded: true
    visibleInUiBuilder: true
  companionStateId: null
  componentId: null
showOnNavigation: true
title: "Home"
label: "Home"
icon: !<io.composeflow.materialicons.Outlined> "Home"
isDefault: true
isSelected: true
topAppBarNode:
  id: "12345678-9abc-4def-a012-3456789abcde"
  paletteNode: "TopAppBar"
  label: "TopAppBar"
  level: 0
  modifierList: [ ]
  params: !<io.composeflow.model.parameter.TopAppBarParams>
    title: !<io.composeflow.model.property.StringProperty.StringIntrinsicValue>
      value: "Home"
      propertyTransformers: [ ]
    topAppBarType: "CenterAligned"
    scrollBehaviorWrapper: "EnterAlways"
  children: [ ]
  lazyListChildParams: !<io.composeflow.model.parameter.lazylist.LazyListChildParams.FixedNumber>
    numOfItems: 5
  dynamicItems: null
  actionHandler: !<io.composeflow.model.project.appscreen.screen.composenode.ActionHandlerImpl>
    actionsMap:
      "OnClick": [ ]
      "OnDoubleClick": [ ]
      "OnLongClick": [ ]
  inspectable: true
  visibilityParams:
    nodeVisibility: !<io.composeflow.model.property.EnumProperty>
      value: !<io.composeflow.model.enumwrapper.NodeVisibility> "AlwaysVisible"
      propertyTransformers: [ ]
    visibilityCondition: !<io.composeflow.model.property.BooleanProperty.Empty> { }
    formFactorVisibility:
      visibleInCompact: true
      visibleInMedium: true
      visibleInExpanded: true
    visibleInUiBuilder: true
  companionStateId: null
  componentId: null
isTopAppBarSet: true
bottomAppBarNode: null
isBottomAppBarSet: false
showFab: false
parameters: [ ]
stateHolderImpl:
  states:
    - !<io.composeflow.model.state.ScreenState.StringScreenState>
      id: "2b3c4d5e-6f7a-4890-b123-cdef01234567"
      name: "searchQuery"
      defaultValue: ""
      userWritable: true
      companionNodeId: "98765432-1fed-4abc-a987-6543210fedcb"
      isList: false 
"""
