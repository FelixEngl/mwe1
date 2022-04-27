package de.fs.db.structures

import kotlin.math.absoluteValue
import kotlin.math.min

@Suppress("NOTHING_TO_INLINE")
inline infix fun Double.isEqualTo(other: Double) = nearlyEqual(this, other, this.getEpsilon())
@Suppress("NOTHING_TO_INLINE")
inline infix fun Double.isNotEqualTo(other: Double) = !nearlyEqual(this, other, this.getEpsilon())

@Suppress("NOTHING_TO_INLINE")
/**
 * [maxDelta] is commonly 0.01% of [this]
 */
inline fun Double.getEpsilon(maxDelta: Double = 0.0001) = (this * maxDelta).absoluteValue

fun nearlyEqual(a: Double, b: Double, epsilon: Double): Boolean {
    val absA: Double = a.absoluteValue
    val absB: Double = b.absoluteValue
    val diff: Double = (a-b).absoluteValue
    return when {
        a == b -> { // shortcut, handles infinities
            true
        }
        a == 0.0 || b == 0.0 || absA + absB < Double.MIN_VALUE -> {
            // a or b is zero or both are extremely close to it
            // relative error is less meaningful here
            diff < epsilon * Double.MIN_VALUE
        }
        else -> { // use relative error
            diff / min(absA + absB, Double.MAX_VALUE) < epsilon
        }
    }
}