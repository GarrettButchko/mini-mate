---
name: project-player-tier
description: PlayerTier is a presentation-layer type defined independently on each platform — it is NOT in the shared-admin Kotlin module
metadata:
  type: project
---

`PlayerTier` is defined as a Swift enum in the iOS layer at `working/iosApps/admin-ios/admin-ios/views/tabViews/analytics/tabs/RetentionView.swift`. It has no counterpart in the shared-admin Kotlin module (only `recomputePlayerTiers()` lives there). On Android it must be declared as a Kotlin `enum class` in the presentation layer.

Fields: `label: String`, `description: String`. Cases: NEW, MID_TIER, FREQUENT, AT_RISK.

**Why:** The shared-admin module only exposes computation logic; the tier type itself is a UI construct defined per platform.

**How to apply:** When converting any Swift component that references `PlayerTier`, define the Kotlin enum class in the same file or in the helperviews package — do not look for it in shared-admin.
