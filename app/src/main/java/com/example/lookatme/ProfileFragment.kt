package com.example.lookatme

import SignIn.LoginActivity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val logoutButton: ImageButton = view.findViewById(R.id.logout_button)
        logoutButton.setOnClickListener {
            logout()
        }

        return view
    }

    private fun logout() {
        val context = requireContext()
        TokenManager.clearTokens(context)
        TokenManager.setLoggedIn(context, false)
        startActivity(Intent(context, LoginActivity::class.java))
        activity?.finish()
    }
}
