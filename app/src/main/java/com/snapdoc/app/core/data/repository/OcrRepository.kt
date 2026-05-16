package com.snapdoc.app.core.data.repository

import com.snapdoc.app.core.data.db.OcrTextDao
import com.snapdoc.app.core.data.db.OcrTextEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface OcrRepository {
    suspend fun textFor(documentId: Long): String?
    fun observeTextFor(documentId: Long): Flow<String?>
    suspend fun save(documentId: Long, text: String)
}

@Singleton
class OcrRepositoryImpl @Inject constructor(
    private val dao: OcrTextDao,
) : OcrRepository {

    override suspend fun textFor(documentId: Long): String? =
        dao.forDocument(documentId)?.text

    override fun observeTextFor(documentId: Long): Flow<String?> =
        dao.observeForDocument(documentId).map { it?.text }

    override suspend fun save(documentId: Long, text: String) {
        dao.upsert(
            OcrTextEntity(
                documentId = documentId,
                text = text,
                extractedAt = System.currentTimeMillis(),
            ),
        )
    }
}
