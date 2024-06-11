package com.example.lookatme

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), ClosetFragment.OnToAddClothesButtonClickListener {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var addLookBookButton: ImageButton
    private lateinit var darkOverlay: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottom_navigation_view)
        addLookBookButton = findViewById(R.id.add_lookbook_button)
        darkOverlay = findViewById(R.id.dark_overlay)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.fragment_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                R.id.fragment_search -> {
                    loadFragment(SearchFragment())
                    true
                }
                R.id.fragment_closet -> {
                    loadFragment(ClosetFragment())
                    true
                }
                R.id.fragment_clip -> {
                    loadFragment(ClipFragment())
                    true
                }
                else -> false
            }
        }

        addLookBookButton.setOnClickListener {
            loadFragment(AddLookBookFragment())
        }

        // Load the default fragment
        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.fragment_profile
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, fragment)
            .commit()
    }

    override fun onToAddClothesButtonClicked() {
        darkOverlay.visibility = View.VISIBLE
    }

    override fun onBottomSheetDismissed() {
        darkOverlay.visibility = View.GONE
    }
}
