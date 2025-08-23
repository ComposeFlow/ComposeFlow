package io.composeflow.model.datatype

import junit.framework.TestCase.assertEquals
import kotlin.test.Test
import kotlin.test.assertTrue

class JsonParserTest {
    private val parser = JsonParser()

    @Test
    fun testEmptyInput() {
        val jsonText = """
            
            
            
            
            
        """

        val parseResult = parser.parseJsonToDataType(jsonText)
        assert(parseResult is DataTypeParseResult.EmptyInput)
    }

    @Test
    fun testParseJson_simpleObject() {
        val jsonText = """
  {
    "plantId": "pyrus-communis",
    "name": "Pear",
    "description": "The avocado (Persea americana) is a tree, long thought to have originated in South Central Mexico, classified as a member of the flowering plant family Lauraceae. The fruit of the plant, also called an avocado (or avocado pear or alligator pear), is botanically a large berry containing a single large seed.<br><br>Avocados are commercially valuable and are cultivated in tropical and Mediterranean climates throughout the world. They have a green-skinned, fleshy body that may be pear-shaped, egg-shaped, or spherical. Commercially, they ripen after harvesting. Avocado trees are partially self-pollinating and are often propagated through grafting to maintain a predictable quality and quantity of the fruit.<br><br>(From <a href=\"https://en.wikipedia.org/wiki/Avocado\">Wikipedia</a>)",
    "growZoneNumber": 3,
    "wateringInterval": 30,
    "float": 30.0,
    "booleanLiteral": true,
    "booleanString": "true",
    "instant": "2010-06-01T22:19:44.475Z",
    "instantDate": "2010-06-01",
    "imageUrl": "https://upload.wikimedia.org/wikipedia/commons/1/13/More_pears.jpg"
  }
        """
        val parseResult = parser.parseJsonToDataType(jsonText)
        assert(parseResult is DataTypeParseResult.Success)
        val dataType = (parseResult as DataTypeParseResult.Success).dataType

        assertTrue(
            dataType.fields.any {
                it.variableName == "plantId" && it.fieldType is FieldType.String
            },
        )
        assertTrue(
            dataType.fields.any {
                it.variableName == "name" && it.fieldType is FieldType.String
            },
        )
        assertTrue(
            dataType.fields.any {
                it.variableName == "description" && it.fieldType is FieldType.String
            },
        )
        assertTrue(
            dataType.fields.any {
                it.variableName == "growZoneNumber" && it.fieldType is FieldType.Int
            },
        )
        assertTrue(
            dataType.fields.any {
                it.variableName == "wateringInterval" && it.fieldType is FieldType.Int
            },
        )
        assertTrue(
            dataType.fields.any {
                it.variableName == "float" && it.fieldType is FieldType.Float
            },
        )
        assertTrue(
            dataType.fields.any {
                it.variableName == "booleanLiteral" && it.fieldType is FieldType.Boolean
            },
        )
        assertTrue(
            dataType.fields.any {
                it.variableName == "booleanString" && it.fieldType is FieldType.Boolean
            },
        )
        assertTrue(
            dataType.fields.any {
                it.variableName == "instant" && it.fieldType is FieldType.Instant
            },
        )
        assertTrue(
            dataType.fields.any {
                it.variableName == "instantDate" && it.fieldType is FieldType.String
            },
        )
        assertTrue(
            dataType.fields.any {
                it.variableName == "imageUrl" && it.fieldType is FieldType.String
            },
        )
    }

    @Test
    fun testParseJson_includingList() {
        val jsonText = """
  {
    "string": "pyrus-communis",
    "stringList": ["a", "b", "c"]
  }
        """
        val parseResult = parser.parseJsonToDataType(jsonText)
        assert(parseResult is DataTypeParseResult.Success)
        val dataType = (parseResult as DataTypeParseResult.Success).dataType

        assertTrue(
            dataType.fields.any {
                it.variableName == "string" && it.fieldType is FieldType.String
            },
        )
        assertTrue(
            dataType.fields.any {
                it.variableName == "stringList" && it.fieldType is FieldType.String
            },
        )
    }

    @Test
    fun testParseJson_nestedObject() {
        val jsonText = """
  {
    "string": "pyrus-communis",
    "nestedObject": {
      "nestedField1": "nestedValue", 
      "nestedBoolean": false, 
    }
  }
        """
        val parseResult = parser.parseJsonToDataType(jsonText)
        assert(parseResult is DataTypeParseResult.SuccessWithWarning)
        val dataType = (parseResult as DataTypeParseResult.SuccessWithWarning).dataType

        assertTrue(
            dataType.fields.any {
                it.variableName == "string" && it.fieldType is FieldType.String
            },
        )
        assertTrue(
            dataType.fields.any {
                it.variableName == "nestedObject" && it.fieldType is FieldType.String
            },
        )
    }

    @Test
    fun testParseJson_defaultValues() {
        val stringField = DataField(name = "string", fieldType = FieldType.String())
        val intField = DataField(name = "int", fieldType = FieldType.Int())
        val booleanField = DataField(name = "boolean", fieldType = FieldType.Boolean())
        val dataType =
            DataType(
                name = "dataType",
                fields =
                    mutableListOf(
                        stringField,
                        intField,
                        booleanField,
                    ),
            )
        val jsonText = """
       [
         {
            "string": "string 1", 
            "int": 1, 
            "boolean": true,
         }, 
         {
            "string": "string 2", 
            "int": 2,
            "boolean": false,
         }, 
       ]
        """

        val parseResult = parser.parseJsonToDefaultValues(dataType = dataType, jsonText)
        assert(parseResult is DefaultValuesParseResult.Success)
        val defaultValues = (parseResult as DefaultValuesParseResult.Success).defaultValues
        assertEquals(2, defaultValues.size)

        val firstValue = defaultValues[0]
        assertEquals(3, firstValue.defaultFields.size)
        assertEquals(stringField.id, firstValue.defaultFields[0].fieldId)
        assertEquals("string 1", firstValue.defaultFields[0].defaultValue.value)
        assertEquals(intField.id, firstValue.defaultFields[1].fieldId)
        assertEquals(1, firstValue.defaultFields[1].defaultValue.value)
        assertEquals(booleanField.id, firstValue.defaultFields[2].fieldId)
        assertEquals(true, firstValue.defaultFields[2].defaultValue.value)

        val secondValue = defaultValues[1]
        assertEquals(3, secondValue.defaultFields.size)
        assertEquals(stringField.id, secondValue.defaultFields[0].fieldId)
        assertEquals("string 2", secondValue.defaultFields[0].defaultValue.value)
        assertEquals(intField.id, secondValue.defaultFields[1].fieldId)
        assertEquals(2, secondValue.defaultFields[1].defaultValue.value)
        assertEquals(booleanField.id, secondValue.defaultFields[2].fieldId)
        assertEquals(false, secondValue.defaultFields[2].defaultValue.value)
    }
}
