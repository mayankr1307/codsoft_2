import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import codsoft.android.daystarter.R
import codsoft.android.daystarter.alarm.AlarmModel
import kotlin.collections.ArrayList

class AlarmAdapter(private val context: Context, private val alarms: ArrayList<AlarmModel>) :
    RecyclerView.Adapter<AlarmAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeTextView: TextView = itemView.findViewById(R.id.tv_time)
        val nameTextView: TextView = itemView.findViewById(R.id.tv_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alarm, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alarm = alarms[position]

        // Display alarm time in your desired format (e.g., 12:34 AM/PM)
        val formattedTime = formatTime(alarm.timeInMillis.toLong())
        holder.timeTextView.text = formattedTime

        holder.nameTextView.text = alarm.name
    }

    override fun getItemCount(): Int {
        return alarms.size
    }

    private fun formatTime(timeInMillis: Long): String {
        val hours = (timeInMillis / (60 * 60 * 1000)) % 24
        val minutes = (timeInMillis / (60 * 1000)) % 60

        val formattedHour = if (hours < 10) "0$hours" else hours.toString()
        val formattedMinute = if (minutes < 10) "0$minutes" else minutes.toString()

        return "$formattedHour:$formattedMinute"
    }



    fun addData(alarm: AlarmModel) {
        alarms.add(alarm)
    }

}
