package com.example.pdftest

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.pdftest.databinding.MainLayoutBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


class MainActivity : ComponentActivity() {

    private var _binding: MainLayoutBinding? = null
    private val binding: MainLayoutBinding
        get() = _binding as MainLayoutBinding

    private val originalFile = createTempFile()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = MainLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        var value = ""
        binding.inputValue.addTextChangedListener {
            value = it.toString()
        }
        binding.selectFile.setOnClickListener {
            val intent = Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent, "Select a file"), 123)
        }
        binding.generateFile.setOnClickListener {
            if (value.isNotEmpty() && originalFile.path.isNotEmpty()) {
                binding.progress.isVisible = true
                lifecycleScope.launch(Dispatchers.Main) {
                    getResultFile(value) {
                        val file = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                .toString(), "GFG.pdf"
                        )
                        val fos = FileOutputStream(file, true)
                        fos.write(it.readBytes())
                        fos.close()
                    }
                    binding.progress.isVisible = false
                    makeText("Ready")
                }
            } else {
                makeText("empty value or list")
            }
        }
    }

    private suspend fun getResultFile(value: String, onFileReady: ((File) -> Unit)) {
        withContext(Dispatchers.Default) {
            onFileReady.invoke(GeneratePdfLogic.generatePdf(value, originalFile))
        }
    }

    @Override
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123 && resultCode == RESULT_OK) {
            val selectedFile = data!!.data!!
            selectedFile.let { this.contentResolver.openInputStream(it) }.use { input ->
                originalFile.outputStream().use { output ->
                    input?.copyTo(output)
                }
            }
        }
    }

}