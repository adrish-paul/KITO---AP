package com.kito.feature.gpa.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kito.core.presentation.components.RopeTabRow
import org.koin.compose.koinInject

@Composable
fun GPAScreen(
    viewModel: GPAViewmodel = koinInject()
) {

    var selectedSemester by remember { mutableStateOf(6) }
    var selectedBranch by remember { mutableStateOf("CSE") }

    val roll by viewModel.roll.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121116))
    ) {

        Spacer(
            modifier = Modifier.height(
                WindowInsets.statusBars
                    .asPaddingValues()
                    .calculateTopPadding() + 12.dp
            )
        )

        Box(
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            GPAHeader(
                roll = roll,
                isLoading = roll.isEmpty(),
                selectedSemester = selectedSemester,
                selectedBranch = selectedBranch,
                onSemesterSelected = { selectedSemester = it },
                onBranchSelected = { selectedBranch = it }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        RopeTabRow(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> SGPAScreen(
                selectedSemester = selectedSemester,
                selectedBranch = selectedBranch
            )
            1 -> CGPAScreen()
        }

        Spacer(
            modifier = Modifier.height(
                WindowInsets.navigationBars
                    .asPaddingValues()
                    .calculateBottomPadding()
            )
        )
    }
}
