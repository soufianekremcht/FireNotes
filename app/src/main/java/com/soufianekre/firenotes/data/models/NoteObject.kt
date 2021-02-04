package com.soufianekre.firenotes.data.models

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
) : Parcelable

data class Note(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "value") var value: String,
    @ColumnInfo(name = "type") var type: Int,
    @ColumnInfo(name = "path") var path: String = ""
) {

    fun getNoteStoredValue(): String? {
        return if (path.isNotEmpty()) {
            try {
                File(path).readText()
            } catch (e: FileNotFoundException) {
                null
            }
        } else {
            value
        }
    }
}
