package com.kito.feature.faculty.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.kito.core.designsystem.UIColors
import com.kito.core.presentation.components.animation.NoInternetAnimation
import com.kito.core.presentation.components.state.SearchResultState
import com.kito.core.presentation.components.state.SyncUiState
import com.kito.core.presentation.navigation3.Routes
import com.kito.feature.faculty.presentation.components.FacultyCard
import com.kito.feature.faculty.presentation.components.FacultyShimmerCard
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.koin.compose.koinInject


@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeApi::class,
    ExperimentalHazeMaterialsApi::class, ExperimentalMaterial3ExpressiveApi::class,
    FlowPreview::class, ExperimentalComposeUiApi::class
)
@Composable
fun FacultyScreen(
    rootNavBackStack: NavBackStack<NavKey>,
    viewModel: FacultyScreenViewModel = koinInject()
) {
    val facultyList by viewModel.faculty.collectAsState()
    val searchResultState by viewModel.searchResultState.collectAsState()
    val facultySearchResult by viewModel.facultySearchResult.collectAsState()
    val hazeState = rememberHazeState()
    val cardHaze = rememberHazeState()
    val uiColors = UIColors()
    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val isOnline by viewModel.isOnline.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = searchBarState.currentValue == SearchBarValue.Expanded,
        onBackCompleted = {
            scope.launch {
                searchBarState.animateToCollapsed()
                viewModel.clearSearchResult()
                textFieldState.clearText()
            }
        }
    )
    val inputField =
        @Composable {
            SearchBarDefaults.InputField(
                colors = SearchBarDefaults.inputFieldColors(
                    focusedContainerColor = uiColors.cardBackground,
                    unfocusedContainerColor = uiColors.cardBackground,
                ),
                modifier = Modifier,
//                    .width(
//                        width = when (isPortrait) {
//                            false -> screenWidthDp * 0.337f
//                            true -> screenWidthDp
//                        }
//                    )
                searchBarState = searchBarState,
                textFieldState = textFieldState,
                onSearch = {
                    keyboardController?.hide()
                },
                placeholder = {
                    Text(
                        text = "Search Faculty...",
                        style = MaterialTheme.typography.titleMediumEmphasized
                    )
                },
                leadingIcon = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                searchBarState.animateToCollapsed()
                                viewModel.clearSearchResult()
                                textFieldState.clearText()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                trailingIcon = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(uiColors.accentOrangeStart)
                            .clickable(onClick = {
                                keyboardController?.hide()
                            }),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Search",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        }
    var targetPadding by remember { mutableStateOf(0.dp) }
    val animatedPadding by animateDpAsState(
        targetValue = targetPadding,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        ),
        label = "attendance"
    )
    LaunchedEffect(searchBarState.currentValue) {
        targetPadding = if (searchBarState.currentValue == SearchBarValue.Expanded) {
            25.dp
        } else {
            0.dp
        }
    }
    LaunchedEffect(textFieldState) {
        launch {
            snapshotFlow { textFieldState.text.toString() }
                .debounce(300)
                .collect { query ->
                    if (searchBarState.currentValue == SearchBarValue.Expanded) {
                        viewModel.getSearchResult(query)
                    }
                }
        }
    }
    Box(
        modifier = Modifier.hazeSource(cardHaze)
    ) {
        Box(
            modifier = Modifier.background(Color(0xFF121116))
        ) {
            when (syncState) {
                is SyncUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = (syncState as SyncUiState.Error).message)
                    }
                }

                SyncUiState.Idle -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        NoInternetAnimation()
                    }
                }

                SyncUiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .padding(
                                top = WindowInsets.statusBars.asPaddingValues()
                                    .calculateTopPadding() + 46.dp + animatedPadding,
                                start = 16.dp,
                                end = 16.dp
                            )
                            .hazeSource(hazeState)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        repeat(20) { index ->
                            FacultyShimmerCard(
                                index = index,
                                listSize = 20,
                                uiColors = uiColors
                            )
                        }
                    }
                }

                SyncUiState.Success -> {
                    Box {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                top = WindowInsets.statusBars.asPaddingValues()
                                    .calculateTopPadding() + 46.dp + animatedPadding,
                                bottom = 86.dp + WindowInsets.navigationBars.asPaddingValues()
                                    .calculateBottomPadding()
                            ),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier
                                .hazeSource(hazeState)
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            if (searchBarState.currentValue != SearchBarValue.Expanded || searchResultState is SearchResultState.Idle) {
                                itemsIndexed(facultyList) { index, faculty ->

                                    FacultyCard(
                                        faculty = faculty,
                                        index = index,
                                        listSize = facultyList.size,
                                        uiColors = uiColors,
                                        onFacultyClick = {
                                            rootNavBackStack.add(
                                                Routes.FacultyDetail(facultyId = it.id ?: 0)
                                            )
                                        }
                                    )
                                }
                            } else {
                                itemsIndexed(facultySearchResult) { index, faculty ->
                                    FacultyCard(
                                        faculty = faculty,
                                        index = index,
                                        listSize = facultySearchResult.size,
                                        uiColors = uiColors,
                                        onFacultyClick = {
                                            rootNavBackStack.add(
                                                Routes.FacultyDetail(facultyId = it.id ?: 0)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Column(
                modifier = Modifier
                    .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin()) {
                        blurRadius = 15.dp
                        noiseFactor = 0.05f
                        alpha = 0.98f
                    }
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(
                    modifier = Modifier.height(
                        16.dp + WindowInsets.statusBars.asPaddingValues()
                            .calculateTopPadding()
                    )
                )
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Faculty",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = uiColors.textPrimary,
                            style = MaterialTheme.typography.titleLargeEmphasized,
                            modifier = Modifier.weight(1f)
                        )
                        if (isOnline) {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        searchBarState.animateToExpanded()
                                    }
                                },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.08f),
                                    contentColor = uiColors.accentOrangeStart
                                ),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonSearch,
                                    contentDescription = "Search",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    this@Column.AnimatedVisibility(
                        visible = searchBarState.currentValue == SearchBarValue.Expanded,
                        enter = fadeIn(
                            tween(
                                durationMillis = 400,
                                easing = FastOutSlowInEasing
                            )
                        ) + expandVertically(
                            tween(
                                durationMillis = 400,
                                easing = FastOutSlowInEasing
                            )
                        ),
                        exit = fadeOut(
                            tween(
                                durationMillis = 400,
                                easing = FastOutSlowInEasing
                            )
                        ) + shrinkVertically(
                            tween(
                                durationMillis = 400,
                                easing = FastOutSlowInEasing
                            )
                        ),
                    ) {
                        SearchBar(
                            state = searchBarState,
                            inputField = inputField
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}



