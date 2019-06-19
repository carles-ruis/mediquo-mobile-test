package com.carles.mediquomobiletest.model

import com.carles.mediquomobiletest.AppExecutors
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import java.util.concurrent.Executor

class WordRepositoryTest {

    val instantExecutor = Executor { it.run() }
    val repository = WordsRepository(AppExecutors(instantExecutor, instantExecutor))
    val onReadLines: (List<Word>) -> Unit = mockk(relaxed = true)
    val onReadFinished: () -> Unit = mockk(relaxed = true)

    @Test
    fun readFile_executeReadFileAndReadFinishedCallbacks() {
        repository.readFile("first line\nsecond line".byteInputStream(), onReadLines, onReadFinished)
        verify { onReadFinished.invoke() }
        verify(exactly = 2) { onReadLines(any()) }
    }

    @Test
    fun updateWordMap_populateEmptyMap() {
        val expectedMap = linkedMapOf<String, Word>().apply {
            put("first", Word("first", 0, 1))
            put("line", Word("line", 1, 1))
        }
        repository.updateWordMap("first line")
        assert(repository.wordMap.equals(expectedMap))
    }

    @Test
    fun updateWordMap_updateExistingMap() {
        val expectedMap = linkedMapOf<String, Word>().apply {
            put("first", Word("first", 0, 1))
            put("line", Word("line", 1, 2))
            put("second", Word("second", 2, 1))
        }

        repository.wordMap.apply {
            put("first", Word("first", 0, 1))
            put("line", Word("line", 1, 1))
        }
        repository.updateWordMap("second line")
        assert(repository.wordMap.equals(expectedMap))
    }

    @Test
    fun updateWordMap_checkEdgyCases() {
        val expectedMap = linkedMapOf<String, Word>().apply {
            put("first", Word("first", 0, 1))
            put("second", Word("second", 1, 1))
        }
        repository.updateWordMap("First         Second")
        assert(repository.wordMap.equals(expectedMap))
    }
}
