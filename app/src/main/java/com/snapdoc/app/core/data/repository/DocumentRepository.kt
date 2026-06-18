package com.snapdoc.app.core.data.repository

import com.snapdoc.app.core.data.db.DocumentDao
import com.snapdoc.app.core.data.db.DocumentEntity
import com.snapdoc.app.core.data.db.PageDao
import com.snapdoc.app.core.data.db.PageEntity
import com.snapdoc.app.core.data.model.Document
import com.snapdoc.app.core.data.model.Page
import com.snapdoc.app.core.data.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface DocumentRepository {
    fun observeAll(): Flow<List<Document>>
    fun observeFavorites(): Flow<List<Document>>
    fun observeByCategory(category: String): Flow<List<Document>>
    fun observeById(id: Long): Flow<Document?>
    fun observePages(documentId: Long): Flow<List<Page>>
    suspend fun findById(id: Long): Document?
    suspend fun create(
        filename: String,
        pdfPath: String,
        category: String,
        fileSizeBytes: Long,
        pages: List<NewPage>,
    ): Long
    suspend fun rename(id: Long, filename: String)
    suspend fun setCategory(id: Long, category: String)
    suspend fun reassignCategory(oldName: String, newName: String)
    suspend fun setFavorite(id: Long, favorite: Boolean)
    suspend fun delete(id: Long)
    suspend fun deleteMany(ids: List<Long>)
    suspend fun search(query: String): List<Document>
}

data class NewPage(val imagePath: String, val thumbnailPath: String? = null)

@Singleton
class DocumentRepositoryImpl @Inject constructor(
    private val documents: DocumentDao,
    private val pages: PageDao,
) : DocumentRepository {

    override fun observeAll(): Flow<List<Document>> =
        documents.observeAll().map { list -> list.map(DocumentEntity::toModel) }

    override fun observeFavorites(): Flow<List<Document>> =
        documents.observeFavorites().map { list -> list.map(DocumentEntity::toModel) }

    override fun observeByCategory(category: String): Flow<List<Document>> =
        documents.observeByCategory(category).map { list -> list.map(DocumentEntity::toModel) }

    override fun observeById(id: Long): Flow<Document?> =
        documents.observeById(id).map { it?.toModel() }

    override fun observePages(documentId: Long): Flow<List<Page>> =
        pages.observeForDocument(documentId).map { list -> list.map { it.toModel() } }

    override suspend fun findById(id: Long): Document? =
        documents.findById(id)?.toModel()

    override suspend fun create(
        filename: String,
        pdfPath: String,
        category: String,
        fileSizeBytes: Long,
        pages: List<NewPage>,
    ): Long {
        val now = System.currentTimeMillis()
        val docId = documents.insert(
            DocumentEntity(
                filename = filename,
                pdfPath = pdfPath,
                pageCount = pages.size,
                fileSizeBytes = fileSizeBytes,
                category = category,
                createdAt = now,
                updatedAt = now,
            ),
        )
        if (pages.isNotEmpty()) {
            this.pages.insertAll(
                pages.mapIndexed { index, page ->
                    PageEntity(
                        documentId = docId,
                        pageIndex = index,
                        imagePath = page.imagePath,
                        thumbnailPath = page.thumbnailPath,
                    )
                },
            )
        }
        return docId
    }

    override suspend fun rename(id: Long, filename: String) {
        documents.rename(id, filename, System.currentTimeMillis())
    }

    override suspend fun setCategory(id: Long, category: String) {
        documents.setCategory(id, category, System.currentTimeMillis())
    }

    override suspend fun reassignCategory(oldName: String, newName: String) {
        if (oldName == newName) return
        documents.reassignCategory(oldName, newName, System.currentTimeMillis())
    }

    override suspend fun setFavorite(id: Long, favorite: Boolean) {
        documents.setFavorite(id, favorite, System.currentTimeMillis())
    }

    override suspend fun delete(id: Long) = documents.delete(id)
    override suspend fun deleteMany(ids: List<Long>) = documents.deleteMany(ids)

    override suspend fun search(query: String): List<Document> {
        if (query.isBlank()) return emptyList()
        return documents.search(query.trim()).map(DocumentEntity::toModel)
    }
}
