package com.snapdoc.app.core.data.repository

import com.snapdoc.app.core.data.db.AiSummaryDao
import com.snapdoc.app.core.data.db.AiSummaryEntity
import com.snapdoc.app.core.data.encodeBullets
import com.snapdoc.app.core.data.model.AiSummary
import com.snapdoc.app.core.data.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface SummaryRepository {
    suspend fun summaryFor(documentId: Long): AiSummary?
    fun observeSummaryFor(documentId: Long): Flow<AiSummary?>
    suspend fun save(documentId: Long, summary: AiSummary)
    suspend fun deleteFor(documentId: Long)
}

@Singleton
class SummaryRepositoryImpl @Inject constructor(
    private val dao: AiSummaryDao,
) : SummaryRepository {

    override suspend fun summaryFor(documentId: Long): AiSummary? =
        dao.forDocument(documentId)?.toModel()

    override fun observeSummaryFor(documentId: Long): Flow<AiSummary?> =
        dao.observeForDocument(documentId).map { it?.toModel() }

    override suspend fun save(documentId: Long, summary: AiSummary) {
        dao.upsert(
            AiSummaryEntity(
                documentId = documentId,
                tldr = summary.tldr,
                bulletsJson = summary.encodeBullets(),
                generatedAt = summary.generatedAt,
            ),
        )
    }

    override suspend fun deleteFor(documentId: Long) = dao.deleteForDocument(documentId)
}
