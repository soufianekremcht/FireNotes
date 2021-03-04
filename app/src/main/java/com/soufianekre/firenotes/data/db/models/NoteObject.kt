package com.soufianekre.firenotes.data.db.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.io.File
import java.io.FileNotFoundException

@Entity(tableName = "notes", indices = [(Index(value = ["id"], unique = true))])
@Parcelize
data class NoteObject(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @ColumnInfo(name = "title")
    var title: String? = "",
    @ColumnInfo(name = "content")
    var content: String? = "",
    @ColumnInfo(name = "path")
    var path: String = "",
    @ColumnInfo(name = "date")
    var date: Long? = 0,
    @ColumnInfo(name = "color")
    var color: Int = 0,
    @ColumnInfo(name = "type")
    var type: Int = 0
) : Parcelable{

    fun getNoteStoredValue(): String? {
        return if (path.isNotEmpty()) {
            try {
                File(path).readText()
            } catch (e: FileNotFoundException) {
                null
            }
        } else {
            content
        }
    }
}
