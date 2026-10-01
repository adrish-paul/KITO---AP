package com.kito.feature.schedule.data

import com.kito.core.database.AppDB
import com.kito.core.database.entity.ActiveSessionEntity
import com.kito.core.database.entity.StudentElectiveEntity
import com.kito.core.database.entity.StudentEntity
import com.kito.core.database.repository.SectionRepository
import com.kito.core.database.repository.StudentRepository
import com.kito.core.datastore.domain.repository.PrefsRepository
import com.kito.core.network.supabase.request.MissingRollReportRequest
import com.kito.core.sync.data.SyncRemoteDataSource
import com.kito.feature.schedule.domain.model.AvailableSectionsData
import com.kito.feature.schedule.domain.model.ElectiveSlotOption
import com.kito.feature.schedule.domain.model.ManualScheduleConfig
import com.kito.feature.schedule.domain.repository.ManualScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.core.annotation.Provided

class ManualScheduleRepositoryImpl(
    @Provided private val syncRemoteDataSource: SyncRemoteDataSource,
    @Provided private val prefs: PrefsRepository,
    @Provided private val studentRepository: StudentRepository,
    @Provided private val sectionRepository: SectionRepository,
    @Provided private val db: AppDB
) : ManualScheduleRepository {

    override fun observeIsManualSchedule(): Flow<Boolean> =
        prefs.isManualScheduleFlow

    override fun observeManualScheduleConfig(): Flow<ManualScheduleConfig?> =
        combine(
            prefs.isManualScheduleFlow,
            prefs.manualSectionFlow,
            prefs.manualBatchFlow,
            prefs.manualElective1Flow,
            prefs.manualElective2Flow
        ) { isManual, section, batch, el1, el2 ->
            if (isManual && section.isNotBlank()) {
                ManualScheduleConfig(
                    section = section,
                    batch = batch,
                    elective1 = el1,
                    elective2 = el2
                )
            } else null
        }

    override suspend fun checkRollExists(rollNo: String): Boolean {
        if (rollNo.isBlank()) return false
        val student = syncRemoteDataSource.getStudentByRoll(rollNo)
        return student != null
    }

    override suspend fun getAvailableSections(): AvailableSectionsData {
        val metadata = runCatching {
            syncRemoteDataSource.getTimetableMetadata()
        }.getOrDefault(emptyList())

        val allStudentElectives = syncRemoteDataSource.getAllStudentElectives()

        val rawBatches = metadata.map { it.batch }.filter { it.isNotBlank() }.distinct().sorted()
        val batches = rawBatches.ifEmpty { listOf("batch_2", "batch_3", "batch_4") }

        // Global fallback branches extracted from all metadata rows or standard defaults
        val globalBranches = metadata.map { extractBranchName(it.section) }
            .filter { it.isNotBlank() && it.length <= 10 && !it.startsWith("elective", ignoreCase = true) }
            .distinct()
            .sortedNaturally()
            .ifEmpty { listOf("CSE", "CSSE", "CSCE", "IT", "ECE", "ETC", "EEE", "ME", "CE") }

        val branchesByBatch = mutableMapOf<String, List<String>>()
        val coreSectionsByBatchAndBranch = mutableMapOf<String, Map<String, List<String>>>()
        val electiveSlotsByBatch = mutableMapOf<String, List<ElectiveSlotOption>>()

        batches.forEach { batch ->
            val batchRows = metadata.filter { it.batch == batch }
            val coreRows = batchRows.filter { it.source == "core" || !it.source.startsWith("elective_") }
            val branches = coreRows.map { extractBranchName(it.section) }
                .filter { it.isNotBlank() && it.length <= 10 && !it.startsWith("elective", ignoreCase = true) }
                .distinct()
                .sortedNaturally()
                .ifEmpty { globalBranches }
            branchesByBatch[batch] = branches

            val branchMap = mutableMapOf<String, List<String>>()
            branches.forEach { branch ->
                val sections = coreRows
                    .filter { row ->
                        val sec = row.section.trim()
                        val rowBranch = extractBranchName(sec)
                        rowBranch.equals(branch, ignoreCase = true)
                    }
                    .map { it.section.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sortedNaturally()
                    .ifEmpty {
                        // Fallback only if no rows exist in Supabase for this branch yet
                        (1..30).map { "$branch-$it" }
                    }
                branchMap[branch] = sections
            }
            coreSectionsByBatchAndBranch[batch] = branchMap

            val batchElectives = allStudentElectives.filter { it.batch == batch }
            val el1Sections = (batchRows.filter { it.source == "elective_1" }.map { it.section } +
                    batchElectives.map { it.elective_1 }).filter { it.isNotBlank() }.distinct().sortedNaturally()
            val el2Sections = (batchRows.filter { it.source == "elective_2" }.map { it.section } +
                    batchElectives.map { it.elective_2 }).filter { it.isNotBlank() }.distinct().sortedNaturally()

            val slots = mutableListOf<ElectiveSlotOption>()
            if (el1Sections.isNotEmpty()) {
                slots.add(ElectiveSlotOption("elective_1", "Elective 1", el1Sections))
            }
            if (el2Sections.isNotEmpty()) {
                slots.add(ElectiveSlotOption("elective_2", "Elective 2", el2Sections))
            }
            electiveSlotsByBatch[batch] = slots
        }

        return AvailableSectionsData(
            availableBatches = batches,
            branchesByBatch = branchesByBatch,
            coreSectionsByBatchAndBranch = coreSectionsByBatchAndBranch,
            electiveSlotsByBatch = electiveSlotsByBatch
        )
    }

    override suspend fun saveManualSchedule(
        rollNo: String,
        config: ManualScheduleConfig
    ): Result<Unit> = runCatching {
        val activeSession = syncRemoteDataSource.getActiveSessionConfig()

        val coreTimetable = syncRemoteDataSource.getTimetableForStudent(
            section = config.section,
            batch = config.batch
        )

        val elective1Timetable = if (config.elective1.isNotBlank()) {
            syncRemoteDataSource.getTimetableForStudent(
                section = config.elective1,
                batch = config.batch
            ).map { it.copy(source = "elective_1") }
        } else emptyList()

        val elective2Timetable = if (config.elective2.isNotBlank()) {
            syncRemoteDataSource.getTimetableForStudent(
                section = config.elective2,
                batch = config.batch
            ).map { it.copy(source = "elective_2") }
        } else emptyList()

        val allTimetable = coreTimetable + elective1Timetable + elective2Timetable

        // Save to DataStore
        prefs.saveManualSchedule(
            section = config.section,
            batch = config.batch,
            elective1 = config.elective1,
            elective2 = config.elective2
        )

        // Save synthetic student, sections, active session and electives to Room
        // First delete previous sections and electives for a clean slate
        sectionRepository.deleteAllSection()
        db.studentElectiveDao().deleteStudentElective(rollNo)

        studentRepository.insertStudent(
            listOf(
                StudentEntity(
                    roll_no = rollNo,
                    section = config.section,
                    batch = config.batch
                )
            )
        )
        sectionRepository.insertSection(allTimetable)
        db.activeSessionDao().insertActiveSession(
            ActiveSessionEntity(
                academic_year = activeSession.academic_year,
                term_code = activeSession.term_code,
                version = activeSession.version
            )
        )
        if (config.elective1.isNotBlank() || config.elective2.isNotBlank()) {
            db.studentElectiveDao().upsertStudentElective(
                StudentElectiveEntity(
                    roll_no = rollNo,
                    elective_1 = config.elective1,
                    elective_2 = config.elective2,
                    batch = config.batch
                )
            )
        }

        // Post to Supabase missing_roll table in background
        syncRemoteDataSource.reportMissingRoll(
            MissingRollReportRequest(
                roll_no = rollNo,
                section = config.section,
                batch = config.batch,
                elective_1 = config.elective1.ifBlank { null },
                elective_2 = config.elective2.ifBlank { null }
            )
        )
    }

    override suspend fun clearManualSchedule(): Result<Unit> = runCatching {
        prefs.clearManualSchedule()
        sectionRepository.deleteAllSection()
        db.studentElectiveDao().deleteAllStudentElectives()
    }
}

private fun extractBranchName(section: String): String {
    val trimmed = section.trim()
    return when {
        trimmed.contains("-") -> trimmed.substringBefore("-").trim()
        trimmed.contains(" ") -> trimmed.substringBefore(" ").trim()
        else -> trimmed
    }
}

private val naturalSortComparator = Comparator<String> { a, b ->
    val re = Regex("(\\d+)|(\\D+)")
    val aMatches = re.findAll(a).map { it.value }.iterator()
    val bMatches = re.findAll(b).map { it.value }.iterator()
    while (aMatches.hasNext() && bMatches.hasNext()) {
        val aToken = aMatches.next()
        val bToken = bMatches.next()
        val aNum = aToken.toLongOrNull()
        val bNum = bToken.toLongOrNull()
        val res = if (aNum != null && bNum != null) {
            aNum.compareTo(bNum)
        } else {
            aToken.compareTo(bToken, ignoreCase = true)
        }
        if (res != 0) return@Comparator res
    }
    a.length.compareTo(b.length)
}

private fun Iterable<String>.sortedNaturally(): List<String> = sortedWith(naturalSortComparator)

