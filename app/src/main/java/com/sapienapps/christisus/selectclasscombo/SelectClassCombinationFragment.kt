package com.sapienapps.christisus.selectclasscombo

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sapienapps.christisus.R
import com.sapienapps.christisus.databinding.FragmentSecondBinding
import com.sapienapps.christisus.utils.ActivityUtils

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnContinue.setOnClickListener {

            if(binding.tilSelectClass.editText?.text.isNullOrEmpty() || binding.tilStudentsPerClass.editText?.text.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val classAmount = binding.tilSelectClass.editText?.text.toString().toInt()

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

        binding.btnCreateClasses.setOnClickListener {

        }
        ActivityUtils.setUpUi(view,requireActivity())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
