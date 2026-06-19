package com.snapdoc.app.core.data.repository

import com.snapdoc.app.core.data.db.CategoryDao
import com.snapdoc.app.core.data.db.CategoryEntity
import com.snapdoc.app.core.data.db.DocumentDao
import com.snapdoc.app.core.data.model.BuiltInCategory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface CategoryRepository {
    fun observeAll(): Flow<List<CategoryEntity>>
    suspend fun all(): List<CategoryEntity>
    suspend fun add(name: String): Long
    suspend fun rename(id: Long, newName: String)
    suspend fun delete(id: Long)
}

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao,
    private val documents: DocumentDao,
) : CategoryRepository {

    override fun observeAll(): Flow<List<CategoryEntity>> = dao.observeAll()
    override suspend fun all(): List<CategoryEntity> = dao.all()

    override suspend fun add(name: String): Long {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Category name cannot be blank" }
        val existing = dao.all()
        require(existing.none { it.name.equals(trimmed, ignoreCase = true) }) {
            "A category with this name already exists"
        }
        val nextOrder = (existing.maxOfOrNull { it.sortOrder } ?: 0) + 1
        return dao.insert(
            CategoryEntity(name = trimmed, isBuiltIn = false, sortOrder = nextOrder),
        )
    }

    /**
     * Rename a custom folder. Documents reference their folder by name, so the
     * rename also re-tags every document currently in the old folder — without
     * that step the rename would orphan the files. Built-in folders are the
     * AI's sorting targets and stay fixed.
     */
    override suspend fun rename(id: Long, newName: String) {
        val trimmed = newName.trim()
        require(trimmed.isNotEmpty()) { "Category name cannot be blank" }
        val existing = dao.findById(id) ?: return
        require(!existing.isBuiltIn) { "Default categories can't be renamed" }
        if (existing.name == trimmed) return
        require(dao.all().none { it.id != id && it.name.equals(trimmed, ignoreCase = true) }) {
            "A category with this name already exists"
        }
        dao.rename(id, trimmed)
        documents.reassignCategory(existing.name, trimmed, System.currentTimeMillis())
    }

    /**
     * Delete a custom folder and move any documents inside it to "Other"
     * (spec screen 23), so nothing is left pointing at a folder that no
     * longer exists.
     */
    override suspend fun delete(id: Long) {
        val existing = dao.findById(id) ?: return
        if (existing.isBuiltIn) return
        dao.delete(id)
        documents.reassignCategory(
            existing.name,
            BuiltInCategory.Other.displayName,
            System.currentTimeMillis(),
        )
    }
}
