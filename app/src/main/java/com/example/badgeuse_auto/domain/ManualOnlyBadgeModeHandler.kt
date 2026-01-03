package com.example.badgeuse_auto.domain

import com.example.badgeuse_auto.data.*

class ManualOnlyBadgeModeHandler : BadgeModeHandler {

    override suspend fun onEnter(
        now: Long,
        workLocation: WorkLocationEntity,
        currentPresence: PresenceEntity?
    ): String {
        return "Mode manuel actif"
    }

    override suspend fun onExit(
        now: Long,
        workLocation: WorkLocationEntity,
        currentPresence: PresenceEntity?
    ): String {
        return "Mode manuel actif"
    }
}
