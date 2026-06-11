package com.kito.feature.friendview.domain.repository

import com.kito.feature.friendview.domain.model.FriendScheduleItem

interface FriendViewRepository {
    suspend fun getFriendSchedule(roll: String): List<FriendScheduleItem>
}
