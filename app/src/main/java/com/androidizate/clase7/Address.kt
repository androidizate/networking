package com.androidizate.clase7

/**
 * Created by andres.oller on 18/08/17.
 */
data class Address(
        val street: String = "",
        val suite: String = "",
        val city: String = "",
        val zipcode: String = "",
        val geo: Geo = Geo()
)