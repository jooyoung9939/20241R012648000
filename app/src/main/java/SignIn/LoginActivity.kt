package SignIn

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.lookatme.FetchDataViewModel
import com.example.lookatme.MainActivity
import com.example.lookatme.R
import com.example.lookatme.UserRequest

class LoginActivity : AppCompatActivity() {

    private lateinit var loginButton: Button
    private lateinit var toSignupButton: Button
    private lateinit var idInput: EditText
    private lateinit var passwordInput: EditText

    private lateinit var viewModel: FetchDataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel = ViewModelProvider(this).get(FetchDataViewModel::class.java)

        initViews()
        setupListeners()
        setupObservers()
    }

    private fun initViews() {
        loginButton = findViewById(R.id.login_button)
        toSignupButton = findViewById(R.id.to_signup_button)
        idInput = findViewById(R.id.id_input)
        passwordInput = findViewById(R.id.pw_input)
    }

    private fun setupListeners() {
        loginButton.setOnClickListener {
            val loginId = idInput.text.toString()
            val password = passwordInput.text.toString()
            val request = UserRequest(loginId, password)
            viewModel.loginUser(request)
        }

        toSignupButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupObservers() {
        viewModel.userResponse.observe(this, Observer { response ->
            if (response != null) {
                if (response.message.contains("성공")) {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Login failed: ${response.message}", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        return true
    }
}
