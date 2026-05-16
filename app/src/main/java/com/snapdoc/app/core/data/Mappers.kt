package com.snapdoc.app.core.data

import com.snapdoc.app.core.data.db.AiSummaryEntity
import com.snapdoc.app.core.data.db.DocumentEntity
import com.snapdoc.app.core.data.db.PageEntity
import com.snapdoc.app.core.data.model.AiSummary
import com.snapdoc.app.core.data.model.Document
import com.snapdoc.app.core.data.model.Page
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }
private val stringList = ListSerializer(String.serializer())

fun DocumentEntity.toModel() = Document(
    id = id,
    filename = filename,
    pdfPath = pdfPath,
    pageCount = pageCount,
    fileSizeBytes = fileSizeBytes,
    category = category,
    createdAt = createdAt,
    updatedAt = updatedAt,
    favorite = favorite,
    passwordProtected = passwordProtected,
)

fun PageEntity.toModel() = Page(
    id = id,
    documentId = documentId,
    pageIndex = pageIndex,
    imagePath = imagePath,
    thumbnailPath = thumbnailPath,
)

fun AiSummaryEntity.toModel() = AiSummary(
    tldr = tldr,
    bullets = json.decodeFromString(stringList, bulletsJson),
    generatedAt = generatedAt,
)

fun AiSummary.encodeBullets(): String = json.encodeToString(stringList, bullets)
