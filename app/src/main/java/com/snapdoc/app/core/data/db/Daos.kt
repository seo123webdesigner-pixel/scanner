package com.snapdoc.app.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Query("SELECT * FROM documents ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE favorite = 1 ORDER BY createdAt DESC")
    fun observeFavorites(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE category = :category ORDER BY createdAt DESC")
    fun observeByCategory(category: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id")
    fun observeById(id: Long): Flow<DocumentEntity?>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun findById(id: Long): DocumentEntity?

    @Insert
    suspend fun insert(document: DocumentEntity): Long

    @Update
    suspend fun update(document: DocumentEntity)

    @Query("UPDATE documents SET favorite = :favorite, updatedAt = :now WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean, now: Long)

    @Query("UPDATE documents SET category = :category, updatedAt = :now WHERE id = :id")
    suspend fun setCategory(id: Long, category: String, now: Long)

    @Query("UPDATE documents SET filename = :filename, updatedAt = :now WHERE id = :id")
    suspend fun rename(id: Long, filename: String, now: Long)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM documents WHERE id IN (:ids)")
    suspend fun deleteMany(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM documents WHERE category = :category")
    suspend fun countInCategory(category: String): Int

    /**
     * Full-text search across filename + OCR content. Returns documents
     * whose filename matches OR whose OCR text contains [query].
     */
    @Query(
        """
        SELECT d.* FROM documents d
        LEFT JOIN ocr_text o ON d.id = o.documentId
        WHERE d.filename LIKE '%' || :query || '%'
           OR o.text     LIKE '%' || :query || '%'
        GROUP BY d.id
        ORDER BY d.createdAt DESC
        """,
    )
    suspend fun search(query: String): List<DocumentEntity>
}

@Dao
interface PageDao {

    @Query("SELECT * FROM pages WHERE documentId = :documentId ORDER BY pageIndex ASC")
    fun observeForDocument(documentId: Long): Flow<List<PageEntity>>

    @Query("SELECT * FROM pages WHERE documentId = :documentId ORDER BY pageIndex ASC")
    suspend fun pagesFor(documentId: Long): List<PageEntity>

    @Insert
    suspend fun insertAll(pages: List<PageEntity>): List<Long>

    @Query("DELETE FROM pages WHERE documentId = :documentId")
    suspend fun deleteForDocument(documentId: Long)
}

@Dao
interface OcrTextDao {

    @Query("SELECT * FROM ocr_text WHERE documentId = :documentId LIMIT 1")
    suspend fun forDocument(documentId: Long): OcrTextEntity?

    @Query("SELECT * FROM ocr_text WHERE documentId = :documentId LIMIT 1")
    fun observeForDocument(documentId: Long): Flow<OcrTextEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(ocr: OcrTextEntity): Long
}

@Dao
interface AiSummaryDao {

    @Query("SELECT * FROM ai_summaries WHERE documentId = :documentId LIMIT 1")
    suspend fun forDocument(documentId: Long): AiSummaryEntity?

    @Query("SELECT * FROM ai_summaries WHERE documentId = :documentId LIMIT 1")
    fun observeForDocument(documentId: Long): Flow<AiSummaryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(summary: AiSummaryEntity): Long

    @Query("DELETE FROM ai_summaries WHERE documentId = :documentId")
    suspend fun deleteForDocument(documentId: Long)
}

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, name ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, name ASC")
    suspend fun all(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: CategoryEntity): Long

    @Query("UPDATE categories SET name = :newName WHERE id = :id AND isBuiltIn = 0")
    suspend fun rename(id: Long, newName: String)

    @Query("DELETE FROM categories WHERE id = :id AND isBuiltIn = 0")
    suspend fun delete(id: Long)

    @Transaction
    suspend fun seedBuiltInIfEmpty() {
        if (all().isEmpty()) {
            insertAll(
                listOf(
                    CategoryEntity(name = "Bills", isBuiltIn = true, sortOrder = 0),
                    CategoryEntity(name = "IDs", isBuiltIn = true, sortOrder = 1),
                    CategoryEntity(name = "Receipts", isBuiltIn = true, sortOrder = 2),
                    CategoryEntity(name = "Notes", isBuiltIn = true, sortOrder = 3),
                    CategoryEntity(name = "Contracts", isBuiltIn = true, sortOrder = 4),
                    CategoryEntity(name = "Other", isBuiltIn = true, sortOrder = 5),
                ),
            )
        }
    }
}
