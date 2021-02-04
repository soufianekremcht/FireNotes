package com.soufianekre.firenotes.data.db

import android.content.Context
import android.graphics.Color
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.soufianekre.firenotes.R
import com.soufianekre.firenotes.data.db.doa.NotesDao
import com.soufianekre.firenotes.data.models.NoteObject
import com.soufianekre.firenotes.helper.AppConstants
import java.util.*
import java.util.concurrent.Executors

@Database(entities = [NoteObject::class], version = 1, exportSchema = false)
abstract class NotesDatabase : RoomDatabase() {


    abstract fun notesDao(): NotesDao

    companion object {

        var instance: NotesDatabase? = null
        fun getInstance(context: Context) {
            if (instance == null) {
                if (instance == null) {
                    synchronized(NotesDatabase::class) {
                        if (instance == null) {
                            instance = Room.databaseBuilder(
                                context.applicationContext,
                                NotesDatabase::class.java, "notes.db"
                            )
                                .addCallback(object : Callback() {
                                    override fun onCreate(db: SupportSQLiteDatabase) {
                                        super.onCreate(db)
                                        insertFirstNote(context)
                                    }
                                })
                                .addMigrations(MIGRATION_1_2)
                                .build()
                            instance!!.openHelper.setWriteAheadLoggingEnabled(true)
                        }
                    }
                }
                return instance!!
            }
        }

        fun destroyInstance() {
            instance = null
        }

        private fun insertFirstNote(context: Context) {

            Executors.newSingleThreadScheduledExecutor().execute {
                val generalNote = context.resources.getString(R.string.general_note)
                val note = NoteObject(
                    1,
                    null,
                    generalNote,
                    "",
                    Date().time,
                    Color.GREEN,
                    AppConstants.NoteType.TYPE_TEXT.value
                )
                instance!!.notesDao().insertNote(note)
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
            }
        }

    }

}