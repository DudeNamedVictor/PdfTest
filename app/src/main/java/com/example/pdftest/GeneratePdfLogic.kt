package com.example.pdftest

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.google.zxing.BinaryBitmap
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.io.File


object GeneratePdfLogic {

    fun generatePdf(originalFirstFile: File, originalSecondFile: File): Boolean {
        val listFirstBitmaps = pdfToBitmap(originalFirstFile)
        val listSecondBitmaps = pdfToBitmap(originalSecondFile)
        val resultFirstQrs = readQrs(listFirstBitmaps)
        val resultSecondQrs = readQrs(listSecondBitmaps)

        return resultFirstQrs == resultSecondQrs
    }

    private fun pdfToBitmap(pdfFile: File): List<Bitmap> {
        val bitmaps = mutableListOf<Bitmap>()
        try {
            val renderer = PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY))
            var bitmap: Bitmap
            val pageCount = renderer.getPageCount()
            for (i in 0 until pageCount) {
                val page = renderer.openPage(i)
                bitmap = Bitmap.createBitmap(3 * page.width, 3 * page.height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmaps.add(bitmap)
                page.close()
            }
            renderer.close()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return bitmaps
    }

    private fun readQrs(listBitmaps: List<Bitmap>) = listBitmaps.map { bitmap ->
        MultiFormatReader().decode(bitmapToBinaryBitmap(bitmap)).text
    }

    private fun bitmapToBinaryBitmap(bitmap: Bitmap): BinaryBitmap {
        val intArray = IntArray(bitmap.getWidth() * bitmap.getHeight())
        bitmap.getPixels(
            intArray,
            0,
            bitmap.getWidth(),
            0,
            0,
            bitmap.getWidth(),
            bitmap.getHeight()
        )
        val source: LuminanceSource = RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray)

        return BinaryBitmap(HybridBinarizer(source))
    }

}