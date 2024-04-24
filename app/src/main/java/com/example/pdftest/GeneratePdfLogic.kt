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

    private fun generatePdf(textValue: String, resultQrs: List<String>): File {
        val pdfDocument = PdfDocument()
        val dataMatrix = Paint()
        val outputText = Paint()
        val pageCount = Paint()

        resultQrs.forEachIndexed { index, iterator ->
            val pageInfo = PdfDocument.PageInfo.Builder(
                PageParameters.PAGE_SIZE_FOR_NIKITA.width,
                PageParameters.PAGE_SIZE_FOR_NIKITA.height,
                1
            ).create()
            val myPage = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = myPage.canvas

            val hintMap = mutableMapOf<EncodeHintType, SymbolShapeHint>()
            hintMap[EncodeHintType.DATA_MATRIX_SHAPE] = SymbolShapeHint.FORCE_SQUARE
            val bitMatrix =
                DataMatrixWriter().encode(iterator, BarcodeFormat.DATA_MATRIX, 100, 100, hintMap)
            canvas.drawBitmap(convertBitMatrixToBitMap(bitMatrix), 15F, 15F, dataMatrix)

            outputText.setColor(Color.BLACK)
            outputText.textSize = 14F
            outputText.textAlign = Paint.Align.CENTER

            val textList = textValue.split(" ").toList()
            var string = ""
            var step = 80F
            for (i in textList.indices) {
                if (string.length < 20) {
                    string += textList[i] + " "
                } else {
                    canvas.drawText(string, 200F, step, outputText)
                    string = textList[i] + " "
                    step += 20F
                }
                if (i == textList.size - 1 && string.isNotEmpty()) {
                    canvas.drawText(string, 200F, step, outputText)
                }
                if (i > 14) {
                    break
                }
            }

            pageCount.setColor(Color.BLACK)
            pageCount.textSize = 9F
            pageCount.textAlign = Paint.Align.RIGHT
            canvas.drawText((index + 1).toString(), 300F, 210F, pageCount)
            pdfDocument.finishPage(myPage)
        }

        val file = File.createTempFile("GFG", ".pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        return file
    }

    private fun convertBitMatrixToBitMap(bitMatrix: BitMatrix): Bitmap {
        val bmp = Bitmap.createBitmap(bitMatrix.width, bitMatrix.height, Bitmap.Config.ARGB_8888)
        for (x in 0 until bitMatrix.width) {
            for (y in 0 until bitMatrix.height) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }

        return bmp
    }

    private enum class PageParameters(val width: Int, val height: Int) {
        PAGE_SIZE_FOR_NIKITA(320, 230)
    }

}