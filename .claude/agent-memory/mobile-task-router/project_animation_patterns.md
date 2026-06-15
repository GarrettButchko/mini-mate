---
name: project-animation-patterns
description: Animation conventions used across iOS (SwiftUI) and Android (Compose) in MiniMate — entrance animations, tab transitions, and ad appearance
metadata:
  type: project
---

## Ad appearance animation

Both platforms — `CourseBannerAdView` — needed a fade + slide-up entrance animation when ads first appear. No animation existed previously (hard snap-in).

- **iOS** (`user-ios/views/helper/CourseBannerAdView.swift`): Added `@State private var isVisible` + `.opacity()` + `.offset(y:)` + `.onAppear { withAnimation(.spring(response: 0.45, dampingFraction: 0.75)) { isVisible = true } }`. Applied to both the custom course ad branch and the Google BannerAdView branch.
- **Android** (`user-android/content/helper/CourseBannerAdView.kt`): Wrapped both `when` branches in `AnimatedVisibility` with `fadeIn + slideInVertically` using a spring spec. A `LaunchedEffect(Unit) { visible = true }` triggers the entrance.

**Why:** Ads previously snapped in with no animation, which looked jarring. Smooth entrance matches the overall UI polish of the app.

**How to apply:** Any future ad components or conditionally-shown banners should use these same patterns — SwiftUI spring with opacity/offset, Compose AnimatedVisibility with spring.

## Tab swipe animation (Android admin app)

The iOS admin app's `TabView` naturally provides a sliding tab transition. The Android `CourseTabView` in `admin-android/content/ContentView.kt` previously used a plain `fadeIn() togetherWith fadeOut()` for `AnimatedContent`.

**Fix applied:** Added `previousTab` state tracking + directional `slideInHorizontally`/`slideOutHorizontally` (300ms tween) combined with `fadeIn`/`fadeOut` (150ms tween). Direction is determined by comparing `targetState` (new tab index) to `previousTab` (prior tab index) — higher index slides in from right, lower index slides in from left. `previousTab` is updated in each nav bar `onClick` before updating `selectedTab`.

**Why:** The iOS leaderboard and stats views have rich directional slide animations when switching sections. The Android tab view had no directional feel at all.

**How to apply:** Any future tab or section switchers on Android should use this same `previousTab` tracking pattern for directional slides. The stats `AnimatedContent` in `user-android/content/stats/StatsView.kt` already does this correctly and was the reference implementation.
