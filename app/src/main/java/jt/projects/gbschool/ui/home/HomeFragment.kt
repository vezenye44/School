package jt.projects.gbschool.ui.home

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.school.databinding.FragmentHomeBinding
import jt.projects.gbschool.model.Lesson
import jt.projects.gbschool.utils.showSnackbar
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by inject()
    private val classesAdapter by lazy { ClassesHomeAdapter(::onItemClicked) }
    private val homeworkAdapter by lazy { HomeworkAdapter() }

    private fun onItemClicked(data: Lesson) {
        try {
            val sky = Intent(Intent.ACTION_VIEW)
            sky.data = Uri.parse("skype:${data.teacher}?call&video=true")
            val chosenIntent = Intent.createChooser(sky, "Выберите программу")
            startActivity(chosenIntent)
        } catch (e: ActivityNotFoundException) {
            showSnackbar(e.message.toString())
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        observeTimerData()
        observeLessonsData()
        observeHomeworkData()
        observeLoadingVisible()
    }

    private fun initUi() {
        with(binding.homeSection2.rvLessonsList) {
            adapter = classesAdapter
        }

        with(binding.homeSection3.rvHomeworkList) {
            adapter = homeworkAdapter
        }
    }

    private fun observeTimerData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.resultTimer.collect {
                    with(binding.homeSection1) {
                        tvHour1.text = it[0].toString()
                        tvHour2.text = it[1].toString()
                        tvMin1.text = it[3].toString()
                        tvMin2.text = it[4].toString()
                        tvSec1.text = it[6].toString()
                        tvSec2.text = it[7].toString()
                    }

                }
            }
        }
    }

    private fun observeLessonsData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.lessonRecycler.collect {
                    binding.homeSection2.tvClassesCount.text = "${it.size} classes today"
                    classesAdapter.setData(it)

                    val curLessonIndex = it.indexOf(it.findLast { lesson ->
                        lesson.isCurrent
                    })
                    if (curLessonIndex != -1) {
                        binding.homeSection2.rvLessonsList.scrollToPosition(curLessonIndex)
                    }
                }
            }
        }
    }

    private fun observeHomeworkData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.homeworkRecycler.collect {
                    homeworkAdapter.setData(it)
                }
            }
        }
    }

    private fun observeLoadingVisible() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoadingLessons.collect {
                    binding.homeSection2.loadingFrameLayout.root.isVisible = it
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoadingHomework.collect {
                    binding.homeSection3.loadingFrameLayout.root.isVisible = it
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}