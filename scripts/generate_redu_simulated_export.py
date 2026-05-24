#!/usr/bin/env python3
"""Generate a REDU-compatible simulated export dataset.

The script reads the current debug-build Room database from a connected
Android device via `adb run-as`, writes traceability CSVs under
`current_device_snapshot/`, then creates deterministic simulated export CSVs
and a REDU-style `redu-export.zip`.
"""

from __future__ import annotations

import argparse
import csv
import math
import random
import shutil
import sqlite3
import subprocess
import sys
import tempfile
import zipfile
from dataclasses import dataclass
from datetime import date, datetime, time, timedelta, timezone
from pathlib import Path
from typing import Iterable
from zoneinfo import ZoneInfo


PACKAGE = "edu.feutech.redu"
DB_NAME = "redu.db"
DEFAULT_SEED = 20260523
PARTICIPANT_COUNT = 50
STUDY_DAYS = 14
STUDY_ZONE = ZoneInfo("Asia/Manila")

EXPORT_HEADERS = {
    "sessions.csv": [
        "study_code",
        "group",
        "platform",
        "start_ms",
        "end_ms",
        "raw_duration_ms",
        "prompt_excluded_duration_ms",
        "mean_dwell_ms",
        "swipe_count",
        "risk_score",
        "risk_level",
        "sentiment_reliability",
        "nsd_percent",
        "oov_ratio",
    ],
    "daily_summaries.csv": [
        "study_code",
        "date",
        "platform",
        "session_count",
        "mean_duration_ms",
        "mean_dwell_ms",
        "mean_nsd_percent",
        "mean_risk_score",
        "reliable_session_count",
    ],
    "prompt_events.csv": [
        "study_code",
        "timestamp_ms",
        "session_id",
        "risk_level",
        "prompt_level",
        "action",
        "cooldown_state",
    ],
    "reliability_events.csv": [
        "study_code",
        "timestamp_ms",
        "platform",
        "event_type",
        "details_code",
        "affected_session_id",
    ],
    "risk_personalization.csv": [
        "study_code",
        "group",
        "locked_at_ms",
        "reliable_baseline_session_count",
        "duration_q25_min",
        "duration_q50_min",
        "duration_q75_min",
        "duration_q95_min",
        "nsd_q25_percent",
        "nsd_q50_percent",
        "nsd_q75_percent",
        "nsd_q95_percent",
    ],
}

SUPPLEMENTAL_HEADERS = {
    "doomscrolling_scale.csv": [
        "study_code",
        "group",
        "administration",
        "week",
        "completed_at_ms",
        "dss_1_urge_bad_news",
        "dss_2_lose_track_time",
        "dss_3_continuous_negative_browsing",
        "dss_4_addicted_negative_news",
        "total_score",
        "mean_score",
    ],
    "survey_iso25010.csv": [
        "study_code",
        "group",
        "completed_at_ms",
        "functional_suitability_1",
        "functional_suitability_2",
        "functional_suitability_3",
        "functional_suitability_mean",
        "performance_efficiency_1",
        "performance_efficiency_2",
        "performance_efficiency_3",
        "performance_efficiency_mean",
        "reliability_1",
        "reliability_2",
        "reliability_3",
        "reliability_mean",
    ],
    "sus_responses.csv": [
        "study_code",
        "group",
        "completed_at_ms",
        "sus_1",
        "sus_2",
        "sus_3",
        "sus_4",
        "sus_5",
        "sus_6",
        "sus_7",
        "sus_8",
        "sus_9",
        "sus_10",
        "sus_score",
    ],
    "tam_responses.csv": [
        "study_code",
        "group",
        "completed_at_ms",
        "pu_1",
        "pu_2",
        "pu_3",
        "pu_4",
        "pu_5",
        "pu_6",
        "perceived_usefulness_mean",
        "peou_1",
        "peou_2",
        "peou_3",
        "peou_4",
        "peou_5",
        "peou_6",
        "perceived_ease_of_use_mean",
    ],
    "open_ended_feedback.csv": [
        "study_code",
        "group",
        "completed_at_ms",
        "liked_most",
        "main_difficulty",
        "privacy_comment",
        "suggested_improvement",
        "coded_theme",
    ],
    "sme_evaluation.csv": [
        "expert_id",
        "expert_role",
        "technical_soundness",
        "input_range_plausibility",
        "rule_base_coherence",
        "privacy_architecture_quality",
        "intervention_design_appropriateness",
        "overall_software_quality",
        "comments",
    ],
    "sme_open_ended_feedback.csv": [
        "expert_id",
        "expert_role",
        "technical_design_comment",
        "privacy_safeguards_comment",
        "heuristic_logic_comment",
        "intervention_structure_comment",
        "recommended_revision",
    ],
}

SNAPSHOT_TABLES = [
    "app_settings",
    "sessions",
    "prompt_events",
    "reliability_events",
    "risk_personalization",
]

PLATFORMS = ["TIKTOK", "INSTAGRAM", "FACEBOOK"]
SESSION_FINALIZED_DELAY_MS = 30_000


@dataclass(frozen=True)
class Profile:
    name: str
    participant_count: int
    daily_session_min: int
    daily_session_max: int
    duration_min: int
    duration_max: int
    dwell_min: int
    dwell_max: int
    reliable_probability: float
    negative_min: float
    negative_max: float
    risk_bias: float
    skip_day_probability: float = 0.0


PROFILES = [
    Profile("heavy_doomscroller", 8, 4, 8, 18, 75, 1_800, 7_000, 0.82, 55.0, 100.0, 24.0),
    Profile("frequent_high_risk", 8, 3, 6, 12, 50, 2_500, 9_000, 0.78, 45.0, 92.0, 16.0),
    Profile("moderate_user", 12, 2, 4, 6, 28, 6_000, 18_000, 0.88, 15.0, 60.0, 0.0),
    Profile("intermittent_user", 8, 0, 3, 4, 22, 8_000, 24_000, 0.84, 10.0, 55.0, -4.0, 0.28),
    Profile("light_user", 8, 0, 2, 1, 12, 12_000, 40_000, 0.92, 0.0, 35.0, -18.0, 0.22),
    Profile("low_risk_baseline", 6, 1, 3, 2, 16, 10_000, 32_000, 0.95, 0.0, 25.0, -24.0),
]


def main() -> int:
    args = parse_args()
    repo_root = Path(__file__).resolve().parents[1]
    output_root = (repo_root / args.output_root).resolve()
    timestamp = datetime.now().strftime("%Y%m%d-%H%M%S")
    output_dir = output_root / f"redu-p01-p50-{timestamp}"
    snapshot_dir = output_dir / "current_device_snapshot"

    output_dir.mkdir(parents=True, exist_ok=False)
    snapshot_dir.mkdir(parents=True, exist_ok=False)

    if args.snapshot_source:
        shutil.copytree(Path(args.snapshot_source).resolve(), snapshot_dir, dirs_exist_ok=True)
    else:
        with tempfile.TemporaryDirectory(prefix="redu-device-db-") as tmp:
            tmp_dir = Path(tmp)
            device_db = pull_device_database(args.adb_serial, tmp_dir)
            write_current_device_snapshot(device_db, snapshot_dir)

    generated = generate_simulated_export(seed=args.seed)
    write_export_csvs(generated, output_dir)
    write_supplemental_csvs(generated, output_dir)
    write_zip(output_dir)
    validate_export(output_dir)

    print(f"Output directory: {output_dir}")
    print(f"Export zip: {output_dir / 'redu-export.zip'}")
    print(f"Participants: {PARTICIPANT_COUNT}")
    print(f"Sessions: {len(generated['sessions'])}")
    print(f"Prompt events: {len(generated['prompt_events'])}")
    print(f"Reliability events: {len(generated['reliability_events'])}")
    print(f"Risk personalization rows: {len(generated['risk_personalization'])}")
    print(f"Supplemental files: {len(SUPPLEMENTAL_HEADERS)}")
    return 0


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Create a deterministic REDU simulated export dataset from a read-only device snapshot.",
    )
    parser.add_argument("--adb-serial", help="ADB serial to read. Defaults to the only connected device.")
    parser.add_argument(
        "--snapshot-source",
        help="Existing current_device_snapshot directory to reuse when no ADB device is connected.",
    )
    parser.add_argument("--seed", type=int, default=DEFAULT_SEED, help="Deterministic simulation seed.")
    parser.add_argument(
        "--output-root",
        default="simulated-export",
        help="Directory under the repo where the timestamped export folder is created.",
    )
    return parser.parse_args()


def pull_device_database(adb_serial: str | None, tmp_dir: Path) -> Path:
    serial = adb_serial or resolve_adb_serial()
    for suffix in ("", "-wal", "-shm"):
        target = tmp_dir / f"{DB_NAME}{suffix}"
        command = [
            "adb",
            "-s",
            serial,
            "exec-out",
            "run-as",
            PACKAGE,
            "cat",
            f"databases/{DB_NAME}{suffix}",
        ]
        with target.open("wb") as out_file:
            subprocess.run(command, check=True, stdout=out_file)

    return checkpoint_wal(tmp_dir / DB_NAME)


def resolve_adb_serial() -> str:
    result = subprocess.run(["adb", "devices"], check=True, capture_output=True, text=True)
    devices = []
    for line in result.stdout.splitlines()[1:]:
        parts = line.split()
        if len(parts) >= 2 and parts[1] == "device":
            devices.append(parts[0])
    if len(devices) != 1:
        joined = ", ".join(devices) if devices else "none"
        raise RuntimeError(f"Expected exactly one connected adb device, found {joined}")
    return devices[0]


def checkpoint_wal(db_path: Path) -> Path:
    stable_dir = db_path.parent / "stable"
    stable_dir.mkdir()
    stable_db = stable_dir / DB_NAME
    shutil.copy2(db_path, stable_db)
    wal_path = db_path.with_name(f"{DB_NAME}-wal")
    if wal_path.exists():
        shutil.copy2(wal_path, stable_db.with_name(f"{DB_NAME}-wal"))
    with sqlite3.connect(stable_db) as conn:
        conn.execute("PRAGMA wal_checkpoint(FULL)")
    return stable_db


def write_current_device_snapshot(db_path: Path, snapshot_dir: Path) -> None:
    with sqlite3.connect(db_path) as conn:
        conn.row_factory = sqlite3.Row
        for table in SNAPSHOT_TABLES:
            rows = conn.execute(f"SELECT * FROM {table}").fetchall()
            columns = [info[1] for info in conn.execute(f"PRAGMA table_info({table})").fetchall()]
            write_csv(snapshot_dir / f"{table}.csv", columns, rows)

        counts = [
            {"table": table, "row_count": conn.execute(f"SELECT COUNT(*) FROM {table}").fetchone()[0]}
            for table in SNAPSHOT_TABLES
        ]
        write_csv(snapshot_dir / "table_counts.csv", ["table", "row_count"], counts)


def generate_simulated_export(seed: int) -> dict[str, list[dict[str, object]]]:
    rng = random.Random(seed)
    participants = build_participants()
    study_start = date(2026, 5, 10)

    sessions: list[dict[str, object]] = []
    prompt_events: list[dict[str, object]] = []
    reliability_events: list[dict[str, object]] = []
    session_id = 1

    for participant in participants:
        participant_sessions: list[dict[str, object]] = []
        for day_index in range(STUDY_DAYS):
            day = study_start + timedelta(days=day_index)
            if rng.random() < participant["profile"].skip_day_probability:
                daily_count = 0
            else:
                daily_count = rng.randint(
                    participant["profile"].daily_session_min,
                    participant["profile"].daily_session_max,
                )

            used_offsets: set[int] = set()
            for _ in range(daily_count):
                start_offset = choose_start_offset(rng, used_offsets)
                session = build_session(
                    rng=rng,
                    session_id=session_id,
                    study_code=participant["code"],
                    group=participant["group"],
                    profile=participant["profile"],
                    day=day,
                    start_offset_minutes=start_offset,
                )
                sessions.append(session)
                participant_sessions.append(session)
                reliability_events.extend(build_reliability_events(session))
                prompt_events.extend(build_prompt_events(rng, session))
                session_id += 1

        ensure_minimum_reliable_baseline(
            rng=rng,
            sessions=sessions,
            participant_sessions=participant_sessions,
            participant=participant,
            reliability_events=reliability_events,
            prompt_events=prompt_events,
            next_session_id_start=session_id,
            study_start=study_start,
        )
        session_id = max(int(row["_id"]) for row in sessions) + 1

    sessions.sort(key=lambda row: (row["start_ms"], row["study_code"], row["_id"]))
    id_remap = {int(row["_id"]): index + 1 for index, row in enumerate(sessions)}
    for index, row in enumerate(sessions, start=1):
        row["_id"] = index
    for row in prompt_events:
        original_session_id = row.get("_session_id")
        row["session_id"] = id_remap[int(original_session_id)] if original_session_id else ""
    for row in reliability_events:
        affected = row.get("_affected_session_id")
        row["affected_session_id"] = id_remap[int(affected)] if affected else ""

    prompt_events.sort(key=lambda row: (row["timestamp_ms"], row["study_code"]))
    reliability_events.sort(key=lambda row: (row["timestamp_ms"], row["study_code"], row["event_type"]))

    supplemental = build_supplemental_files(
        rng=rng,
        participants=participants,
        sessions=sessions,
        prompt_events=prompt_events,
    )

    return {
        "sessions": export_sessions(sessions),
        "daily_summaries": build_daily_summaries(sessions),
        "prompt_events": export_prompt_events(prompt_events),
        "reliability_events": export_reliability_events(reliability_events),
        "risk_personalization": build_risk_personalization(sessions),
        "supplemental": supplemental,
    }


def build_participants() -> list[dict[str, object]]:
    profile_sequence: list[Profile] = []
    for profile in PROFILES:
        profile_sequence.extend([profile] * profile.participant_count)
    if len(profile_sequence) != PARTICIPANT_COUNT:
        raise RuntimeError("Profile participant counts must total 50")

    participants = []
    for number, profile in enumerate(profile_sequence, start=1):
        suffix = "X" if number % 2 == 1 else "Y"
        code = f"P-{number:02d}{suffix}"
        group = "CONTROL" if suffix == "Y" else "INTERVENTION"
        participants.append({"code": code, "group": group, "profile": profile})
    return participants


def choose_start_offset(rng: random.Random, used_offsets: set[int]) -> int:
    for _ in range(50):
        offset = rng.randint(7 * 60, 23 * 60)
        if all(abs(offset - existing) >= 18 for existing in used_offsets):
            used_offsets.add(offset)
            return offset
    offset = rng.randint(7 * 60, 23 * 60)
    used_offsets.add(offset)
    return offset


def build_session(
    rng: random.Random,
    session_id: int,
    study_code: str,
    group: str,
    profile: Profile,
    day: date,
    start_offset_minutes: int,
) -> dict[str, object]:
    duration_minutes = triangular_int(rng, profile.duration_min, profile.duration_max, profile.duration_min)
    raw_duration_ms = duration_minutes * 60_000 + rng.randint(0, 59_999)
    started_at = datetime.combine(day, time(), tzinfo=STUDY_ZONE) + timedelta(
        minutes=start_offset_minutes,
        seconds=rng.randint(0, 59),
    )
    start_ms = epoch_ms(started_at)
    end_ms = start_ms + raw_duration_ms
    mean_dwell_ms = rng.randint(profile.dwell_min, profile.dwell_max)
    swipe_count = max(0, int(raw_duration_ms / max(mean_dwell_ms, 1)) + rng.randint(-3, 6))

    reliable = rng.random() < profile.reliable_probability
    if reliable:
        oov_ratio = round(rng.uniform(0.04, 0.42), 15)
        nsd_percent = round(rng.uniform(profile.negative_min, profile.negative_max), 13)
        resolvable_units = rng.randint(8, 42)
        negative_units = min(resolvable_units, round(resolvable_units * nsd_percent / 100.0))
    else:
        oov_ratio = round(rng.uniform(0.48, 1.0), 15)
        nsd_percent = ""
        resolvable_units = rng.randint(0, 8)
        negative_units = rng.randint(0, resolvable_units)

    platform = weighted_choice(
        rng,
        [
            ("TIKTOK", 0.45 if "doomscroller" in profile.name or "high_risk" in profile.name else 0.35),
            ("INSTAGRAM", 0.35),
            ("FACEBOOK", 0.20 if "doomscroller" in profile.name or "high_risk" in profile.name else 0.30),
        ],
    )
    risk_score = compute_risk_score(
        duration_minutes=raw_duration_ms / 60_000.0,
        mean_dwell_ms=mean_dwell_ms,
        nsd_percent=nsd_percent if isinstance(nsd_percent, float) else None,
        reliable=reliable,
        profile_bias=profile.risk_bias,
    )
    risk_level = risk_level_for_score(risk_score)

    return {
        "_id": session_id,
        "study_code": study_code,
        "group": group,
        "platform": platform,
        "start_ms": start_ms,
        "end_ms": end_ms,
        "raw_duration_ms": raw_duration_ms,
        "prompt_excluded_duration_ms": max(0, raw_duration_ms - prompt_excluded_ms(group, risk_level, rng)),
        "mean_dwell_ms": mean_dwell_ms,
        "swipe_count": swipe_count,
        "risk_score": risk_score,
        "risk_level": risk_level,
        "sentiment_reliability": "RELIABLE" if reliable else "SENTIMENT_UNRELIABLE",
        "nsd_percent": nsd_percent,
        "oov_ratio": oov_ratio,
        "_resolvable_units": resolvable_units,
        "_negative_units": negative_units,
        "_profile": profile.name,
    }


def triangular_int(rng: random.Random, low: int, high: int, mode: int) -> int:
    return max(low, min(high, int(round(rng.triangular(low, high, mode)))))


def prompt_excluded_ms(group: str, risk_level: str, rng: random.Random) -> int:
    if group != "INTERVENTION" or risk_level == "SAFE":
        return 0
    if risk_level == "WARNING":
        return rng.randint(0, 90_000)
    return rng.randint(20_000, 180_000)


def compute_risk_score(
    duration_minutes: float,
    mean_dwell_ms: int,
    nsd_percent: float | None,
    reliable: bool,
    profile_bias: float,
) -> float:
    duration_component = min(duration_minutes / 45.0, 1.0) * 38.0
    dwell_component = max(0.0, (16_000.0 - mean_dwell_ms) / 16_000.0) * 30.0
    if reliable and nsd_percent is not None:
        sentiment_component = nsd_percent * 0.32
    else:
        sentiment_component = 12.0
    score = 8.0 + duration_component + dwell_component + sentiment_component + profile_bias
    return round(max(0.0, min(99.0, score)), 2)


def risk_level_for_score(score: float) -> str:
    if score < 33.33:
        return "SAFE"
    if score < 66.67:
        return "WARNING"
    return "CRITICAL"


def weighted_choice(rng: random.Random, choices: list[tuple[str, float]]) -> str:
    total = sum(weight for _, weight in choices)
    mark = rng.random() * total
    running = 0.0
    for value, weight in choices:
        running += weight
        if mark <= running:
            return value
    return choices[-1][0]


def build_reliability_events(session: dict[str, object]) -> list[dict[str, object]]:
    start = int(session["start_ms"])
    end = int(session["end_ms"])
    platform = str(session["platform"])
    platform_code = platform.lower()
    events = [
        event_row(session, start - 2_000, "", "SERVICE_STARTED", "service_connected", None),
        event_row(session, start + 300, platform, "TARGET_FOREGROUND", f"{platform_code}_foreground", None),
        event_row(session, end + 4, platform, "TARGET_BACKGROUND", "target_background_bridge_started", None),
    ]
    if session["sentiment_reliability"] == "SENTIMENT_UNRELIABLE":
        events.append(event_row(session, end + SESSION_FINALIZED_DELAY_MS - 18, platform, "HIGH_OOV", "sentiment_unreliable", int(session["_id"])))
    events.append(event_row(session, end + SESSION_FINALIZED_DELAY_MS, platform, "SESSION_FINALIZED", "session_saved", int(session["_id"])))
    return events


def event_row(
    session: dict[str, object],
    timestamp_ms: int,
    platform: str,
    event_type: str,
    details_code: str,
    affected_session_id: int | None,
) -> dict[str, object]:
    return {
        "study_code": session["study_code"],
        "timestamp_ms": timestamp_ms,
        "platform": platform,
        "event_type": event_type,
        "details_code": details_code,
        "affected_session_id": affected_session_id if affected_session_id is not None else "",
        "_affected_session_id": affected_session_id,
    }


def build_prompt_events(rng: random.Random, session: dict[str, object]) -> list[dict[str, object]]:
    if session["group"] != "INTERVENTION" or session["risk_level"] == "SAFE":
        return []
    show_probability = 0.58 if session["risk_level"] == "WARNING" else 0.78
    if rng.random() > show_probability:
        return []

    prompt_level = "L2_PAUSE" if session["risk_level"] == "WARNING" else "L3_BREATHING"
    shown_at = int(session["start_ms"]) + int(int(session["raw_duration_ms"]) * rng.uniform(0.35, 0.72))
    events = [
        {
            "study_code": session["study_code"],
            "timestamp_ms": shown_at,
            "session_id": "",
            "risk_level": session["risk_level"],
            "prompt_level": prompt_level,
            "action": "SHOWN",
            "cooldown_state": "false",
            "_session_id": session["_id"],
        },
    ]
    action = weighted_choice(
        rng,
        [
            ("TAKE_BREAK", 0.32 if session["risk_level"] == "CRITICAL" else 0.18),
            ("CONTINUE", 0.40),
            ("DISMISSED", 0.20),
            ("VIEW_DASHBOARD", 0.08),
        ],
    )
    events.append(
        {
            "study_code": session["study_code"],
            "timestamp_ms": shown_at + rng.randint(12_000, 95_000),
            "session_id": "",
            "risk_level": session["risk_level"],
            "prompt_level": prompt_level,
            "action": action,
            "cooldown_state": "false" if action == "TAKE_BREAK" else str(rng.random() < 0.25).lower(),
            "_session_id": session["_id"],
        },
    )
    return events


def ensure_minimum_reliable_baseline(
    rng: random.Random,
    sessions: list[dict[str, object]],
    participant_sessions: list[dict[str, object]],
    participant: dict[str, object],
    reliability_events: list[dict[str, object]],
    prompt_events: list[dict[str, object]],
    next_session_id_start: int,
    study_start: date,
) -> None:
    reliable_count = sum(1 for row in participant_sessions if row["sentiment_reliability"] == "RELIABLE")
    session_id = next_session_id_start
    day_offset = 0
    while reliable_count < 10:
        profile = participant["profile"]
        day = study_start + timedelta(days=day_offset % STUDY_DAYS)
        session = build_session(
            rng=rng,
            session_id=session_id,
            study_code=str(participant["code"]),
            group=str(participant["group"]),
            profile=profile,
            day=day,
            start_offset_minutes=8 * 60 + (day_offset * 37) % (10 * 60),
        )
        session["sentiment_reliability"] = "RELIABLE"
        if session["nsd_percent"] == "":
            session["nsd_percent"] = round(rng.uniform(profile.negative_min, profile.negative_max), 13)
            session["oov_ratio"] = round(rng.uniform(0.04, 0.35), 15)
        sessions.append(session)
        participant_sessions.append(session)
        reliability_events.extend(build_reliability_events(session))
        prompt_events.extend(build_prompt_events(rng, session))
        reliable_count += 1
        session_id += 1
        day_offset += 1


def export_sessions(rows: list[dict[str, object]]) -> list[dict[str, object]]:
    return [{header: row[header] for header in EXPORT_HEADERS["sessions.csv"]} for row in rows]


def export_prompt_events(rows: list[dict[str, object]]) -> list[dict[str, object]]:
    return [{header: row[header] for header in EXPORT_HEADERS["prompt_events.csv"]} for row in rows]


def export_reliability_events(rows: list[dict[str, object]]) -> list[dict[str, object]]:
    return [{header: row[header] for header in EXPORT_HEADERS["reliability_events.csv"]} for row in rows]


def build_daily_summaries(sessions: list[dict[str, object]]) -> list[dict[str, object]]:
    grouped: dict[tuple[str, str, str], list[dict[str, object]]] = {}
    for session in sessions:
        key = (
            str(session["study_code"]),
            datetime.fromtimestamp(int(session["start_ms"]) / 1000, tz=STUDY_ZONE).date().isoformat(),
            str(session["platform"]),
        )
        grouped.setdefault(key, []).append(session)

    summaries = []
    for (study_code, day, platform), rows in sorted(grouped.items()):
        reliable_rows = [row for row in rows if row["sentiment_reliability"] == "RELIABLE"]
        nsd_values = [float(row["nsd_percent"]) for row in reliable_rows if row["nsd_percent"] != ""]
        summaries.append(
            {
                "study_code": study_code,
                "date": day,
                "platform": platform,
                "session_count": len(rows),
                "mean_duration_ms": int(sum(int(row["raw_duration_ms"]) for row in rows) / len(rows)),
                "mean_dwell_ms": int(sum(int(row["mean_dwell_ms"]) for row in rows) / len(rows)),
                "mean_nsd_percent": mean(nsd_values) if nsd_values else "",
                "mean_risk_score": mean(float(row["risk_score"]) for row in rows),
                "reliable_session_count": len(reliable_rows),
            },
        )
    return summaries


def build_risk_personalization(sessions: list[dict[str, object]]) -> list[dict[str, object]]:
    by_participant: dict[tuple[str, str], list[dict[str, object]]] = {}
    for row in sessions:
        by_participant.setdefault((str(row["study_code"]), str(row["group"])), []).append(row)

    locked_at = epoch_ms(datetime(2026, 5, 24, 0, 0, tzinfo=STUDY_ZONE))
    rows = []
    for (study_code, group), participant_sessions in sorted(by_participant.items()):
        reliable = [row for row in participant_sessions if row["sentiment_reliability"] == "RELIABLE"]
        durations = sorted(int(row["raw_duration_ms"]) / 60_000.0 for row in reliable)
        nsd_values = sorted(float(row["nsd_percent"]) for row in reliable if row["nsd_percent"] != "")
        duration_quantiles = nearest_rank_quantiles(durations) if len(reliable) >= 10 else None
        nsd_quantiles = nearest_rank_quantiles(nsd_values) if len(reliable) >= 10 and nsd_values else None
        rows.append(
            {
                "study_code": study_code,
                "group": group,
                "locked_at_ms": locked_at,
                "reliable_baseline_session_count": len(reliable),
                "duration_q25_min": duration_quantiles[0] if duration_quantiles else "",
                "duration_q50_min": duration_quantiles[1] if duration_quantiles else "",
                "duration_q75_min": duration_quantiles[2] if duration_quantiles else "",
                "duration_q95_min": duration_quantiles[3] if duration_quantiles else "",
                "nsd_q25_percent": nsd_quantiles[0] if nsd_quantiles else "",
                "nsd_q50_percent": nsd_quantiles[1] if nsd_quantiles else "",
                "nsd_q75_percent": nsd_quantiles[2] if nsd_quantiles else "",
                "nsd_q95_percent": nsd_quantiles[3] if nsd_quantiles else "",
            },
        )
    return rows


def build_supplemental_files(
    rng: random.Random,
    participants: list[dict[str, object]],
    sessions: list[dict[str, object]],
    prompt_events: list[dict[str, object]],
) -> dict[str, list[dict[str, object]]]:
    by_participant: dict[str, list[dict[str, object]]] = {}
    prompts_by_participant: dict[str, list[dict[str, object]]] = {}
    for row in sessions:
        by_participant.setdefault(str(row["study_code"]), []).append(row)
    for row in prompt_events:
        prompts_by_participant.setdefault(str(row["study_code"]), []).append(row)

    profiles = {
        str(participant["code"]): str(participant["profile"].name)
        for participant in participants
    }
    return {
        "doomscrolling_scale.csv": build_doomscrolling_scale_rows(rng, participants, by_participant, profiles),
        "survey_iso25010.csv": build_iso25010_rows(rng, participants, by_participant),
        "sus_responses.csv": build_sus_rows(rng, participants, by_participant),
        "tam_responses.csv": build_tam_rows(rng, participants, by_participant, prompts_by_participant),
        "open_ended_feedback.csv": build_open_feedback_rows(rng, participants, by_participant, prompts_by_participant),
        "sme_evaluation.csv": build_blank_sme_evaluation_rows(),
        "sme_open_ended_feedback.csv": build_blank_sme_feedback_rows(),
    }


def build_doomscrolling_scale_rows(
    rng: random.Random,
    participants: list[dict[str, object]],
    by_participant: dict[str, list[dict[str, object]]],
    profiles: dict[str, str],
) -> list[dict[str, object]]:
    rows = []
    for participant in participants:
        code = str(participant["code"])
        group = str(participant["group"])
        week1 = [row for row in by_participant[code] if session_week(row) == 1]
        week2 = [row for row in by_participant[code] if session_week(row) == 2]
        base_score = latent_doomscrolling_score(week1, profiles[code])
        change = -0.35 if group == "INTERVENTION" else 0.05
        if profiles[code] in {"heavy_doomscroller", "frequent_high_risk"}:
            change += 0.1
        week_scores = [
            ("baseline", 1, week1, base_score),
            ("post", 2, week2, max(1.0, min(7.0, base_score + change + rng.uniform(-0.35, 0.25)))),
        ]
        for administration, week, week_rows, target in week_scores:
            items = likert_items(rng, target, 4, 1, 7)
            rows.append(
                {
                    "study_code": code,
                    "group": group,
                    "administration": administration,
                    "week": week,
                    "completed_at_ms": survey_timestamp_ms(week, code),
                    "dss_1_urge_bad_news": items[0],
                    "dss_2_lose_track_time": items[1],
                    "dss_3_continuous_negative_browsing": items[2],
                    "dss_4_addicted_negative_news": items[3],
                    "total_score": sum(items),
                    "mean_score": round(sum(items) / len(items), 2),
                },
            )
    return rows


def build_iso25010_rows(
    rng: random.Random,
    participants: list[dict[str, object]],
    by_participant: dict[str, list[dict[str, object]]],
) -> list[dict[str, object]]:
    rows = []
    for participant in participants:
        code = str(participant["code"])
        group = str(participant["group"])
        reliability_rate = participant_reliability_rate(by_participant[code])
        functional = likert_items(rng, 4.05 if group == "INTERVENTION" else 3.85, 3, 1, 5)
        performance = likert_items(rng, 3.55 + reliability_rate * 0.35, 3, 1, 5)
        reliability = likert_items(rng, 3.45 + reliability_rate * 0.55, 3, 1, 5)
        rows.append(
            {
                "study_code": code,
                "group": group,
                "completed_at_ms": survey_timestamp_ms(2, code, hour=19),
                "functional_suitability_1": functional[0],
                "functional_suitability_2": functional[1],
                "functional_suitability_3": functional[2],
                "functional_suitability_mean": item_mean(functional),
                "performance_efficiency_1": performance[0],
                "performance_efficiency_2": performance[1],
                "performance_efficiency_3": performance[2],
                "performance_efficiency_mean": item_mean(performance),
                "reliability_1": reliability[0],
                "reliability_2": reliability[1],
                "reliability_3": reliability[2],
                "reliability_mean": item_mean(reliability),
            },
        )
    return rows


def build_sus_rows(
    rng: random.Random,
    participants: list[dict[str, object]],
    by_participant: dict[str, list[dict[str, object]]],
) -> list[dict[str, object]]:
    rows = []
    for participant in participants:
        code = str(participant["code"])
        group = str(participant["group"])
        reliability_rate = participant_reliability_rate(by_participant[code])
        target = 3.65 + reliability_rate * 0.45
        items = likert_items(rng, target, 10, 1, 5)
        # SUS even-numbered items are negative. Keep them lower for usable cases.
        for index in [1, 3, 5, 7, 9]:
            items[index] = max(1, min(5, 6 - items[index] + rng.choice([-1, 0, 0, 1])))
        rows.append(
            {
                "study_code": code,
                "group": group,
                "completed_at_ms": survey_timestamp_ms(2, code, hour=19, minute=20),
                **{f"sus_{index + 1}": value for index, value in enumerate(items)},
                "sus_score": sus_score(items),
            },
        )
    return rows


def build_tam_rows(
    rng: random.Random,
    participants: list[dict[str, object]],
    by_participant: dict[str, list[dict[str, object]]],
    prompts_by_participant: dict[str, list[dict[str, object]]],
) -> list[dict[str, object]]:
    rows = []
    for participant in participants:
        code = str(participant["code"])
        group = str(participant["group"])
        prompt_count = len(prompts_by_participant.get(code, []))
        risk_mean = mean(float(row["risk_score"]) for row in by_participant[code])
        pu_target = 3.65 + (0.45 if group == "INTERVENTION" else 0.05) + min(prompt_count / 80.0, 0.35)
        peou_target = 3.85 + (0.25 if risk_mean < 60 else -0.05)
        pu = likert_items(rng, pu_target, 6, 1, 5)
        peou = likert_items(rng, peou_target, 6, 1, 5)
        rows.append(
            {
                "study_code": code,
                "group": group,
                "completed_at_ms": survey_timestamp_ms(2, code, hour=19, minute=40),
                **{f"pu_{index + 1}": value for index, value in enumerate(pu)},
                "perceived_usefulness_mean": item_mean(pu),
                **{f"peou_{index + 1}": value for index, value in enumerate(peou)},
                "perceived_ease_of_use_mean": item_mean(peou),
            },
        )
    return rows


def build_open_feedback_rows(
    rng: random.Random,
    participants: list[dict[str, object]],
    by_participant: dict[str, list[dict[str, object]]],
    prompts_by_participant: dict[str, list[dict[str, object]]],
) -> list[dict[str, object]]:
    rows = []
    for participant in participants:
        code = str(participant["code"])
        group = str(participant["group"])
        prompt_count = len(prompts_by_participant.get(code, []))
        reliability_rate = participant_reliability_rate(by_participant[code])
        if group == "INTERVENTION" and prompt_count > 20:
            theme = "Prompt timing and interruption"
            liked = "The break reminders made me notice long scrolling sessions."
            difficulty = "Some prompts appeared while I was still deciding whether to continue."
            improvement = "Let users choose quiet hours or a gentler prompt frequency."
        elif reliability_rate < 0.78:
            theme = "Technical reliability"
            liked = "The dashboard still summarized my activity without saving raw posts."
            difficulty = "Some sessions seemed harder for the app to interpret."
            improvement = "Make platform detection and sentiment reliability clearer in the dashboard."
        elif rng.random() < 0.25:
            theme = "Permission and onboarding clarity"
            liked = "The setup steps were short once the service was enabled."
            difficulty = "Accessibility permission wording was intimidating at first."
            improvement = "Add a simple explanation before opening Android settings."
        elif rng.random() < 0.35:
            theme = "Privacy and trust"
            liked = "I liked that the export used study codes and aggregate numbers."
            difficulty = "I still wanted more assurance that screenshots or captions were not stored."
            improvement = "Show a privacy summary inside the app before monitoring starts."
        else:
            theme = "Dashboard usefulness"
            liked = "The risk summary helped me compare short and long scrolling sessions."
            difficulty = "The numbers were useful but could use more plain-language labels."
            improvement = "Add weekly trends and clearer safe, warning, and critical explanations."

        rows.append(
            {
                "study_code": code,
                "group": group,
                "completed_at_ms": survey_timestamp_ms(2, code, hour=20),
                "liked_most": liked,
                "main_difficulty": difficulty,
                "privacy_comment": privacy_comment_for_theme(theme),
                "suggested_improvement": improvement,
                "coded_theme": theme,
            },
        )
    return rows


def build_blank_sme_evaluation_rows() -> list[dict[str, object]]:
    roles = [
        ("SME-01", "software_engineering_or_mobile_development"),
        ("SME-02", "data_science_machine_learning_or_fuzzy_logic"),
        ("SME-03", "digital_wellbeing_behavioral_psychology_or_educational_technology"),
    ]
    return [
        {
            "expert_id": expert_id,
            "expert_role": role,
            "technical_soundness": "",
            "input_range_plausibility": "",
            "rule_base_coherence": "",
            "privacy_architecture_quality": "",
            "intervention_design_appropriateness": "",
            "overall_software_quality": "",
            "comments": "",
        }
        for expert_id, role in roles
    ]


def build_blank_sme_feedback_rows() -> list[dict[str, object]]:
    return [
        {
            "expert_id": row["expert_id"],
            "expert_role": row["expert_role"],
            "technical_design_comment": "",
            "privacy_safeguards_comment": "",
            "heuristic_logic_comment": "",
            "intervention_structure_comment": "",
            "recommended_revision": "",
        }
        for row in build_blank_sme_evaluation_rows()
    ]


def write_supplemental_csvs(generated: dict[str, object], output_dir: Path) -> None:
    supplemental = generated["supplemental"]
    if not isinstance(supplemental, dict):
        raise AssertionError("Supplemental rows are missing")
    for filename, header in SUPPLEMENTAL_HEADERS.items():
        write_csv(output_dir / filename, header, supplemental[filename])


def session_week(row: dict[str, object]) -> int:
    session_day = datetime.fromtimestamp(int(row["start_ms"]) / 1000, tz=STUDY_ZONE).date()
    return 1 if session_day <= date(2026, 5, 16) else 2


def latent_doomscrolling_score(rows: list[dict[str, object]], profile_name: str) -> float:
    if not rows:
        return 3.0
    duration_minutes = mean(int(row["raw_duration_ms"]) / 60_000.0 for row in rows)
    risk_score = mean(float(row["risk_score"]) for row in rows)
    profile_offset = {
        "heavy_doomscroller": 1.3,
        "frequent_high_risk": 0.95,
        "moderate_user": 0.25,
        "intermittent_user": -0.15,
        "light_user": -0.75,
        "low_risk_baseline": -0.95,
    }[profile_name]
    value = 2.2 + duration_minutes / 30.0 + risk_score / 65.0 + profile_offset
    return max(1.0, min(7.0, value))


def likert_items(rng: random.Random, target_mean: float, count: int, low: int, high: int) -> list[int]:
    items = []
    for _ in range(count):
        value = int(round(target_mean + rng.uniform(-0.85, 0.85)))
        items.append(max(low, min(high, value)))
    return items


def item_mean(items: list[int]) -> float:
    return round(sum(items) / len(items), 2)


def sus_score(items: list[int]) -> float:
    total = 0
    for index, value in enumerate(items, start=1):
        total += value - 1 if index % 2 == 1 else 5 - value
    return round(total * 2.5, 1)


def participant_reliability_rate(rows: list[dict[str, object]]) -> float:
    reliable = sum(1 for row in rows if row["sentiment_reliability"] == "RELIABLE")
    return reliable / len(rows) if rows else 0.0


def survey_timestamp_ms(week: int, code: str, hour: int = 18, minute: int = 30) -> int:
    participant_number = int(code[2:4])
    day = date(2026, 5, 16 if week == 1 else 23)
    completed_at = datetime.combine(day, time(hour, minute), tzinfo=STUDY_ZONE) + timedelta(
        minutes=participant_number * 3,
    )
    return epoch_ms(completed_at)


def privacy_comment_for_theme(theme: str) -> str:
    if theme == "Privacy and trust":
        return "The aggregate-only design sounded good, but I wanted to see that clearly inside the app."
    if theme == "Permission and onboarding clarity":
        return "The permission screen needed more context before I felt comfortable enabling it."
    return "I was comfortable with study-code exports as long as raw captions and screenshots were not saved."


def nearest_rank_quantiles(values: list[float]) -> tuple[float, float, float, float]:
    def quantile(p: float) -> float:
        rank = max(1, min(len(values), math.ceil(p * len(values))))
        return round(values[rank - 1], 13)

    return quantile(0.25), quantile(0.50), quantile(0.75), quantile(0.95)


def mean(values: Iterable[float]) -> float:
    items = list(values)
    return round(sum(items) / len(items), 13)


def write_export_csvs(generated: dict[str, list[dict[str, object]]], output_dir: Path) -> None:
    write_csv(output_dir / "sessions.csv", EXPORT_HEADERS["sessions.csv"], generated["sessions"])
    write_csv(output_dir / "daily_summaries.csv", EXPORT_HEADERS["daily_summaries.csv"], generated["daily_summaries"])
    write_csv(output_dir / "prompt_events.csv", EXPORT_HEADERS["prompt_events.csv"], generated["prompt_events"])
    write_csv(output_dir / "reliability_events.csv", EXPORT_HEADERS["reliability_events.csv"], generated["reliability_events"])
    write_csv(
        output_dir / "risk_personalization.csv",
        EXPORT_HEADERS["risk_personalization.csv"],
        generated["risk_personalization"],
    )


def write_csv(path: Path, fieldnames: list[str], rows: Iterable[dict[str, object] | sqlite3.Row]) -> None:
    with path.open("w", newline="", encoding="utf-8") as out_file:
        writer = csv.DictWriter(out_file, fieldnames=fieldnames, extrasaction="ignore")
        writer.writeheader()
        for row in rows:
            writer.writerow(dict(row))


def write_zip(output_dir: Path) -> None:
    zip_path = output_dir / "redu-export.zip"
    with zipfile.ZipFile(zip_path, "w", compression=zipfile.ZIP_DEFLATED) as archive:
        for name in EXPORT_HEADERS:
            archive.write(output_dir / name, arcname=name)


def validate_export(output_dir: Path) -> None:
    rows_by_file: dict[str, list[dict[str, str]]] = {}
    for filename, expected_header in EXPORT_HEADERS.items():
        with (output_dir / filename).open(newline="", encoding="utf-8") as in_file:
            reader = csv.DictReader(in_file)
            if reader.fieldnames != expected_header:
                raise AssertionError(f"{filename} header mismatch: {reader.fieldnames}")
            rows_by_file[filename] = list(reader)

    session_rows = rows_by_file["sessions.csv"]
    participants = sorted({row["study_code"] for row in session_rows})
    expected_participants = [f"P-{index:02d}{'X' if index % 2 == 1 else 'Y'}" for index in range(1, 51)]
    if participants != expected_participants:
        raise AssertionError("Participant code set does not match P-01X through P-50Y")

    for row in session_rows:
        expected_group = "CONTROL" if row["study_code"].endswith("Y") else "INTERVENTION"
        if row["group"] != expected_group:
            raise AssertionError(f"Wrong group for {row['study_code']}: {row['group']}")
        start = int(row["start_ms"])
        end = int(row["end_ms"])
        duration = int(row["raw_duration_ms"])
        if not start < end or end - start != duration:
            raise AssertionError(f"Invalid timing for {row['study_code']} at {start}")
        if row["sentiment_reliability"] == "SENTIMENT_UNRELIABLE" and row["nsd_percent"] != "":
            raise AssertionError("Unreliable sentiment rows must have blank nsd_percent")

    for row in rows_by_file["prompt_events.csv"]:
        if row["study_code"].endswith("Y"):
            raise AssertionError("Control participants should not receive prompt events")

    with zipfile.ZipFile(output_dir / "redu-export.zip") as archive:
        names = sorted(archive.namelist())
        if names != sorted(EXPORT_HEADERS):
            raise AssertionError(f"Zip contents mismatch: {names}")

    validate_supplemental_files(output_dir, expected_participants)


def validate_supplemental_files(output_dir: Path, expected_participants: list[str]) -> None:
    supplemental_rows: dict[str, list[dict[str, str]]] = {}
    for filename, expected_header in SUPPLEMENTAL_HEADERS.items():
        with (output_dir / filename).open(newline="", encoding="utf-8") as in_file:
            reader = csv.DictReader(in_file)
            if reader.fieldnames != expected_header:
                raise AssertionError(f"{filename} header mismatch: {reader.fieldnames}")
            supplemental_rows[filename] = list(reader)

    participant_files = [
        "survey_iso25010.csv",
        "sus_responses.csv",
        "tam_responses.csv",
        "open_ended_feedback.csv",
    ]
    for filename in participant_files:
        participants = sorted(row["study_code"] for row in supplemental_rows[filename])
        if participants != expected_participants:
            raise AssertionError(f"{filename} participant coverage mismatch")
        for row in supplemental_rows[filename]:
            expected_group = "CONTROL" if row["study_code"].endswith("Y") else "INTERVENTION"
            if row["group"] != expected_group:
                raise AssertionError(f"{filename} has wrong group for {row['study_code']}")

    doom_rows = supplemental_rows["doomscrolling_scale.csv"]
    if len(doom_rows) != len(expected_participants) * 2:
        raise AssertionError("doomscrolling_scale.csv should have baseline and post rows per participant")
    for row in doom_rows:
        items = [
            int(row["dss_1_urge_bad_news"]),
            int(row["dss_2_lose_track_time"]),
            int(row["dss_3_continuous_negative_browsing"]),
            int(row["dss_4_addicted_negative_news"]),
        ]
        if not all(1 <= item <= 7 for item in items):
            raise AssertionError("Doomscrolling items must be 1-7")
        if int(row["total_score"]) != sum(items):
            raise AssertionError("Doomscrolling total score mismatch")

    if len(supplemental_rows["sme_evaluation.csv"]) != 3:
        raise AssertionError("sme_evaluation.csv should include three blank expert rows")
    if len(supplemental_rows["sme_open_ended_feedback.csv"]) != 3:
        raise AssertionError("sme_open_ended_feedback.csv should include three blank expert rows")
    for filename in ["sme_evaluation.csv", "sme_open_ended_feedback.csv"]:
        for row in supplemental_rows[filename]:
            for key, value in row.items():
                if key not in {"expert_id", "expert_role"} and value != "":
                    raise AssertionError(f"{filename} should leave expert-provided field {key} blank")


def epoch_ms(value: datetime) -> int:
    return int(value.timestamp() * 1000)


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except subprocess.CalledProcessError as exc:
        print(f"Command failed: {' '.join(exc.cmd)}", file=sys.stderr)
        raise SystemExit(exc.returncode)
