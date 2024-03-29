package com.joshskeen.kwrapper.processor

import com.google.auto.service.AutoService
import com.joshskeen.kwrapper.annotation.KWrapper
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.TypeParameterElement
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap
import kotlin.reflect.jvm.internal.impl.name.FqName

private const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
private const val WRAPPED_CLASS_NAME = "wrappee"
private const val EXTENSION_PROP_NAME = "KWrapper"

@AutoService(Processor::class)
@SupportedOptions(KAPT_KOTLIN_GENERATED_OPTION_NAME)
@SupportedSourceVersion(value = SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("*")
class KWrapperAnnotationProcessor : AbstractProcessor() {
    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val generatedSourcesRoot: String = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME].orEmpty()
        roundEnv.getElementsAnnotatedWith(KWrapper::class.java).forEach { element ->
            processingEnv.log(element.simpleName.toString())
            val typeElement = element as TypeElement
            val packageName = processingEnv.elementUtils.getPackageOf(typeElement).qualifiedName.toString()
            val className = typeElement.simpleName.toString() + "KWrapper"
            val functions = typeElement.functions()
            val type = TypeSpec.classBuilder(className)
                .addModifiers(KModifier.VALUE)
                .addAnnotation(JvmInline::class)
                .addTypeVariables(typeElement.typeParameters.toTypeVariables())
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter(WRAPPED_CLASS_NAME, typeElement.asType().asTypeName())
                        .build()
                )
                .addProperty(
                    PropertySpec.builder(WRAPPED_CLASS_NAME, typeElement.asType().asTypeName())
                        .addModifiers(KModifier.PRIVATE)
                        .initializer(WRAPPED_CLASS_NAME)
                        .build()
                )
                .addFunctions(functions)
                .build()
            val extensionProp = PropertySpec.builder(EXTENSION_PROP_NAME, ClassName(packageName, className).parameterizedBy(
                typeArguments = typeElement.typeParameters.toTypeVariables()
            ))
                .receiver(typeElement.asType().asTypeName())
                .addTypeVariables(typeElement.typeParameters.toTypeVariables())
                .getter(
                    FunSpec.getterBuilder()
                        .addStatement("return $className(this)")
                        .build()
                )
                .build()
            val file = File(generatedSourcesRoot)
            file.mkdir()
            FileSpec.builder(packageName, className)
                .addType(type)
                .addProperty(extensionProp)
                .build()
                .writeTo(file)
        }
        return false
    }
}

private fun List<TypeParameterElement>.toTypeVariables() = map { it.typeVariableName() }

private fun TypeElement.functions(): List<FunSpec> =
    enclosedElements.filterIsInstance<ExecutableElement>().drop(1).map {
        it.toFunSpec()
    }

private fun ExecutableElement.toFunSpec(): FunSpec {
    val parameters = this.parameters.map { element ->
        val methodName = element.simpleName.toString()
        ParameterSpec.builder(
            name = methodName,
            type = element.asType().asTypeName().javaToKotlinType()
        ).build()
    }

    val parametersString = parameters.joinToString(", ") {
        it.name
    }

    return FunSpec.builder(simpleName.toString())
        .addParameters(parameters)
        .addTypeVariables(typeParameters.toTypeVariables())
        .addModifiers(KModifier.PUBLIC)
        .addCode("return $WRAPPED_CLASS_NAME.${simpleName}(${parametersString})")
        .returns(this.returnType.asTypeName().javaToKotlinType())
        .build()
}

fun TypeParameterElement.typeVariableName(): TypeVariableName {
    val name = simpleName.toString()
    val boundsTypeNames = bounds.map {
        it.asTypeName().javaToKotlinType()
    }
    return TypeVariableName(name, boundsTypeNames, variance = null)
}

private fun TypeName.javaToKotlinType(): TypeName {
    return when (this) {
        is ParameterizedTypeName -> {
            (rawType.javaToKotlinType() as ClassName).parameterizedBy(*(typeArguments.map { it.javaToKotlinType() }
                .toTypedArray()))
        }
        is WildcardTypeName -> {
            outTypes[0].javaToKotlinType()
        }
        else -> {
            val className =
                JavaToKotlinClassMap.INSTANCE.mapJavaToKotlin(FqName(toString()))?.asSingleFqName()?.asString()
            return if (className == null) {
                this
            } else {
                ClassName.bestGuess(className)
            }
        }
    }
}

private fun ProcessingEnvironment.loge(msg: String) {
    messager.printMessage(Diagnostic.Kind.ERROR, msg)
}

private fun ProcessingEnvironment.log(msg: String) {
    messager.printMessage(Diagnostic.Kind.NOTE, msg)
}