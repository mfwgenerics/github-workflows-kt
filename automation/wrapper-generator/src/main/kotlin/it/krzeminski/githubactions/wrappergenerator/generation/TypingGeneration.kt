package it.krzeminski.githubactions.wrappergenerator.generation

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import it.krzeminski.githubactions.actionsmetadata.model.ActionCoords
import it.krzeminski.githubactions.actionsmetadata.model.BooleanTyping
import it.krzeminski.githubactions.actionsmetadata.model.EnumTyping
import it.krzeminski.githubactions.actionsmetadata.model.FloatTyping
import it.krzeminski.githubactions.actionsmetadata.model.IntegerTyping
import it.krzeminski.githubactions.actionsmetadata.model.IntegerWithSpecialValueTyping
import it.krzeminski.githubactions.actionsmetadata.model.ListOfTypings
import it.krzeminski.githubactions.actionsmetadata.model.StringTyping
import it.krzeminski.githubactions.actionsmetadata.model.Typing

fun Typing.getClassName(actionPackageName: String, actionClassName: String, fieldName: String): TypeName =
    when (this) {
        BooleanTyping -> Boolean::class.asTypeName()
        is EnumTyping -> {
            val typeName = this.typeName?.toPascalCase() ?: fieldName.toPascalCase()
            ClassName("it.krzeminski.githubactions.actions.$actionPackageName", "$actionClassName.$typeName")
        }
        FloatTyping -> Float::class.asTypeName()
        IntegerTyping -> Integer::class.asTypeName()
        is IntegerWithSpecialValueTyping -> {
            val typeName = this.typeName?.toPascalCase() ?: fieldName.toPascalCase()
            ClassName("it.krzeminski.githubactions.actions.$actionPackageName", "$actionClassName.$typeName")
        }
        is ListOfTypings -> List::class.asClassName()
            .parameterizedBy(typing.getClassName(actionPackageName, actionClassName, fieldName))
        StringTyping -> String::class.asTypeName()
    }

fun Typing.asString(): String =
    when (this) {
        BooleanTyping -> ".toString()"
        is EnumTyping -> ".stringValue"
        FloatTyping -> ".toString()"
        IntegerTyping -> ".toString()"
        is IntegerWithSpecialValueTyping -> ".integerValue.toString()"
        is ListOfTypings -> {
            val mapValue: String = when (typing) {
                is StringTyping -> ""
                is IntegerTyping -> " { it.toString() }"
                is EnumTyping -> " { it.stringValue }"
                is IntegerWithSpecialValueTyping -> " { it.integerValue.toString() }"
                else -> error("ListOfTypings: typing=$typing is not supported")
            }
            ".joinToString(\"${if (delimiter == "\n") "\\n" else delimiter}\")$mapValue"
        }
        else -> ""
    }

fun Typing.buildCustomType(coords: ActionCoords, fieldName: String): TypeSpec? =
    when (this) {
        is EnumTyping -> buildEnumCustomType(coords, fieldName)
        is IntegerWithSpecialValueTyping -> buildIntegerWithSpecialValueCustomType(coords, fieldName)
        is ListOfTypings -> typing.buildCustomType(coords, fieldName)
        else -> null
    }

private fun EnumTyping.buildEnumCustomType(coords: ActionCoords, fieldName: String): TypeSpec {
    val itemsNames = itemsNames ?: items.map { it.toPascalCase() }
    val itemsNameMap = items.zip(itemsNames).toMap()
    val typeName = this.typeName?.toPascalCase() ?: fieldName.toPascalCase()
    val actionPackageName = coords.owner.toKotlinPackageName()
    val actionClassName = coords.buildActionClassName()
    val sealedClassName = this.getClassName(actionPackageName, actionClassName, fieldName)

    return TypeSpec.classBuilder(typeName)
        .addModifiers(KModifier.SEALED)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(ParameterSpec.builder("stringValue", String::class).build())
                .build(),
        )
        .addProperty(PropertySpec.builder("stringValue", String::class).initializer("stringValue").build())
        .addTypes(
            this.items.map {
                val itemName = itemsNameMap[it]?.let {
                    if (it == "Custom") "CustomEnum" else it
                } ?: error("FIXME: key=$it absent from $itemsNameMap")
                TypeSpec.objectBuilder(itemName)
                    .superclass(sealedClassName)
                    .addSuperclassConstructorParameter("%S", it)
                    .build()
            },
        )
        .addType(
            TypeSpec.classBuilder("Custom")
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter(ParameterSpec.builder("customStringValue", String::class).build())
                        .build(),
                )
                .superclass(sealedClassName)
                .addSuperclassConstructorParameter("customStringValue")
                .build(),
        )
        .build()
}

private fun IntegerWithSpecialValueTyping.buildIntegerWithSpecialValueCustomType(coords: ActionCoords, fieldName: String): TypeSpec {
    val typeName = this.typeName?.toPascalCase() ?: fieldName.toPascalCase()
    val actionPackageName = coords.owner.toKotlinPackageName()
    val actionClassName = coords.buildActionClassName()
    val sealedClassName = this.getClassName(actionPackageName, actionClassName, fieldName)
    return TypeSpec.classBuilder(typeName)
        .addModifiers(KModifier.SEALED)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(ParameterSpec.builder("integerValue", Int::class).build())
                .build(),
        )
        .addProperty(PropertySpec.builder("integerValue", Int::class).initializer("integerValue").build())
        .addType(
            TypeSpec.classBuilder("Value")
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter(ParameterSpec.builder("requestedValue", Int::class).build())
                        .build(),
                )
                .superclass(sealedClassName)
                .addSuperclassConstructorParameter("requestedValue")
                .build(),
        )
        .addTypes(
            this.specialValues.map { (name, value) ->
                TypeSpec.objectBuilder(name.toPascalCase())
                    .superclass(sealedClassName)
                    .addSuperclassConstructorParameter("%L", value)
                    .build()
            },
        )
        .build()
}
