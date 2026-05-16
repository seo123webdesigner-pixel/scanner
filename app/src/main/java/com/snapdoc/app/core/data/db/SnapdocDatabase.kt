package com.snapdoc.app.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        DocumentEntity::class,
        PageEntity::class,
        OcrTextEntity::class,
        AiSummaryEntity::class,
        CategoryEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class SnapdocDatabase : RoomDatabase() {
    abstract fun documents(): DocumentDao
    abstract fun pages(): PageDao
    abstract fun ocr(): OcrTextDao
    abstract fun summaries(): AiSummaryDao
    abstract fun categories(): CategoryDao

    companion object {
        const val NAME = "snapdoc.db"
    }
}
