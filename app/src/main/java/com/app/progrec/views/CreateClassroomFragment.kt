package com.app.progrec.views


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.app.progrec.R
import com.app.progrec.data.AppRepository
import com.app.progrec.databinding.FragmentCreateClassroomBinding
import com.app.progrec.viewmodels.CreateClassroomViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_create_classroom.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf


class CreateClassroomFragment : Fragment() {

    private val appRepository: AppRepository by inject()

    private lateinit var createClassroomViewModel: CreateClassroomViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        (activity as? AppCompatActivity)?.supportActionBar?.title =
            context?.getString(R.string.create_classroom_title)
        (activity as? AppCompatActivity)?.progresee_toolbar?.menu?.clear()

        val binding: FragmentCreateClassroomBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_create_classroom, container, false)
        (activity as? AppCompatActivity)?.progresee_toolbar?.setOnClickListener(null)
        binding.lifecycleOwner = this
        val arguments = TaskFragmentArgs.fromBundle(arguments!!)
        val classroomId = arguments.classroomId
        val createClassroomViewModel: CreateClassroomViewModel by viewModel {
            parametersOf(
                appRepository,
                classroomId
            )
        }
        this.createClassroomViewModel = createClassroomViewModel
        binding.createClassroomViewModel = createClassroomViewModel


        createClassroomViewModel.getClassroom().observe(viewLifecycleOwner, Observer {
            it?.let {
                (activity as? AppCompatActivity)?.progresee_toolbar?.title =
                    context?.getString(R.string.edit_classroom)
                create_classroom_title.text = context?.getString(R.string.edit_classroom)
                editText_classroom_name.setText(it.name)
                editText_classroom_description.setText(it.description)

            }
        })
        createClassroomViewModel.navigateBackToClassroomFragment.observe(
            viewLifecycleOwner,
            Observer {
                if (it == 0L) {
                    this.findNavController()
                        .navigate(CreateClassroomFragmentDirections.actionCreateClassroomFragmentToClassroomFragment())
                    createClassroomViewModel.onDoneNavigating()
                }
            })

        createClassroomViewModel.showProgressBar.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                hideKeyboard()
                layout_progress_bar_create_classroom.visibility = View.VISIBLE
                save_classroom.isEnabled = false
                save_classroom.text = getString(R.string.saving)
            } else {
                layout_progress_bar_create_classroom.visibility = View.GONE
                save_classroom.isEnabled = true
                save_classroom.text = getString(R.string.save)
            }

        })

        createClassroomViewModel.stringLength.observe(viewLifecycleOwner, Observer {
            if (it == 1) {
                R.string.name_too_long.showSnackBar()
            } else if (it == 2) {
                R.string.name_cant_be_empty.showSnackBar()
            }
        })

        createClassroomViewModel.descriptionStringLength.observe(viewLifecycleOwner, Observer {
            if (it == 1) {
                R.string.description_too_long.showSnackBar()
            } else if (it == 2) {
                R.string.description_cant_be_empty.showSnackBar()
            }
        })
        createClassroomViewModel.showSnackBarHttpError.observe(viewLifecycleOwner, Observer {
            if (it==1) {
                R.string.failed_to_save_classroom.showSnackBar()
                createClassroomViewModel.hideHttpErrorSnackBar()
            } else if (it==2) {
                R.string.network_error.showSnackBar()
                createClassroomViewModel.hideHttpErrorSnackBar()
            }
        })

        return binding.root
    }

    private fun Int.showSnackBar() {
        Snackbar.make(
            activity!!.findViewById(android.R.id.content),
            getString(this),
            Snackbar.LENGTH_LONG
        ).show()
        createClassroomViewModel.snackBarShown()
    }


    private fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

}
