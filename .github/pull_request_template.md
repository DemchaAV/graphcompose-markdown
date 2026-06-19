<!--
Title (the field above) follows Conventional Commits:  type(scope): concrete effect
  type:  feat | fix | refactor | chore | docs | test
  scope: api | parser | mapper | model | theme | render | extension | composer | docs
  e.g.   feat(render): code-block renderer with language label bar

Write the body in three beats — Why → What → Verification. Delete any heading you
have nothing real to put under — never ship an empty placeholder.
-->

## Why

<!-- The problem or motivation: what was broken, missing, or risky. Name the root
     cause. Do NOT restate the title. -->

## What changed

<!-- Bullets. Name the real class/method. Justify each non-obvious decision inline. -->
-

## Verification

<!-- The proof it works:
     - command run + result, e.g.  `./mvnw -B -ntp clean verify` → BUILD SUCCESS, <N> tests, 0 failures
     - the new tests and what each asserts; regenerate the sample PDF if the change is visual. -->

Closes #<!-- issue number; delete this line if none -->

---

<details>
<summary>Pre-merge checklist</summary>

- [ ] Targets **`develop`** (not `main`); branch is `<type>/<short-description>`.
- [ ] `./mvnw -B -ntp clean verify` passes locally — this is the **Verification** proof above.
- [ ] **Java 17 compatible** — no `getFirst()`/`getLast()`, type/deconstruction `switch`, `case null, default`. (CI runs Temurin 17 / 21 / 25.)
- [ ] **Public API changed** → `CHANGELOG.md` entry under the next in-progress version heading.

</details>
