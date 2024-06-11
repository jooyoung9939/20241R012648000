package signin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import api.FetchDataViewModel
import com.example.lookatme.R

class SignupActivity : AppCompatActivity() {

    private lateinit var idInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var passwordReInput: EditText
    private lateinit var doubleCheckButton: Button
    private lateinit var identityVerificationButton: Button
    private lateinit var backButton: ImageButton
    private var isIdAvailable: Boolean = false

    private lateinit var viewModel: FetchDataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        viewModel = ViewModelProvider(this).get(FetchDataViewModel::class.java)

        initViews()
        setupListeners()
        setupObservers()
    }

    private fun initViews() {
        idInput = findViewById(R.id.id_input)
        passwordInput = findViewById(R.id.pw_input)
        passwordReInput = findViewById(R.id.pw_re_input)
        doubleCheckButton = findViewById(R.id.double_check_button)
        identityVerificationButton = findViewById(R.id.identity_verification_button)
        backButton = findViewById(R.id.back_button)
    }

    private fun setupListeners() {
        doubleCheckButton.setOnClickListener {
            val loginId = idInput.text.toString()
            viewModel.checkIdAvailability(loginId)
        }

        identityVerificationButton.setOnClickListener {
            if (!isIdAvailable) {
                Toast.makeText(this, "중복 확인을 해주세요.", Toast.LENGTH_LONG).show()
            } else if (verifyPasswords(passwordInput.text.toString(), passwordReInput.text.toString())) {
                val intent = Intent(this, AuthActivity::class.java)
                intent.putExtra("loginId", idInput.text.toString())
                intent.putExtra("password", passwordInput.text.toString())
                startActivity(intent)
            }
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        viewModel.duplicateResponse.observe(this, Observer { response ->
            if (response != null) {
                if (response.message.contains("로그인 가능합니다.")) {
                    Toast.makeText(this, "사용 가능한 아이디입니다.", Toast.LENGTH_LONG).show()
                    isIdAvailable = true
                } else {
                    Toast.makeText(this, "이미 사용 중인 아이디입니다.", Toast.LENGTH_LONG).show()
                    isIdAvailable = false
                }
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        return true
    }

    private fun verifyPasswords(password: String, confirmPassword: String): Boolean {
        if (password == confirmPassword) {
            Toast.makeText(this, "비밀번호가 일치합니다.", Toast.LENGTH_LONG).show()
            return true
        } else {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_LONG).show()
            return false
        }
    }
}
