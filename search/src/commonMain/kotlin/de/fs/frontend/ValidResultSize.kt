package de.fs.frontend

import kotlinx.serialization.Serializable

@Serializable
data class ValidResultSize(
    val min: Int,
    val max: Int
) {
    constructor(range: IntRange): this(range.first, range.last)

    fun toRange(): IntRange = IntRange(min, max)
}
