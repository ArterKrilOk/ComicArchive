package space.pixelsg.comicarchive.data.database.typeconverters

import androidx.room.TypeConverter
import kotlinx.datetime.Instant

object InstantTypeConverter {
    @TypeConverter
    fun fromInstant(instant: Instant): Long {
        return instant.toEpochMilliseconds()
    }

    @TypeConverter
    fun toInstant(millis: Long): Instant {
        return Instant.fromEpochMilliseconds(millis)
    }

}