package com.example.badgeuse_auto.domain

import com.example.badgeuse_auto.data.PresenceEntity
import com.example.badgeuse_auto.data.WorkLocationEntity

interface BadgeModeHandler {

    suspend fun onEnter(
        now: Long,
        workLocation: WorkLocationEntity,
        currentPresence: PresenceEntity?
    ): String

    suspend fun onExit(
        now: Long,
        workLocation: WorkLocationEntity,
        currentPresence: PresenceEntity?
    ): String
}