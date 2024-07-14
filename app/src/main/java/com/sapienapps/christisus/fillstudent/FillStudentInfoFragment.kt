package com.sapienapps.christisus.fillstudent

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sapienapps.christisus.MainActivity
import com.sapienapps.christisus.databinding.FragmentFirstBinding
import com.sapienapps.christisus.excelreader.ExcelReader
import com.sapienapps.christisus.utils.ActivityUtils


class FillStudentInfoFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    private val binding get() = _binding!!

    private val pickFile =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                Toast.makeText(requireActivity(), "Please wait while loading content", Toast.LENGTH_SHORT).show()
                val rowData = ExcelReader.readExcelFile(requireActivity(), it)
                fillStudentDataInList(rowData)
            }
        }


    private var studentListInfo = mutableListOf<StudentInfoViewData>()
    private var adapter: StudentInfoAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSelectSheet.setOnClickListener {
            checkPermissions()
        }

        binding.btnSave.setOnClickListener{
            if(studentListInfo.isEmpty()){
                Toast.makeText(requireActivity(), "Please select a file first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            (requireActivity() as MainActivity).selectedList = studentListInfo
            findNavController().navigate(com.sapienapps.christisus.R.id.action_FirstFragment_to_SecondFragment)
        }


        ActivityUtils.setUpUi(view,requireActivity())
    }

    private fun fillStudentDataInList(list: List<List<String>>) {
        // Fill student data in list
        val studentInfoDataList = mutableListOf<StudentInfoViewData>()

        for (cell in list) {
            val studentInfoViewData = StudentInfoViewData(
                name = cell.getOrNull(0) ?: "",
                firstName = cell.getOrNull(1) ?: "",
                Profile = cell.getOrNull(2) ?: "",
                language = cell.getOrNull(3) ?: "",
                friend1 = cell.getOrNull(4) ?: "",
                friend2 = cell.getOrNull(5) ?: "",
                unFriend1 = cell.getOrNull(6) ?: "",
                unFriend2 = cell.getOrNull(7) ?: ""
            )
            studentInfoDataList.add(studentInfoViewData)
        }

        val names = studentInfoDataList.map {
            it.name + " " + it.firstName
        }

        studentInfoDataList.sortBy {
            it.name
        }

        studentListInfo = studentInfoDataList
        adapter = StudentInfoAdapter(
            studentInfoDataList,
            ArrayAdapter(requireActivity(), R.layout.simple_dropdown_item_1line, names),
            ArrayAdapter(requireActivity(), R.layout.simple_dropdown_item_1line, names),
            ArrayAdapter(requireActivity(), R.layout.simple_dropdown_item_1line, names),
            ArrayAdapter(requireActivity(), R.layout.simple_dropdown_item_1line, names),
            { position, text ->
                studentListInfo[position].friend1 = text
                adapter?.notifyItemChanged(position)
            },
            { position, text ->
                studentListInfo[position].friend2 = text
                adapter?.notifyItemChanged(position)
            },
            { position, text ->
                studentListInfo[position].unFriend1 = text
                adapter?.notifyItemChanged(position)
            },
            { position, text ->
                studentListInfo[position].unFriend2 = text
                adapter?.notifyItemChanged(position)
            }
        )

        binding.rvStudentInfo.adapter = adapter
        binding.rvStudentInfo.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvStudentInfo.setHasFixedSize(true)
        adapter?.notifyDataSetChanged()
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 and above
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivityForResult(intent, 101)
            } else {
                pickFile.launch("application/*")
            }
        } else {
            // Below Android 11
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionsLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            } else {
                pickFile.launch("application/*")
            }
        }
    }

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var allGranted = true
            for ((_, isGranted) in permissions) {
                if (!isGranted) {
                    allGranted = false
                    break
                }
            }
            if (allGranted) {
                pickFile.launch("application/*")
            } else {
                Toast.makeText(
                    requireActivity(),
                    "Storage permissions are required to select a file",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
