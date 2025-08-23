package io.composeflow.model.datatype

import io.composeflow.model.project.Project
import io.composeflow.model.project.firebase.FirestoreCollection
import io.composeflow.trimForCompare
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DataTypeTest {
    private val project = Project()

    @Test
    fun emptyFields_verify_nullTypeSpec() {
        val dataType =
            DataType(
                name = "NullDataClass",
            )
        assertNull(dataType.generateDataClassSpec(project))
    }

    @Test
    fun stringField_verifyGeneratedDataClass() {
        val dataType =
            DataType(
                name = "DataClass",
            ).apply {
                fields.add(
                    DataField(
                        name = "str",
                        fieldType = FieldType.String(),
                    ),
                )
            }
        val typeSpec = dataType.generateDataClassSpec(project)
        assertEquals(
            """
@kotlinx.serialization.Serializable
public data class DataClass(
  public val str: kotlin.String = "",
) 
            """.trimForCompare(),
            typeSpec.toString().trimForCompare(),
        )
    }

    @Test
    fun intField_verifyGeneratedDataClass() {
        val dataType =
            DataType(
                name = "DataClass",
            ).apply {
                fields.add(
                    DataField(
                        name = "int",
                        fieldType = FieldType.Int(),
                    ),
                )
            }
        val typeSpec = dataType.generateDataClassSpec(project)
        assertEquals(
            """
@kotlinx.serialization.Serializable
public data class DataClass(
  public val int: kotlin.Int = 0,
) 
            """.trimForCompare(),
            typeSpec.toString().trimForCompare(),
        )
    }

    @Test
    fun booleanField_verifyGeneratedDataClass() {
        val dataType =
            DataType(
                name = "DataClass",
            ).apply {
                fields.add(
                    DataField(
                        name = "boolean",
                        fieldType = FieldType.Boolean(),
                    ),
                )
            }
        val typeSpec = dataType.generateDataClassSpec(project)
        assertEquals(
            """
@kotlinx.serialization.Serializable
public data class DataClass(
  public val boolean: kotlin.Boolean = false,
) 
            """.trimForCompare(),
            typeSpec.toString().trimForCompare(),
        )
    }

    @Test
    fun multipleFields_verifyGeneratedDataClass() {
        val dataType =
            DataType(
                name = "DataClass",
            ).apply {
                fields.add(
                    DataField(
                        name = "str",
                        fieldType = FieldType.String(),
                    ),
                )
                fields.add(
                    DataField(
                        name = "int",
                        fieldType = FieldType.Int(),
                    ),
                )
                fields.add(
                    DataField(
                        name = "boolean",
                        fieldType = FieldType.Boolean(),
                    ),
                )
            }
        val typeSpec = dataType.generateDataClassSpec(project)
        assertEquals(
            """
@kotlinx.serialization.Serializable
public data class DataClass(
  public val str: kotlin.String = "",
  public val int: kotlin.Int = 0,
  public val boolean: kotlin.Boolean = false,
) 
            """.trimForCompare(),
            typeSpec.toString().trimForCompare(),
        )
    }

    @Test
    fun multipleFields_withDefaultValue_verifyGeneratedDataClass() {
        val dataType =
            DataType(
                name = "DataClass",
            ).apply {
                fields.add(
                    DataField(
                        name = "str",
                        fieldType = FieldType.String(defaultValue = "default"),
                    ),
                )
                fields.add(
                    DataField(
                        name = "int",
                        fieldType = FieldType.Int(defaultValue = 3),
                    ),
                )
                fields.add(
                    DataField(
                        name = "boolean",
                        fieldType = FieldType.Boolean(defaultValue = true),
                    ),
                )
            }
        val typeSpec = dataType.generateDataClassSpec(project)
        assertEquals(
            """
@kotlinx.serialization.Serializable
public data class DataClass(
  public val str: kotlin.String = "default",
  public val int: kotlin.Int = 3,
  public val boolean: kotlin.Boolean = true,
) 
            """.trimForCompare(),
            typeSpec.toString().trimForCompare(),
        )
    }

    @Test
    fun multipleFields_usedAsFirestoreCollection_verifyDocumentId_included() {
        val dataType =
            DataType(
                name = "DataClass",
            ).apply {
                fields.add(
                    DataField(
                        name = "str",
                        fieldType = FieldType.String(defaultValue = "default"),
                    ),
                )
                fields.add(
                    DataField(
                        name = "int",
                        fieldType = FieldType.Int(defaultValue = 3),
                    ),
                )
                fields.add(
                    DataField(
                        name = "boolean",
                        fieldType = FieldType.Boolean(defaultValue = true),
                    ),
                )
            }
        project.firebaseAppInfoHolder.firebaseAppInfo.firestoreCollections.add(
            FirestoreCollection(
                name = "testCollection",
                dataTypeId = dataType.id,
            ),
        )
        val typeSpec = dataType.generateDataClassSpec(project)
        assertEquals(
            """
@kotlinx.serialization.Serializable
public data class DataClass(
  public val documentId: kotlin.String = "",
  public val str: kotlin.String = "default",
  public val int: kotlin.Int = 3,
  public val boolean: kotlin.Boolean = true,
) 
            """.trimForCompare(),
            typeSpec.toString().trimForCompare(),
        )
    }
}
