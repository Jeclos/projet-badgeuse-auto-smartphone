package com.example.badgeuse_auto.data

fun PresenceEntity.Companion.autoEnter(
    workLocationId: Long,
    time: Long,
    type: String
): PresenceEntity {
    return PresenceEntity(
        workLocationId = workLocationId,

        enterTime = time,
        exitTime = null,

        enterType = type,
        exitType = null,
        lastDepotExitTime = null,

        locked = false,

        pendingEnterAt = null,
        pendingExitAt = null,
        isPending = false,
        isExitPending = false,

        timestamp = System.currentTimeMillis(),
        minutesOfDay = ((time / 60000) % 1440).toInt(),

        isEnter = true,
        isExit = false
    )
}
