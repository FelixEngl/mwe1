package cfg

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File
import java.nio.file.Path
import java.util.Date
import javax.inject.Inject

infix fun String.meta(value: String) = metaStringF{value}
infix fun String.metaStringF(value: () -> String) = MetadataDefinition.StringMetadata(this, value)
infix fun String.nullableMeta(value: String?) = nullableMetaStringF{value}
infix fun String.nullableMetaStringF(value: () -> String?) = MetadataDefinition.StringNullableMetadata(this, value)
infix fun String.meta(value: Int) = metaIntF{value}
infix fun String.metaIntF(value: () -> Int) = MetadataDefinition.IntMetadata(this, value)
infix fun String.nullableMeta(value: Int?) = nullableMetaIntF{value}
infix fun String.nullableMetaIntF(value: () -> Int?) = MetadataDefinition.IntNullableMetadata(this, value)
infix fun String.meta(value: Long) = metaLongF{value}
infix fun String.metaLongF(value: () -> Long) = MetadataDefinition.LongMetadata(this, value)
infix fun String.nullableMeta(value: Long?) = nullableMetaLongF{value}
infix fun String.nullableMetaLongF(value: () -> Long?) = MetadataDefinition.LongNullableMetadata(this, value)
infix fun String.meta(value: Boolean) = metaBooleanF{value}
infix fun String.metaBooleanF(value: () -> Boolean) = MetadataDefinition.BooleanMetadata(this, value)
infix fun String.nullableMeta(value: Boolean?) = nullableMetaBooleanF{value}
infix fun String.nullableMetaBooleanF(value: () -> Boolean?) = MetadataDefinition.BooleanNullableMetadata(this, value)

infix fun String.meta(value: java.time.Instant) = metaInstantF{value}
infix fun String.metaInstantF(value: () -> java.time.Instant) = MetadataDefinition.InstantMetadata(this, value)
infix fun String.nullableMeta(value: java.time.Instant?) = nullableMetametaInstantF{value}
infix fun String.nullableMetametaInstantF(value: () -> java.time.Instant?) = MetadataDefinition.InstantNullableMetadata(this, value)
infix fun String.meta(value: java.util.Date) = metaDateF{value}
infix fun String.metaDateF(value: () -> java.util.Date) = MetadataDefinition.DateMetadata(this, value)
infix fun String.nullableMeta(value: java.util.Date?) = nullableMetaDateF{value}
infix fun String.nullableMetaDateF(value: () -> java.util.Date?) = MetadataDefinition.DateNullableMetadata(this, value)


inline fun <reified T:Task> TaskContainer.create(name: String, configuration: Action<T>) =
    create(name, T::class.java, configuration)

inline fun <reified T:Task> TaskContainer.create(name: String, vararg args: Any) =
    create(name, T::class.java, *args)


fun TaskContainer.createCustomMetadataTask(
    name: String = "generateCustomMetadata",
    cfg: GenerateCustomMetadataTask.() -> Unit
) = create<GenerateCustomMetadataTask>(name).apply(cfg)


private val Project.kotlin get() =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName("kotlin") as org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@DslMarker
annotation class GenerateCustomMetadataTaskDSL

abstract class GenerateCustomMetadataTask @Inject constructor() : DefaultTask() {

    private val hasBuildTimeProperty_: Property<Boolean>
    private val packageName_: Property<String>
    private val metadataName_: Property<String>
    private val targetContexts_: MapProperty<String, TargetContext<*>>
    private val targetContexts get() = targetContexts_.getOrElse(emptyMap())

    private var dateCache: Date? = null
    private val lock = Any()

    @get:Input
    var packageName: String
        get() = packageName_.getOrElse("generated.metadata")
        set(value) {packageName_.set(value)}

    @get:Input
    var metadataName: String
        get() = metadataName_.getOrElse("Metadata")
        set(value) {metadataName_.set(value)}

    @get:Input
    var hasBuildTimeProperty: Boolean
        get() = hasBuildTimeProperty_.getOrElse(false)
        set(value) {hasBuildTimeProperty_.set(value)}

    @get:Input
    val buildTimeOrNull: Date?
        get() = if (hasBuildTimeProperty) {
            if (dateCache != null){
                dateCache
            } else {
                synchronized(lock){
                    if (dateCache != null){
                        dateCache
                    } else {
                        dateCache = Date()
                        dateCache
                    }
                }
            }
        } else null

    @get:Internal
    val buildTime: Date
        get() = checkNotNull(buildTimeOrNull)


    private val rootOutputPath = project.buildDir / "generatedMetadata"

    @get:OutputDirectories
    val pathsToCode get() = targetContexts.map { it.value.pathToCode }


    init {
        val objectFactory = project.objects

        packageName_ = objectFactory.property(String::class.java)
        metadataName_ = objectFactory.property(String::class.java)
        targetContexts_ = objectFactory.mapProperty(String::class.java, TargetContext::class.java)
        hasBuildTimeProperty_ = objectFactory.property(Boolean::class.java)
    }

    companion object {
        private val regexMain = Regex(".*([Mm]ain).*")
//        private val regexTest = Regex(".*([Tt]est).*")
        private operator fun File.div(part: String) = File(this, part)

        private val constantTime = Date(0)
    }

    inner class TargetContext<T: Named>(
        sourceSet: SourceSetSurrogate<T>
    ) {

        val sourceSetName: String = sourceSet.name

        val fileNameSuffix: String = sourceSetName.replace(regexMain, "").let {
            if (it.isNotBlank()) it else sourceSetName
        }

        internal val pathToCode: File = rootOutputPath / sourceSetName
        private val defaultMetadataMapName = "METADATA_MAP_${fileNameSuffix.toUpperCase()}"
        internal val sourceSetDependencyNames: Set<String> =
            (sourceSet as? SourceSetSurrogate.KotlinSourceSetWrapper)
                ?.let { it.value.dependsOn.map { it.name }.toSet() }
                ?: emptySet()

        internal val sourceSetDependenciesFlat get() = sourceSetDependencyNames.mapNotNull { targetContexts[it] }

        internal val sourceSetDependencies: Sequence<TargetContext<*>> get() = getAllSourceSetDependencies()
        private fun getAllSourceSetDependencies(originName: String = sourceSetName): Sequence<TargetContext<*>> {
            val root = sourceSetDependenciesFlat
            return (root.asSequence() + root.asSequence().flatMap { it.sourceSetDependencies }).filterNot { it.sourceSetName == originName }.distinctBy { it.sourceSetName }
        }

        private val createMetadataMap_: Property<Boolean>
        private val createMetadataValueSequence_: Property<Boolean>
        private val metadataValues_: MapProperty<String, MetadataDefinition<*>>
        private val metadataMapName_: Property<String>
        private val supplierFunctionName_: Property<String>

        var createMetadataMap: Boolean
            get() = createMetadataMap_.getOrElse(false)
            set(value) = createMetadataMap_.set(value)

        var createMetadataValueSequence: Boolean
            get() = createMetadataValueSequence_.getOrElse(false)
            set(value) = createMetadataValueSequence_.set(value)

        internal val metadataPropertyNameToDefinition: Map<String, MetadataDefinition<*>>
            get() = metadataValues_.getOrElse(emptyMap())

        var metadataMapPropertyName
            get() = metadataMapName_.getOrElse(defaultMetadataMapName)
            set(value) = metadataMapName_.set(value)

        var metadataSequenceFunctionName
            get() = supplierFunctionName_.getOrElse("metadataSequence$sourceSetName")
            set(value) = supplierFunctionName_.set(value)

        init {
            val objectFactory = project.objects
            createMetadataMap_ = objectFactory.property(Boolean::class.java)
            createMetadataValueSequence_ = objectFactory.property(Boolean::class.java)
            metadataValues_ = objectFactory.mapProperty(String::class.java, MetadataDefinition::class.java)
            metadataMapName_ = objectFactory.property(String::class.java)
            supplierFunctionName_ = objectFactory.property(String::class.java)
            sourceSet.configurePathFor(pathToCode)
        }

        fun <T> add(toRegister: MetadataDefinition<T>){
            check(toRegister.propertyName !in metadataPropertyNameToDefinition){
                "The name ${metadataPropertyNameToDefinition} is allready used in for this metadatas!"
            }
            metadataValues_.put(toRegister.propertyName, toRegister)
        }

        fun <T> register(toRegister: MetadataDefinition<T>){
            add(toRegister)
        }

        operator fun <T> MetadataDefinition<T>.unaryPlus(): Unit {
            register(this)
        }
    }

    private fun TargetContext<*>.createYields(generateYieldOfSequence: Boolean): Sequence<String> = sequence {
        when {
            generateYieldOfSequence && createMetadataValueSequence -> {
                yield("yieldAll(${metadataSequenceFunctionName}())")
            }
            createMetadataMap -> {
                yield("yieldAll(${metadataMapPropertyName}.entries.asSequence().map{it.toPair()})")
            }
            else -> {
                metadataPropertyNameToDefinition.values.forEach { definition ->
                    yield("yield(Pair(\"${definition.propertyName}\", ${definition.propertyName}))")
                }
            }
        }
    }

    private fun metadataForInternal(target: Any, definition: TargetContext<*>.() -> Unit) {
        val toAdd = TargetContext(SourceSetSurrogate.createSurrogate(target)).apply { definition() }

        if (toAdd.sourceSetName in targetContexts){
            error("The sourceset ${toAdd.sourceSetName} is already registered for metadata creation!")
        }

        if (toAdd.createMetadataMap){
            check(toAdd.metadataPropertyNameToDefinition.values.none {  it.propertyName == toAdd.metadataMapPropertyName }){
                "The variableName ${toAdd.metadataMapPropertyName} was set to reserved for a map used for backing the metadata, " +
                        "but was also set as a normal metadata name!"
            }
        }

        if (toAdd.createMetadataValueSequence){
            check(toAdd.metadataPropertyNameToDefinition.values.none {  it.propertyName == toAdd.metadataSequenceFunctionName }){
                "The variableName ${toAdd.metadataMapPropertyName} was set to reserved for a map used for backing the metadata, " +
                        "but was also set as the metadata sequence function name!"
            }
        }

        if (toAdd.sourceSetDependencyNames.isNotEmpty()){
            toAdd.sourceSetDependencies.forEach { dependency ->
                if (toAdd.createMetadataMap){
                    check(toAdd.metadataMapPropertyName !in dependency.metadataPropertyNameToDefinition){
                        "The name ${toAdd.metadataMapPropertyName} of the MetadataMap of ${toAdd.sourceSetName} is in the metadata of its dependency sourceset ${dependency.sourceSetName}!"
                    }
                    if (dependency.createMetadataMap){
                        check(toAdd.metadataMapPropertyName != dependency.metadataMapPropertyName){
                            "The name ${toAdd.metadataMapPropertyName} is already set as the metadataMapName of ${dependency.sourceSetName} which is a dependency of ${toAdd.sourceSetName}!"
                        }
                    }
                }

                if (toAdd.createMetadataValueSequence){
                    check(toAdd.metadataSequenceFunctionName !in dependency.metadataPropertyNameToDefinition){
                        "The name ${toAdd.metadataSequenceFunctionName} of the Metadata of ${toAdd.sourceSetName} is in the metadata of its dependency sourceset ${dependency.sourceSetName}!"
                    }
                    if (dependency.createMetadataValueSequence){
                        check(toAdd.metadataSequenceFunctionName != dependency.metadataSequenceFunctionName){
                            "The name ${toAdd.metadataSequenceFunctionName} is already set as the metadataSequenceFunctionName of ${dependency.sourceSetName} which is a dependency of ${toAdd.sourceSetName}!"
                        }
                    }
                }

                if (dependency.createMetadataMap){
                    check(dependency.metadataMapPropertyName !in toAdd.metadataPropertyNameToDefinition){
                        "The metadataMapName ${dependency.metadataMapPropertyName} of the dependency ${dependency.sourceSetName} was used in the metadata of the dependant ${toAdd.sourceSetName}"
                    }
                }

                if (dependency.createMetadataValueSequence){
                    check(dependency.metadataSequenceFunctionName !in toAdd.metadataPropertyNameToDefinition){
                        "The metadataSequenceFunctionName ${dependency.metadataSequenceFunctionName} of the dependency ${dependency.sourceSetName} was used in the metadata of the dependant ${toAdd.sourceSetName}"
                    }
                }

                toAdd.metadataPropertyNameToDefinition.keys.forEach { toAddMetadataNames ->
                    check(toAddMetadataNames !in dependency.metadataPropertyNameToDefinition)
                }
            }
        }

        targetContexts_.put(toAdd.sourceSetName, toAdd)
    }

    fun metadataFor(target: SourceDirectorySet, definition: TargetContext<*>.() -> Unit) {
        metadataForInternal(target, definition)
    }

    fun metadataFor(target: KotlinSourceSet, definition: TargetContext<*>.() -> Unit) {
        metadataForInternal(target, definition)
    }

    fun metadataFor(target: Provider<out Named>, definition: TargetContext<*>.() -> Unit) {
        metadataForInternal(target, definition)
    }

    fun metadataFor(target: NamedDomainObjectContainer<out Named>, definition: TargetContext<*>.() -> Unit) {
        metadataForInternal(target, definition)
    }

    @TaskAction
    fun generate() {
        dateCache = null

        if (rootOutputPath.exists()){
            rootOutputPath.deleteRecursively()
            rootOutputPath.mkdirs()
        }

        targetContexts.forEach { (_, metadataDefinition) ->
            metadataDefinition.pathToCode.mkdirs()

            val target = packageName
                .split('.')
                .fold(metadataDefinition.pathToCode){
                        acc: File, s: String -> acc / s
                } / "${metadataName}_${metadataDefinition.fileNameSuffix}.kt"

            target.parentFile.mkdirs()
            target.writer().use { writer ->
                writer.appendLine("package $packageName")

                if (metadataDefinition.metadataPropertyNameToDefinition.values.any { it.imports.isNotEmpty() }){
                    writer.appendLine()
                    metadataDefinition.metadataPropertyNameToDefinition.values.forEach {
                        it.imports.forEach { import ->
                            writer.appendLine("import $import")
                        }
                    }
                }
                if (metadataDefinition.createMetadataMap){
                    writer.appendLine()
                    writer.appendLine("val ${metadataDefinition.metadataMapPropertyName}: Map<String, Any?> = mutableMapOf<String, Any?>().also {")
                    metadataDefinition.metadataPropertyNameToDefinition.values.forEach {
                        writer.appendLine("    ${it.mapValueSetter("it")}")
                    }
                    writer.appendLine("}")
                    writer.appendLine()
                    metadataDefinition.metadataPropertyNameToDefinition.values.forEach {
                        writer.appendLine(it.mapDefinition(metadataDefinition.metadataMapPropertyName))
                    }
                } else {
                    writer.appendLine()
                    metadataDefinition.metadataPropertyNameToDefinition.values.forEach {
                        writer.appendLine(it.definition())
                    }
                }
                if (metadataDefinition.createMetadataValueSequence){
                    writer.appendLine()
                    writer.appendLine("fun ${metadataDefinition.metadataSequenceFunctionName}(): Sequence<Pair<String, Any?>> = sequence { ")

                    metadataDefinition.sourceSetDependencies.forEach { dependency ->
                        writer.appendLine("    //src: ${dependency.sourceSetName}")
                        dependency.createYields(true).forEach {
                            writer.appendLine("    ${it}")
                        }
                        writer.appendLine()
                    }
                    writer.appendLine("    //src: ${metadataDefinition.sourceSetName}")
                    metadataDefinition.createYields(false).forEach {
                        writer.appendLine("    ${it}")
                    }

                    writer.appendLine("")
                    writer.appendLine("}")
                }
            }
        }

        dateCache = null
    }
}

sealed class SourceSetSurrogate<T: Named>(
    val value: T
): Named by value {

    abstract fun configurePathFor(pathToCode: File)
    fun configurePathFor(pathToCode: Path) = configurePathFor(pathToCode.toFile())

    class SourceDirectorySetWrapper(value: SourceDirectorySet): SourceSetSurrogate<SourceDirectorySet>(value) {
        override fun configurePathFor(pathToCode: File) {
            value.srcDir(pathToCode)
        }
    }
    class KotlinSourceSetWrapper(value: KotlinSourceSet): SourceSetSurrogate<KotlinSourceSet>(value) {
        constructor(provider: Provider<KotlinSourceSet>) : this(checkNotNull(provider.orNull))

        override fun configurePathFor(pathToCode: File) {
            value.kotlin.srcDir(pathToCode)
        }
    }

    companion object {
        fun createSurrogate(toSurrogate: Any): SourceSetSurrogate<*> {
            return when(toSurrogate){
                is Provider<*> -> createSurrogate(toSurrogate.get())
                is KotlinSourceSet -> KotlinSourceSetWrapper(toSurrogate)
                is SourceDirectorySet -> SourceDirectorySetWrapper(toSurrogate)
                else -> error("Unsupported: ${toSurrogate::class}")
            }
        }
    }
}

