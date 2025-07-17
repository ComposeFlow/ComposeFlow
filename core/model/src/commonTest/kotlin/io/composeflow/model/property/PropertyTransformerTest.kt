package io.composeflow.model.property

import androidx.compose.runtime.mutableStateOf
import io.composeflow.kotlinpoet.GenerationContext
import io.composeflow.model.datatype.DataField
import io.composeflow.model.datatype.DataFieldType
import io.composeflow.model.datatype.DataType
import io.composeflow.model.datatype.FieldType
import io.composeflow.model.parameter.wrapper.InstantWrapper
import io.composeflow.model.project.Project
import io.composeflow.model.state.AppState
import io.composeflow.model.type.ComposeFlowType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import org.junit.Assert.assertEquals
import kotlin.test.Test

class PropertyTransformerTest {

    private val project = Project()
    private val context = GenerationContext()

    @Test
    fun testAddBefore() {
        val transformer = FromString.ToString.AddBefore(
            value = mutableStateOf(StringProperty.StringIntrinsicValue("before string ")),
        )

        val property = StringProperty.StringIntrinsicValue("test string")
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(result.toString(), """"before string ".plus("test string")""")
    }

    @Test
    fun testAddAfter() {
        val transformer = FromString.ToString.AddAfter(
            value = mutableStateOf(StringProperty.StringIntrinsicValue("after string")),
        )

        val property = StringProperty.StringIntrinsicValue("test string")
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(result.toString(), """"test string".plus("after string")""")
    }

    @Test
    fun testSubstringBefore() {
        val transformer = FromString.ToString.SubstringBefore(
            value = mutableStateOf(StringProperty.StringIntrinsicValue("?")),
        )

        val property = StringProperty.StringIntrinsicValue("test string?afterquestion")
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(result.toString(), """"test string?afterquestion".substringBefore("?")""")
    }

    @Test
    fun testSubstringAfter() {
        val transformer = FromString.ToString.SubstringAfter(
            value = mutableStateOf(StringProperty.StringIntrinsicValue("?")),
        )

        val property = StringProperty.StringIntrinsicValue("test string?afterquestion")
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(result.toString(), """"test string?afterquestion".substringAfter("?")""")
    }


    @Test
    fun testContains() {
        val transformer = FromString.ToBoolean.StringContains(
            value = mutableStateOf(StringProperty.StringIntrinsicValue("contains")),
        )

        val property = StringProperty.StringIntrinsicValue("test string")
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(
            project,
            context,
            writeType = ComposeFlowType.BooleanType(),
            dryRun = false
        )

        assertEquals(result.toString(), """"test string".contains("contains")""")
    }

    @Test
    fun testStartsWith() {
        val transformer = FromString.ToBoolean.StartsWith(
            value = mutableStateOf(StringProperty.StringIntrinsicValue("argument")),
        )

        val property = StringProperty.StringIntrinsicValue("test string")
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(
            project,
            context,
            writeType = ComposeFlowType.BooleanType(),
            dryRun = false
        )

        assertEquals(result.toString(), """"test string".startsWith("argument")""")
    }

    @Test
    fun testEndsWith() {
        val transformer = FromString.ToBoolean.EndsWith(
            value = mutableStateOf(StringProperty.StringIntrinsicValue("argument")),
        )

        val property = StringProperty.StringIntrinsicValue("test string")
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(
            project,
            context,
            writeType = ComposeFlowType.BooleanType(),
            dryRun = false
        )

        assertEquals(result.toString(), """"test string".endsWith("argument")""")
    }

    @Test
    fun testIsEmpty() {
        val transformer = FromString.ToBoolean.IsEmpty

        val property = StringProperty.StringIntrinsicValue("test string")
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(
            project,
            context,
            writeType = ComposeFlowType.BooleanType(),
            dryRun = false
        )

        assertEquals(result.toString(), """"test string".isEmpty()""")
    }

    @Test
    fun testLength() {
        val transformer = FromString.ToInt.Length

        val property = StringProperty.StringIntrinsicValue("test string")
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(
            project,
            context,
            writeType = ComposeFlowType.IntType(),
            dryRun = false
        )

        assertEquals(result.toString(), """"test string".length""")
    }

    @Test
    fun testSplit() {
        val transformer =
            FromString.ToStringList.Split(mutableStateOf(StringProperty.StringIntrinsicValue(",")))

        val property = StringProperty.StringIntrinsicValue("test,string")
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(
            project,
            context,
            writeType = ComposeFlowType.StringType(isList = true),
            dryRun = false
        )

        assertEquals(result.toString(), """("test,string").split(",")""")
    }

    @Test
    fun testToString() {
        val transformer = FromBoolean.ToStringType.ToString

        val property = BooleanProperty.BooleanIntrinsicValue()
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(result.toString(), "false.toString()")
    }

    @Test
    fun testToggleValue() {
        val transformer = FromBoolean.ToBoolean.ToggleValue

        val property = BooleanProperty.BooleanIntrinsicValue()
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(result.toString(), "(!false)")
    }

    @Test
    fun testPlus() {
        val transformer = FromInt.ToInt.IntPlus(mutableStateOf(IntProperty.IntIntrinsicValue(3)))

        val property = IntProperty.IntIntrinsicValue()
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(result.toString(), "0 + 3")
    }

    @Test
    fun testMultipliedBy() {
        val transformer =
            FromInt.ToInt.IntMultipliedBy(mutableStateOf(IntProperty.IntIntrinsicValue(3)))

        val property = IntProperty.IntIntrinsicValue(2)
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(result.toString(), "2 * 3")
    }

    @Test
    fun testLessThan() {
        val transformer =
            FromInt.ToBoolean.IntLessThan(mutableStateOf(IntProperty.IntIntrinsicValue(3)))

        val property = IntProperty.IntIntrinsicValue(2)
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(result.toString(), "2 < 3")
    }

    @Test
    fun testLessThanOrEqualTo() {
        val transformer =
            FromInt.ToBoolean.IntLessThanOrEqualTo(mutableStateOf(IntProperty.IntIntrinsicValue(3)))

        val property = IntProperty.IntIntrinsicValue(2)
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(result.toString(), "2 <= 3")
    }

    @Test
    fun testEquals() {
        val transformer =
            FromInt.ToBoolean.IntEquals(mutableStateOf(IntProperty.IntIntrinsicValue(3)))

        val property = IntProperty.IntIntrinsicValue(2)
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(result.toString(), "2 == 3")
    }

    @Test
    fun testGreaterThanOrEqualTo() {
        val transformer =
            FromInt.ToBoolean.IntGreaterThanOrEqualTo(mutableStateOf(IntProperty.IntIntrinsicValue(3)))

        val property = IntProperty.IntIntrinsicValue(2)
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(result.toString(), "2 >= 3")
    }

    @Test
    fun testGreaterThan() {
        val transformer =
            FromInt.ToBoolean.IntGreaterThan(mutableStateOf(IntProperty.IntIntrinsicValue(3)))

        val property = IntProperty.IntIntrinsicValue(2)
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(result.toString(), "2 > 3")
    }

    @Test
    fun testPlusFloat() {
        val transformer =
            FromFloat.ToFloat.FloatPlus(mutableStateOf(FloatProperty.FloatIntrinsicValue(3f)))

        val property = FloatProperty.FloatIntrinsicValue()
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(result.toString(), "0.0f + 3.0f")
    }

    @Test
    fun testMultipliedByFloat() {
        val transformer =
            FromFloat.ToFloat.FloatMultipliedBy(mutableStateOf(FloatProperty.FloatIntrinsicValue(3f)))

        val property = FloatProperty.FloatIntrinsicValue(2f)
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(result.toString(), "2.0f * 3.0f")
    }

    @Test
    fun testLessThanFloat() {
        val transformer =
            FromFloat.ToBoolean.FloatLessThan(mutableStateOf(FloatProperty.FloatIntrinsicValue(3f)))

        val property = FloatProperty.FloatIntrinsicValue(2f)
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(result.toString(), "2.0f < 3.0f")
    }

    @Test
    fun testLessThanOrEqualToFloat() {
        val transformer =
            FromFloat.ToBoolean.FloatLessThanOrEqualTo(
                mutableStateOf(
                    FloatProperty.FloatIntrinsicValue(
                        3f
                    )
                )
            )

        val property = FloatProperty.FloatIntrinsicValue(2f)
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(result.toString(), "2.0f <= 3.0f")
    }

    @Test
    fun testEqualsFloat() {
        val transformer =
            FromFloat.ToBoolean.FloatEquals(mutableStateOf(FloatProperty.FloatIntrinsicValue(3f)))

        val property = FloatProperty.FloatIntrinsicValue(2f)
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(result.toString(), "2.0f == 3.0f")
    }

    @Test
    fun testGreaterThanOrEqualToFloat() {
        val transformer =
            FromFloat.ToBoolean.FloatGreaterThanOrEqualTo(
                mutableStateOf(
                    FloatProperty.FloatIntrinsicValue(
                        3f
                    )
                )
            )

        val property = FloatProperty.FloatIntrinsicValue(2f)
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(result.toString(), "2.0f >= 3.0f")
    }

    @Test
    fun testGreaterThanFloat() {
        val transformer =
            FromFloat.ToBoolean.FloatGreaterThan(mutableStateOf(FloatProperty.FloatIntrinsicValue(3f)))

        val property = FloatProperty.FloatIntrinsicValue(2f)
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(result.toString(), "2.0f > 3.0f")
    }

    @Test
    fun testPlusDayInstant() {
        val transformer =
            FromInstant.ToInstant.PlusDay(mutableStateOf(IntProperty.IntIntrinsicValue(3)))

        val instant = LocalDate(2024, 7, 15).atStartOfDayIn(TimeZone.UTC)
        val property = InstantProperty.InstantIntrinsicValue(
            InstantWrapper(instant)
        )
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(
            result.toString(),
            "kotlinx.datetime.LocalDate.parse(\"2024-07-15\").kotlinx.datetime.atStartOfDayIn(kotlinx.datetime.TimeZone.UTC).kotlinx.datetime.plus(3, kotlinx.datetime.DateTimeUnit.DAY, kotlinx.datetime.TimeZone.currentSystemDefault())"
        )
    }

    @Test
    fun testPlusMonthInstant() {
        val transformer =
            FromInstant.ToInstant.PlusMonth(mutableStateOf(IntProperty.IntIntrinsicValue(3)))

        val instant = LocalDate(2024, 7, 15).atStartOfDayIn(TimeZone.UTC)
        val property = InstantProperty.InstantIntrinsicValue(
            InstantWrapper(instant)
        )
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(
            result.toString(),
            "kotlinx.datetime.LocalDate.parse(\"2024-07-15\").kotlinx.datetime.atStartOfDayIn(kotlinx.datetime.TimeZone.UTC).kotlinx.datetime.plus(3, kotlinx.datetime.DateTimeUnit.MONTH, kotlinx.datetime.TimeZone.currentSystemDefault())"
        )
    }

    @Test
    fun testPlusYearInstant() {
        val transformer =
            FromInstant.ToInstant.PlusYear(mutableStateOf(IntProperty.IntIntrinsicValue(3)))

        val instant = LocalDate(2024, 7, 15).atStartOfDayIn(TimeZone.UTC)
        val property = InstantProperty.InstantIntrinsicValue(
            InstantWrapper(instant)
        )
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(
            result.toString(),
            "kotlinx.datetime.LocalDate.parse(\"2024-07-15\").kotlinx.datetime.atStartOfDayIn(kotlinx.datetime.TimeZone.UTC).kotlinx.datetime.plus(3 * 12, kotlinx.datetime.DateTimeUnit.MONTH, kotlinx.datetime.TimeZone.currentSystemDefault())"
        )
    }

    @Test
    fun testListContains() {
        val transformer = FromList.ToBoolean.ListContains(
            innerType = ComposeFlowType.StringType(),
            mutableStateOf(StringProperty.StringIntrinsicValue("textIncluded"))
        )

        val stringListState = AppState.StringListAppState(name = "stringList")
        project.globalStateHolder.addState(stringListState)
        val property = ValueFromState(readFromStateId = stringListState.id)
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(result.toString(), "stringList.contains(\"textIncluded\")")
    }

    @Test
    fun testListFilter() {
        val transformer = FromList.ToList.Filter(
            innerType = ComposeFlowType.StringType(), mutableStateOf(
                FunctionScopeParameterProperty(
                    functionName = "filter",
                    variableType = ComposeFlowType.StringType(),
                ).apply {
                    propertyTransformers.add(
                        FromString.ToBoolean.StringContains(
                            mutableStateOf(
                                StringProperty.StringIntrinsicValue("filtered")
                            )
                        )
                    )
                })
        )

        val stringListState = AppState.StringListAppState(name = "stringList")
        project.globalStateHolder.addState(stringListState)
        val property = ValueFromState(readFromStateId = stringListState.id)
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(result.toString(), """stringList.filter { it.contains("filtered") }""")
    }

    @Test
    fun testListFilter_dataType() {
        val stringField = DataField(name = "stringField", fieldType = FieldType.String())
        val intField = DataField(name = "intField", fieldType = FieldType.Int())
        val dataType = DataType(name = "dataType").apply {
            fields.add(stringField)
            fields.add(intField)
        }
        project.dataTypeHolder.dataTypes.add(dataType)
        val transformer = FromList.ToList.Filter(
            innerType = ComposeFlowType.CustomDataType(dataTypeId = dataType.id), mutableStateOf(
                FunctionScopeParameterProperty(
                    functionName = "filter",
                    variableType = ComposeFlowType.CustomDataType(dataTypeId = dataType.id),
                    dataFieldType = DataFieldType.FieldInDataType(
                        dataType.id,
                        fieldId = stringField.id
                    ),
                ).apply {
                    propertyTransformers.add(
                        FromString.ToBoolean.StringContains(
                            mutableStateOf(
                                StringProperty.StringIntrinsicValue("filtered")
                            )
                        )
                    )
                }
            )
        )

        val dataTypeListState =
            AppState.CustomDataTypeListAppState(name = "dataTypeList", dataTypeId = dataType.id)
        project.globalStateHolder.addState(dataTypeListState)
        val property = ValueFromState(readFromStateId = dataTypeListState.id)
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(
            result.toString(),
            """dataTypeList.filter { it.stringField.contains("filtered") }"""
        )
    }

    @Test
    fun testListMap_dataType() {
        val stringField = DataField(name = "stringField", fieldType = FieldType.String())
        val intField = DataField(name = "intField", fieldType = FieldType.Int())
        val dataType = DataType(name = "dataType").apply {
            fields.add(stringField)
            fields.add(intField)
        }
        project.dataTypeHolder.dataTypes.add(dataType)
        val transformer = FromList.ToList.Map(
            innerType = ComposeFlowType.CustomDataType(dataTypeId = dataType.id),
            outputType = mutableStateOf(ComposeFlowType.CustomDataType(dataTypeId = dataType.id)),
            value = mutableStateOf(
                AssignablePropertyValue.ForDataType(
                    dataTypeId = dataType.id,
                    properties = mutableMapOf(
                        stringField.id to FunctionScopeParameterProperty(
                            functionName = "map",
                            variableType = ComposeFlowType.CustomDataType(dataTypeId = dataType.id),
                            dataFieldType = DataFieldType.FieldInDataType(
                                dataType.id,
                                fieldId = stringField.id
                            ),
                        )
                    )
                )
            ),
        )

        val dataTypeListState =
            AppState.CustomDataTypeListAppState(name = "dataTypeList", dataTypeId = dataType.id)
        project.globalStateHolder.addState(dataTypeListState)
        val property = ValueFromState(readFromStateId = dataTypeListState.id)
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(
            result.toString(),
            """dataTypeList.map { com.example.datatype.DataType(stringField = it.stringField,intField = 0,) }"""
        )
    }

    @Test
    fun testListIsEmpty() {
        val transformer = FromList.ToBoolean.IsEmpty(innerType = ComposeFlowType.StringType())

        val stringListState = AppState.StringListAppState(name = "stringList")
        project.globalStateHolder.addState(stringListState)
        val property = ValueFromState(readFromStateId = stringListState.id)
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(result.toString(), "stringList.isEmpty()")
    }

    @Test
    fun testListSize() {
        val transformer = FromList.ToInt.Size(innerType = ComposeFlowType.StringType())

        val stringListState = AppState.StringListAppState(name = "stringList")
        project.globalStateHolder.addState(stringListState)
        val property = ValueFromState(readFromStateId = stringListState.id)
        property.propertyTransformers.add(transformer)
        val result = property.transformedCodeBlock(project, context, dryRun = false)

        assertEquals(result.toString(), "stringList.size")
    }

    // Tests for getAssignableProperties() method
    @Test
    fun testGetAssignableProperties_singleProperty() {
        val transformer = FromString.ToString.AddBefore(
            value = mutableStateOf(StringProperty.StringIntrinsicValue("test"))
        )

        val assignableProperties = transformer.getAssignableProperties()
        assertEquals(1, assignableProperties.size)
        assertEquals("test", (assignableProperties[0] as StringProperty.StringIntrinsicValue).value)
    }

    @Test
    fun testGetAssignableProperties_nestedProperties() {
        // Create a nested property with a transformer
        val nestedProperty = StringProperty.StringIntrinsicValue("nested")
        nestedProperty.propertyTransformers.add(
            FromString.ToString.AddAfter(mutableStateOf(StringProperty.StringIntrinsicValue("suffix")))
        )

        val transformer = FromString.ToString.AddBefore(value = mutableStateOf(nestedProperty))

        val assignableProperties = transformer.getAssignableProperties()
        // Should contain: nestedProperty itself + the suffix property from its transformer
        assertEquals(2, assignableProperties.size)
        assertEquals("nested", (assignableProperties[0] as StringProperty.StringIntrinsicValue).value)
        assertEquals("suffix", (assignableProperties[1] as StringProperty.StringIntrinsicValue).value)
    }

    @Test
    fun testGetAssignableProperties_multipleFields() {
        val transformer = FromInt.ToBoolean.IntModEqualsTo(
            mod = mutableStateOf(IntProperty.IntIntrinsicValue(5)),
            equalsTo = mutableStateOf(IntProperty.IntIntrinsicValue(0))
        )

        val assignableProperties = transformer.getAssignableProperties()
        assertEquals(2, assignableProperties.size)
        assertEquals(5, (assignableProperties[0] as IntProperty.IntIntrinsicValue).value)
        assertEquals(0, (assignableProperties[1] as IntProperty.IntIntrinsicValue).value)
    }

    @Test
    fun testGetAssignableProperties_multipleFieldsWithNested() {
        // Create nested properties for both fields
        val modProperty = IntProperty.IntIntrinsicValue(5)
        modProperty.propertyTransformers.add(
            FromInt.ToInt.IntPlus(mutableStateOf(IntProperty.IntIntrinsicValue(1)))
        )

        val equalsToProperty = IntProperty.IntIntrinsicValue(0)
        equalsToProperty.propertyTransformers.add(
            FromInt.ToInt.IntMultipliedBy(mutableStateOf(IntProperty.IntIntrinsicValue(2)))
        )

        val transformer = FromInt.ToBoolean.IntModEqualsTo(
            mod = mutableStateOf(modProperty),
            equalsTo = mutableStateOf(equalsToProperty)
        )

        val assignableProperties = transformer.getAssignableProperties()
        // Should contain: modProperty, equalsToProperty, and their nested properties
        assertEquals(4, assignableProperties.size)
        assertEquals(5, (assignableProperties[0] as IntProperty.IntIntrinsicValue).value)
        assertEquals(0, (assignableProperties[1] as IntProperty.IntIntrinsicValue).value)
        assertEquals(1, (assignableProperties[2] as IntProperty.IntIntrinsicValue).value)
        assertEquals(2, (assignableProperties[3] as IntProperty.IntIntrinsicValue).value)
    }

    @Test
    fun testGetAssignableProperties_listFilter() {
        val conditionProperty = FunctionScopeParameterProperty(
            functionName = "filter",
            variableType = ComposeFlowType.StringType(),
        ).apply {
            propertyTransformers.add(
                FromString.ToBoolean.StringContains(
                    mutableStateOf(StringProperty.StringIntrinsicValue("filtered"))
                )
            )
        }

        val transformer = FromList.ToList.Filter(
            innerType = ComposeFlowType.StringType(),
            condition = mutableStateOf(conditionProperty)
        )

        val assignableProperties = transformer.getAssignableProperties()
        // Should contain: conditionProperty and the StringIntrinsicValue from its transformer
        assertEquals(2, assignableProperties.size)
        assertEquals(conditionProperty, assignableProperties[0])
        assertEquals("filtered", (assignableProperties[1] as StringProperty.StringIntrinsicValue).value)
    }

    @Test
    fun testGetAssignableProperties_listContains() {
        val valueProperty = StringProperty.StringIntrinsicValue("searchValue")
        valueProperty.propertyTransformers.add(
            FromString.ToString.AddBefore(mutableStateOf(StringProperty.StringIntrinsicValue("prefix_")))
        )

        val transformer = FromList.ToBoolean.ListContains(
            innerType = ComposeFlowType.StringType(),
            value = mutableStateOf(valueProperty)
        )

        val assignableProperties = transformer.getAssignableProperties()
        // Should contain: valueProperty and the prefix property from its transformer
        assertEquals(2, assignableProperties.size)
        assertEquals("searchValue", (assignableProperties[0] as StringProperty.StringIntrinsicValue).value)
        assertEquals("prefix_", (assignableProperties[1] as StringProperty.StringIntrinsicValue).value)
    }

    @Test
    fun testGetAssignableProperties_floatModEqualsTo() {
        val transformer = FromFloat.ToBoolean.FloatModEqualsTo(
            mod = mutableStateOf(FloatProperty.FloatIntrinsicValue(3.5f)),
            equalsTo = mutableStateOf(FloatProperty.FloatIntrinsicValue(1.5f))
        )

        val assignableProperties = transformer.getAssignableProperties()
        assertEquals(2, assignableProperties.size)
        assertEquals(3.5f, (assignableProperties[0] as FloatProperty.FloatIntrinsicValue).value)
        assertEquals(1.5f, (assignableProperties[1] as FloatProperty.FloatIntrinsicValue).value)
    }

    @Test
    fun testGetAssignableProperties_dateTransformers() {
        val transformer = FromInstant.ToInstant.PlusDay(
            value = mutableStateOf(IntProperty.IntIntrinsicValue(7))
        )

        val assignableProperties = transformer.getAssignableProperties()
        assertEquals(1, assignableProperties.size)
        assertEquals(7, (assignableProperties[0] as IntProperty.IntIntrinsicValue).value)
    }

    @Test
    fun testGetAssignableProperties_splitTransformer() {
        val delimiterProperty = StringProperty.StringIntrinsicValue(",")
        delimiterProperty.propertyTransformers.add(
            FromString.ToString.AddAfter(mutableStateOf(StringProperty.StringIntrinsicValue(" ")))
        )

        val transformer = FromString.ToStringList.Split(
            value = mutableStateOf(delimiterProperty)
        )

        val assignableProperties = transformer.getAssignableProperties()
        // Should contain: delimiterProperty and the space property from its transformer
        assertEquals(2, assignableProperties.size)
        assertEquals(",", (assignableProperties[0] as StringProperty.StringIntrinsicValue).value)
        assertEquals(" ", (assignableProperties[1] as StringProperty.StringIntrinsicValue).value)
    }
}
