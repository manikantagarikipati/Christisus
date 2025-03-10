package com.sapienapps.christisus.selectclasscombo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sapienapps.christisus.MainActivity
import com.sapienapps.christisus.R
import com.sapienapps.christisus.planner.Language
import com.sapienapps.christisus.planner.Profile
import com.sapienapps.christisus.planner.Student
import com.sapienapps.christisus.databinding.FragmentSecondBinding
import com.sapienapps.christisus.planner.ClassPlanner
import com.sapienapps.christisus.planner.ManiClassPlanner
import com.sapienapps.christisus.utils.ActivityUtils
import java.io.File

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SelectClassCombinationFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    var classAdapter: ClassAdapter? = null

    @SuppressLint("QueryPermissionsNeeded")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnContinue.setOnClickListener {

            if(binding.tilSelectClass.editText?.text.isNullOrEmpty() || binding.tilStudentsPerClass.editText?.text.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val classAmount = binding.tilSelectClass.editText?.text.toString().toInt()

            if(classAmount*binding.tilStudentsPerClass.editText?.text.toString().toInt() < (requireActivity() as MainActivity).selectedList.size) {
                Toast.makeText(requireContext(), "Please enter more students per class", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val classNamesList = mutableListOf<ClassInfo>()

            for (i in 0 until classAmount) {
                classNamesList.add(ClassInfo("Class ${i + 1}"))
            }

            classAdapter = ClassAdapter(
                classNamesList,
                { position, text ->
                    classNamesList[position].profile1 = text
                    val profiles = requireContext().resources.getStringArray(R.array.profile_names_1)
                    val profilePosition = profiles.indexOf(text)
                    classNamesList[position].profile1Position = profilePosition
                    classAdapter?.notifyItemChanged(position)
                },
                { position, text ->
                    classNamesList[position].profile2 = text
                    val profiles = requireContext().resources.getStringArray(R.array.profile_names_2)
                    val profilePosition = profiles.indexOf(text)
                    classNamesList[position].profile2Position = profilePosition
                    classAdapter?.notifyItemChanged(position)
                },
                { position, text ->
                    classNamesList[position].profile3 = text
                    val profiles = requireContext().resources.getStringArray(R.array.profile_names_3)
                    val profilePosition = profiles.indexOf(text)
                    classNamesList[position].profile3Position = profilePosition
                    classAdapter?.notifyItemChanged(position)
                },
                { position, text ->
                    classNamesList[position].language = text
                    val languages = requireContext().resources.getStringArray(R.array.language_entries)
                    val languagePosition = languages.indexOf(text)
                    classNamesList[position].languagePosition = languagePosition
                    classAdapter?.notifyItemChanged(position)
                }
            )
            binding.rvClasstName.adapter = classAdapter
            binding.rvClasstName.setHasFixedSize(true)
            binding.rvClasstName.layoutManager = LinearLayoutManager(requireContext())
            classAdapter?.notifyDataSetChanged()
        }

        binding.btnMasterList.setOnClickListener {
            val filePath = (requireActivity() as MainActivity).fileName
            openFile(filePath)
        }
        binding.btnCreateClasses.setOnClickListener {

            val classAmount = binding.tilSelectClass.editText?.text.toString().toInt()
            val studentsPerClass = binding.tilStudentsPerClass.editText?.text.toString().toInt()

            if(classAdapter?.classList.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //show dialog asking for file name
            showFileNameDialog(classAmount, studentsPerClass)
        }

        binding.btnClassKurs.setOnClickListener{
            openFile((requireActivity() as MainActivity).finalFileName)
        }

        ActivityUtils.setUpUi(view,requireActivity())
    }

    private fun showFileNameDialog(classAmount: Int, studentsPerClass: Int) {
        val editText = EditText(context).apply {
            hint = "Enter file name"
        }

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
            addView(editText)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Save File")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val fileName = editText.text.toString().trim()
                if (fileName.isNotEmpty()) {
                    createClassesExcelSheet(classAmount = classAmount, studentsPerClass = studentsPerClass,fileName = fileName)
                } else {
                    Toast.makeText(context, "File name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createClassesExcelSheet(classAmount: Int, studentsPerClass: Int,fileName:String) {
        val allowedProfileCombinations = mutableListOf<List<Profile>>()
        val allowedLanguageCombinations = mutableListOf<List<Language>>()

        classAdapter?.classList?.forEach { classInfo ->
            val profileCombo = buildList {
                classInfo.profile1.let {
                    val info1 = when (it) {
                        "Normal" -> Profile.N
                        "Bilingual" -> Profile.B
                        "Musik" -> Profile.M
                        else -> null
                    }
                    if (info1 != null) {
                        add(info1)
                    }
                }

                classInfo.profile2.let {
                    val info2 = when (it) {
                        "Normal" -> Profile.N
                        "Bilingual" -> Profile.B
                        "Musik" -> Profile.M
                        else -> null
                    }
                    if (info2 != null) {
                        add(info2)
                    }
                }

                classInfo.profile3.let {
                    val info3 = when (it) {
                        "Normal" -> Profile.N
                        "Bilingual" -> Profile.B
                        "Musik" -> Profile.M
                        else -> null
                    }
                    if (info3 != null) {
                        add(info3)
                    }
                }
            }


            val languageComboList = classInfo.language.let {
                when (it) {
                    "French" -> listOf(Language.F)
                    "Latin" -> listOf(Language.L)
                    else -> listOf(Language.F, Language.L)
                }
            }

            allowedProfileCombinations.add(profileCombo)
            allowedLanguageCombinations.add(languageComboList)
        }

        val planner: ClassPlanner = ManiClassPlanner(
            maxClasses = classAmount,
            maxStudentsPerClass = studentsPerClass,
            allowedProfileCombinations = allowedProfileCombinations,
            allowedLanguageCombinations = allowedLanguageCombinations
        )

        planner.assignStudentsToClasses(getStudents())
        planner.optimizeClassAssignments()
        val outputFile = planner.writeResultsToExcel(requireContext(), "",fileName = fileName)
        (requireActivity() as MainActivity).finalFileName = outputFile
        Toast.makeText(requireContext(), "Classes Created Successfully", Toast.LENGTH_SHORT).show()
    }

    private fun openFile(filePath: String?) {
        val file = filePath?.let { it1 -> File(it1) }
        if (file != null && file.exists()) {
            val uri: Uri =
                // For Android N and above, use FileProvider
                FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.provider",
                    file
                )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    uri,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ) // XLSX MIME type
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Check if there's an app to handle this intent
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                requireContext().startActivity(intent)
            } else {
                Toast.makeText(context, "No app found to open Excel file.", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            Toast.makeText(context, "File does not exist.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getStudents():List<Student> {
        val studentList = (requireActivity() as MainActivity).selectedList
        return studentList.map { studentViewData ->
            Student(
                lastName = studentViewData.name,
                firstName = studentViewData.firstName,
                profile = studentViewData.Profile.let {
                    when(it) {
                        "B" -> Profile.B
                        "N" -> Profile.N
                        "M" -> Profile.M
                        else -> Profile.N
                    }
                },
                language = studentViewData.language.let {
                    when(it) {
                        "F" -> Language.F
                        else -> Language.L
                    }
                },
                friendsList = listOfNotNull(
                    studentViewData.friend1.ifEmpty { null },
                    studentViewData.friend2.ifEmpty { null }
                ).toMutableList(),
                nonFriendsList = listOfNotNull(
                    studentViewData.unFriend1.ifEmpty { null },
                    studentViewData.unFriend2.ifEmpty { null }
                ).toMutableList()
            )
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
