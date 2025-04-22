package com.example.studentportal.ui.profile

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.studentportal.R
import com.example.studentportal.databinding.FragmentReportProblemBinding
import java.io.IOException
import java.io.InputStream

class ReportProblemFragment : Fragment() {

    private var _binding: FragmentReportProblemBinding? = null
    private val binding get() = _binding!!
    private var attachedImagesCount = 0

    // Контракт для выбора изображений
    private val pickImages = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { intent ->
                if (intent.clipData != null) {
                    // Множественный выбор
                    val count = intent.clipData!!.itemCount
                    for (i in 0 until count) {
                        val imageUri = intent.clipData!!.getItemAt(i).uri
                        insertImageIntoEditText(imageUri)
                    }
                } else if (intent.data != null) {
                    // Одиночный выбор
                    insertImageIntoEditText(intent.data!!)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportProblemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinner()
        setupButtons()
        setupEditTextListener()

        // Для API 21+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.progressBar.indeterminateTintList = ColorStateList.valueOf(Color.parseColor("#8A6EFF"))
        } else {
            // Для старых версий Android
            val layers = arrayOfNulls<Drawable>(1)
            val array = AnimationDrawable()
            array.addFrame(layers[0]!!, 50)
            binding.progressBar.indeterminateDrawable = array
        }
    }

    private fun setupSpinner() {
        val problemTypes = resources.getStringArray(R.array.problem_types)

        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.spinner_item,
            problemTypes
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.setTextColor(if (position == 0) Color.parseColor("#777883") else Color.WHITE)
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) ?: LayoutInflater.from(context)
                    .inflate(R.layout.spinner_dropdown_item, parent, false)

                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.text = getItem(position)
                textView.setTextColor(if (position == 0) Color.parseColor("#777883") else Color.WHITE)

                return view
            }
        }

        binding.problemTypeSpinner.adapter = adapter
    }

    private fun setupButtons() {
        // Кнопка "Назад"
        binding.backToAllSettings.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.attachButton.apply {
            setOnClickListener {
                openImagePicker()
            }

            // Добавляем эффект нажатия программно, так как теперь это ImageView
            setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.alpha = 1f // Эффект нажатия
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        v.alpha = 1.0f // Возвращаем обычную прозрачность
                        v.performClick() // Вызываем обычный click
                        true
                    }
                    else -> false
                }
            }
        }

        // Кнопка отправки
        binding.sendButton.setOnClickListener {
            sendReport()
        }
    }

    private fun setupEditTextListener() {
        binding.problemDescription.addTextChangedListener {
            // Можно добавить логику для обработки изменений текста
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        pickImages.launch(intent)
    }

    private fun insertImageIntoEditText(uri: Uri) {
        try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Масштабируем изображение по ширине EditText
            val width = binding.problemDescription.width -
                    binding.problemDescription.paddingLeft -
                    binding.problemDescription.paddingRight

            val scaledBitmap = Bitmap.createScaledBitmap(
                bitmap,
                width,
                (bitmap.height * width / bitmap.width),
                true
            )

            // Создаем ImageSpan
            val imageSpan = ImageSpan(requireContext(), scaledBitmap)
            val ssb = SpannableStringBuilder()

            // Добавляем перенос строки, если уже есть текст
            if (binding.problemDescription.text.isNotEmpty()) {
                ssb.append("\n\n")
            }

            // Добавляем изображение
            ssb.append("[image_${++attachedImagesCount}]")
            ssb.setSpan(
                imageSpan,
                ssb.length - "[image_$attachedImagesCount]".length,
                ssb.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            ssb.append("\n")

            // Вставляем в EditText
            binding.problemDescription.text.insert(binding.problemDescription.selectionStart, ssb)
        } catch (e: IOException) {
            Toast.makeText(context, "Ошибка при загрузке изображения", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun sendReport() {
        if (binding.problemTypeSpinner.selectedItemPosition <= 0) {
            Toast.makeText(context, "Пожалуйста, выберите тему", Toast.LENGTH_SHORT).show()
            return
        }

        val problemType = binding.problemTypeSpinner.selectedItem.toString()
        val description = binding.problemDescription.text.toString()

        if (description.isBlank()) {
            Toast.makeText(context, "Пожалуйста, опишите проблему", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        // Имитация отправки на сервер
        Handler(Looper.getMainLooper()).postDelayed({
            showLoading(false)
            Toast.makeText(context, "Сообщение отправлено", Toast.LENGTH_SHORT).show()

            // Сбрасываем значения после успешной отправки
            resetForm()
        }, 2000)
    }

    private fun resetForm() {
        // Анимация исчезновения для обоих элементов
        val fadeOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out).apply {
            duration = 200
        }

        binding.problemDescription.startAnimation(fadeOut)
        binding.problemTypeSpinner.startAnimation(fadeOut)

        // Задержка перед сбросом значений и появлением
        Handler(Looper.getMainLooper()).postDelayed({
            // Сбрасываем значения
            binding.problemTypeSpinner.setSelection(0)
            binding.problemDescription.text.clear()
            attachedImagesCount = 0

            // Анимация появления
            val fadeIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in).apply {
                duration = 200
                startOffset = 70 // Небольшая задержка для плавности
            }

            binding.problemDescription.startAnimation(fadeIn)
            binding.problemTypeSpinner.startAnimation(fadeIn)

        }, 200)
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.sendButton.isEnabled = !show
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ReportProblemFragment()
    }
}