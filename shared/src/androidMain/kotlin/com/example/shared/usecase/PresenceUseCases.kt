package com.example.shared.usecase

import com.example.shared.models.WorkLocationRecord
import com.example.shared.data.PresenceRecord
import com.example.shared.models.WorkLocation
import com.example.shared.models.WorkLocationRepository
import com.example.shared.models.PresenceRepository

class PresenceUseCases(
    private val presenceRepository: PresenceRepository,
    private val workLocationRepository: WorkLocationRepository
) {

    suspend fun addPresence(timestamp: Long, type: String, locationName: String) {
        presenceRepository.addPresence(
            PresenceRecord(
                timestamp = timestamp,
                type = type,
                locationName = locationName
            )
        )
    }

    suspend fun getAll(): List<PresenceRecord> =
        presenceRepository.getAllPresences()

    suspend fun saveWorkLocation(location: WorkLocation) =
        workLocationRepository.save(
            WorkLocationRecord(
                latitude = location.latitude,
                longitude = location.longitude,
                //radius = location.radius,
                //name = location.name
            )
        )

    suspend fun getWorkLocation(): WorkLocation? =
        workLocationRepository.get()?.let {
            WorkLocation(
                latitude = it.latitude,
                longitude = it.longitude,
                //radius = it.radius,
                //name = it.name
            )
        }
}
