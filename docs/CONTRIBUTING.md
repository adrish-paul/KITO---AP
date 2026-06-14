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
Every screen **must** be split into two composables:

```kotlin
// Stateful wrapper — DI only, no UI logic
@Composable
fun FeatureScreen(viewModel: FeatureViewModel = koinInject()) {
    val state by viewModel.state.collectAsState()
    FeatureContent(state = state, onEvent = viewModel::onEvent)
}

// Stateless content — previewable, testable, no DI
@Composable
fun FeatureContent(state: ..., onEvent: ...) { ... }

@Preview
@Composable
private fun FeatureContentPreview() {
    FeatureContent(state = ..., onEvent = {})
}
```

Rules:
- `*Screen` — injects ViewModel via Koin, collects state, delegates to `*Content`
- `*Content` — pure composable, no `koinInject()`, no `collectAsState()`, previewable without DI
- Navigation-aware components (`SharedExpandContainer`, `LocalNavAnimatedContentScope`) go in `*Screen` only — never in `*Content`
- If `*Content` has `while(true)` animation loops, add `enableAnimations: Boolean = true` param and guard every loop: `LaunchedEffect(Unit) { if (!enableAnimations) return@LaunchedEffect; while(true) { ... } }`

### State and Event Separation
- **Separate Files**: Do not keep `*UiState` or `*Event` classes inside the same file as the ViewModel or Screen. Place each of them in their own dedicated `.kt` file at the root of the screen/feature package to keep files clean and readable.
- **Unidirectional Event Pipeline**:
  - Composables must not invoke ViewModel functions directly. Instead, all user interactions must be sent to the ViewModel as UI Events via `viewModel.onEvent(event)`.
  - Define `*Event.kt` for UI actions sent *to* the ViewModel (e.g. `OnboardingEvent.CompleteOnboarding`).
  - Define `*UiEvent.kt` for navigation or side-effect events sent *from* the ViewModel back to the screen (e.g. `OnboardingUiEvent.OnboardingCompleted`).

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
