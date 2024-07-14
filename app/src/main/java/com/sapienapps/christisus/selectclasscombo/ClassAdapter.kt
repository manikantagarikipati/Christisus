package com.sapienapps.christisus.selectclasscombo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sapienapps.christisus.R

class ClassAdapter(
    val classList: List<ClassInfo>,
    val onProfile1Selected: (position: Int, text: String) -> Unit,
    val onProfile2Selected: (position: Int, text: String) -> Unit,
    val onProfile3Selected: (position: Int, text: String) -> Unit,
    val onLanguageSelected: (position: Int, text: String) -> Unit,
) : RecyclerView.Adapter<ClassAdapter.ClassInfoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassInfoViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_class_info, parent, false)
        return ClassInfoViewHolder(itemView)
    }

    override fun getItemCount(): Int = classList.size

    override fun onBindViewHolder(holder: ClassInfoViewHolder, position: Int) {
        val cell = classList[position]
        holder.bind(
            cell,
            onProfile1Selected,
            onProfile2Selected,
            onProfile3Selected,
            onLanguageSelected,
        )
    }

    class ClassInfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val name: TextView = itemView.findViewById(R.id.tvClasstName)
        private val profile1: Spinner = itemView.findViewById(R.id.spinnerProfile1)
        private val profile2: Spinner = itemView.findViewById(R.id.spinnerProfile2)
        private val profile3: Spinner = itemView.findViewById(R.id.spinnerProfile3)
        private val language: Spinner = itemView.findViewById(R.id.language_spinner)


        fun bind(
            cell: ClassInfo,
            onProfile1Selected: (position: Int, text: String) -> Unit,
            onProfile2Selected: (position: Int, text: String) -> Unit,
            onProfile3Selected: (position: Int, text: String) -> Unit,
            onLanguageSelected: (position: Int, text: String) -> Unit,
        ) {
            name.text = cell.name
            profile1.setSelection(cell.profile1Position)
            profile2.setSelection(cell.profile2Position)
            profile3.setSelection(cell.profile3Position)
            language.setSelection(cell.languagePosition)
            profile1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    onProfile1Selected(adapterPosition, parent?.getItemAtPosition(position).toString())
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Do nothing
                }
            }

            profile2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    onProfile2Selected(adapterPosition, parent?.getItemAtPosition(position).toString())
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Do nothing
                }
            }

            profile3.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    onProfile3Selected(adapterPosition, parent?.getItemAtPosition(position).toString())
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Do nothing
                }
            }

            language.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    onLanguageSelected(adapterPosition, parent?.getItemAtPosition(position).toString())
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Do nothing
                }
            }
        }
    }


}
