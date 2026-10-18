# JellyBoi

JellyBoi is the MiniMate platform for modern mini golf experiences, including digital scorekeeping, live leaderboards, and operator tools.

## Repository Overview

This repository contains multiple apps and shared modules:

- `web/` – Next.js web application
- `functions/` – Firebase Cloud Functions (TypeScript)
- `working/shared/` – Kotlin Multiplatform shared module
- `working/androidApps/` – Android apps
- `working/iosApps/` – iOS apps

## Getting Started

### Web app

```bash
cd web
npm install
npm run dev
```

### Firebase Functions

```bash
cd functions
npm install
npm run build
```

### Mobile (KMP)

Open the repository in Android Studio for Android/KMP development, or open `working/iosApps/MiniMate.xcworkspace` in Xcode for iOS development.

## Useful Commands

### Web

```bash
cd web
npm run lint
npm run build
```

### Functions

```bash
cd functions
npm run build
```

### Shared/mobile

```bash
./gradlew build
```
