package com.androidizate.clase7

/**
 * Created by andres.oller on 18/08/17.
 */
data class User(
        val id: Long = 0L,
        val name: String = "",
        val username: String = "",
        val email: String = "",
        val address: Address = Address(),
        val phone: String = "",
        val website: String = "",
        val company: Company = Company()
)