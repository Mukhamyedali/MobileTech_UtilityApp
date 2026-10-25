# Pocket Exchange
CP3406 / CP5307 Assignment 1 - Mukhamyedali Askhar

## Purpose and features
A focused AUD currency converter for students and travellers. The main screen automatically requests daily exchange rates and displays one converted amount. Settings immediately change the AUD amount, destination currency (KZT, MNT, USD, EUR, GBP, TRY or NZD), and visibility of the unit rate. No API key is required for the lecturer-provided endpoint.

## Implementation
- Kotlin, single activity, Jetpack Compose and Material 3; original starter package/theme retained.
- MainActivity.kt: UtilityApp scaffold, bottom navigation, UtilityScreen and SettingsScreen. Scrollable content supports smaller displays and larger text.
- ExchangeViewModel.kt: immutable StateFlow state, lifecycle-aware collection, input validation, coroutine requests, cancellation propagation and duplicate-request protection.
- data/ExchangeRepository.kt: Retrofit service, repository interface/implementation and manual dependency injection through AppContainer and a ViewModel factory. This permits a fake repository in future tests.
- Retrofit endpoint: https://api.exchangerate-api.com/v4/latest/AUD (provided in class). INTERNET permission is declared; HTTPS only.
- Network data is reused when changing settings. Refresh failure retains previous data with an explicit warning. No fabricated or bundled rates are displayed.
- Settings survive rotation through the ViewModel, but not process death. Persistence is not required by the assignment.

## Open and run
1. Extract this ZIP and open the project folder containing settings.gradle.kts in Android Studio.
2. Use JDK 17 for Gradle (Android Studio: Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK). Install Android SDK 36 and accept required SDK licences.
3. Allow Gradle sync to download dependencies, then run on an API 26+ emulator/device with internet access.
4. Run ./gradlew testDebugUnitTest assembleDebug (Windows: gradlew.bat testDebugUnitTest assembleDebug).

The valid Compose artifact is lifecycle-viewmodel-compose, not lifecycle-viewmodel-ktx-compose. Lifecycle versions remain aligned with the starter at 2.9.2.

## Verification status
The build/test command was retried on 25 October 2026 after removing the starter's vendor-specific JDK 21 daemon override so Gradle uses the configured JDK 17. Gradle still could not download because network access was unavailable. Compilation, JUnit execution, emulator behavior and the live endpoint have NOT been verified. Do not interpret included tests as passed tests.
ConversionTest covers conversion, zero, decimals, malformed/negative/non-finite input, invalid or missing rates and overflow.

Manual checks to perform before submission:
- Launch online: see loading, then a conversion and rate date.
- In Settings change 100 AUD to 10 AUD and choose USD: conversion changes without an extra request.
- Toggle unit-rate visibility and rotate: selected settings survive.
- Enter blank, negative and non-number amounts: validation appears without a crash.
- Enable airplane mode before launch: error and refresh are available, with no invented data.
- Load rates, disconnect, refresh: previous result remains and stale-data warning appears.
- Check small screen, landscape, large font and dark mode.

## Submission and GitHub
Submit the project ZIP, a personalised 500-1000 word Gibbs reflection PDF, the AI declaration and your real GitHub repository link. The supplied reflection is a draft based on the implementation, not a record of your feelings or completed testing. Edit it after using and understanding the app.
Create a repository in your own GitHub account, upload the project and share it with your lecturer/coordinator. No repository has been created or accessed here. No commit history has been fabricated. The required regular development history cannot be reconstructed retrospectively; make genuine commits as you review, run and improve the project.
The organisation membership HTTP 404 in the supplied chat does not establish repository access. Confirm your invitation/access with your lecturer if needed.

## Attribution and AI use
Starter: MobileTech_UtilityApp supplied by Cu Ngoc Nguyen / JCU-Mobile-Tech. Class examples informed the ViewModel, coroutine and Retrofit approach.
ChatGPT generated the converter implementation, documentation, tests and reflection draft; see AI_Declaration.pdf. Review all generated material and follow the subject's AI rules.
References:
- https://developer.android.com/jetpack/compose
- https://developer.android.com/topic/libraries/architecture/viewmodel
- https://developer.android.com/jetpack/androidx/releases/lifecycle
- https://square.github.io/retrofit/
- https://www.exchangerate-api.com/docs/overview
