package com.carles.mediquomobiletest.model

import com.carles.mediquomobiletest.AppExecutors
import java.io.InputStream

class WordsRepository(val appExecutors: AppExecutors) {

    val wordMap = linkedMapOf<String, Word>()

    fun readFile(inputStream: InputStream?, onReadLines: (List<Word>) -> Unit, onReadFinished: () -> Unit) {

        appExecutors.workerThread.execute {
            wordMap.clear()
            inputStream?.bufferedReader()?.readLines()?.forEach { line ->
                updateWordMap(line)

                val wordsAsList = wordMap.values.toList()
                appExecutors.mainThread.execute {
                    onReadLines(wordsAsList)
                }
            }
            appExecutors.mainThread.execute { onReadFinished() }
        }
    }

    fun updateWordMap(line: String) {
        val words = line.split(' ', '.', ',', ':', ';')
        words.map { it.trim().toLowerCase() }.filter { it.isNotEmpty() }.forEach { word ->
            wordMap.get(word)?.apply { times++ } ?: wordMap.put(word, Word(word, wordMap.size))
        }
    }
}