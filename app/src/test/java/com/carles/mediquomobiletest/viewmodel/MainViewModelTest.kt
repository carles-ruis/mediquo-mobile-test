package com.carles.mediquomobiletest.viewmodel

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.Observer
import com.carles.mediquomobiletest.R
import com.carles.mediquomobiletest.model.Word
import com.carles.mediquomobiletest.model.WordsRepository
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import java.io.InputStream

class MainViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    val repository: WordsRepository = mockk(relaxed = true)
    val observer: Observer<Boolean> = mockk()
    val viewModel = MainViewModel(mockk(), repository)

    @Test
    fun openFile_readFromRepository() {
        val inputStream: InputStream = mockk()
        viewModel.openFile(inputStream)

        verify { repository.readFile(inputStream, any(), any()) }
        assert(viewModel.showOpenFileActionLiveData.value == false)
        assert(viewModel.showActionsLiveData.value == false)
        assert(viewModel.wordsLiveData.value!!.isEmpty())
    }

    @Test
    fun onReadLines_updateLiveData() {
        val words = listOf(Word("first", 1, 0))
        viewModel.onReadLines(words)
        assert(viewModel.wordsLiveData.value!!.equals(words))
    }

    @Test
    fun onReadFinished_updateLiveData() {
        viewModel.onReadFinished()
        assert(viewModel.showActionsLiveData.value == true)
        assert(viewModel.showOpenFileActionLiveData.value == true)
    }

    @Test
    fun sortWords_sortBySelection() {
        val lorem = Word("lorem", 0, 1)
        val ipsum = Word("ipsum", 1, 3)
        val loco = Word("loco", 2, 2)
        viewModel.wordsLiveData.value = listOf(lorem, ipsum, loco)

        viewModel.sortWords(R.string.main_sort_alphabetical)
        val wordsAlphabetical = viewModel.wordsLiveData.value
        assert(wordsAlphabetical!!.equals(listOf(ipsum, loco, lorem)))

        viewModel.sortWords(R.string.main_sort_insertion)
        val wordsByInsertion = viewModel.wordsLiveData.value
        assert(wordsByInsertion!!.equals(listOf(lorem, ipsum, loco)))

        viewModel.sortWords(R.string.main_sort_appearances)
        val wordsByAppearances = viewModel.wordsLiveData.value
        assert(wordsByAppearances!!.equals(listOf(ipsum, loco, lorem)))
    }
}