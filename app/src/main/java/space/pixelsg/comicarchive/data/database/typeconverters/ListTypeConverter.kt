package space.pixelsg.comicarchive.data.database.typeconverters

import androidx.room.TypeConverter

object ListTypeConverter {
    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return list.joinToString("=-=")
    }

    @TypeConverter
    fun toStringList(string: String): List<String> {
        return string.split("=-=")
    }
}