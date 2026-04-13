package org.pakicek.monoforecast.presentation.sdui.dialog.util

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter

class NoFilterArrayAdapter(
    context: Context,
    layoutRes: Int,
    private val allItems: List<String>
) : ArrayAdapter<String>(context, layoutRes, ArrayList(allItems)) {

    private val noFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            return FilterResults().apply {
                values = allItems
                count = allItems.size
            }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            clear()
            addAll(allItems)
            notifyDataSetChanged()
        }

        override fun convertResultToString(resultValue: Any): CharSequence {
            return resultValue.toString()
        }
    }

    override fun getFilter(): Filter = noFilter
}