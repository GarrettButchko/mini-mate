package com.garrettbutchko.minimate.content

import androidx.compose.runtime.staticCompositionLocalOf
import com.garrettbutchko.minimate.viewModels.LeaderBoardViewModel
import com.garrettbutchko.minimate.viewModels.analytics.AnalyticsViewModel

// AnalyticsViewModel and LeaderBoardViewModel are Koin `factory`-scoped (mirrors iOS,
// where both are @StateObjects declared inside CourseTabView and get a fresh instance
// whenever it remounts). They're resolved exactly once, in App(), and shared with every
// descendant through these CompositionLocals — calling koinInject() again anywhere else
// would each yield a different factory instance.
val LocalAnalyticsViewModel = staticCompositionLocalOf<AnalyticsViewModel> {
    error("LocalAnalyticsViewModel not provided — must be read within App()'s composition")
}

val LocalLeaderBoardViewModel = staticCompositionLocalOf<LeaderBoardViewModel> {
    error("LocalLeaderBoardViewModel not provided — must be read within App()'s composition")
}
