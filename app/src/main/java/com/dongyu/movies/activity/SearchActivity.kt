package com.dongyu.movies.activity

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.viewModels
import androidx.core.view.MenuProvider
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.dongyu.movies.R
import com.dongyu.movies.base.BaseActivity
import com.dongyu.movies.databinding.ActivitySearchBinding
import com.dongyu.movies.databinding.ItemListSearchBinding
import com.dongyu.movies.fragment.search.SearchFragment
import com.dongyu.movies.model.search.SearchSuggestItem
import com.dongyu.movies.model.search.SearchUiResult
import com.dongyu.movies.model.search.SearchUiSuggest
import com.dongyu.movies.utils.dp2px
import com.dongyu.movies.viewmodel.SearchViewModel
import com.drake.brv.utils.setDifferModels
import com.drake.brv.utils.setup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchActivity : BaseActivity(), MenuProvider {

    private val binding by lazy {
        ActivitySearchBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<SearchViewModel>()

    private val inputMethodManager by lazy {
        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(0, 0)
        }
        addMenuProvider(this, this)

        initView()

        val searchResult = binding.searchResult
        lifecycleScope.launch {
            viewModel.searchUiState.collect {
                binding.searchSuggest.isInvisible = it !is SearchUiSuggest
                searchResult.isInvisible = it !is SearchUiResult

                if (it is SearchUiResult) {
                    searchResult.getFragment<SearchFragment>().search(it.name)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.searchSuggestState.collectLatest { result ->
                binding.searchSuggest.setDifferModels(result)
            }
        }
    }

    private fun startSearch(v: EditText) {
        v.clearFocus()
        inputMethodManager.hideSoftInputFromWindow(
            v.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
        val text = v.text.toString().trim()
        if (text.isBlank()) {
            return
        }
        viewModel.changeSearchState(SearchUiResult(text))
    }

    private fun initView() {
        binding.searchSuggest.setup {
            addInterfaceType<SearchSuggestItem> {
                R.layout.item_list_search
            }
            onCreate {
                getBinding<ItemListSearchBinding>().tvName.compoundDrawablePadding = 10.dp2px()
            }
            onBind {
                val searchSuggestItem = getModel<SearchSuggestItem>()
                val itemBinding = getBinding<ItemListSearchBinding>()
                itemBinding.apply {
                    val icon =
                        if (searchSuggestItem is SearchSuggestItem.Record) R.drawable.baseline_history_24
                        else R.drawable.baseline_search_24
                    tvName.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)
                    tvName.text = searchSuggestItem.name
                    btnDelete.isVisible = searchSuggestItem is SearchSuggestItem.Record
                }
            }
            R.id.search_item.onClick {
                val name = this.getModel<SearchSuggestItem>().name
                binding.editSearch.apply {
                    setText(name)
                    setSelection(name.length)
                }
                viewModel.changeSearchState(SearchUiResult(name))
            }
            R.id.btn_delete.onClick {
                val model = getModel<SearchSuggestItem.Record>()
                lifecycleScope.launch(Dispatchers.IO) {
                    model.history.delete()
                    runOnUiThread {
                        mutable.removeAt(modelPosition)
                        notifyItemRemoved(modelPosition)
                    }
                }
            }
        }

        val searchEditText = binding.editSearch
        searchEditText.post {
            searchEditText.requestFocus()
        }
        searchEditText.addTextChangedListener {
            if (!searchEditText.hasFocus()) {
                return@addTextChangedListener
            }
            val text = it.toString().trim()
            viewModel.changeSearchState(SearchUiSuggest(text))
        }
        searchEditText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                startSearch(v as EditText)
            }
            false
        }

        searchEditText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                // 显示搜索
                viewModel.changeSearchState(
                    SearchUiSuggest(
                        (v as EditText).text.toString().trim()
                    )
                )
            }
        }

        searchEditText.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_ENTER) {
                v.clearFocus()
                inputMethodManager.hideSoftInputFromWindow(
                    v.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
                val text = (v as EditText).text.toString().trim()
                if (text.isBlank()) {
                    return@setOnKeyListener false
                }
                viewModel.changeSearchState(SearchUiResult(text))
            }
            false
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.add(getString(R.string.search)).apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            setIcon(R.drawable.baseline_search_24)
            setOnMenuItemClickListener {
                startSearch(binding.editSearch)
                true
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }
}