package com.sapienapps.christisus.fillstudent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sapienapps.christisus.R
import com.sapienapps.christisus.excelreader.Cell

class StudentInfoAdapter(
    private val dataList: List<StudentInfoViewData>,
    private val friend1ListAdapter: ArrayAdapter<String>,
    private val friend2ListAdapter: ArrayAdapter<String>,
    private val unFriend1ListAdapter: ArrayAdapter<String>,
    private val unFriend2ListAdapter: ArrayAdapter<String>,
    private val onFriend1Selected: (position: Int, text: String) -> Unit,
    private val onFriend2Selected: (position: Int, text: String) -> Unit,
    private val onUnFriend1Selected: (position: Int, text: String) -> Unit,
    private val onUnFriend2Selected: (position: Int, text: String) -> Unit,
) : RecyclerView.Adapter<StudentInfoAdapter.StudentInfoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentInfoViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_student_info, parent, false)
        return StudentInfoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: StudentInfoViewHolder, position: Int) {
        val cell = dataList[position]
        holder.bind(
            cell,
            friend1ListAdapter,
            friend2ListAdapter,
            unFriend1ListAdapter,
            unFriend2ListAdapter,
            onFriend1Selected,
            onFriend2Selected,
            onUnFriend1Selected,
            onUnFriend2Selected,
        )
    }

    override fun getItemCount(): Int = dataList.size

    class StudentInfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val atvFriend1: AutoCompleteTextView = itemView.findViewById(R.id.atvFriend1)
        private val atvFriend2: AutoCompleteTextView = itemView.findViewById(R.id.atvFriend2)
        private val atvUnFriend1: AutoCompleteTextView = itemView.findViewById(R.id.atvUnFriend1)
        private val atvUnFriend2: AutoCompleteTextView = itemView.findViewById(R.id.atvUnFriend2)
        private val name: TextView = itemView.findViewById(R.id.tvStudentName)
        private val firstName: TextView = itemView.findViewById(R.id.tvStudentFirstName)
        private val profile: TextView = itemView.findViewById(R.id.tvStudentProfile)
        private val language: TextView = itemView.findViewById(R.id.tvStudentLanguage)

        fun bind(
            cell: StudentInfoViewData,
            friend1ListAdapter: ArrayAdapter<String>,
            friend2ListAdapter: ArrayAdapter<String>,
            unFriend1ListAdapter: ArrayAdapter<String>,
            unFriend2ListAdapter: ArrayAdapter<String>,
            onFriend1Selected: (position: Int, text: String) -> Unit,
            onFriend2Selected: (position: Int, text: String) -> Unit,
            onUnFriend1Selected: (position: Int, text: String) -> Unit,
            onUnFriend2Selected: (position: Int, text: String) -> Unit,
        ) {
            name.text = cell.name
            firstName.text = cell.firstName
            profile.text = cell.Profile
            language.text = cell.language
            atvFriend1.setText(cell.friend1)
            atvFriend1.setAdapter(friend1ListAdapter)
            atvFriend1.setOnItemClickListener { adapterView, view, i, l ->
                onFriend1Selected(adapterPosition, adapterView.getItemAtPosition(i).toString())
            }
            atvFriend2.setText(cell.friend2)
            atvFriend2.setAdapter(friend2ListAdapter)

            atvFriend2.setOnItemClickListener { adapterView, view, i, l ->
                onFriend2Selected(adapterPosition, adapterView.getItemAtPosition(i).toString())
            }

            atvUnFriend1.setText(cell.unFriend1)
            atvUnFriend1.setAdapter(unFriend1ListAdapter)
            atvUnFriend1.setOnItemClickListener { adapterView, view, i, l ->
                onUnFriend1Selected(adapterPosition, adapterView.getItemAtPosition(i).toString())
            }
            atvUnFriend2.setText(cell.unFriend2)
            atvUnFriend2.setAdapter(unFriend2ListAdapter)
            atvUnFriend2.setOnItemClickListener { adapterView, view, i, l ->
                onUnFriend2Selected(adapterPosition, adapterView.getItemAtPosition(i).toString())
            }
        }
    }
}
