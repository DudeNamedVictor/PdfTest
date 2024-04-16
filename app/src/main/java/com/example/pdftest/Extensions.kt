package com.example.pdftest

import android.app.Activity
import android.widget.Toast


fun Activity.makeText(text: String) {
    Toast.makeText(
        this,
        text,
        Toast.LENGTH_SHORT
    ).show()

}