package codsoft.android.daystarter.activities

import AlarmAdapter
import AlarmDatabaseHelper
import android.app.*
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import codsoft.android.daystarter.R
import codsoft.android.daystarter.databinding.ActivityMainBinding
import codsoft.android.daystarter.alarm.AlarmModel
import codsoft.android.daystarter.alarm.AlarmReceiver
import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var binding: ActivityMainBinding? = null
    private var ALARM_NAME = ""
    private var ALARM_TIME = ""
    private lateinit var alarmAdapter: AlarmAdapter

    private val timeUpdateHandler = Handler()
    private val timeUpdateRunnable = object : Runnable {
        override fun run() {
            setTimeInTextView()
            timeUpdateHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "alarm_id"
            val channelName = "Alarm Channel"
            val channelDescription = "Channel for alarm notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance)
            channel.description = channelDescription

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        timeUpdateHandler.post(timeUpdateRunnable)
        setTimeInTextView()
        setImageInImageView()
        setupOnClickListeners()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.rv_alarm)
        val layoutManager = LinearLayoutManager(this)

        val db = AlarmDatabaseHelper(this@MainActivity)

        val alarmList = db.getAllAlarms()

        alarmAdapter = AlarmAdapter(this, alarmList)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = alarmAdapter
    }


    private fun setupOnClickListeners() {
        binding?.ivAdd?.setOnClickListener(this@MainActivity)
    }

    private fun setImageInImageView() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        if (hour in 5..16) {
            binding?.ivTime?.setImageResource(R.drawable.ic_sunny)
        } else if (hour in 17..19) {
            binding?.ivTime?.setImageResource(R.drawable.ic_twilight)
        } else {
            binding?.ivTime?.setImageResource(R.drawable.ic_night)
        }
    }

    private fun setTimeInTextView() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val minuteString = if (minute < 10) "0$minute" else minute.toString()

        val timeString = "$hour:$minuteString"

        binding?.tvTime?.text = timeString
    }

    override fun onDestroy() {
        binding = null
        timeUpdateHandler.removeCallbacks(timeUpdateRunnable)
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun showAlarmNotification(context: Context, alarmName: String, alarmId: Int) {
        val channelId = "alarm_id" // Use the same channel ID you defined in Step 1
        val notificationId = alarmId

        // Create an intent to open your app when the user taps the notification
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_icon)
            .setContentTitle("Alarm")
            .setContentText("Alarm: $alarmName is ringing!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_ALL)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, builder.build())
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.iv_add -> {
                openAddAlarmDialog()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun openAddAlarmDialog() {
        val dialog = Dialog(this@MainActivity)
        dialog.setContentView(R.layout.add_alarm_dialog)

        val closeImageView = dialog.findViewById<ImageView>(R.id.iv_close)
        val alarmNameEditText = dialog.findViewById<EditText>(R.id.et_alarm_name)
        val setTimeButton = dialog.findViewById<Button>(R.id.btn_set_time)
        val addAlarmButton = dialog.findViewById<Button>(R.id.btn_add)
        val timeTextView = dialog.findViewById<TextView>(R.id.tv_time)

        closeImageView.setOnClickListener {
            dialog.cancel()
        }

        setTimeButton.setOnClickListener {
            val setTimeDialog = Dialog(this@MainActivity)
            setTimeDialog.setContentView(R.layout.set_time_dialog)

            val setTimeCloseImageView = setTimeDialog.findViewById<ImageView>(R.id.iv_set_time_close)
            val setTimeHoursEditText = setTimeDialog.findViewById<EditText>(R.id.et_hours)
            val setTimeMinutesEditText = setTimeDialog.findViewById<EditText>(R.id.et_minutes)
            val setTimeDialogSetButton = setTimeDialog.findViewById<Button>(R.id.btn_set)

            setTimeCloseImageView.setOnClickListener {
                setTimeDialog.cancel()
            }

            setTimeDialogSetButton.setOnClickListener {
                val hoursStr = setTimeHoursEditText.text.toString()
                val minutesStr = setTimeMinutesEditText.text.toString()

                if (hoursStr.isEmpty() || minutesStr.isEmpty()) {
                    Toast.makeText(this@MainActivity, "Field cannot be left empty", Toast.LENGTH_SHORT).show()
                } else {
                    val hours = hoursStr.toInt()
                    val minutes = minutesStr.toInt()

                    if (hours in 0..23 && minutes in 0..59) {
                        val formattedHours = String.format("%02d", hours)
                        val formattedMinutes = String.format("%02d", minutes)

                        val selectedTime = "$formattedHours:$formattedMinutes"
                        timeTextView.text = selectedTime
                        setTimeDialog.dismiss()
                    } else {
                        Toast.makeText(this@MainActivity, "Entered time is out of range", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            setTimeDialog.show()
        }

        addAlarmButton.setOnClickListener {
            val alarmName = alarmNameEditText.text.toString()
            val alarmTimeText = timeTextView.text.toString()

            if (alarmName.isEmpty()) {
                Toast.makeText(this@MainActivity, "Please enter an alarm name", Toast.LENGTH_SHORT).show()
            } else if (alarmTimeText.isEmpty()) {
                Toast.makeText(this@MainActivity, "Please set the alarm time", Toast.LENGTH_SHORT).show()
            } else {
                val timeParts = alarmTimeText.split(":")
                val hours = timeParts[0].toInt()
                val minutes = timeParts[1].toInt()

                val calendar = Calendar.getInstance()
                val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                val currentMinute = calendar.get(Calendar.MINUTE)

                if (hours < currentHour || (hours == currentHour && minutes <= currentMinute)) {
                    // The specified time is in the past
                    // Increment the date to the next day
                    calendar.add(Calendar.DAY_OF_YEAR, 1) // Add one day to the current date
                    calendar.set(Calendar.HOUR_OF_DAY, hours)
                    calendar.set(Calendar.MINUTE, minutes)

                    // Now, the calendar object contains the date and time for the next day's alarm
                    val alarmTimeInMillis = calendar.timeInMillis

                    ALARM_NAME = alarmName
                    ALARM_TIME = alarmTimeInMillis.toString()

                    val alarm = AlarmModel(0, name = ALARM_NAME, timeInMillis = ALARM_TIME)
                    val db = AlarmDatabaseHelper(this@MainActivity)
                    val alarmId = db.addAlarm(alarm).toInt()

                    // Schedule the alarm using AlarmManager
                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val alarmIntent = Intent(this@MainActivity, AlarmReceiver::class.java)
                    alarmIntent.putExtra("ALARM_NAME", ALARM_NAME)
                    val pendingIntent = PendingIntent.getBroadcast(
                        this@MainActivity,
                        alarmId,
                        alarmIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )


                    // Set the alarm to trigger at the specified time
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        alarmTimeInMillis,
                        pendingIntent
                    )

                    showAlarmNotification(this, alarm.name, alarmId)

                    alarmAdapter.addData(alarm)
                    alarmAdapter.notifyDataSetChanged()

                    Toast.makeText(this@MainActivity, "Alarm Added Successfully!", Toast.LENGTH_LONG).show()
                } else {
                    // The specified time is in the future or on the same day
                    val alarmTimeInMillis = (hours * 60 + minutes) * 60 * 1000L

                    ALARM_NAME = alarmName
                    ALARM_TIME = alarmTimeInMillis.toString()

                    val alarm = AlarmModel(0, name = ALARM_NAME, timeInMillis = ALARM_TIME)
                    val db = AlarmDatabaseHelper(this@MainActivity)
                    val alarmId = db.addAlarm(alarm).toInt()

                    // Schedule the alarm using AlarmManager
                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val alarmIntent = Intent(this@MainActivity, AlarmReceiver::class.java)
                    alarmIntent.putExtra("ALARM_NAME", ALARM_NAME)
                    val pendingIntent = PendingIntent.getBroadcast(
                        this@MainActivity,
                        alarmId,
                        alarmIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    // Set the alarm to trigger at the specified time
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        alarmTimeInMillis,
                        pendingIntent
                    )

                    showAlarmNotification(this, alarm.name, alarmId)

                    alarmAdapter.addData(alarm)
                    alarmAdapter.notifyDataSetChanged()

                    Toast.makeText(this@MainActivity, "Alarm Added Successfully!", Toast.LENGTH_LONG).show()
                }
                dialog.dismiss()
            }
        }

        dialog.show()
    }

}