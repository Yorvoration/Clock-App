package app.clock.alarmclock

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import app.clock.alarmclock.adapters.DataAdapters
import app.clock.alarmclock.cleint.ApiCleint
import app.clock.alarmclock.models.GetTimes
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import java.text.SimpleDateFormat
import java.util.*

class Sample : AppCompatActivity() {

    private var sharedPreferences: SharedPreferences? = null
    private var token = ""
    var listSample: ListView? = null
    private var dataAdapters: DataAdapters? = null
    private var timeList: ArrayList<GetTimes>? = null
    private var imgSampleSet: ImageView? = null
    private var progressSample: ProgressBar? = null
    private var floatRefresh: FloatingActionButton? = null
    private var floatAdd: FloatingActionButton? = null
    var times = JSONArray()
    var switchS = JSONArray()

    @SuppressLint("MissingInflatedId", "UnspecifiedImmutableFlag", "InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_sample)

        listSample = findViewById(R.id.listSample)
        progressSample = findViewById(R.id.progressSampe)
        imgSampleSet = findViewById(R.id.imgSamleSet)
        floatRefresh = findViewById(R.id.floatRefresh)
        floatAdd = findViewById(R.id.floatAdd)


//        Handler(Looper.getMainLooper()).postDelayed({
//            val intent = Intent(this, Resiver::class.java)
//            val pendingIntent = PendingIntent.getBroadcast(this, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE)
//            alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
//            alarmManager!!.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 0, pendingIntent)
//
//        }, 3000)
        sharedPreferences = getSharedPreferences("app.clock.alarmClock", MODE_PRIVATE)
        token = sharedPreferences?.getString("token", "")!!

        timeList = ArrayList()
        dataAdapters = DataAdapters(this, timeList!!)
        listSample?.adapter = dataAdapters
        listSample?.divider = null
        listSample?.dividerHeight = 20

        getAllTimes()

        imgSampleSet?.setOnClickListener {
            startActivity(Intent(this, SettingsPage::class.java))
        }

        floatRefresh?.setOnClickListener {
            timeList?.clear()
            dataAdapters?.notifyDataSetChanged()
            getAllTimes()
        }
        floatRefresh?.setOnLongClickListener {

            Toast.makeText(this, "Refresh", Toast.LENGTH_SHORT).show()
            true
        }
        floatAdd?.setOnClickListener {
            addTime()
        }
    }

    private fun getAllTimes() {
        val getTime: Call<Any?>? = ApiCleint().userService.gettimes("Bearer $token")
        getTime?.enqueue(object : retrofit2.Callback<Any?> {
            @SuppressLint("NewApi")
            override fun onResponse(call: Call<Any?>, response: retrofit2.Response<Any?>) {
                if (response.isSuccessful) {
                    listSample?.visibility = View.VISIBLE
                    progressSample?.visibility = View.GONE
                    val gson = Gson()
                    val json = gson.toJson(response.body())
                    val jsonObject = JSONObject(json)
                    times = jsonObject.getJSONArray(getString(R.string.time))
                    switchS = jsonObject.getJSONArray(getString(R.string.switchCheck))
                    val comments = jsonObject.getJSONArray(getString(R.string.comment))
                    for (i in 0 until times.length()) {
                        val time = times.getString(i)
                        val switchBox = switchS.getString(i)
                        val coMntS = comments.getString(i)
                        timeList?.add(GetTimes(time, coMntS, switchBox))
                        dataAdapters?.notifyDataSetChanged()
                    }
                    dataAdapters?.notifyDataSetChanged()
                    startAlarm()
                } else {
                    Toast.makeText(this@Sample, "Error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Any?>, t: Throwable) {
            }
        })
    }


    @SuppressLint("InflateParams", "MissingInflatedId", "NewApi")
    private fun addTime() {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.add_item, null)

        val ediSamComment = view.findViewById<EditText>(R.id.ediSamComment)
        val btnSamAdd = view.findViewById<Button>(R.id.btnSamAdd)
        val digitalClock = view.findViewById<TimePicker>(R.id.digitalClock)

        digitalClock.setIs24HourView(true)
        val addDialog = AlertDialog.Builder(this)
        addDialog.setView(view)
        val dialog = addDialog.create()

        btnSamAdd.setOnClickListener {

            val hour = digitalClock.hour
            val minute = digitalClock.minute
            var comment = ediSamComment.text.toString()

            if (comment.isEmpty()) {
                comment = "No Comment"
            }

            onTimeSet(hour, minute, comment)
            dialog.dismiss()

            /*val addTime: Call<Any?>? =
                ApiCleint().userService.addtime("Bearer $token", GetTimes(time, comment, "false"))
            addTime?.enqueue(object : retrofit2.Callback<Any?> {
                override fun onResponse(call: Call<Any?>, response: retrofit2.Response<Any?>) {
                    if (response.isSuccessful) {
                        timeList?.clear()
                        dataAdapters?.notifyDataSetChanged()
                        getAllTimes()
                        dialog.dismiss()
                    } else {
                        dialog.dismiss()
                        Toast.makeText(this@Sample, "Error", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Any?>, t: Throwable) {
                }
            })*/
        }

        dialog.show()
    }

    @SuppressLint("ShortAlarm")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun startAlarm() {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        for (i in 0 until times.length()) {
            if (switchS.getString(i) == "true") {
                val time = times.getString(i)
                var hour: String?
                var minute: String?
                val timeSplit = time.split(":")
                hour = timeSplit[0]
                minute = timeSplit[1]
                Toast.makeText(this, "$hour:$minute", Toast.LENGTH_SHORT).show()
                val calendar = Calendar.getInstance()
                calendar[Calendar.MINUTE] = minute.toInt()
                calendar[Calendar.HOUR_OF_DAY] = hour.toInt()

                if (calendar.timeInMillis < System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }


                Toast.makeText(this, "$hour:$minute", Toast.LENGTH_SHORT).show()

                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

                val alarmClockInfo =
                    AlarmClockInfo(calendar.timeInMillis, getAlarmInfoPendingIntent())

                alarmManager.setAlarmClock(alarmClockInfo, getAlarmActionPendingIntent())
                Toast.makeText(this, "Ajoyib " + sdf.format(calendar.time), Toast.LENGTH_SHORT)
                    .show()
            }

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }

    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getAlarmInfoPendingIntent(): PendingIntent? {
        val alarmInfoIntent = Intent(this, MainActivity::class.java)
        alarmInfoIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getActivity(
            this,
            0,
            alarmInfoIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getAlarmActionPendingIntent(): PendingIntent? {
        val intent = Intent(this, AlarmActvity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun onTimeSet(hourOfDay: Int, minute: Int,coment:String) {
        val time = "$hourOfDay:$minute"
        val addTime: Call<Any?>? =
            ApiCleint().userService.addtime("Bearer $token", GetTimes(time, coment, "false"))
        addTime?.enqueue(object : retrofit2.Callback<Any?> {
            override fun onResponse(call: Call<Any?>, response: retrofit2.Response<Any?>) {
                if (response.isSuccessful) {
                    timeList?.clear()
                    dataAdapters?.notifyDataSetChanged()
                    getAllTimes()
                } else {
                    Toast.makeText(this@Sample, "Error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Any?>, t: Throwable) {
            }
        })
    }

}