#!/usr/bin/env python3
"""Summarize the REDU simulated export for thesis Chapters 4-6.

The script intentionally uses only the Python standard library so the analysis
can be rerun on a clean macOS install without project-specific dependencies.
"""

from __future__ import annotations

import csv
import math
import sys
from collections import Counter, defaultdict
from dataclasses import dataclass
from datetime import date, datetime, timedelta, timezone
from pathlib import Path


STUDY_TZ = timezone(timedelta(hours=8))
WEEK_1_END = date(2026, 5, 16)


@dataclass(frozen=True)
class TestResult:
    n: str
    group_a: str
    group_b: str
    test: str
    effect: str
    p: float | None


@dataclass(frozen=True)
class SurveySummary:
    n: int
    items: int
    mean_sd: str
    alpha: str


def read_csv(path: Path) -> list[dict[str, str]]:
    with path.open(newline="", encoding="utf-8") as handle:
        return list(csv.DictReader(handle))


def mean(values: list[float]) -> float:
    return sum(values) / len(values)


def sd(values: list[float]) -> float:
    if len(values) < 2:
        return 0.0
    m = mean(values)
    return math.sqrt(sum((value - m) ** 2 for value in values) / (len(values) - 1))


def fmt(value: float, digits: int = 2) -> str:
    return f"{value:.{digits}f}"


def fmt_mean_sd(values: list[float], digits: int = 2) -> str:
    return f"{fmt(mean(values), digits)} ({fmt(sd(values), digits)})"


def fmt_p(value: float | None) -> str:
    if value is None:
        return "N/A"
    if value < 0.001:
        return "<0.001"
    return fmt(value, 3)


def betacf(a: float, b: float, x: float) -> float:
    max_iter = 200
    eps = 3.0e-14
    fpmin = 1.0e-300
    qab = a + b
    qap = a + 1.0
    qam = a - 1.0
    c = 1.0
    d = 1.0 - qab * x / qap
    if abs(d) < fpmin:
        d = fpmin
    d = 1.0 / d
    h = d
    for m in range(1, max_iter + 1):
        m2 = 2 * m
        aa = m * (b - m) * x / ((qam + m2) * (a + m2))
        d = 1.0 + aa * d
        if abs(d) < fpmin:
            d = fpmin
        c = 1.0 + aa / c
        if abs(c) < fpmin:
            c = fpmin
        d = 1.0 / d
        h *= d * c
        aa = -(a + m) * (qab + m) * x / ((a + m2) * (qap + m2))
        d = 1.0 + aa * d
        if abs(d) < fpmin:
            d = fpmin
        c = 1.0 + aa / c
        if abs(c) < fpmin:
            c = fpmin
        d = 1.0 / d
        delta = d * c
        h *= delta
        if abs(delta - 1.0) < eps:
            break
    return h


def betai(a: float, b: float, x: float) -> float:
    if x < 0.0 or x > 1.0:
        raise ValueError("x out of range")
    if x == 0.0 or x == 1.0:
        return x
    bt = math.exp(
        math.lgamma(a + b)
        - math.lgamma(a)
        - math.lgamma(b)
        + a * math.log(x)
        + b * math.log(1.0 - x)
    )
    if x < (a + 1.0) / (a + b + 2.0):
        return bt * betacf(a, b, x) / a
    return 1.0 - bt * betacf(b, a, 1.0 - x) / b


def student_t_cdf(t: float, df: float) -> float:
    x = df / (df + t * t)
    ib = betai(df / 2.0, 0.5, x)
    if t >= 0:
        return 1.0 - 0.5 * ib
    return 0.5 * ib


def t_two_tailed_p(t: float, df: float) -> float:
    tail = 1.0 - student_t_cdf(abs(t), df)
    return max(0.0, min(1.0, 2.0 * tail))


def welch(values_a: list[float], values_b: list[float]) -> tuple[float, float]:
    ma, mb = mean(values_a), mean(values_b)
    va, vb = sd(values_a) ** 2, sd(values_b) ** 2
    na, nb = len(values_a), len(values_b)
    se2 = va / na + vb / nb
    if se2 == 0:
        return 0.0, 1.0
    t = (ma - mb) / math.sqrt(se2)
    num = se2**2
    den = ((va / na) ** 2) / (na - 1) + ((vb / nb) ** 2) / (nb - 1)
    df = num / den if den else na + nb - 2
    return t, t_two_tailed_p(t, df)


def paired_t(before: list[float], after: list[float]) -> tuple[float, float, float]:
    diffs = [a - b for b, a in zip(before, after)]
    s = sd(diffs)
    if s == 0:
        return 0.0, 1.0, 0.0
    t = mean(diffs) / (s / math.sqrt(len(diffs)))
    p = t_two_tailed_p(t, len(diffs) - 1)
    dz = mean(diffs) / s
    return t, p, dz


def cohens_d(values_a: list[float], values_b: list[float]) -> tuple[float, float, float]:
    na, nb = len(values_a), len(values_b)
    pooled = math.sqrt(
        ((na - 1) * sd(values_a) ** 2 + (nb - 1) * sd(values_b) ** 2)
        / (na + nb - 2)
    )
    d = 0.0 if pooled == 0 else (mean(values_a) - mean(values_b)) / pooled
    se = math.sqrt((na + nb) / (na * nb) + (d * d) / (2 * (na + nb - 2)))
    return d, d - 1.96 * se, d + 1.96 * se


def pearson(xs: list[float], ys: list[float]) -> tuple[float, float]:
    mx, my = mean(xs), mean(ys)
    sx = math.sqrt(sum((x - mx) ** 2 for x in xs))
    sy = math.sqrt(sum((y - my) ** 2 for y in ys))
    if sx == 0 or sy == 0:
        return 0.0, 1.0
    r = sum((x - mx) * (y - my) for x, y in zip(xs, ys)) / (sx * sy)
    df = len(xs) - 2
    t = r * math.sqrt(df / max(1e-12, 1 - r * r))
    return r, t_two_tailed_p(t, df)


def holm(p_values: list[float | None]) -> list[float | None]:
    indexed = [(i, p) for i, p in enumerate(p_values) if p is not None]
    indexed.sort(key=lambda item: item[1])
    adjusted: list[float | None] = [None] * len(p_values)
    running = 0.0
    m = len(indexed)
    for rank, (i, p) in enumerate(indexed):
        value = min(1.0, (m - rank) * p)
        running = max(running, value)
        adjusted[i] = running
    return adjusted


def cronbach_alpha(rows: list[dict[str, str]], item_columns: list[str]) -> float | None:
    matrix: list[list[float]] = []
    for row in rows:
        try:
            matrix.append([float(row[column]) for column in item_columns])
        except (TypeError, ValueError):
            continue
    if len(matrix) < 2 or len(item_columns) < 2:
        return None
    item_variances = [sd([row[index] for row in matrix]) ** 2 for index in range(len(item_columns))]
    totals = [sum(row) for row in matrix]
    total_variance = sd(totals) ** 2
    if total_variance == 0:
        return None
    k = len(item_columns)
    return k / (k - 1) * (1 - sum(item_variances) / total_variance)


def survey_summary(
    rows: list[dict[str, str]],
    mean_column: str,
    item_columns: list[str],
    digits: int = 2,
) -> SurveySummary:
    values = [float(row[mean_column]) for row in rows if row.get(mean_column) not in ("", None)]
    alpha = cronbach_alpha(rows, item_columns)
    return SurveySummary(
        n=len(values),
        items=len(item_columns),
        mean_sd=fmt_mean_sd(values, digits),
        alpha="N/A" if alpha is None else fmt(alpha, 3),
    )


def session_date(row: dict[str, str]) -> date:
    return datetime.fromtimestamp(int(row["start_ms"]) / 1000, tz=STUDY_TZ).date()


def week_for(row: dict[str, str]) -> int:
    return 1 if session_date(row) <= WEEK_1_END else 2


def metric_by_participant_week(
    sessions: list[dict[str, str]],
    metric: str,
) -> dict[tuple[str, int], float]:
    by_day: dict[tuple[str, int, date], list[float]] = defaultdict(list)
    for row in sessions:
        reliable = row["sentiment_reliability"] == "RELIABLE"
        value: float | None
        if metric == "raw_duration_min":
            value = int(row["raw_duration_ms"]) / 60000.0
        elif metric == "prompt_excluded_duration_min":
            value = int(row["prompt_excluded_duration_ms"]) / 60000.0
        elif metric == "dwell_sec":
            value = int(row["mean_dwell_ms"]) / 1000.0
        elif metric == "nsd":
            value = float(row["nsd_percent"]) if reliable and row["nsd_percent"] else None
        elif metric == "risk_reliable":
            value = float(row["risk_score"]) if reliable else None
        else:
            raise ValueError(metric)
        if value is not None:
            by_day[(row["study_code"], week_for(row), session_date(row))].append(value)

    by_week: dict[tuple[str, int], list[float]] = defaultdict(list)
    for (study_code, week, _day), values in by_day.items():
        by_week[(study_code, week)].append(mean(values))
    return {key: mean(values) for key, values in by_week.items()}


def group_for_code(code: str) -> str:
    return "INTERVENTION" if code.endswith("X") else "CONTROL"


def between_change_result(
    sessions: list[dict[str, str]],
    metric: str,
    digits: int = 2,
) -> TestResult:
    values = metric_by_participant_week(sessions, metric)
    changes: dict[str, float] = {}
    for code in {row["study_code"] for row in sessions}:
        if (code, 1) in values and (code, 2) in values:
            changes[code] = values[(code, 2)] - values[(code, 1)]
    intervention = [value for code, value in changes.items() if group_for_code(code) == "INTERVENTION"]
    control = [value for code, value in changes.items() if group_for_code(code) == "CONTROL"]
    t, p = welch(intervention, control)
    d, low, high = cohens_d(intervention, control)
    return TestResult(
        n=f"I={len(intervention)}; C={len(control)}",
        group_a=fmt_mean_sd(intervention, digits),
        group_b=fmt_mean_sd(control, digits),
        test="Welch t",
        effect=f"d={fmt(d, 2)} [{fmt(low, 2)}, {fmt(high, 2)}]",
        p=p,
    )


def between_change_from_scores(
    score_by_code_week: dict[tuple[str, int], float],
    digits: int = 2,
) -> TestResult:
    changes: dict[str, float] = {}
    for code in {code for code, _week in score_by_code_week}:
        if (code, 1) in score_by_code_week and (code, 2) in score_by_code_week:
            changes[code] = score_by_code_week[(code, 2)] - score_by_code_week[(code, 1)]
    intervention = [value for code, value in changes.items() if group_for_code(code) == "INTERVENTION"]
    control = [value for code, value in changes.items() if group_for_code(code) == "CONTROL"]
    t, p = welch(intervention, control)
    d, low, high = cohens_d(intervention, control)
    return TestResult(
        n=f"I={len(intervention)}; C={len(control)}",
        group_a=fmt_mean_sd(intervention, digits),
        group_b=fmt_mean_sd(control, digits),
        test="Welch t",
        effect=f"d={fmt(d, 2)} [{fmt(low, 2)}, {fmt(high, 2)}]",
        p=p,
    )


def within_intervention_result(
    sessions: list[dict[str, str]],
    metric: str,
    digits: int = 2,
) -> TestResult:
    values = metric_by_participant_week(sessions, metric)
    codes = sorted(
        code
        for code in {row["study_code"] for row in sessions}
        if group_for_code(code) == "INTERVENTION" and (code, 1) in values and (code, 2) in values
    )
    week1 = [values[(code, 1)] for code in codes]
    week2 = [values[(code, 2)] for code in codes]
    _t, p, dz = paired_t(week1, week2)
    return TestResult(
        n=str(len(codes)),
        group_a=fmt_mean_sd(week1, digits),
        group_b=fmt_mean_sd(week2, digits),
        test="Paired t",
        effect=f"p={fmt_p(p)}; dz={fmt(dz, 2)}",
        p=p,
    )


def within_intervention_from_scores(
    score_by_code_week: dict[tuple[str, int], float],
    digits: int = 2,
) -> TestResult:
    codes = sorted(
        {
            code
            for code, _week in score_by_code_week
        }
    )
    codes = [
        code
        for code in codes
        if group_for_code(code) == "INTERVENTION"
        and (code, 1) in score_by_code_week
        and (code, 2) in score_by_code_week
    ]
    week1 = [score_by_code_week[(code, 1)] for code in codes]
    week2 = [score_by_code_week[(code, 2)] for code in codes]
    _t, p, dz = paired_t(week1, week2)
    return TestResult(
        n=str(len(codes)),
        group_a=fmt_mean_sd(week1, digits),
        group_b=fmt_mean_sd(week2, digits),
        test="Paired t",
        effect=f"p={fmt_p(p)}; dz={fmt(dz, 2)}",
        p=p,
    )


def print_table(title: str, rows: list[tuple[str, str]]) -> None:
    print(f"\n## {title}")
    for label, value in rows:
        print(f"{label}: {value}")


def main() -> int:
    root = Path(sys.argv[1]) if len(sys.argv) > 1 else Path("simulated-export/redu-p01-p50-20260523-224114")
    sessions = read_csv(root / "sessions.csv")
    prompts = read_csv(root / "prompt_events.csv")
    reliability = read_csv(root / "reliability_events.csv")
    personalization = read_csv(root / "risk_personalization.csv")
    doomscrolling_rows = read_csv(root / "doomscrolling_scale.csv") if (root / "doomscrolling_scale.csv").exists() else []
    iso_rows = read_csv(root / "survey_iso25010.csv") if (root / "survey_iso25010.csv").exists() else []
    sus_rows = read_csv(root / "sus_responses.csv") if (root / "sus_responses.csv").exists() else []
    tam_rows = read_csv(root / "tam_responses.csv") if (root / "tam_responses.csv").exists() else []
    feedback_rows = read_csv(root / "open_ended_feedback.csv") if (root / "open_ended_feedback.csv").exists() else []
    sme_rows = read_csv(root / "sme_evaluation.csv") if (root / "sme_evaluation.csv").exists() else []

    participants = sorted({row["study_code"] for row in sessions})
    group_counts = Counter(group_for_code(code) for code in participants)
    weeks_by_code = defaultdict(set)
    reliable_week1_by_code = Counter()
    unreliable_week1_by_code = Counter()
    for row in sessions:
        week = week_for(row)
        weeks_by_code[row["study_code"]].add(week)
        if week == 1 and row["sentiment_reliability"] == "RELIABLE":
            reliable_week1_by_code[row["study_code"]] += 1
        if week == 1 and row["sentiment_reliability"] == "SENTIMENT_UNRELIABLE":
            unreliable_week1_by_code[row["study_code"]] += 1

    print_table(
        "Participant Flow",
        [
            ("enrolled", f"I={group_counts['INTERVENTION']}; C={group_counts['CONTROL']}; N={len(participants)}"),
            (
                "completed week 1",
                f"I={sum(1 for c in participants if group_for_code(c) == 'INTERVENTION' and 1 in weeks_by_code[c])}; "
                f"C={sum(1 for c in participants if group_for_code(c) == 'CONTROL' and 1 in weeks_by_code[c])}",
            ),
            (
                "completed week 2",
                f"I={sum(1 for c in participants if group_for_code(c) == 'INTERVENTION' and 2 in weeks_by_code[c])}; "
                f"C={sum(1 for c in participants if group_for_code(c) == 'CONTROL' and 2 in weeks_by_code[c])}",
            ),
            (
                "baseline reliable >=3",
                f"I={sum(1 for c in participants if group_for_code(c) == 'INTERVENTION' and reliable_week1_by_code[c] >= 3)}; "
                f"C={sum(1 for c in participants if group_for_code(c) == 'CONTROL' and reliable_week1_by_code[c] >= 3)}",
            ),
        ],
    )

    total_sessions = len(sessions)
    reliable_sessions = sum(1 for row in sessions if row["sentiment_reliability"] == "RELIABLE")
    unreliable_sessions = total_sessions - reliable_sessions
    print_table(
        "Export Counts",
        [
            ("sessions", str(total_sessions)),
            ("reliable sessions", f"{reliable_sessions} ({reliable_sessions / total_sessions * 100:.1f}%)"),
            ("unreliable sessions", f"{unreliable_sessions} ({unreliable_sessions / total_sessions * 100:.1f}%)"),
            ("daily summary rows", str(sum(1 for _ in read_csv(root / "daily_summaries.csv")))),
            ("prompt events", str(len(prompts))),
            ("reliability events", str(len(reliability))),
            ("risk personalization rows", str(len(personalization))),
            ("doomscrolling scale rows", str(len(doomscrolling_rows))),
            ("ISO survey rows", str(len(iso_rows))),
            ("SUS rows", str(len(sus_rows))),
            ("TAM rows", str(len(tam_rows))),
            ("open-ended feedback rows", str(len(feedback_rows))),
            ("SME rows with ratings", str(sum(1 for row in sme_rows if any(row.get(column) for column in [
                "technical_soundness",
                "input_range_plausibility",
                "rule_base_coherence",
                "privacy_architecture_quality",
                "intervention_design_appropriateness",
                "overall_software_quality",
            ])))),
        ],
    )

    platform_rows = []
    for platform in ["TIKTOK", "FACEBOOK", "INSTAGRAM"]:
        rows = [row for row in sessions if row["platform"] == platform]
        users = {row["study_code"] for row in rows}
        reliable = sum(1 for row in rows if row["sentiment_reliability"] == "RELIABLE")
        platform_rows.append(
            (
                platform,
                f"users={len(users)} ({len(users) / len(participants) * 100:.1f}%); "
                f"sessions={len(rows)}; reliable={reliable / len(rows) * 100:.1f}%",
            ),
        )
    multi_users = sum(
        1
        for code in participants
        if len({row["platform"] for row in sessions if row["study_code"] == code}) >= 2
    )
    platform_rows.append(("MULTI", f"users={multi_users} ({multi_users / len(participants) * 100:.1f}%)"))
    print_table("Platform Coverage", platform_rows)

    between_metrics = [
        ("raw_duration_min", "Mean daily raw elapsed session duration (min)", 2),
        ("dwell_sec", "Mean daily raw elapsed video dwell time (s)", 2),
        ("nsd", "Mean daily Negative Sentiment Density (%)", 2),
        ("prompt_excluded_duration_min", "Mean daily prompt-excluded session duration (min)", 2),
    ]
    between_results = [(label, between_change_result(sessions, metric, digits)) for metric, label, digits in between_metrics]
    dss_scores = {
        (row["study_code"], int(row["week"])): float(row["mean_score"])
        for row in doomscrolling_rows
        if row.get("mean_score")
    }
    dss_between = between_change_from_scores(dss_scores, 2) if dss_scores else None
    adjusted = holm([result.p for _, result in between_results[:3]] + ([dss_between.p] if dss_between else []))
    print("\n## Between-Group Change Scores")
    for index, (label, result) in enumerate(between_results):
        p = adjusted[index] if index < 3 else result.p
        print(
            f"{label}: n={result.n}; I change={result.group_a}; C change={result.group_b}; "
            f"{result.test}; {result.effect}; p={fmt_p(p)}"
        )
    if dss_between:
        print(
            f"Doomscrolling Scale mean score: n={dss_between.n}; I change={dss_between.group_a}; "
            f"C change={dss_between.group_b}; {dss_between.test}; {dss_between.effect}; p={fmt_p(adjusted[3])}"
        )

    within_results = [
        ("Mean daily raw elapsed session duration (min)", within_intervention_result(sessions, "raw_duration_min", 2)),
        ("Mean daily raw elapsed video dwell time (s)", within_intervention_result(sessions, "dwell_sec", 2)),
        ("Mean daily Negative Sentiment Density (%)", within_intervention_result(sessions, "nsd", 2)),
        (
            "Mean daily prompt-excluded session duration (min)",
            within_intervention_result(sessions, "prompt_excluded_duration_min", 2),
        ),
    ]
    if dss_scores:
        within_results.append(("Doomscrolling Scale mean score", within_intervention_from_scores(dss_scores, 2)))
    print("\n## Within Intervention")
    for label, result in within_results:
        print(f"{label}: n={result.n}; W1={result.group_a}; W2={result.group_b}; {result.effect}")

    print_table(
        "DSI Coverage",
        [
            (
                "participants >=3 reliable W1",
                f"I={sum(1 for c in participants if group_for_code(c) == 'INTERVENTION' and reliable_week1_by_code[c] >= 3)}; "
                f"C={sum(1 for c in participants if group_for_code(c) == 'CONTROL' and reliable_week1_by_code[c] >= 3)}; "
                f"N={sum(1 for c in participants if reliable_week1_by_code[c] >= 3)}",
            ),
            (
                "W1 reliable sessions",
                f"I={sum(reliable_week1_by_code[c] for c in participants if group_for_code(c) == 'INTERVENTION')}; "
                f"C={sum(reliable_week1_by_code[c] for c in participants if group_for_code(c) == 'CONTROL')}; "
                f"N={sum(reliable_week1_by_code.values())}",
            ),
            (
                "W1 unreliable sessions",
                f"I={sum(unreliable_week1_by_code[c] for c in participants if group_for_code(c) == 'INTERVENTION')}; "
                f"C={sum(unreliable_week1_by_code[c] for c in participants if group_for_code(c) == 'CONTROL')}; "
                f"N={sum(unreliable_week1_by_code.values())}",
            ),
            (
                "undefined W1 DSI cases",
                f"I={sum(1 for c in participants if group_for_code(c) == 'INTERVENTION' and reliable_week1_by_code[c] < 3)}; "
                f"C={sum(1 for c in participants if group_for_code(c) == 'CONTROL' and reliable_week1_by_code[c] < 3)}",
            ),
        ],
    )

    risk_w1 = metric_by_participant_week(sessions, "risk_reliable")
    dur_w1 = metric_by_participant_week(sessions, "raw_duration_min")
    dwell_w1 = metric_by_participant_week(sessions, "dwell_sec")
    nsd_w1 = metric_by_participant_week(sessions, "nsd")
    baseline_codes = [c for c in participants if reliable_week1_by_code[c] >= 3]
    print("\n## Internal Week 1 Log Correlations With Composite Risk")
    for label, values in [
        ("duration", dur_w1),
        ("dwell", dwell_w1),
        ("nsd", nsd_w1),
    ]:
        xs = [values[(c, 1)] for c in baseline_codes if (c, 1) in values and (c, 1) in risk_w1]
        ys = [risk_w1[(c, 1)] for c in baseline_codes if (c, 1) in values and (c, 1) in risk_w1]
        r, p = pearson(xs, ys)
        print(f"{label}: n={len(xs)}; r={fmt(r, 3)}; p={fmt_p(p)}")

    if dss_scores:
        print("\n## Baseline Correlations With Doomscrolling Scale")
        for label, values in [
            ("duration", dur_w1),
            ("dwell", dwell_w1),
            ("nsd", nsd_w1),
            ("composite DSI", risk_w1),
        ]:
            xs = [values[(c, 1)] for c in baseline_codes if (c, 1) in values and (c, 1) in dss_scores]
            ys = [dss_scores[(c, 1)] for c in baseline_codes if (c, 1) in values and (c, 1) in dss_scores]
            r, p = pearson(xs, ys)
            print(f"{label}: n={len(xs)}; r={fmt(r, 3)}; p={fmt_p(p)}")

    if iso_rows:
        print("\n## ISO 25010 User Survey")
        for label, mean_column, items in [
            ("Functional Suitability", "functional_suitability_mean", [
                "functional_suitability_1", "functional_suitability_2", "functional_suitability_3"
            ]),
            ("Performance Efficiency", "performance_efficiency_mean", [
                "performance_efficiency_1", "performance_efficiency_2", "performance_efficiency_3"
            ]),
            ("Reliability", "reliability_mean", ["reliability_1", "reliability_2", "reliability_3"]),
        ]:
            summary = survey_summary(iso_rows, mean_column, items)
            met = "met" if mean([float(row[mean_column]) for row in iso_rows]) >= 3.50 else "not met"
            print(f"{label}: n={summary.n}; items={summary.items}; mean(SD)={summary.mean_sd}; alpha={summary.alpha}; target {met}")

    if sus_rows:
        sus_values = [float(row["sus_score"]) for row in sus_rows]
        sus_scored_rows = []
        for row in sus_rows:
            scored = {}
            for index in range(1, 11):
                raw_value = float(row[f"sus_{index}"])
                scored[f"sus_{index}"] = raw_value - 1 if index % 2 == 1 else 5 - raw_value
            sus_scored_rows.append(scored)
        sus_alpha = cronbach_alpha(sus_scored_rows, [f"sus_{index}" for index in range(1, 11)])
        print_table(
            "SUS User Survey",
            [
                ("n", str(len(sus_values))),
                ("mean(SD)", fmt_mean_sd(sus_values, 2)),
                ("alpha", "N/A" if sus_alpha is None else fmt(sus_alpha, 3)),
                ("target", "met" if mean(sus_values) >= 70 else "not met"),
            ],
        )

    if tam_rows:
        print("\n## TAM User Survey")
        for label, mean_column, items in [
            ("Perceived Usefulness", "perceived_usefulness_mean", [f"pu_{index}" for index in range(1, 7)]),
            ("Perceived Ease of Use", "perceived_ease_of_use_mean", [f"peou_{index}" for index in range(1, 7)]),
        ]:
            summary = survey_summary(tam_rows, mean_column, items)
            met = "met" if mean([float(row[mean_column]) for row in tam_rows]) >= 3.50 else "not met"
            print(f"{label}: n={summary.n}; items={summary.items}; mean(SD)={summary.mean_sd}; alpha={summary.alpha}; target {met}")

    if feedback_rows:
        theme_counts = Counter(row["coded_theme"] for row in feedback_rows)
        print_table(
            "Open-Ended Feedback",
            [
                ("themes", "; ".join(f"{theme}={count}" for theme, count in sorted(theme_counts.items()))),
            ],
        )

    prompt_counts = Counter(row["action"] for row in prompts)
    shown_rows = [row for row in prompts if row["action"] == "SHOWN"]
    response_rows = [row for row in prompts if row["action"] != "SHOWN"]
    print_table(
        "Prompt Events",
        [
            ("shown", str(len(shown_rows))),
            ("level shown", "; ".join(f"{k}={v}" for k, v in sorted(Counter(row["prompt_level"] for row in shown_rows).items()))),
            ("responses", "; ".join(f"{k}={v}" for k, v in sorted(Counter(row["action"] for row in response_rows).items()))),
            ("take break", f"{prompt_counts['TAKE_BREAK']} ({prompt_counts['TAKE_BREAK'] / max(1, len(response_rows)) * 100:.1f}% of responses)"),
        ],
    )

    print_table(
        "Reliability Events",
        [
            ("event types", "; ".join(f"{k}={v}" for k, v in sorted(Counter(row["event_type"] for row in reliability).items()))),
        ],
    )

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
