package cfg

import java.time.Instant
import java.util.*

sealed class MetadataDefinition<T>(
    val canBeConst: Boolean,
    protected val factory: () -> T
) {
    abstract val propertyName: String
    abstract val isNullable: Boolean
    val isConst: Boolean get() = canBeConst && !isNullable
    val value: T get() = factory()
    open val imports: List<String> = emptyList()
    abstract val needsType: Boolean
    abstract val type: String

    fun definition(): String =
        buildString {
            if (isConst) append("const ")
            append("val ")
            append(propertyName)
            if (needsType) {
                append(": ${checkNotNull(type)}")
                if (isNullable) append("?")
            }
            append(" = ")
            append(value.toInitialisation())
        }

    fun mapValueSetter(mapName: String): String =
        "$mapName[\"$propertyName\"] = ${value.toInitialisation()}"

    fun mapDefinition(mapName: String): String = buildString {
        append("val ")
        append(propertyName)
        append(": ")
        append(type)
        if(isNullable) append("?")
        append(" by ")
        append(mapName)
    }

    protected abstract fun T.toInitialisation(): String

    class Generic<T>(
        override val propertyName: String,
        factory: () -> T,
        canBeConst: Boolean,
        override val isNullable: Boolean,
        override val imports: List<String> = emptyList(),
        override val needsType: Boolean,
        override val type: String,
        val converter: T.() -> String
    ): MetadataDefinition<T>(canBeConst, factory){
        override fun T.toInitialisation(): String =
            converter()

        val asNullable: MetadataDefinition<T?>
            get() = Generic<T?>(
                propertyName, factory, canBeConst, true, imports, needsType, type, {if(this == null) "null" else converter(this)}
            )
    }

    class StringMetadata(
        override val propertyName: String,
        factory: () -> String
    ): MetadataDefinition<String>(true, factory) {
        override val isNullable: Boolean
            get() = false

        override val needsType: Boolean
            get() = false

        override val type: String
            get() = "String"

        override fun String.toInitialisation(): String =
            "\"$this\""

        val asNullable: StringNullableMetadata
            get() = StringNullableMetadata(propertyName, factory)
    }

    class StringNullableMetadata(
        override val propertyName: String,
        factory: () -> String?
    ): MetadataDefinition<String?>(true, factory) {

        override val needsType: Boolean
            get() = false

        override val type: String
            get() = "String"

        override val isNullable: Boolean get() = true
        override fun String?.toInitialisation(): String =
            if (this == null) "null" else "\"$this\""
    }

    class IntMetadata(
        override val propertyName: String,
        factory: () -> Int,
    ): MetadataDefinition<Int>(true, factory) {

        override val needsType: Boolean
            get() = false

        override val type: String
            get() = "Int"

        override val isNullable: Boolean
            get() = false
        override fun Int.toInitialisation(): String =
            toString()

        val asNullable: IntNullableMetadata
            get() = IntNullableMetadata(propertyName, factory)
    }

    class IntNullableMetadata(
        override val propertyName: String,
        factory: () -> Int?
    ): MetadataDefinition<Int?>(true, factory) {

        override val needsType: Boolean
            get() = false

        override val type: String
            get() = "Int"

        override val isNullable: Boolean get() = true
        override fun Int?.toInitialisation(): String =
            if (this == null) "null" else toString()
    }

    class BooleanMetadata(
        override val propertyName: String,
        factory: () -> Boolean
    ): MetadataDefinition<Boolean>(true, factory) {

        override val needsType: Boolean
            get() = false

        override val type: String
            get() = "Boolean"

        override val isNullable: Boolean
            get() = false
        override fun Boolean.toInitialisation(): String =
            toString()

        val asNullable: BooleanNullableMetadata
            get() = BooleanNullableMetadata(propertyName, factory)
    }

    class BooleanNullableMetadata(
        override val propertyName: String,
        factory: () ->  Boolean?
    ): MetadataDefinition<Boolean?>(true, factory) {

        override val needsType: Boolean
            get() = false

        override val type: String
            get() = "Boolean"

        override val isNullable: Boolean
            get() = false
        override fun Boolean?.toInitialisation(): String =
            if(this == null) "null" else toString()
    }

    class LongMetadata(
        override val propertyName: String,
        factory: () -> Long
    ): MetadataDefinition<Long>(true, factory) {

        override val needsType: Boolean
            get() = false

        override val type: String
            get() = "Long"

        override val isNullable: Boolean
            get() = false
        override fun Long.toInitialisation(): String =
            "${this}L"

        val asNullable: LongNullableMetadata
            get() = LongNullableMetadata(propertyName, factory)
    }

    class LongNullableMetadata(
        override val propertyName: String,
        factory: () -> Long?
    ): MetadataDefinition<Long?>(true, factory) {
        override val needsType: Boolean
            get() = false

        override val type: String
            get() = "Long"

        override val isNullable: Boolean get() = true
        override fun Long?.toInitialisation(): String =
            if (this == null) "null" else "${this}L"

    }

    class DateMetadata(
        override val propertyName: String,
        factory: () -> java.util.Date
    ): MetadataDefinition<java.util.Date>(false, factory){

        override val isNullable: Boolean
            get() = false

        override val type: String
            get() = "java.util.Date"

        override val needsType: Boolean
            get() = true

        override fun Date.toInitialisation(): String =
            "java.util.Date($time)"

        val instant: InstantMetadata get() =
            InstantMetadata(propertyName, {factory().toInstant()})

        val asNullable: DateNullableMetadata
            get() = DateNullableMetadata(propertyName, factory)
    }

    class DateNullableMetadata(
        override val propertyName: String,
        factory: () -> java.util.Date?
    ): MetadataDefinition<java.util.Date?>(false, factory){

        override val isNullable: Boolean
            get() = false

        override val type: String
            get() = "java.util.Date"

        override val needsType: Boolean
            get() = true
        override fun Date?.toInitialisation(): String =
            if(this == null) "null" else "java.util.Date($time)"

        val instant: InstantNullableMetadata get() =
            InstantNullableMetadata(propertyName, {factory()?.toInstant()})
    }

    class InstantMetadata(
        override val propertyName: String,
        factory: () -> Instant
    ): MetadataDefinition<java.time.Instant>(false, factory){
        override val isNullable: Boolean
            get() = false

        override val type: String
            get() = "java.time.Instant"

        override val needsType: Boolean
            get() = true
        override fun Instant.toInitialisation(): String =
            "java.time.Instant.ofEpochSecond(${this.epochSecond}, ${this.nano})"

        val kt: InstantKtMetadata get() = InstantKtMetadata(propertyName, factory)

        val asNullable: InstantNullableMetadata
            get() = InstantNullableMetadata(propertyName, factory)
    }

    class InstantNullableMetadata(
        override val propertyName: String,
        factory: () -> Instant?
    ): MetadataDefinition<java.time.Instant?>(false, factory){

        override val isNullable: Boolean
            get() = true

        override val type: String
            get() = "java.time.Instant"

        override val needsType: Boolean
            get() = true
        override fun Instant?.toInitialisation(): String =
            if(this == null) "null" else "java.time.Instant.ofEpochSecond(${this.epochSecond}, ${this.nano})"

        val kt: InstantKtNullableMetadata get() =
            InstantKtNullableMetadata(propertyName, factory)
    }

    class InstantKtMetadata(
        override val propertyName: String,
        factory: () -> Instant
    ): MetadataDefinition<java.time.Instant>(false, factory){

        override val isNullable: Boolean
            get() = false

        override val type: String
            get() = "kotlinx.datetime.Instant"

        override val needsType: Boolean
            get() = true

        override fun Instant.toInitialisation(): String =
            "kotlinx.datetime.Instant.fromEpochSeconds(${this.epochSecond}, ${this.nano})"

        val asNullable: InstantKtNullableMetadata
            get() = InstantKtNullableMetadata(propertyName, factory)
    }

    class InstantKtNullableMetadata(
        override val propertyName: String,
        factory: () -> Instant?
    ): MetadataDefinition<java.time.Instant?>(false, factory){
        override val isNullable: Boolean
            get() = true

        override val needsType: Boolean
            get() = true

        override val type: String
            get() = "kotlinx.datetime.Instant"

        override fun Instant?.toInitialisation(): String =
            if(this == null) "null" else "kotlinx.datetime.Instant.fromEpochSeconds(${this.epochSecond}, ${this.nano})"
    }
}
