package com.kito.feature.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.kito.feature.home.domain.model.EventOrAd
import com.kito.feature.schedule.presentation.horizontalCarouselTransition
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EventAndAdBanner(
    eventsAndAds: List<EventOrAd>,
    modifier: Modifier = Modifier,
    onClick: (
        url : String,
        isAd : Boolean
    ) -> Unit
) {
    if (eventsAndAds.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { eventsAndAds.size })

    LaunchedEffect(eventsAndAds) {
        while (true) {
            delay(3000)
            val next = (pagerState.currentPage + 1) % eventsAndAds.size
            pagerState.animateScrollToPage(next)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            contentPadding = PaddingValues(
                horizontal = if (eventsAndAds.size > 1) 24.dp else 12.dp
            ),
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(6f)
        ) { page ->
            val eventOrAd = eventsAndAds[page]
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalCarouselTransition(
                        page = page,
                        pagerState = pagerState,
                        scale = 0.90f
                    ),
                shape = RoundedCornerShape(16.dp),
                onClick = {
                    eventOrAd.let {
                        onClick(
                            it.clickUrl ?: "",
                            it.isAd
                        )
                    }
                }
            ) {
                Box() {
                    AsyncImage(
                        model = eventOrAd.mediaUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (eventOrAd.isAd) {
                        Card(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(horizontal = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Gray.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                text = "Sponsor",
                                style = MaterialTheme.typography.labelSmallEmphasized,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        // Dot indicators
        if (eventsAndAds.size > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(eventsAndAds.size) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (pagerState.currentPage == index) 8.dp else 5.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index)
                                    Color(0xFFFF6B35)
                                else
                                    Color.Gray.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }
    }
}
