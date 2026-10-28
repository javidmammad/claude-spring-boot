# Session 1: key points (Phase 0 + Phase 1)

## Learner
- Javid: a frontend/mobile developer learning backend: Kotlin + Spring Boot + MongoDB
- 5–8 hours a week. The plan is 16 weeks in 9 phases: see `ROADMAP.md`
- Project: **TaskFlow**, a task and project manager REST API
- **Working mode: Javid writes the code. Claude explains, gives tasks, and reviews.**
  - Put review notes into the code as `// TODO(n)` comments (Javid asked for this, and it worked well)
  - Use step-by-step hint skeletons for anything Javid gets stuck on, rather than full solutions
  - Explain ideas by comparing them with TypeScript/React or Swift (this worked well for sealed types)
  - Javid sometimes asks Claude to write a file directly (for example `Main.kt`). That's fine when asked.

## Environment
- macOS, JDK 17 (Zulu, managed by SDKMAN). JDK 21 is recommended but not installed yet: `sdk install java 21.0.8-zulu`
- IntelliJ IDEA, Docker Desktop, and Android Studio are installed
- No standalone `kotlinc`. To compile from the CLI, use IntelliJ's bundled compiler:
  `"/Applications/IntelliJ IDEA.app/Contents/plugins/Kotlin/kotlinc/bin/kotlinc" src/main/kotlin/*.kt -include-runtime -d out.jar`
- The git repo is initialized at the root, on branch `master`, with **no commits yet**

## Phase 0: done
Java, tools, git init, and Kotlin Koans (the backend-relevant set of about 32 exercises)

## Phase 1: done ✅ (all 15 expected checks pass)
- Brief: `tasks/phase-1-kotlin.md`. Code: `phase1-kotlin/src/main/kotlin/`
- `phase1-kotlin` was created with IntelliJ's own build system instead of Gradle, so its package is `main.kotlin`. That's acceptable for this exercise.
- What Javid has learned: data classes and `copy()`, null safety (`?.`, `?:`, `?: return`, `== true`), enums with `when`, sealed interfaces with an exhaustive `when` (no `else`), extension functions, `associateBy`, `associateWith`, `groupingBy().eachCount()`, `count`, `sortedBy`, `filter`, named and default arguments, `if` as an expression, and private maps inside a class
- Mistakes Javid made, worth watching for:
  - Wrote a rule (`isOverdue`) and then didn't reuse it
  - Added `else` to a `when` over a sealed type or enum, which turns off the exhaustiveness check
  - Named a type `Result`, which clashes with `kotlin.Result`
  - Wrong id passed silently (`"1"` instead of `"p1"`)
  - Positional arguments where named ones are clearer
  - Exposed a public `var` on a store
  - Duplicated the same filter in several places
- Open minor items, all optional:
  - Extract `private fun tasksOf(projectId)`
  - Use `!` instead of `.not()`
  - `contains(query, ignoreCase = true)` in `search`
  - Put one parameter per line in `Task`
  - Stretch goals in the brief
- Housekeeping still to do:
  - Delete the leftover `phase1-kotlin/src/Main.kt` (the IntelliJ template)
  - Add a root `.gitignore` with `.idea/`, `build/` and `out/`
  - **Make the first commit**

## Next: Phase 2, Spring Boot fundamentals (weeks 3–4, about 12h)
- Install JDK 21 first, as recommended
- Generate `taskflow/` at the repo root from start.spring.io:
  - Kotlin, Gradle (Kotlin DSL)
  - Spring Web, Validation, Data MongoDB, Actuator
- Topics:
  - HTTP and REST basics
  - Gradle (`build.gradle.kts`, `./gradlew bootRun`)
  - IoC and dependency injection
  - Controller → Service → Repository
  - `@RestController` and the mapping annotations
  - `ResponseEntity`
  - `application.yml`, profiles, `@ConfigurationProperties`
  - Package by feature
- Milestone: a Projects and Tasks CRUD API stored in memory, using Phase 1's `TaskStore` ideas as the repository, tested with Bruno or Postman
- Done when: Javid can explain the request lifecycle, from an HTTP request arriving to JSON being sent back
- Follow the same format as Phase 1: write `tasks/phase-2-spring-boot.md` with setup steps, the functions or endpoints to build, rules, expected results, and stretch goals
