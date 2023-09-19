import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import codsoft.android.daystarter.alarm.AlarmModel

class AlarmDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "alarm_database_new"
    }

    // Alarm table contract
    object AlarmContract {
        const val TABLE_NAME = "alarms"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_TIME_IN_MILLIS = "timeInMillis"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_ALARMS_TABLE = ("CREATE TABLE ${AlarmContract.TABLE_NAME} (" +
                "${AlarmContract.COLUMN_ID} INTEGER PRIMARY KEY," +
                "${AlarmContract.COLUMN_NAME} TEXT," +
                "${AlarmContract.COLUMN_TIME_IN_MILLIS} TEXT)")

        db.execSQL(CREATE_ALARMS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${AlarmContract.TABLE_NAME}")
        onCreate(db)
    }

    fun addAlarm(alarm: AlarmModel): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(AlarmContract.COLUMN_NAME, alarm.name)
        values.put(AlarmContract.COLUMN_TIME_IN_MILLIS, alarm.timeInMillis)

        val id = db.insert(AlarmContract.TABLE_NAME, null, values)
        db.close()
        return id
    }

    fun getAlarm(id: Long): AlarmModel? {
        val db = this.readableDatabase
        val cursor: Cursor?
        try {
            cursor = db.query(
                AlarmContract.TABLE_NAME,
                arrayOf(
                    AlarmContract.COLUMN_ID,
                    AlarmContract.COLUMN_NAME,
                    AlarmContract.COLUMN_TIME_IN_MILLIS
                ),
                "${AlarmContract.COLUMN_ID}=?",
                arrayOf(id.toString()),
                null,
                null,
                null,
                null
            )

            cursor?.moveToFirst()

            val alarm = cursor?.let {
                AlarmModel(
                    it.getInt(it.getColumnIndexOrThrow(AlarmContract.COLUMN_ID)),
                    it.getString(it.getColumnIndexOrThrow(AlarmContract.COLUMN_NAME)),
                    it.getString(it.getColumnIndexOrThrow(AlarmContract.COLUMN_TIME_IN_MILLIS))
                )
            }

            cursor?.close()
            return alarm
        } catch (e: SQLException) {
            return null
        }
    }

    fun getAllAlarms(): ArrayList<AlarmModel> {
        val alarms = ArrayList<AlarmModel>()
        val selectQuery = "SELECT * FROM ${AlarmContract.TABLE_NAME}"
        val db = this.readableDatabase
        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery, null)

            cursor?.use {
                if (it.moveToFirst()) {
                    do {
                        val alarm = AlarmModel(
                            it.getInt(it.getColumnIndexOrThrow(AlarmContract.COLUMN_ID)),
                            it.getString(it.getColumnIndexOrThrow(AlarmContract.COLUMN_NAME)),
                            it.getString(it.getColumnIndexOrThrow(AlarmContract.COLUMN_TIME_IN_MILLIS))
                        )
                        alarms.add(alarm)
                    } while (it.moveToNext())
                }
            }

            cursor?.close()
        } catch (e: SQLException) {
            // Handle any exceptions here
        }

        return alarms
    }

    // Update an existing alarm
    fun updateAlarm(alarm: AlarmModel): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(AlarmContract.COLUMN_NAME, alarm.name)
        values.put(AlarmContract.COLUMN_TIME_IN_MILLIS, alarm.timeInMillis)

        return db.update(
            AlarmContract.TABLE_NAME,
            values,
            "${AlarmContract.COLUMN_ID} = ?",
            arrayOf(alarm.id.toString())
        )
    }

    // Delete an alarm by ID
    fun deleteAlarm(id: Long): Int {
        val db = this.writableDatabase
        return db.delete(
            AlarmContract.TABLE_NAME,
            "${AlarmContract.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
    }
}
