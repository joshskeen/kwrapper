package com.joshskeen.kwrapper

import com.google.auto.service.AutoService
import com.joshskeen.annotation.KWrapper
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap
import kotlin.reflect.jvm.internal.impl.name.FqName

private const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

@AutoService(Processor::class)
@SupportedOptions(KAPT_KOTLIN_GENERATED_OPTION_NAME)
@SupportedSourceVersion(value = SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("*")
class KWrapperAnnotationProcessor : AbstractProcessor() {
    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val generatedSourcesRoot: String = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME].orEmpty()
        roundEnv.getElementsAnnotatedWith(KWrapper::class.java).forEach { element ->
            processingEnv.log(element.simpleName.toString())
            val el = element as TypeElement
            val packageName = processingEnv.elementUtils.getPackageOf(el).qualifiedName.toString()
            val className = el.simpleName.toString() + "KWrapper"

            val funcs = el.enclosedElements.filterIsInstance<ExecutableElement>().drop(1).map {
                it.toFunSpec(packageName, className)
            }
            val file = File(generatedSourcesRoot)
            file.mkdir()
            FileSpec.builder(packageName, className)
                .addType(
                    TypeSpec.classBuilder(className)
                        .primaryConstructor(
                            FunSpec.constructorBuilder()
                                .addParameter("wrappee", el.asType().asTypeName())
                                .build()
                        )
                        .addProperty(
                            PropertySpec.builder("wrappee", el.asType().asTypeName())
                                .addModifiers(KModifier.PRIVATE)
                                .initializer("wrappee")
                                .build()
                        )
                        .addFunctions(
                            funcs
                        )
                        .build()
                )
                .build().writeTo(file)
        }
        return false
    }
}

fun ExecutableElement.toFunSpec(packageName: String, className: String): FunSpec {
    val parameters = this.parameters.map { element ->
        val methodName = element.simpleName.toString()
        ParameterSpec.builder(
            name = methodName,
            type = element.asType().asTypeName().javaToKotlinType()
        ).build()
    }

    val parametersString = parameters.joinToString(",") {
        it.name
    }

    return FunSpec.builder(simpleName.toString())
        .addParameters(parameters)
        .addModifiers(KModifier.PUBLIC)
        .addCode("wrappee.${simpleName}(${parametersString})")
        .build()
}

fun ProcessingEnvironment.loge(msg: String) {
    messager.printMessage(Diagnostic.Kind.ERROR, msg)
}

fun ProcessingEnvironment.log(msg: String) {
    messager.printMessage(Diagnostic.Kind.NOTE, msg)
}

fun TypeName.javaToKotlinType(): TypeName {
    return when (this) {
        is ParameterizedTypeName -> {
            (rawType.javaToKotlinType() as ClassName).parameterizedBy(*(typeArguments.map { it.javaToKotlinType() }.toTypedArray()))
        }
        is WildcardTypeName -> {
            outTypes[0].javaToKotlinType()
        }
        else -> {
            val className = JavaToKotlinClassMap.INSTANCE.mapJavaToKotlin(FqName(toString()))?.asSingleFqName()?.asString()
            return if (className == null) {
                this
            } else {
                ClassName.bestGuess(className)
            }
        }
    }
}