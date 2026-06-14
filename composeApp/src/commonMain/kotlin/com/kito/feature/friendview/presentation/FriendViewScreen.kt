package com.kito.feature.friendview.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.kito.core.designsystem.SharedExpandContainer
import com.kito.core.presentation.navigation3.Routes
import org.koin.compose.koinInject

@Composable
fun FriendView(
    onBack: () -> Unit,
    viewmodel: FriendViewViewmodel = koinInject()
) {
    val selectedRoll by viewmodel.selectedFriendRoll.collectAsState()
    val friendRolls by viewmodel.friendRolls.collectAsState()
    val schedule by viewmodel.weeklySchedule.collectAsState()

    SharedExpandContainer(
        routeKey = Routes.FriendView,
        backgroundColor = Color(0xFF121116),
    ) {
        FriendViewContent(
            selectedRoll = selectedRoll,
            friendRolls = friendRolls,
            schedule = schedule,
            onBack = onBack,
            onSelectFriend = { roll -> viewmodel.onEvent(FriendViewEvent.SelectFriend(roll)) },
            onRemoveFriend = { roll -> viewmodel.onEvent(FriendViewEvent.RemoveFriend(roll)) },
            onAddFriend = { roll -> viewmodel.onEvent(FriendViewEvent.AddFriend(roll)) }
        )
    }
}