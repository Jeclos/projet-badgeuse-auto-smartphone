// WorkLocationRecord
package com.example.shared.models


data class WorkLocationRecord(
    val id: Long = 0L,
    val latitude: Double,
    val longitude: Double,
    val name: String? = null
)
