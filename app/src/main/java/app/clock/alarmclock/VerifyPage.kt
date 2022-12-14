package app.clock.alarmclock

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import app.clock.alarmclock.cleint.ApiCleint
import app.clock.alarmclock.models.LoginModels
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call


class VerifyPage : AppCompatActivity() {

    private var ediVerCode: EditText? = null
    private var txtVerTime: TextView? = null
    private var btnVerOk: Button? = null
    private var email: String? = null
    private var token: String? = null
    private var veRey: String? = null
    private var progressVer: ProgressBar? = null
    private var isLoading: Boolean = false
    private var sharedPreferences: SharedPreferences? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_page)

        ediVerCode = findViewById(R.id.ediVerCode)
        txtVerTime = findViewById(R.id.txtVerTime)
        btnVerOk = findViewById(R.id.btnVerOk)
        progressVer = findViewById(R.id.progressVer)
        sharedPreferences = getSharedPreferences("app.clock.alarmClock", MODE_PRIVATE)

        email = intent.getStringExtra("email")
        token = intent.getStringExtra("token")
        veRey = intent.getStringExtra("veRey")
        val gson = Gson()
        codeTimersText()

        btnVerOk?.setOnClickListener {
            val code = ediVerCode?.text.toString()
            if (code.isEmpty()) {
                ediVerCode?.error = "Kodni kiriting"
                ediVerCode?.requestFocus()
                return@setOnClickListener
            }
            if (code != veRey?.split(".")?.get(0)) {
                ediVerCode?.error = "Kod noto'g'ri"
                ediVerCode?.requestFocus()
                return@setOnClickListener
            }
            isLoading = true
            progressVer?.visibility = ProgressBar.VISIBLE
            btnVerOk?.visibility = Button.GONE
            val loginModels = email?.let { it1 -> LoginModels(it1, "") }
            val verifyUser: Call<Any?>? = ApiCleint().userService.verefyuser(loginModels)
            verifyUser?.enqueue(object : retrofit2.Callback<Any?> {
                override fun onResponse(call: Call<Any?>, response: retrofit2.Response<Any?>) {
                    if (response.code() == 400) {
                        isLoading = false
                        progressVer?.visibility = ProgressBar.GONE
                        btnVerOk?.visibility = Button.VISIBLE
                        val json = gson.fromJson(
                            response.errorBody()?.charStream(),
                            JsonObject::class.java
                        )

                        val message = json.get("error").asString
                        if (message == "email is incorrect") {
                            Toast.makeText(this@VerifyPage, "Email noto'g'ri", Toast.LENGTH_SHORT)
                                .show()
                            finish()
                        }
                        if (message == "email is already verified") {
                            Toast.makeText(
                                this@VerifyPage,
                                "Email tasdiqlangan",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }

                    } else {
                        if (response.code() == 200) {
                            isLoading = false
                            progressVer?.visibility = ProgressBar.GONE
                            btnVerOk?.visibility = Button.VISIBLE
                            Toast.makeText(this@VerifyPage, "Siz ro'yxatdan o'tdingiz", Toast.LENGTH_SHORT).show()
                            val json = gson.fromJson(response.body()?.toString(), JsonObject::class.java)
                            token = json.get("token").asString
                            sharedPreferences?.edit()?.putString("token", token)?.apply()
                            startActivity(intent.setClass(this@VerifyPage, Sample::class.java))
                            finish()
                        }
                    }
                }

                override fun onFailure(call: Call<Any?>, t: Throwable) {
                    isLoading = false
                    progressVer?.visibility = ProgressBar.GONE
                    btnVerOk?.visibility = Button.VISIBLE
                    Toast.makeText(this@VerifyPage, "Internet bilan bog'lanishda xatolik", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun codeTimersText() {
        object : CountDownTimer(59000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                txtVerTime?.text = "00:" + millisUntilFinished / 1000
            }

            @SuppressLint("SetTextI18n", "ResourceAsColor")
            override fun onFinish() {
                txtVerTime?.text = "Qayta urinib ko'ring"
                txtVerTime?.setTextColor(R.color.teal_700)
                txtVerTime?.setOnClickListener {
                    codeTimersText()
                    val resendverefy: Call<Any?>? =
                        ApiCleint().userService.resendverefy(email?.let { LoginModels(it, "") })
                    resendverefy?.enqueue(object : retrofit2.Callback<Any?> {
                        override fun onResponse(
                            call: Call<Any?>,
                            response: retrofit2.Response<Any?>
                        ) {
                            if (response.code() == 400) {
                                val json = Gson().fromJson(
                                    response.errorBody()?.charStream(),
                                    JsonObject::class.java
                                )
                                val message = json.get("error").asString
                                if (message == "email is incorrect") {
                                    Toast.makeText(this@VerifyPage, "Email noto'g'ri", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                            } else {
                                if (response.code() == 200) {
                                    val json = Gson().fromJson(
                                        response.body().toString(),
                                        JsonObject::class.java
                                    )
                                    veRey = json.get("verefyCode").asString
                                    Toast.makeText(this@VerifyPage, "Kod yuborildi", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        override fun onFailure(call: Call<Any?>, t: Throwable) {
                            t.printStackTrace()
                        }
                    })
                }
            }
        }.start()
    }

}