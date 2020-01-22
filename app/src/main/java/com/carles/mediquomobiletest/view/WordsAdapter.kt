package com.carles.mediquomobiletest.view

import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.carles.mediquomobiletest.R
import com.carles.mediquomobiletest.model.Word
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_word.*

class WordsAdapter : RecyclerView.Adapter<WordsAdapter.ViewHolder>(), Filterable {

    val items = arrayListOf<Word>()
    val filteredItems = arrayListOf<Word>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent.inflate(R.layout.item_word))

    override fun getItemCount() = filteredItems.count()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindView(filteredItems[position])
    }

    override fun getFilter() = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filterResults = FilterResults()
            val text = constraint.toString()

            if (text.isEmpty()) {
                filterResults.values = items
            } else {
                val filteredItems = arrayListOf<Word>()
                for (word in items) {
                    if (word.text.startsWith(text.toLowerCase())) filteredItems.add(word)
                }
                filterResults.values = filteredItems
            }

            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredItems.clear()
            filteredItems.addAll(results?.values as ArrayList<Word>)
            notifyDataSetChanged()
        }
    }

    fun setWords(words: List<Word>) {
        items.clear()
        items.addAll(words)
        filteredItems.clear()
        filteredItems.addAll(words)
        notifyDataSetChanged()
    }

    inner class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun onBindView(word: Word) {
            item_word_textview.text = item_word_textview.resources.getQuantityString(
                R.plurals.item_word_description, word.times,
                word.text, word.times
            )
        }
    }
}