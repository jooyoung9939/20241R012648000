package SignIn

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.CountDownTimer
import android.content.Intent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.example.lookatme.FetchDataViewModel
import com.example.lookatme.R
import com.example.lookatme.UserRequest

class AuthActivity : AppCompatActivity() {

    private lateinit var signupButton: Button
    private lateinit var nameInput: EditText
    private lateinit var nicknameInput: EditText
    private lateinit var phoneNumInput: EditText
    private lateinit var sendAuthNumButton: Button
    private lateinit var resendButton: Button
    private lateinit var authNumInput: EditText
    private lateinit var timerText: TextView
    private lateinit var backButton: ImageButton

    private var timer: CountDownTimer? = null

    private lateinit var viewModel: FetchDataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        viewModel = ViewModelProvider(this).get(FetchDataViewModel::class.java)

        initViews()
        setupListeners()
        setupObservers()
    }

    private fun initViews() {
        signupButton = findViewById(R.id.signup_button)
        nameInput = findViewById(R.id.name_input)
        nicknameInput = findViewById(R.id.nickname_input)
        phoneNumInput = findViewById(R.id.phone_num_input)
        sendAuthNumButton = findViewById(R.id.send_auth_num_button)
        resendButton = findViewById(R.id.resend_button)
        authNumInput = findViewById(R.id.auth_num_input)
        timerText = findViewById(R.id.timer_text)
        backButton = findViewById(R.id.back_button1)

        authNumInput.visibility = View.GONE
        timerText.visibility = View.GONE
    }

    private fun setupListeners() {
        sendAuthNumButton.setOnClickListener {
            startAuthProcess()
        }

        resendButton.setOnClickListener {
            startAuthProcess()
        }

        signupButton.setOnClickListener {
            attemptSignup()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        viewModel.userResponse.observe(this, Observer { response ->
            if (response != null) {
                if (response.message.contains("생성되었습니다.")) {
                    Toast.makeText(this, "회원가입 성공", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "회원가입 실패: ${response.message}", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun startAuthProcess() {
        authNumInput.visibility = View.VISIBLE
        sendAuthNumButton.visibility = View.GONE
        resendButton.visibility = View.VISIBLE
        resendButton.isEnabled = true
        timerText.visibility = View.VISIBLE
        startTimer(180000)
    }

    private fun attemptSignup() {
        val name = nameInput.text.toString()
        val nickname = nicknameInput.text.toString()
        val phoneNumber = phoneNumInput.text.toString()

        if (name.isEmpty()) {
            Toast.makeText(this, "이름을 입력해주세요.", Toast.LENGTH_LONG).show()
            return
        }
        if (nickname.isEmpty()) {
            Toast.makeText(this, "닉네임을 입력해주세요.", Toast.LENGTH_LONG).show()
            return
        }
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "전화번호를 입력해주세요.", Toast.LENGTH_LONG).show()
            return
        }

        val loginId = intent.getStringExtra("loginId") ?: ""
        val password = intent.getStringExtra("password") ?: ""
        val request = UserRequest(loginId, password, phoneNumber, nickname)
        viewModel.createUser(request)
    }

    private fun startTimer(timeInMillis: Long) {
        timer?.cancel()  // Cancel any existing timer
        timer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 60000
                val seconds = (millisUntilFinished % 60000) / 1000
                timerText.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                timerText.text = "Time expired"
            }
        }.also { it.start() }  // Start the new timer and assign it to the timer variable
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()  // Ensure the timer is cancelled when the activity is destroyed
    }
}
