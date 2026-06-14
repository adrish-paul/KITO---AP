package com.kito.feature.friendview.presentation

sealed interface FriendViewEvent {
    data class SelectFriend(val roll: String) : FriendViewEvent
    data class AddFriend(val roll: String) : FriendViewEvent
    data class RemoveFriend(val roll: String) : FriendViewEvent
}
