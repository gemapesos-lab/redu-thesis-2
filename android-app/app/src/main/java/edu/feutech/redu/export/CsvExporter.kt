package edu.feutech.redu.export

import android.content.Context
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
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class CsvExporter(
    private val context: Context,
    private val database: ReduDatabase,
) {
    suspend fun exportAll(): File {
        val dir = File(context.cacheDir, "redu-export-${System.currentTimeMillis()}")
        dir.mkdirs()

        val sessions = database.sessionDao().all()
        write(File(dir, "sessions.csv"), sessionsCsv(sessions))
        write(File(dir, "daily_summaries.csv"), dailySummariesCsv(sessions))
        write(File(dir, "prompt_events.csv"), promptEventsCsv(database.promptEventDao().all()))
        write(File(dir, "reliability_events.csv"), reliabilityEventsCsv(database.reliabilityEventDao().all()))
        write(File(dir, "risk_personalization.csv"), riskPersonalizationCsv(database.riskPersonalizationDao().all()))
        return dir
    }

    /**
     * Exports all CSVs and zips them into a single shareable .zip file.
     */
    suspend fun exportAsZip(): File {
        val dir = exportAll()
        val zipFile = File(context.cacheDir, "redu-export.zip")
        zipDirectoryAndDeleteStaging(dir, zipFile)
        return zipFile
    }

    private fun write(file: File, content: String) {
        file.writeText(content)
    }

    companion object {
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

        internal fun sessionsCsv(rows: List<SessionEntity>): String = buildString {
            appendLine("study_code,group,platform,start_ms,end_ms,raw_duration_ms,prompt_excluded_duration_ms,mean_dwell_ms,swipe_count,risk_score,risk_level,sentiment_reliability,nsd_percent,oov_ratio")
            rows.forEach {
                appendLine(listOf(
                    it.studyCode,
                    it.studyGroup.name,
                    it.platform.name,
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
                    it.oovRatio,
                ).joinToString(",") { cell -> csv(cell.toString()) })
            }
        }

        internal fun dailySummariesCsv(rows: List<SessionEntity>): String = buildString {
            appendLine("study_code,date,platform,session_count,mean_duration_ms,mean_dwell_ms,mean_nsd_percent,mean_risk_score,reliable_session_count")
            val summaries = rows.groupBy { Triple(it.studyCode, dateOf(it.startedAtMillis), it.platform) }.map { (key, sessions) ->
                val reliable = sessions.filter { it.sentimentReliability == SentimentReliability.RELIABLE }
                DailySummary(
                    studyCode = key.first,
                    date = key.second.toString(),
                    platform = key.third,
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
                    it.sessionCount,
                    it.meanDurationMillis,
                    it.meanDwellMillis,
                    it.meanNsdPercent ?: "",
                    it.meanRiskScore,
                    it.reliableSessionCount,
                ).joinToString(",") { cell -> csv(cell.toString()) })
            }
        }

        internal fun promptEventsCsv(rows: List<PromptEventEntity>): String = buildString {
            appendLine("study_code,timestamp_ms,session_id,risk_level,prompt_level,action,cooldown_state")
            rows.forEach {
                appendLine(listOf(
                    it.studyCode,
                    it.timestampMillis,
                    it.sessionId ?: "",
                    it.riskLevel.name,
                    it.promptLevel.name,
                    it.action.name,
                    it.cooldownActive,
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
            Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()

        private fun List<SessionEntity>.averageOfLong(selector: (SessionEntity) -> Long): Long =
            if (isEmpty()) 0L else map(selector).average().toLong()

        private fun csv(value: String): String {
            val escaped = value.replace("\"", "\"\"")
            return if (escaped.any { it == ',' || it == '"' || it == '\n' }) "\"$escaped\"" else escaped
        }
    }
}
