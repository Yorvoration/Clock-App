package app.clock.alarmclock

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class LoginPage : AppCompatActivity() {

    var ediLogEmail: EditText? = null
    var ediLogPas: EditText? = null
    var txtLogPass: TextView? = null
    var btnLogLogin: Button? = null
    var txtLogReg: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_login_page)

        ediLogEmail = findViewById(R.id.ediLogEmail)
        ediLogPas = findViewById(R.id.ediLogPas)
        txtLogPass = findViewById(R.id.txtLogPass)
        btnLogLogin = findViewById(R.id.btnLogLogin)
        txtLogReg = findViewById(R.id.txtLogReg)

        btnLogLogin?.setOnClickListener {
            val email = ediLogEmail?.text.toString()
            val password = ediLogPas?.text.toString()
            if (email.isEmpty()) {
                ediLogEmail?.error = "Please enter email"
            } else if (password.isEmpty()) {
                ediLogPas?.error = "Please enter password"
            } else {
                if (email.length < 6){
                    ediLogEmail?.error = "Email must be at least 6 characters"
                } else if (password.length < 4){
                    ediLogPas?.error = "Password must be at least 5 characters"
                } else {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                }
            }
        }

        txtLogReg?.setOnClickListener {
            Toast.makeText(this, "Register", Toast.LENGTH_SHORT).show()
        }
    }

    private fun Login(){

    }
}