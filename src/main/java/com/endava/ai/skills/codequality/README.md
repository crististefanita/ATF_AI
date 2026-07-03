# Code Quality Skill

## Purpose

The Code Quality skill owns Sonar-like maintainability guardrails that are
generic across ATF_name skills. It is not a replacement for SonarQube; it is the
local executable contract layer for package shape, class-size trend and
readability limits.

Current package: `com.endava.ai.skills.codequality`.

The skill is surfaced beside MCP and Atlas because it governs all skills, not
just Core. Any remaining references to old Core-hosted Code Quality packages
are compatibility debt and should be removed with the suite XML, YAML contract
and architecture-test updates.

## Entry Points

- `CodeQualitySkillSurface`: small facade that names the skill and its primary
  sanity suite.
- `src/main/resources/skills/codequality/contracts/code-quality.yml`: local policy for
  direct package class-count limits, class-size limits, YAML resource limits,
  TestNG suite count, dependency boundaries, Java baseline compatibility and
  legacy ratchets.
- `src/test/java/com/endava/ai/skills/codequality/tests`: executable
  Code Quality contracts.
- `src/test/resources/skills/codequality/testng-code-quality-sanity.xml`:
  isolated sanity profile for this skill.

## Reading Path

1. `src/main/resources/skills/codequality/contracts/code-quality.yml`
2. `src/test/java/com/endava/ai/skills/codequality/tests`
3. `src/test/resources/skills/codequality/testng-code-quality-sanity.xml`

## Rules

- New packages should keep at most 8 direct Java classes.
- Existing packages above 8 classes must be explicitly ratcheted in
  `code-quality.yml`.
- Java sources at or above 220 lines are warning-level trend signals.
- Java sources above 320 lines must either shrink or declare a legacy
  class-size ratchet.
- YAML resources at or above 10 KB are warning-level governance signals.
- YAML resources above 20 KB must either split or declare a legacy YAML-size
  ratchet.
- TestNG suite count is capped so profile proliferation stays deliberate.
- Dependency boundaries are checked through Java imports only, so prose and
  examples do not create false positives.
- Java compatibility bans APIs that are outside the current project baseline,
  such as direct `Stream.toList()`.
- A ratchet is not permission to grow; it documents debt until the package is
  split by responsibility.
- Ratchets must be lowered once the actual value is safely below the cap.
- Code Quality validates maintainability shape. It must not decide MCP package
  publishability, Atlas business quality or Reporting visual meaning.

## Verification

Run:

```powershell
mvn -q "-Dsurefire.suiteXmlFiles=src/test/resources/skills/codequality/testng-code-quality-sanity.xml" test
```

If a guard fails, split the package/class first. Add a temporary legacy ratchet
only when the current debt is real, named and paired with a repair hint.
