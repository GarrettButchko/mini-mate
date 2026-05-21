---
name: open-questions
description: Android-side gaps and open decisions flagged during admin-ios migration
metadata:
  type: project
---

## Open Questions for Mobile Delegation Team

**Why:** These items were encountered during the batch SwiftUI→Kotlin migration on 2026-05-21 and require Android-side decisions or implementations before the screens are fully functional.

**How to apply:** Review before doing Android-side QA or wiring sessions.

### 1. InfoButton — no Android equivalent yet
- iOS has `InfoButton` (info.circle icon → Alert popup)
- All usages replaced with `Spacer` as a placeholder
- Recommendation: Create a reusable `InfoButton` composable using `AlertDialog`
- Files affected: RetentionTab.kt, ExperienceTab.kt, DataCard.kt (title row)

### 2. BouncingBallsView — physics animation not ported
- iOS used named "BouncingBallsView" but actually rendered a static podium
- Android renders the same static podium; no physics animation was in the Swift source either
- No action needed unless animation is desired later

### 3. Image upload (logo, ad image) — stub only
- iOS used `PhotoPicker` (UIKit image picker) in CourseSettingsView
- Android implementation requires `ActivityResultContracts.GetContent` + a file provider
- Currently shows text "upload not yet wired" placeholder
- Action: wire `rememberLauncherForActivityResult` for image selection in `CourseSettingsView.kt`

### 4. ViewManager / navigation wiring in App.kt
- ContentView.kt exports a `ViewState` sealed class mirroring iOS ViewManager states
- App.kt currently has a stub that doesn't render ContentView
- Action: replace App.kt body with `ContentView(viewState=..., ...)` and wire ViewModel factories

### 5. WelcomeView and SignInView
- Not present in `admin-ios/views/` — they live in shared-admin
- ContentView.kt has placeholder Text composables for those states
- Action: locate and port or re-use from shared once available

### 6. ProfileView sheet
- CourseListView and MainView had profile avatar buttons that open ProfileView
- ProfileView is not in `admin-ios/views/` (likely in a different directory)
- Currently the avatar taps do nothing on Android
- Action: locate ProfileView.swift and convert; wire tap handler

### 7. SectionHealthDetailView sheet
- iOS MainView opens SectionHealthDetailView on HealthRatingChart tap
- Not in scope of this migration directory
- Action: locate and convert; wire tap on HealthRatingChart

### 8. AnalyticsSection.entries / AnalyticsSection.rawValue
- AnalyticsView.kt references `AnalyticsSection.entries` (Kotlin enum `values()`) and `.rawValue`
- Verify shared-admin `AnalyticsSection` Kotlin enum has matching API; adjust if it uses `.name` instead

### 9. GrowthViewModel.growthChartTopic.copy(name)
- GrowthTab.kt calls `vm.growthChartTopic.copy("total")` — verify ChartTopic is a data class with that copy API
- If not, adjust to the actual setter the shared-admin ViewModel exposes

### 10. CourseListViewModel API assumptions
- CourseListView.kt calls `vm.kotlin.navigateToCourseTab(tab:)` — verify this exists on the KMM VM
- Also assumes `vm.hasCourse`, `vm.userCourses`, `vm.loadingCourse`, `vm.message`, `vm.timeRemaining`, `vm.password`, `vm.showAddCourseAlert` exist as observable Kotlin properties
