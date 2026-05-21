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
- Section icons with no good match → emoji text stand-ins (📈⚙️⛳)

## No-equivalent substitutions
- `InfoButton` → `Spacer` (flagged as open question)
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
