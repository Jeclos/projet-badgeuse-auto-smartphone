package com.example.shared.models
interface WorkLocationRepository {
    suspend fun save(location: WorkLocationRecord)
    suspend fun get(): WorkLocationRecord?
}




