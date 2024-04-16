package com.example.pdftest

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.EncodeHintType
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Reader
import com.google.zxing.Result
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.datamatrix.DataMatrixWriter
import com.google.zxing.datamatrix.encoder.SymbolShapeHint
import java.io.File
import java.io.FileOutputStream


object GeneratePdfLogic {

    fun generatePdf(value: String, originalFile: File): File {
        val listBitmaps = pdfToBitmap(originalFile)
        val resultQrs = readQrs(listBitmaps)

        return generatePdf(value, resultQrs)
    }

    private fun pdfToBitmap(pdfFile: File): List<Bitmap> {
        val bitmaps = mutableListOf<Bitmap>()
        try {
            val renderer =
                PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY))
            var bitmap: Bitmap
            val pageCount = renderer.getPageCount()
            for (i in 0 until pageCount) {
                val page = renderer.openPage(i)
                val width = 240 / 72 * page.width
                val height = 240 / 72 * page.height
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
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

    private fun readQrs(listBitmaps: List<Bitmap>): MutableList<String> {
        val resultQrs = mutableListOf<String>()
        listBitmaps.forEach { bitmap ->
            val reader: Reader = MultiFormatReader()
            val result: Result = reader.decode(bitmapToBinaryBitmap(bitmap))
            resultQrs.add(result.text)
        }

        return resultQrs
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
        val source: LuminanceSource =
            RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray)

        return BinaryBitmap(HybridBinarizer(source))
    }

    private fun generatePdf(value: String, resultQrs: MutableList<String>): File {
        val width = 164
        val height = 113
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val title = Paint()
        val page = Paint()

        resultQrs.forEachIndexed { index, iterator ->
            val mypageInfo = PdfDocument.PageInfo.Builder(width, height, 1).create()
            val myPage = pdfDocument.startPage(mypageInfo)
            val canvas: Canvas = myPage.canvas

            val hintMap = mutableMapOf<EncodeHintType, SymbolShapeHint>()
            hintMap[EncodeHintType.DATA_MATRIX_SHAPE] = SymbolShapeHint.FORCE_SQUARE
            val bitMatrix =
                DataMatrixWriter().encode(iterator, BarcodeFormat.DATA_MATRIX, 30, 30, hintMap)
            canvas.drawBitmap(convertBitMatrixToBitMap(bitMatrix), 6F, 6F, paint)

            title.setColor(Color.BLACK)
            title.textSize = 8F
            title.textAlign = Paint.Align.CENTER

            val text = mutableListOf<String>()
            var test = ""
            value.forEachIndexed { index, char ->
                test += char
                if (index != 0 && index.mod(20) == 0) {
                    text.add(test)
                    test = ""
                }
                if (index == value.length - 1) {
                    text.add(test)
                    test = ""
                }
                if (index > 80) {
                    return@forEachIndexed
                }
            }

            text.forEachIndexed { index, it ->
                canvas.drawText(it, 110F, 30F + ((index + 1) * 10), title)
            }

            page.setColor(Color.BLACK)
            page.textSize = 6F
            page.textAlign = Paint.Align.RIGHT
            canvas.drawText((index + 1).toString(), 154F, 107F, page)
            pdfDocument.finishPage(myPage)
        }

        val file = File.createTempFile("GFG", ".pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        return file
    }

    private fun convertBitMatrixToBitMap(bitMatrix: BitMatrix): Bitmap {
        val height = bitMatrix.height
        val width = bitMatrix.width
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }

        return bmp
    }

}