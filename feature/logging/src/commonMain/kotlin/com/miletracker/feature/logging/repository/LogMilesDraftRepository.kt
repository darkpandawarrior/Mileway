package com.miletracker.feature.logging.repository

import com.miletracker.core.data.dao.LogMilesDraftDao
import com.miletracker.core.data.model.db.LogMilesDraftEntity
import kotlinx.coroutines.flow.Flow

class LogMilesDraftRepository(private val dao: LogMilesDraftDao) {
    fun allDrafts(): Flow<List<LogMilesDraftEntity>> = dao.getAllDrafts()

    suspend fun save(draft: LogMilesDraftEntity) = dao.upsertDraft(draft)

    suspend fun delete(draftId: String) = dao.deleteDraftById(draftId)

    suspend fun deleteAll() = dao.deleteAllDrafts()
}
