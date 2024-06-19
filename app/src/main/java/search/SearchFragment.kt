package search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lookatme.R
import lookbook.LookBookDetailFragment

class SearchFragment : Fragment() {

    private lateinit var viewModel: SearchViewModel
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var searchBar: EditText

    private var cursor: Int? = null
    private var hasNext: Boolean = true
    private var keyword: String = ""
    private var isLoading: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        searchBar = view.findViewById(R.id.searchbar_text)
        searchRecyclerView = view.findViewById(R.id.search_lookbook_gridview)
        viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)

        setupRecyclerView()
        observeViewModel()

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                keyword = s.toString()
                resetSearch()
                searchLookBook()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        searchLookBook()

        return view
    }

    private fun setupRecyclerView() {
        searchAdapter = SearchAdapter(mutableListOf()) { lookbookId ->
            openLookBookDetail(lookbookId)
        }
        searchRecyclerView.adapter = searchAdapter
        searchRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        searchRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!hasNext || isLoading) return

                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (totalItemCount <= (lastVisibleItemPosition + 2)) {
                    searchLookBook()
                }
            }
        })
    }

    private fun observeViewModel() {
        viewModel.searchResults.observe(viewLifecycleOwner, Observer { response ->
            response?.let {
                if (cursor == null) {
                    searchAdapter.setItems(it.lookBookCollection)
                } else {
                    searchAdapter.addItems(it.lookBookCollection)
                }
                cursor = it.cursorPaginationMetaData.cursor
                hasNext = it.cursorPaginationMetaData.hasNext
                isLoading = false
            }
        })
    }

    private fun resetSearch() {
        cursor = null
        hasNext = true
        isLoading = false
        searchAdapter.clearItems()
    }

    private fun searchLookBook() {
        if (!hasNext || isLoading) return
        isLoading = true
        viewModel.searchLookBook(15, cursor ?: 0, keyword)
    }

    private fun openLookBookDetail(lookbookId: Int) {
        val fragment = LookBookDetailFragment.newInstance(lookbookId, false)
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_container, fragment)
            .addToBackStack(null)
            .commit()
    }

}
