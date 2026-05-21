---
name: project-migration-status
description: Track which admin-ios SwiftUI files have been converted to admin-android Kotlin Compose and which remain
metadata:
  type: project
---

Migration of `admin-ios/views/` → `admin-android/.../content/` is 100% complete as of 2026-05-21.

**Why:** Batch migration job requested by user; all 28 views (9 pre-existing + 19 new) now have Kotlin counterparts.

**How to apply:** No files remain to convert from admin-ios/views. Future work should focus on wiring ViewModels and navigation in App.kt.

## Converted files (19 converted in this session)

| Swift | Kotlin |
|---|---|
| ContentView.swift | content/ContentView.kt |
| CourseButtonView.swift | content/CourseButtonView.kt |
| CourseListView.swift | content/CourseListView.kt |
| tabViews/MainView.swift | content/tabviews/MainView.kt |
| tabViews/analytics/AnalyticsView.swift | content/tabviews/analytics/AnalyticsView.kt |
| tabViews/analytics/helperViews/SkeletonModifier.swift | content/tabviews/analytics/helperviews/SkeletonModifier.kt |
| tabViews/analytics/tabs/ExpierenceTab.swift | content/tabviews/analytics/tabs/ExperienceTab.kt |
| tabViews/analytics/tabs/GrowthTab.swift | content/tabviews/analytics/tabs/GrowthTab.kt |
| tabViews/analytics/tabs/OperationsTab.swift | content/tabviews/analytics/tabs/OperationsTab.kt |
| tabViews/analytics/tabs/RetentionView.swift | content/tabviews/analytics/tabs/RetentionTab.kt |
| tabViews/analytics/topBar/AnalyticsRangeBar.swift | content/tabviews/analytics/topbar/AnalyticsRangeBar.kt |
| tabViews/analytics/topBar/CustomRangeSheet.swift | content/tabviews/analytics/topbar/CustomRangeSheet.kt |
| tabViews/leaderboard/BouncingBallsView.swift | content/tabviews/leaderboard/BouncingBallsView.kt |
| tabViews/leaderboard/LeaderBoardView.swift | content/tabviews/leaderboard/LeaderBoardView.kt |
| tabViews/leaderboard/PlayerScoreReview.swift | content/tabviews/leaderboard/PlayerScoreReview.kt |
| tabViews/settings/ColorHolderView.swift | content/tabviews/settings/ColorHolderView.kt |
| tabViews/settings/ColorPicker.swift | content/tabviews/settings/ColorPicker.kt |
| tabViews/settings/CourseSettingsView.swift | content/tabviews/settings/CourseSettingsView.kt |
| tabViews/tournament/TournamentView.swift | content/tabviews/tournament/TournamentView.kt |
