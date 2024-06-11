package profile

import api.TokenManager
import signin.LoginActivity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.lookatme.R

class ProfileFragment : Fragment() {

    private lateinit var nicknameTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        nicknameTextView = view.findViewById(R.id.profile_fragment_nickname)

        val logoutButton: ImageButton = view.findViewById(R.id.logout_button)
        logoutButton.setOnClickListener {
            logout()
        }

        displayNickname()

        return view
    }

    private fun displayNickname() {
        val nickname = TokenManager.getNickname(requireContext())
        nicknameTextView.text = nickname ?: "Guest"
    }

    private fun logout() {
        val context = requireContext()
        TokenManager.clearTokens(context)
        TokenManager.setLoggedIn(context, false)
        startActivity(Intent(context, LoginActivity::class.java))
        activity?.finish()
    }
}
