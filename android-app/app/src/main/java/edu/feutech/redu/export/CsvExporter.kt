package edu.feutech.redu.export

import android.content.Context
import androidx.room.withTransaction
import edu.feutech.redu.data.AppSettingsEntity
import edu.feutech.redu.data.DailySummary
import edu.feutech.redu.data.PromptEventEntity
import edu.feutech.redu.data.ReduDatabase
import edu.feutech.redu.data.ReliabilityEventEntity
import edu.feutech.redu.data.RiskPersonalizationEntity
import edu.feutech.redu.data.SentimentReliability
import edu.feutech.redu.data.SessionEntity
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class CsvExporter(
    private val context: Context,
    private val database: ReduDatabase,
) {
    suspend fun exportAll(): File {
        val dir = File(context.cacheDir, "redu-export-${System.currentTimeMillis()}")
        dir.mkdirs()

        try {
            val snapshot = database.withTransaction {
                ExportSnapshot(
                    settings = database.settingsDao().get(),
                    sessions = database.sessionDao().all(),
                    promptEvents = database.promptEventDao().all(),
                    reliabilityEvents = database.reliabilityEventDao().all(),
                    riskPersonalization = database.riskPersonalizationDao().all(),
                )
            }
            val sessions = snapshot.sessions
            write(File(dir, "sessions.csv"), sessionsCsv(sessions, snapshot.settings))
            write(File(dir, "daily_summaries.csv"), dailySummariesCsv(sessions, snapshot.settings))
            write(File(dir, "study_periods.csv"), studyPeriodsCsv(snapshot.settings))
            write(File(dir, "prompt_events.csv"), promptEventsCsv(snapshot.promptEvents))
            write(File(dir, "reliability_events.csv"), reliabilityEventsCsv(snapshot.reliabilityEvents))
            write(File(dir, "risk_personalization.csv"), riskPersonalizationCsv(snapshot.riskPersonalization))
            return dir
        } catch (e: Throwable) {
            dir.deleteRecursively()
            throw e
        }
    }

    /**
     * Exports all CSVs and zips them into a single shareable .zip file.
     * The name carries the study code and timestamp so repeated exports never
     * overwrite a file a share target may still be reading.
     */
    suspend fun exportAsZip(): File {
        val studyCode = database.settingsDao().get()?.studyCode?.takeIf { it.isNotBlank() } ?: "UNSET"
        val dir = exportAll()
        pruneOldExportZips()
        val zipFile = File(context.cacheDir, exportZipName(studyCode, System.currentTimeMillis()))
        zipDirectoryAndDeleteStaging(dir, zipFile)
        return zipFile
    }

    private fun write(file: File, content: String) {
        file.writeText(content)
    }

    private fun pruneOldExportZips(nowMillis: Long = System.currentTimeMillis()) {
        val cutoff = nowMillis - TimeUnit.HOURS.toMillis(24)
        context.cacheDir.listFiles()
            ?.filter { it.isFile && it.name.startsWith("redu-export-") && it.name.endsWith(".zip") }
            ?.filter { it.lastModified() in 1 until cutoff }
            ?.forEach { it.delete() }
    }

    companion object {
        // Daily summaries use the fixed study timezone so a device timezone
        // change before export cannot shift sessions across daily buckets.
        internal val STUDY_ZONE: ZoneId = ZoneId.of("Asia/Manila")

        private val ZIP_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS", Locale.US)

        internal fun exportZipName(studyCode: String, timestampMillis: Long): String {
            val safeCode = studyCode.replace(Regex("[^A-Za-z0-9_-]"), "").ifBlank { "UNSET" }
            val stamp = Instant.ofEpochMilli(timestampMillis).atZone(STUDY_ZONE).format(ZIP_TIMESTAMP_FORMAT)
            return "redu-export-$safeCode-$stamp.zip"
        }

        internal fun zipDirectoryAndDeleteStaging(dir: File, zipFile: File) {
            try {
                zipFile.delete() // Remove any previous export
                ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                    dir.listFiles()?.forEach { file ->
                        zos.putNextEntry(ZipEntry(file.name))
                        file.inputStream().use { it.copyTo(zos) }
                        zos.closeEntry()
                    }
                }
            } finally {
                dir.deleteRecursively()
            }
        }

        internal fun sessionsCsv(rows: List<SessionEntity>, settings: AppSettingsEntity? = null): String = buildString {
            val period = settings?.toStudyPeriod()
            appendLine("study_code,group,platform,study_week,study_day,start_ms,end_ms,raw_duration_ms,prompt_excluded_duration_ms,mean_dwell_ms,swipe_count,risk_score,risk_level,sentiment_reliability,nsd_percent,resolvable_units,negative_units,oov_ratio,session_id")
            rows.forEach {
                val annotation = period?.annotationFor(it.startedAtMillis)
                appendLine(listOf(
                    it.studyCode,
                    it.studyGroup.name,
                    it.platform.name,
                    annotation?.week ?: "",
                    annotation?.day ?: "",
                    it.startedAtMillis,
                    it.endedAtMillis,
                    it.rawDurationMillis,
                    it.promptExcludedDurationMillis,
                    it.meanDwellMillis,
                    it.swipeCount,
                    it.riskScore,
                    it.riskLevel.name,
                    it.sentimentReliability.name,
                    it.nsdPercent ?: "",
                    it.resolvableUnits,
                    it.negativeUnits,
                    it.oovRatio,
                    it.id,
                ).joinToString(",") { cell -> csv(cell.toString()) })
            }
        }

        internal fun dailySummariesCsv(rows: List<SessionEntity>, settings: AppSettingsEntity? = null): String = buildString {
            val period = settings?.toStudyPeriod()
            appendLine("study_code,date,platform,study_week,study_day,session_count,mean_duration_ms,mean_dwell_ms,mean_nsd_percent,mean_risk_score,reliable_session_count")
            val summaries = rows.groupBy { Triple(it.studyCode, dateOf(it.startedAtMillis), it.platform) }.map { (key, sessions) ->
                val reliable = sessions.filter { it.sentimentReliability == SentimentReliability.RELIABLE }
                val annotation = period?.annotationFor(sessions.minOf { it.startedAtMillis })
                DailySummary(
                    studyCode = key.first,
                    date = key.second.toString(),
                    platform = key.third,
                    studyWeek = annotation?.week,
                    studyDay = annotation?.day,
                    sessionCount = sessions.size,
                    meanDurationMillis = sessions.averageOfLong { it.rawDurationMillis },
                    meanDwellMillis = sessions.averageOfLong { it.meanDwellMillis },
                    meanNsdPercent = reliable.mapNotNull { it.nsdPercent }.takeIf { it.isNotEmpty() }?.average(),
                    meanRiskScore = sessions.map { it.riskScore }.average(),
                    reliableSessionCount = reliable.size,
                )
            }
            summaries.forEach {
                appendLine(listOf(
                    it.studyCode,
                    it.date,
                    it.platform.name,
                    it.studyWeek ?: "",
                    it.studyDay ?: "",
                    it.sessionCount,
                    it.meanDurationMillis,
                    it.meanDwellMillis,
                    it.meanNsdPercent ?: "",
                    it.meanRiskScore,
                    it.reliableSessionCount,
                ).joinToString(",") { cell -> csv(cell.toString()) })
            }
        }

        internal fun studyPeriodsCsv(settings: AppSettingsEntity?): String = buildString {
            appendLine("study_code,group,timezone,week1_start_ms,week1_end_ms,week2_start_ms,week2_end_ms")
            settings?.let {
                appendLine(listOf(
                    it.studyCode.takeIf { code -> code.isNotBlank() } ?: "UNSET",
                    it.studyGroup.name,
                    STUDY_ZONE.id,
                    it.week1StartMillis ?: "",
                    it.week1EndMillis ?: "",
                    it.week2StartMillis ?: "",
                    it.week2EndMillis ?: "",
                ).joinToString(",") { cell -> csv(cell.toString()) })
            }
        }

        internal fun promptEventsCsv(rows: List<PromptEventEntity>): String = buildString {
            appendLine("study_code,timestamp_ms,session_id,risk_level,prompt_level,action,cooldown_state,group,risk_score,trigger_reason")
            rows.forEach {
                appendLine(listOf(
                    it.studyCode,
                    it.timestampMillis,
                    it.sessionId ?: "",
                    it.riskLevel.name,
                    it.promptLevel.name,
                    it.action.name,
                    it.cooldownActive,
                    it.studyGroup.name,
                    it.riskScore,
                    it.trigger.name,
                ).joinToString(",") { cell -> csv(cell.toString()) })
            }
        }

        internal fun reliabilityEventsCsv(rows: List<ReliabilityEventEntity>): String = buildString {
            appendLine("study_code,timestamp_ms,platform,event_type,details_code,affected_session_id")
            rows.forEach {
                appendLine(listOf(
                    it.studyCode,
                    it.timestampMillis,
                    it.platform?.name ?: "",
                    it.type.name,
                    it.detailsCode,
                    it.affectedSessionId ?: "",
                ).joinToString(",") { cell -> csv(cell.toString()) })
            }
        }

        internal fun riskPersonalizationCsv(rows: List<RiskPersonalizationEntity>): String = buildString {
            appendLine("study_code,group,locked_at_ms,reliable_baseline_session_count,duration_q25_min,duration_q50_min,duration_q75_min,duration_q95_min,nsd_q25_percent,nsd_q50_percent,nsd_q75_percent,nsd_q95_percent")
            rows.forEach {
                appendLine(listOf(
                    it.studyCode,
                    it.studyGroup.name,
                    it.lockedAtMillis,
                    it.reliableBaselineSessionCount,
                    it.durationQ25Minutes ?: "",
                    it.durationQ50Minutes ?: "",
                    it.durationQ75Minutes ?: "",
                    it.durationQ95Minutes ?: "",
                    it.nsdQ25Percent ?: "",
                    it.nsdQ50Percent ?: "",
                    it.nsdQ75Percent ?: "",
                    it.nsdQ95Percent ?: "",
                ).joinToString(",") { cell -> csv(cell.toString()) })
            }
        }

        private fun dateOf(epochMillis: Long): LocalDate =
            Instant.ofEpochMilli(epochMillis).atZone(STUDY_ZONE).toLocalDate()

        private fun List<SessionEntity>.averageOfLong(selector: (SessionEntity) -> Long): Long =
            if (isEmpty()) 0L else map(selector).average().toLong()

        private fun csv(value: String): String {
            val escaped = value.replace("\"", "\"\"")
            return if (escaped.any { it == ',' || it == '"' || it == '\n' }) "\"$escaped\"" else escaped
        }

        private fun AppSettingsEntity.toStudyPeriod(): StudyPeriod =
            StudyPeriod(
                week1StartMillis = week1StartMillis,
                week1EndMillis = week1EndMillis,
                week2StartMillis = week2StartMillis,
                week2EndMillis = week2EndMillis,
            )

        private data class StudyPeriod(
            val week1StartMillis: Long?,
            val week1EndMillis: Long?,
            val week2StartMillis: Long?,
            val week2EndMillis: Long?,
        ) {
            fun annotationFor(timestampMillis: Long): StudyAnnotation? =
                when {
                    week1StartMillis != null &&
                        week1EndMillis != null &&
                        timestampMillis in week1StartMillis..week1EndMillis -> {
                        StudyAnnotation(
                            week = 1,
                            day = dayNumberFrom(week1StartMillis, timestampMillis),
                        )
                    }
                    week2StartMillis != null &&
                        week2EndMillis != null &&
                        timestampMillis in week2StartMillis..week2EndMillis -> {
                        StudyAnnotation(
                            week = 2,
                            day = dayNumberFrom(week1StartMillis ?: week2StartMillis, timestampMillis),
                        )
                    }
                    else -> null
                }

            private fun dayNumberFrom(baseMillis: Long, timestampMillis: Long): Int =
                ChronoUnit.DAYS.between(dateOf(baseMillis), dateOf(timestampMillis)).toInt() + 1
        }

        private data class StudyAnnotation(
            val week: Int,
            val day: Int,
        )

        private data class ExportSnapshot(
            val settings: AppSettingsEntity?,
            val sessions: List<SessionEntity>,
            val promptEvents: List<PromptEventEntity>,
            val reliabilityEvents: List<ReliabilityEventEntity>,
            val riskPersonalization: List<RiskPersonalizationEntity>,
        )
    }
}
