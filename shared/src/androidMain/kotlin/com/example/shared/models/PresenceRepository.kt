package com.example.shared.models

import com.example.shared.data.PresenceRecord

interface PresenceRepository {

    suspend fun addPresence(record: PresenceRecord)

    suspend fun getAllPresences(): List<PresenceRecord>
}
