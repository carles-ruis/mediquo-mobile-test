package com.carles.mediquomobiletest.view

import android.arch.lifecycle.Observer
import android.content.Intent
import android.content.Intent.*
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.SearchView
import android.support.v7.widget.SimpleItemAnimator
import android.view.Menu
import android.view.MenuItem
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.carles.mediquomobiletest.R
import com.carles.mediquomobiletest.model.Word
import com.carles.mediquomobiletest.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val SHORT_TEXTFILE = R.raw.short_textfile
    private val LARGE_TEXTFILE = R.raw.large_textfile
    private val STATE_IS_SEARCHING = "state_is_searching"
    private val STATE_SEARCH_QUERY = "state_search_query"

    val viewModel by viewModel<MainViewModel>()
    private val adapter = WordsAdapter()
    private lateinit var searchView: SearchView
    private var savedQueryState: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        viewModel.wordsLiveData.observe(this, Observer<List<Word>> { words -> showWords(words ?: emptyList()) })
        viewModel.messageEvent.observe(this, Observer { messageId -> showMessage(messageId ?: 0) })
    }

    private fun initViews() {
        setSupportActionBar(main_toolbar)
        main_toolbar.setNavigationOnClickListener { finish() }

        main_recyclerview.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        (main_recyclerview.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        main_recyclerview.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val searchItem = menu?.findItem(R.id.main_menu_search)
        searchView = searchItem?.actionView as SearchView
        searchView.isSubmitButtonEnabled = false
        searchView.queryHint = getString(R.string.main_menu_search_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true

            override fun onQueryTextChange(query: String?) = consume { adapter.filter.filter(query) }
        })
        if (savedQueryState != null) {
            searchItem.expandActionView()
            searchView.setQuery(savedQueryState, false)
            searchView.clearFocus()
        }
        viewModel.showActionsLiveData.observe(this, Observer<Boolean> { visible -> setActionsVisible(visible ?: false) })
        viewModel.showOpenFileActionLiveData.observe(this, Observer<Boolean> { visible -> setOpenFileActionVisible(visible ?: true) })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?) =
        when (item?.itemId) {
            R.id.main_menu_search -> true
            R.id.main_menu_sort -> consume { showSortOptions() }
            R.id.main_menu_open_file -> consume { showOpenFileDialog() }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK) {
            val uri = data?.data
            if (uri == null || MimeTypeMap.getSingleton().getExtensionFromMimeType(application.contentResolver.getType(uri)) != "txt") {
                showError(R.string.main_file_reading_error)
            } else {
                application.contentResolver.openInputStream(uri)?.let { viewModel.openFile(it) }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putBoolean(STATE_IS_SEARCHING, !searchView.isIconified)
        outState?.putString(STATE_SEARCH_QUERY, searchView.query.toString())
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        savedQueryState =
            if (savedInstanceState?.getBoolean(STATE_IS_SEARCHING) ?: false) savedInstanceState?.getString(STATE_SEARCH_QUERY) ?: "" else null
    }

    override fun onBackPressed() {
        main_toolbar.collapseActionView()
        super.onBackPressed()
    }

    private fun showSortOptions() {
        main_toolbar.collapseActionView()
        val options = listOf(R.string.main_sort_insertion, R.string.main_sort_alphabetical, R.string.main_sort_appearances)
        AlertDialog.Builder(this).setTitle(R.string.main_sort_title)
            .setItems(getStrings(options)) { _, which -> viewModel.sortWords(options[which]) }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .setCancelable(false)
            .show()
    }

    private fun showWords(words: List<Word>) {
        adapter.setWords(words)
    }

    private fun showError(resourceId: Int) {
        Toast.makeText(this, getString(R.string.error_title) + getString(resourceId), Toast.LENGTH_LONG).show()
    }

    private fun showMessage(messageId: Int) {
        Toast.makeText(this, messageId, Toast.LENGTH_LONG).show()
    }

    private fun setActionsVisible(visible: Boolean) {
        main_toolbar.menu.findItem(R.id.main_menu_sort).setVisible(visible)
        main_toolbar.menu.findItem(R.id.main_menu_search).setVisible(visible)
    }

    private fun setOpenFileActionVisible(visible: Boolean) {
        main_toolbar.menu.findItem(R.id.main_menu_open_file).setVisible(visible)
    }

    private fun showOpenFileDialog(cancelable: Boolean = true) {
        main_toolbar.collapseActionView()
        val options = listOf(R.string.main_open_short_file, R.string.main_open_large_file, R.string.main_open_chosen_file)
        val builder = AlertDialog.Builder(this).setTitle(R.string.main_open_file_title).setCancelable(false)
            .setItems(getStrings(options)) { _, which ->
                when (options[which]) {
                    R.string.main_open_short_file -> viewModel.openFile(resources.openRawResource(SHORT_TEXTFILE))
                    R.string.main_open_large_file -> viewModel.openFile(resources.openRawResource(LARGE_TEXTFILE))
                    R.string.main_open_chosen_file -> showLocalFileChooser()
                }
            }
        if (cancelable) {
            builder.setNegativeButton(R.string.cancel) { _, _ -> }
        }
        builder.show()
    }

    private fun showLocalFileChooser() {
        val intent = Intent(ACTION_GET_CONTENT).setType("text/plain").putExtra(EXTRA_LOCAL_ONLY, true)
        if (intent.resolveActivity(packageManager) == null) {
            showError(R.string.main_no_file_manager)
        } else {
            startActivityForResult(createChooser(intent, getString(R.string.main_file_chooser_title)), FILE_CHOOSER_REQUEST_CODE)
        }
    }
}
