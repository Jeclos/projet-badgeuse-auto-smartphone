package com.example.badgeuse_auto.export

data class ExportHeader(
    val employeeName: String = "",
    val employeeAddress: String = "",
    val employerName: String = "",
    val employerAddress: String = "",
    val periodStart: Long,
    val periodEnd: Long,
    val city: String = "Paris"
)