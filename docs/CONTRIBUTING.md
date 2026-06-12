# Contributing to Kiito

Thank you for contributing. This document is **mandatory reading** before opening a PR. Every rule here exists to prevent merge conflicts, regressions, and pattern drift.

---

## 1. Before you start

1. **Set up git hooks** (once, after cloning):
   ```bash
   git config core.hooksPath .githooks
   ```
   This auto-fixes `gradlew` permissions on every commit.

2. **Verify your environment**: Android Studio Meerkat+, JDK 17, Kotlin 2.3+.

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
| Bug fix | Regression test that would have caught the bug |

### How to write tests

- **Test location**: `composeApp/src/commonTest/kotlin/com/kito/`  
- **Use shared fakes** from `com.kito.testing` — do not create MockK mocks in commonTest (JVM-only, breaks iOS)
- **Inject `TestDispatcher`** via the `dispatcher` constructor param in VMs
- **No real I/O** — use `FakeRepository`, `FakeSyncUseCase`, temp DataStore file
- **Naming**: `methodOrScenario_condition_expectedResult` e.g. `toDomain_nullOfficeRoom_mapsToNull`

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
| Wildcard imports (`import com.kito.*`) | Explicit imports only |
| Commit generated files (`build/`, `.idea/`) | They're gitignored — don't force-add |
| Touch `sap/sensitive/` files in PRs | These are gitignored intentionally |

---

## 6. PR checklist

Before requesting review, verify every item:

- [ ] `./gradlew :composeApp:compileAndroidMain :androidApp:compileDebugKotlin` is green
- [ ] `./gradlew :composeApp:desktopTest` is green (all existing tests pass)
- [ ] **New/changed code has tests** — mapper, repository, use case, or ViewModel as applicable (PRs without tests will not be merged)
- [ ] No `database.entity` or `supabase.model` imports in any `presentation/` file
- [ ] New feature has: domain model, repository interface, mapper, DI module, mapper test
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
