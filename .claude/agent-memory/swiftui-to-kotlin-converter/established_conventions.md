---
name: established-conventions
description: Recurring iOS→Android patterns established in the admin-android migration; consult before writing new conversions
metadata:
  type: project
---

## Color tokens
- `.sub` → `MaterialTheme.colorScheme.surfaceVariant`
- `.subTwo` → `MaterialTheme.colorScheme.surface`
- `.subThree` → `MaterialTheme.colorScheme.tertiary`
- `.mainOpp` → `MaterialTheme.colorScheme.onSurface`
- `.subOpp` / `.secondary` → `MaterialTheme.colorScheme.onSurfaceVariant`
- `.bg` → `MaterialTheme.colorScheme.background`

## Card pattern
```kotlin
Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(17.dp or 25.dp),
    color = MaterialTheme.colorScheme.surfaceVariant,
    tonalElevation = 2.dp
)
```
`.cardShadow()` → `tonalElevation = 2.dp`

## SF Symbol → Material Icon mappings used
- `chevron.right` → `Icons.AutoMirrored.Filled.KeyboardArrowRight`
- `chevron.down/up` → `Icons.Default.KeyboardArrowDown/Up`
- `arrow.trianglehead.2.clockwise` → `Icons.Default.Refresh`
- `medal.fill` → `Icons.Default.EmojiEvents`
- `chart.pie.fill` → `Icons.Default.PieChart`
- `lock.fill` → `Icons.Default.Lock`
- `plus` → `Icons.Default.Add`
- `gear` → `Icons.Default.Settings`
- `house.fill` → `Icons.Default.Home`
- `chart.bar.xaxis` → `Icons.Default.BarChart`
- `flag.pattern.checkered` → `Icons.Default.SportsScore`
- `person.2.fill` → `Icons.Default.Group`
- `qrcode` → `Icons.Default.QrCode`
- `eye` / `eye.slash` → `Icons.Default.Visibility` / `Icons.Default.VisibilityOff`
- Section icons with no good match → proper `Icon` composables (emojis removed 2026-06-03):
  - `chart.line.uptrend.xyaxis` → `Icons.AutoMirrored.Filled.TrendingUp`
  - `gearshape.fill` → `Icons.Default.Settings`
  - `flag.fill` (experience/golf) → `Icons.Default.SportsGolf`
  - `calendar.badge.clock` → `Icons.Default.CalendarMonth`
  - `doc.on.doc` → `Icons.Default.ContentCopy`
  - `checkmark.circle.fill` → `Icons.Default.CheckCircle`
  - `square.and.arrow.up` → `Icons.Default.Share`

## Inner card background rule (important)
iOS uses a two-level background system: `.sub` (section card) wraps `.subTwo` (inner chart/data cards).
In Compose, each level maps:
- Outer section card → `surfaceVariant`
- Inner chart / data card (subTwo) → `surface`
NEVER use `surfaceVariant` for a chart Surface that sits inside a `surfaceVariant` section card.
Fixed in 2026-06-03 audit: VisitorDonutChart, PlayerTrendChart, GamesPerDayChart, BusiestTimesChart, GameDurationTrendChart all corrected to `surface`.

## Button icon spacing rule
iOS: `HStack(spacing: 6) { Image / Text }` → Compose: `Spacer(Modifier.size(6.dp))` between
Icon and Text inside a Button, NOT `Modifier.padding(end = 6.dp)` on the Icon.

## Skeleton shimmer rule
`.skeleton(active: true)` on iOS applies shimmer to every placeholder rect.
Compose: always call `.skeleton(active = true)` on each placeholder `Surface` in loading skeletons.
Import: `com.garrettbutchko.minimate.content.tabviews.analytics.helperviews.skeleton`

## No-equivalent substitutions
- `InfoButton` → `Spacer(Modifier.width(8.dp))` (flagged as open question for full button)
- `MarqueeText` → `Text(maxLines=1, overflow=TextOverflow.Ellipsis)`
- `AsyncImage` → `coil.compose.AsyncImage` or `SubcomposeAsyncImage`
- `SwiftUI Form` → `Column + verticalScroll` with `Card` sections
- `iOS DatePicker inline` → M3 `DatePickerDialog` (button-triggered)
- `iOS NumberPickerView wheel` → `+/−` StepperControl composable
- `ShareLink` → `Intent.ACTION_SEND` via `LocalContext.current`
- `.presentationDetents` → `ModalBottomSheet` with `rememberModalBottomSheetState`
- `.skeleton(active:)` → `Modifier.skeleton()` in `SkeletonModifier.kt`
- `TitleView` (gradient banner) → `CourseBanner` with `Brush.horizontalGradient` from course colors
- `.ifAvailableGlassEffect` → `Surface(color=surfaceVariant.copy(alpha=0.92f))`
- `TabView(selection:)` → `Scaffold + NavigationBar + AnimatedContent`
- `EnvironmentObject` → explicit ViewModel parameters passed to composables

## Animation conventions (added 2026-06-03)

### Tab/section crossfades
iOS `.transition(.opacity)` on SwiftUI views inside a ZStack/switch →
Compose `AnimatedContent(targetState = ..., transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(150)) })`

### Loading state fade
iOS `.transition(.opacity)` on loading skeleton vs content →
Compose `AnimatedContent(targetState = isLoading, transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) })`

### Expand/collapse (snappy spring)
iOS `withAnimation(.snappy(duration: 0.2))` →
```kotlin
AnimatedVisibility(
    enter = expandVertically(spring(DampingRatioMediumBouncy, StiffnessMedium)) + fadeIn(tween(150)),
    exit = shrinkVertically(spring(DampingRatioNoBouncy, StiffnessMedium)) + fadeOut(tween(120))
)
```

### Chart scale+fade (topic change)
iOS `.transition(.opacity.combined(with: .scale(scale: 0.95)))` →
```kotlin
AnimatedContent(transitionSpec = {
    (fadeIn(tween(250)) + scaleIn(0.95f, spring(MediumBouncy, Medium))) togetherWith
    (fadeOut(tween(180)) + scaleOut(0.95f, tween(180)))
})
```

### Chart line draw-on animation
iOS Charts animate in automatically on appear. Compose equivalent:
```kotlin
val drawProgress = remember(data) { Animatable(0f) }
LaunchedEffect(data) {
    drawProgress.snapTo(0f)
    drawProgress.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
}
// In Canvas: clipRect(right = chartLeft + chartWidth * drawProgress) { ... draw paths ... }
```

### Donut chart draw-on animation
iOS SectorMark animates in via Charts framework. Compose equivalent:
Scale each arc's sweepAngle by `drawProgress`: `val animatedSweep = (fullSweep * drawProgress).coerceAtLeast(0f)`

### Duration API
Replace `delay(3_000L)` with `delay(3.seconds)` (import `kotlin.time.Duration.Companion.seconds`).
The Long overload triggers "Legacy Long overload can be converted to Duration" diagnostic.

## ViewModel import paths
- `AnalyticsViewModel` → `com.garrettbutchko.minimate.viewModels.analytics.AnalyticsViewModel`
- `GrowthViewModel` → `com.garrettbutchko.minimate.viewModels.analytics.GrowthViewModel`
- `OperationsViewModel` → `com.garrettbutchko.minimate.viewModels.analytics.OperationsViewModel`
- `ExperienceViewModel` → `com.garrettbutchko.minimate.viewModels.analytics.ExperienceViewModel`
- `RetentionViewModel` → `com.garrettbutchko.minimate.viewModels.analytics.RetentionViewModel`
- `LeaderBoardViewModel` → `com.garrettbutchko.minimate.viewModels.LeaderBoardViewModel`
- `CourseSettingsViewModel` → `com.garrettbutchko.minimate.viewModels.CourseSettingsViewModel`
- `CourseListViewModel` → `com.garrettbutchko.minimate.viewModels.CourseListViewModel`
- `PlayerTier` → `com.garrettbutchko.minimate.viewModels.analytics.PlayerTier`
- `DataPointObject` → `com.garrettbutchko.minimate.viewModels.analytics.DataPointObject`
- `String.toComposeColor()` → defined in `helperviews/DataCard.kt`
- `PlayerTier.composeColor` → defined in `helperviews/ExpandedPlayerTierContent.kt`
