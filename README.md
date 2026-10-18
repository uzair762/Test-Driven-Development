# GPA System TDD Template

The sample domain lives in `org.example.tdd.gpasystem`; its calculator and grading policy tests, `FakeStudent`, and `TestDataGenerator` are under `src/test/java`. See the [UML class diagram](docs/GpaSystemClassDiagram.md) for class relationships and calculator behavior.

## Model and Relationships

- `Student` implements `IStudent`, which extends `IPerson`, and aggregates semester records, indexed from 1 through 8.
- A `SemesterRecord` owns its course results. A completed semester must contain four or five courses; a semester still in progress may have fewer.
- Each `CourseResult` associates a course with a percentage and its policy grade. `GradingPolicyManager` creates results so letter grades and points are kept in sync.
- `GpaCalculator` depends on these domain types to calculate weighted GPA and summarize performance. Course credits weight grade points in semester and cumulative GPA.
- `Course` represents a course and its details, including its code, title, and credits.
- `GradeBand` is one row of the percentage-to-grade table, `GradingPolicyManager` applies a table of bands (the grading policy), and `GradingPolicyFactory` builds ready-made or custom policies, including the new plus/minus scale.

## GPA Calculation and Dependency Injection

`GpaCalculator` receives its student dependency through method parameters typed as `IStudent` (or a collection of `IStudent`s). This is parameter injection: production callers pass `Student`, while unit tests can pass `FakeStudent` without constructing a validated production entity.

`IStudent` defines only the data the calculator needs; names and IDs are reporting details, and GPA calculation does not reject a student because its ID is blank or missing. The tests demonstrate both an individual semester calculation and course rankings using fake students.

For example, a unit test can supply the fake directly:

```java
IStudent fake = new FakeStudent(null, "Ada Lovelace");
fake.addSemester(completedSemesterWithKnownCourseResults);
double gpa = new GpaCalculator().calculateSemesterGpa(fake, 1);
```

## Grading Policy (Question 1: plus/minus scale)

`GradeBand` holds a minimum percentage and the `Grade` it earns. `GradingPolicyManager` takes a list of bands (in any order), sorts them from highest to lowest threshold, and converts a percentage to a grade. A percentage earns the grade of the highest band whose minimum it reaches.

`GradingPolicyFactory` is the grading policy factory:

- `GradingPolicyFactory.standardFourPointScale()` - the original A, B, C, D, F policy (90, 80, 70, 60).
- `GradingPolicyFactory.plusMinusScale()` - the new plus/minus policy (table below).
- `GradingPolicyFactory.fromTable(Map<Double, Grade>)` - builds a policy from any custom percentage-to-grade table, with the same validation.

`GradingPolicyManager.standardFourPointScale()` and `plusMinusScale()` remain as shortcuts that delegate to the factory. The plus/minus table:

| Percentage | Grade | Points |
|-----------:|:-----:|-------:|
| 93 - 100   | A     | 4.0 |
| 90 - 92.99 | A-    | 3.7 |
| 87 - 89.99 | B+    | 3.3 |
| 83 - 86.99 | B     | 3.0 |
| 80 - 82.99 | B-    | 2.7 |
| 77 - 79.99 | C+    | 2.3 |
| 73 - 76.99 | C     | 2.0 |
| 70 - 72.99 | C-    | 1.7 |
| 67 - 69.99 | D+    | 1.3 |
| 60 - 66.99 | D     | 1.0 |
| 0 - 59.99  | F     | 0.0 |

A custom policy is rejected when it is null or empty, contains a null band, repeats a threshold or a letter, has no band starting at 0 (incomplete policy), or gives higher grade points to a lower percentage. Percentages outside 0-100 (including NaN and infinity) are rejected.

Cumulative GPA and cohort comparisons include completed semesters only. Course comparisons combine completed attempts of that course. The reported average performer is the student whose GPA is closest to the cohort's arithmetic mean.

The Question 1 UML class diagram (grading classes, factory and tests only) is in `docs/Q1GradingPolicyClassDiagram.md`; the full system diagram stays in `docs/GpaSystemClassDiagram.md`.

## Grading Policy Tests

`GradingPolicyManagerTest` (grouped in nested classes), `GradingPolicyFactoryTest` and `GradeBandTest` cover:

1. **Exactly at each cutoff:** 100, 93, 90, 87, 83, 80, 77, 73, 70, 67, 60 and 0, plus a loop proving every band's cutoff returns that band's own grade.
2. **Just below each cutoff:** the true boundary value `Math.nextDown(cutoff)` for all ten cutoffs, the familiar values 92.99 ... 59.99, and a loop proving the next-lower band is returned.
3. **Invalid percentages:** -0.01, -1, -100, 100.01, 150, `Double.MAX_VALUE`, NaN, +/- infinity, `nextDown(0)` and `nextUp(100)`.
4. **Incomplete or invalid policies:** null table, empty table, null band, missing 0 band, a lone high band that leaves low scores uncovered, duplicate thresholds, duplicate letters, and grade points that increase as the percentage drops. Equal points in neighbouring bands are accepted.
5. **Factory behaviour:** the plus/minus and standard tables, `fromTable` with a valid custom table, and `fromTable` rejecting null, empty, incomplete, out-of-range and inconsistent tables.
6. **Table behaviour:** ordering, unordered input, an unmodifiable exposed list, and defensive copying of the source list.
7. `GpaCalculatorTest` also checks a semester GPA calculated with the plus/minus scale.

## Test-Driven Development

The project follows the Red-Green-Refactor approach:

1. **Red:** Write a test that describes the expected behavior and confirm that it fails.
2. **Green:** Implement the minimum code needed to make the test pass.
3. **Refactor:** Improve the code while keeping all tests passing.

The plus/minus grading module follows this approach: each cutoff, each just-below value, each invalid percentage, and each invalid policy rule is described by a test first, then implemented in `GradingPolicyFactory`, `GradingPolicyManager` and `GradeBand`.

## Running the Tests

Run the JUnit 5 test suite from the project root:

```bash
mvn test
```

The current test suite includes the GPA calculator, course result, grade band, grading policy and grading policy factory tests. All 104 tests passed in the latest successful run (102 JUnit 5 tests plus the 2 JUnit 4 calculator example tests).
