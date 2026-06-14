# Contributing to Kiito

Thank you for contributing. This document is **mandatory reading** before opening a PR. Every rule here exists to prevent merge conflicts, regressions, and pattern drift.

---

## 1. Before you start

1. **Verify your environment**: Android Studio Meerkat+, JDK 17, Kotlin 2.3+.

---

## 2. Branch & commit rules

- One feature or fix per branch: `feature/`, `fix/`, `refactor/`, `test/`
- One atomic step per commit — small, independently verifiable, reversible
- Commit message format: `type: short description` (e.g. `fix: null crash in AttendanceRepository`)
- **Never commit to `main` directly** — always open a PR

---

## 3. Architecture rules (non-negotiable)

### Feature structure
Every feature **must** follow this package layout:
```
feature/<name>/
├── domain/
│   ├── model/          # Pure Kotlin data classes only — no Room/Ktor/Android imports
│   ├── repository/     # Interfaces only — no implementations
│   └── usecase/        # Business logic (only when real logic exists)
├── data/
│   ├── mapper/         # ONLY place allowed to import both entity/DTO and domain model
│   └── <Name>RepositoryImpl.kt
├── di/
│   └── <Name>Module.kt
└── presentation/
    ├── <Name>ViewModel.kt
    ├── <Name>Screen.kt
    └── components/
```

### File Atomicity (Mandatory across all layers)
To prevent bloated files, avoid merge conflicts, and keep the codebase modular, **every file must be strictly atomic**. This means each file must contain exactly one class, interface, function, or configuration object.

This rule is strictly enforced across all layers of a feature:
- **Domain Layer**:
  - Each domain model (`*Model.kt`), repository interface (`*Repository.kt`), and usecase (`*UseCase.kt`) must reside in its own dedicated file.
- **Data Layer**:
  - The repository implementation (`*RepositoryImpl.kt`), each data source, and each mapping function or class must be defined in its own file.
- **Presentation Layer**:
  - The stateful screen wrapper (`*Screen.kt`), stateless content layout (`*Content.kt`), and ViewModel (`*ViewModel.kt`) must each have their own separate files.
  - UI state models (`*UiState.kt`), event classes (`*Event.kt`), and side-effect events (`*UiEvent.kt`) must not be bundled together or inside the ViewModel file; each must reside in a separate dedicated file.
  - All sub-composables, shimmers, local helper functions, sorting logic, and configuration constants/maps must be extracted into their own individual, atomic `.kt` files inside the `components/` subfolder.

### The golden rule (enforced by CI)
**No Room entity or network DTO import in any `presentation/` file — ever.**

```kotlin
// ❌ NEVER in a ViewModel or Screen
import com.kito.core.database.entity.AttendanceEntity
import com.kito.core.network.supabase.model.TeacherModel

// ✅ Only domain models
import com.kito.feature.attendance.domain.model.Attendance
```

### Dependency direction
```
presentation → domain ← data
```
- `presentation` depends on `domain` interfaces — never on `data` concretions  
- `data` depends on `domain` interfaces — implements them  
- `domain` depends on nothing in the project

### ViewModels
- Inject `CoroutineDispatcher` (defaulted to `Dispatchers.Default`) for all `viewModelScope.launch` calls — required for testability
- Expose state via `StateFlow` / `SharedFlow` — no `LiveData`
- No `toast()` calls inside stateless `*Content` composables — keep them pure

### Screen structure (mandatory)
Every screen **must** be split into separate, atomic files:

1. **Stateful Screen (`*Screen.kt`)**: Placed in the root of the feature's `presentation` folder. This is a thin stateful wrapper containing **only** Koin DI injection, state collection, and delegation to the stateless content.
2. **Stateless Content (`*Content.kt`)**: Placed in the root of the feature's `presentation` folder. This contains the layout logic and must be pure, previewable, and testable without DI. The `@Preview` function for the content can remain at the bottom of this file.
3. **Components & Helpers (`components/` subfolder)**:
   - Sub-composables, custom layouts, and shimmers (e.g. `FeatureShimmerItem`, `FeatureItemCard`) must go in their own dedicated files inside the `components/` subfolder.
   - Helper/utility functions (e.g. `formatValue`), sorting logic, and configuration maps/constants (e.g. `categoryPriorityMap`) must be extracted into their own individual, atomic `.kt` files inside the `components/` subfolder.
   - Absolutely no bloated files containing multiple unrelated top-level classes, functions, or configurations are permitted.

Rules:
- `*Screen` — injects ViewModel via Koin, collects state, delegates to `*Content`
- `*Content` — pure composable, no `koinInject()`, no `collectAsState()`, previewable without DI
- Navigation-aware components (`SharedExpandContainer`, `LocalNavAnimatedContentScope`) go in `*Screen` only — never in `*Content`
- If `*Content` has `while(true)` animation loops, add `enableAnimations: Boolean = true` param and guard every loop: `LaunchedEffect(Unit) { if (!enableAnimations) return@LaunchedEffect; while(true) { ... } }`

### State and Event Separation
- **Separate Files**: Do not keep `*UiState` or `*Event` classes inside the same file as the ViewModel or Screen. Place each of them in their own dedicated `.kt` file at the root of the screen/feature package to keep files clean and readable.
- **Unidirectional Event Pipeline**:
  - Composables must not invoke ViewModel functions directly. Instead, all user interactions must be sent to the ViewModel as UI Events via `viewModel.onEvent(event)`.
  - Define `*Event.kt` for UI actions sent *to* the ViewModel (e.g. `FeatureEvent.SubmitAction`).
  - Define `*UiEvent.kt` for navigation or side-effect events sent *from* the ViewModel back to the screen (e.g. `FeatureUiEvent.NavigationTriggered`).

### Dependency Injection (Koin)
- Every feature owns its own `di/<Name>Module.kt`
- Register using `single<Impl>() bind Interface::class`
- Register the module in **all three** platform entry points: `androidMain`, `iosMain`, `desktopMain`
- Remove VMs from `CommonModule` when adding a feature module

---

## 4. Testing rules

**Tests are not optional. PRs without tests for new or changed code will not be merged.**

Every PR must include tests covering what was added or changed:

| What you wrote | Tests required |
|---------------|----------------|
| Domain model + mapper | Mapper test (every field, every null case) |
| Repository implementation | Repository test via fake (all methods) |
| Use case with real logic | Use case test (all branches) |
| ViewModel | VM test (initial state, state transitions, all public functions) |
| New screen `*Content` composable | Compose UI test (content state + empty/error state) |
| Bug fix | Regression test that would have caught the bug |

### How to write tests

- **Test location**: `composeApp/src/commonTest/kotlin/com/kito/`  
- **Use shared fakes** from `com.kito.testing` — do not create MockK mocks in commonTest (JVM-only, breaks iOS)
- **Inject `TestDispatcher`** via the `dispatcher` constructor param in VMs
- **No real I/O** — use `FakeRepository`, `FakeSyncUseCase`, temp DataStore file
- **DataStore Testing Constraints**:
  - **Single Active Instance**: Avoid the `multiple active DataStores` conflict by passing a trackable `CoroutineScope(testDispatcher + SupervisorJob())` to `PreferenceDataStoreFactory.createWithPath(...)`, and calling `datastoreScope.cancel()` in your `@AfterTest` teardown. This releases the file lock before the next test runs.
  - **No `java.io.File`**: Do not use standard JVM `java.io.File` or its JVM-specific `toOkioPath()` extension inside `commonTest` (which breaks iOS and non-JVM target builds). Instead, use Okio's generic `toPath()` extension on `String` (e.g. `"test.preferences_pb".toPath()`) and delete it via `FileSystem.SYSTEM.delete(path)`.
  - **Import `okio.SYSTEM`**: When deleting files via `FileSystem.SYSTEM`, make sure to explicitly include `import okio.SYSTEM` in your imports, as it is an extension property and will not compile on KMP targets without this import.
- **Naming**: `methodOrScenario_condition_expectedResult` e.g. `toDomain_nullOfficeRoom_mapsToNull`

### Compose UI tests

Use `runComposeUiTest` v2 (requires CMP 1.11+) in `commonTest`:

```kotlin
@OptIn(ExperimentalTestApi::class)
class FeatureUiTest {
    @Test
    fun feature_content_rendersList() = runComposeUiTest {
        setContent {
            FeatureContent(
                items = listOf(featureItem()),
                enableAnimations = false  // always false in tests
            )
        }
        onNodeWithTag("feature_list").assertIsDisplayed()
    }
}
```

Rules:
- Always pass `enableAnimations = false` to `*Content` composables in tests
- Use `Modifier.semantics { testTag = "..." }` on key nodes: list container, empty state, loading indicator
- Test at minimum: content renders, empty state renders
- Never instantiate a ViewModel or use Koin in a Compose UI test — pass data directly

### Run before pushing
```bash
./gradlew :composeApp:desktopTest
```
All existing tests must still pass. New tests must pass.

---

## 5. What NOT to do

| ❌ Don't | ✅ Do instead |
|----------|--------------|
| Add logic to `data/mapper/` | Logic goes in `domain/usecase/` |
| Use `SupabaseRepository` directly in a ViewModel | Create a feature repository interface |
| Add to `CommonModule` | Create a per-feature DI module |
| Add `viewModelScope.launch {` without dispatcher | `viewModelScope.launch(dispatcher) {` |
| Put feature-specific UI in `core/` | Put it in `feature/<name>/presentation/components/` |
| Put DI (`koinInject`) or nav scope in `*Content` composable | Keep those in the stateful `*Screen` wrapper |
| Leave `while(true)` loops unguarded in `*Content` | Add `enableAnimations: Boolean = true` and guard the loop |
| Keep helpers, configurations, or multiple composables/functions in Screen/Content files | Extract each helper function, constant, configuration map, and sub-composable into its own atomic `.kt` file under `components/` |
| Wildcard imports (`import com.kito.*`) | Explicit imports only |
| Commit generated files (`build/`, `.idea/`) | They're gitignored — don't force-add |
| Touch `sap/sensitive/` files in PRs | These are gitignored intentionally |

---

## 6. PR checklist

Before requesting review, verify every item:

- [ ] `./gradlew :composeApp:compileAndroidMain :androidApp:compileDebugKotlin` is green
- [ ] `./gradlew :composeApp:desktopTest` is green (all existing tests pass)
- [ ] **New/changed code has tests** — mapper, repository, use case, ViewModel, and Compose UI as applicable (PRs without tests will not be merged)
- [ ] No `database.entity` or `supabase.model` imports in any `presentation/` file
- [ ] New feature has: domain model, repository interface, mapper, DI module, mapper test
- [ ] Screen has stateless `*Content(params, lambdas)` composable + `@Preview`
- [ ] **Files are atomic**: No helper functions, configuration constants, or sub-composables are in screen or content files; each resides in its own `.kt` file under `components/`
- [ ] `*Content` has `testTag` on key nodes (list, empty, loading) and a Compose UI test
- [ ] `*Content` has `enableAnimations: Boolean = true` if it contains any `while(true)` animation loop
- [ ] ViewModel has `CoroutineDispatcher` constructor param with default
- [ ] Feature DI module registered in Android + iOS + Desktop entry points
- [ ] No new entries added to `CommonModule` for feature-specific types
- [ ] PR description explains what changed and what was tested

---

## 7. CI

Two workflows run automatically:

- **`pr-check.yml`** — runs on every PR: compile + `desktopTest` + coverage verify. Must be green to merge.
- **`release-gate.yml`** — runs on version tags: full matrix including iOS simulator and Android emulator.

A red CI = no merge. Fix it before requesting review.

---

## 8. Questions?

If an architectural decision is unclear, open a discussion on GitHub — don't guess and diverge silently.
