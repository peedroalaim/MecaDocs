# Implementation Plan - Local AI Result Caching & String Resource Extraction

Implement two major production enhancements:
1. **Local AI Result Caching via Room DB**: Dynamically cache successfully fetched AI datasheets so any component queried online becomes available offline.
2. **String Resource Extraction**: Move hardcoded UI strings into `res/values/strings.xml` for localization readiness.

## User Review Required

> [!IMPORTANT]
> - **AI Caching**: A new Room table `cached_components` will store successfully fetched AI datasheets. When offline or when AI pool is exhausted, the app will search local favorites first, then cached components, then hardcoded mocks.
> - **Strings**: UI strings across screens and dialogs will be moved to `strings.xml` and accessed via `stringResource`.

## Proposed Changes

### Local Database & Caching
#### [NEW] [CachedComponentEntity.kt](file:///C:/Users/SoftWorld/AndroidStudioProjects/MecaDocs/app/src/main/java/com/softworld/mecadocs/data/local/entity/CachedComponentEntity.kt)
- Room entity for caching online AI datasheet results.
#### [MODIFY] [MecaDocsDao.kt](file:///C:/Users/SoftWorld/AndroidStudioProjects/MecaDocs/app/src/main/java/com/softworld/mecadocs/data/local/dao/MecaDocsDao.kt)
- Add queries and insert methods for cached components.
#### [MODIFY] [MecaDocsDatabase.kt](file:///C:/Users/SoftWorld/AndroidStudioProjects/MecaDocs/app/src/main/java/com/softworld/mecadocs/data/local/database/MecaDocsDatabase.kt)
- Register `CachedComponentEntity` in Room database schema and increment version.
#### [MODIFY] [MecaDocsRepository.kt](file:///C:/Users/SoftWorld/AndroidStudioProjects/MecaDocs/app/src/main/java/com/softworld/mecadocs/data/repository/MecaDocsRepository.kt)
- Check cached components in step 1.5 of `getComponentDetails()`, and cache successful online responses.

### Localization & String Resources
#### [MODIFY] [strings.xml](file:///C:/Users/SoftWorld/AndroidStudioProjects/MecaDocs/app/src/main/res/values/strings.xml)
- Add string keys for app labels, button text, search placeholders, headers, and error messages.
#### [MODIFY] UI Composables & Dialogs
- Update `HomeScreen`, `DatasheetResultScreen`, and `VoltageDividerDialog` to use `stringResource(...)`.

## Verification Plan

### Automated Tests
- Run `app:testDebugUnitTest` to verify repository and viewmodel tests pass.
- Run `app:assembleDebug` to verify compilation.
