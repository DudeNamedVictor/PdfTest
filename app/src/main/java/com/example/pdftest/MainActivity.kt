package com.example.pdftest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.pdftest.databinding.MainLayoutBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity() {

    private var _binding: MainLayoutBinding? = null
    private val binding: MainLayoutBinding
        get() = _binding as MainLayoutBinding

    private val originalFirstFile = createTempFile()
    private val originalSecondFile = createTempFile()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = MainLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        var value = ""
        binding.selectFirstFile.setOnClickListener {
            val intent = Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent, "Select a file"), 123)
        }
        binding.selectSecondFile.setOnClickListener {
            val intent = Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent, "Select a file"), 321)
        }
        binding.comparePdfs.setOnClickListener {
            if (originalFirstFile.path.isNotEmpty() && originalSecondFile.path.isNotEmpty()) {
                binding.progress.isVisible = true
                lifecycleScope.launch(Dispatchers.Main) {
                    getResultFile {
                        Log.d("checkThis", it.toString())
                    }
                    binding.progress.isVisible = false
                    makeText("Check logs")
                }
            } else {
                makeText("Empty value or list")
            }
        }
    }

    private suspend fun getResultFile(onFileReady: ((Boolean) -> Unit)) {
        withContext(Dispatchers.Default) {
            onFileReady.invoke(GeneratePdfLogic.generatePdf(originalFirstFile, originalSecondFile))
        }
    }

    @Override
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123 && resultCode == RESULT_OK) {
            val selectedFile = data!!.data!!
            selectedFile.let { this.contentResolver.openInputStream(it) }.use { input ->
                originalFirstFile.outputStream().use { output ->
                    input?.copyTo(output)
                }
            }
        } else if (requestCode == 321 && resultCode == RESULT_OK) {
            val selectedFile = data!!.data!!
            selectedFile.let { this.contentResolver.openInputStream(it) }.use { input ->
                originalSecondFile.outputStream().use { output ->
                    input?.copyTo(output)
                }
            }
        }
    }

}