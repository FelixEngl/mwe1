package de.fs.db.structures

import kotlinx.serialization.Serializable

/**
 * A geo-point with a [longitude] and a [latitude]
 * @property longitude the longitude (Längengrad)
 * @property latitude the latitude (Breitengrad)
 */
@Serializable
data class Point(
    val longitude: Double,
    val latitude: Double,
) {

    init {
        require(longitude in -180.0..180.0){
            "The longitude has to be in [-180.0, 180.0] but was $longitude!"
        }

        require(latitude in -90.0..90.0){
            "The latitude has to be in [-90.0, 90.0] but was $latitude!"
        }
    }

    companion object {

        val ZERO = Point(0.0, 0.0)

        /**
         * Interprets a [DoubleArray] of the size 2 as [Longitude, Latitude].
         */
        fun createFrom(array: DoubleArray): Point {
            require(array.size == 2){
                "The size of the array has to be 2!"
            }
            return Point(array[0], array[1])
        }

        /**
         * Interprets a [Pair] as Pair<Longitude, Latitude>
         */
        fun createFrom(pair: Pair<Double, Double>): Point {
            return Point(pair.first, pair.second)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Point) return false

        if (longitude isNotEqualTo other.longitude) return false
        if (latitude isNotEqualTo other.latitude) return false

        return true
    }

    override fun hashCode(): Int {
        var result = longitude.hashCode()
        result = 31 * result + latitude.hashCode()
        return result
    }
}

