package com.snapdoc.app.core.data.repository

import com.snapdoc.app.core.data.db.CategoryDao
import com.snapdoc.app.core.data.db.CategoryEntity
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
) : CategoryRepository {

    override fun observeAll(): Flow<List<CategoryEntity>> = dao.observeAll()
    override suspend fun all(): List<CategoryEntity> = dao.all()

    override suspend fun add(name: String): Long {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Category name cannot be blank" }
        val nextOrder = (dao.all().maxOfOrNull { it.sortOrder } ?: 0) + 1
        return dao.insert(
            CategoryEntity(name = trimmed, isBuiltIn = false, sortOrder = nextOrder),
        )
    }

    override suspend fun rename(id: Long, newName: String) {
        val trimmed = newName.trim()
        require(trimmed.isNotEmpty()) { "Category name cannot be blank" }
        dao.rename(id, trimmed)
    }

    override suspend fun delete(id: Long) = dao.delete(id)
}
