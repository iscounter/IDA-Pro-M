package com.idapro.mobile.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.idapro.mobile.data.database.dao.AnnotationDao
import com.idapro.mobile.data.database.dao.BinaryFileDao
import com.idapro.mobile.data.database.entities.AnnotationEntity
import com.idapro.mobile.data.database.entities.BinaryFileEntity

@Database(
    entities = [BinaryFileEntity::class, AnnotationEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun binaryFileDao(): BinaryFileDao
    abstract fun annotationDao(): AnnotationDao
}

class Converters {
    // Room converters for complex types if needed
}
