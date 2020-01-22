package com.carles.mediquomobiletest.viewmodel

import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.carles.carleskotlin.common.livedata.SingleLiveEvent
import com.carles.mediquomobiletest.R
import com.carles.mediquomobiletest.model.Word
import com.carles.mediquomobiletest.model.WordsRepository
import java.io.InputStream
import java.util.*

class MainViewModel(application: Application, val repository: WordsRepository) : AndroidViewModel(application) {

    private val insertionComparator = Comparator<Word> { one, other -> one.position.compareTo(other.position) }
    private val timesComparator = Comparator<Word> { one, other -> other.times.compareTo(one.times) }
    private val alphabeticalComparator = Comparator<Word> { one, other -> one.text.compareTo(other.text) }
    private var wordComparator: Comparator<Word>? = null

    val wordsLiveData: MutableLiveData<List<Word>> by lazy { MutableLiveData<List<Word>>().apply { value = emptyList()}  }
    val showActionsLiveData : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>().apply { value = false }}
    val showOpenFileActionLiveData : MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>().apply { value = true }}
    val messageEvent = SingleLiveEvent<Int>()

    fun openFile(inputStream: InputStream) {
        wordComparator = null

        showOpenFileActionLiveData.value = false
        showActionsLiveData.value = false
        wordsLiveData.value = emptyList()
        repository.readFile(inputStream, ::onReadLines, ::onReadFinished)
    }

    @VisibleForTesting
    fun onReadLines(wordsAsList: List<Word>) {
        wordsLiveData.value = wordsAsList
    }

    @VisibleForTesting
    fun onReadFinished() {
        showActionsLiveData.value = true
        showOpenFileActionLiveData.value = true
        messageEvent.value = R.string.main_file_read_message
    }

    fun sortWords(optionId: Int) {
        val wordComparator = when (optionId) {
            R.string.main_sort_insertion -> insertionComparator
            R.string.main_sort_alphabetical -> alphabeticalComparator
            R.string.main_sort_appearances -> timesComparator
            else -> null
        }

        val wordsAsList = wordsLiveData.value
        wordComparator?.let { Collections.sort(wordsAsList, it) }
        wordsLiveData.value = wordsAsList
    }

}
