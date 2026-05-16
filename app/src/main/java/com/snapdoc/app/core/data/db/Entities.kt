package com.snapdoc.app.core.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "documents",
    indices = [Index("createdAt"), Index("category"), Index("favorite")],
)
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filename: String,
    val pdfPath: String,
    val pageCount: Int,
    val fileSizeBytes: Long,
    val category: String,
    val createdAt: Long,
    val updatedAt: Long,
    val favorite: Boolean = false,
    val passwordProtected: Boolean = false,
)

@Entity(
    tableName = "pages",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("documentId")],
)
data class PageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentId: Long,
    val pageIndex: Int,
    val imagePath: String,
    val thumbnailPath: String?,
)

@Entity(
    tableName = "ocr_text",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("documentId", unique = true)],
)
data class OcrTextEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentId: Long,
    val text: String,
    val extractedAt: Long,
)

@Entity(
    tableName = "ai_summaries",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("documentId", unique = true)],
)
data class AiSummaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentId: Long,
    val tldr: String,
    val bulletsJson: String,
    val generatedAt: Long,
)

@Entity(
    tableName = "categories",
    indices = [Index("name", unique = true)],
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isBuiltIn: Boolean,
    val sortOrder: Int,
)
