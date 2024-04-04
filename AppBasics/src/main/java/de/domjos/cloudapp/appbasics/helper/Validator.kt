package de.domjos.cloudapp.appbasics.helper

import android.content.Context
import de.domjos.cloudapp.appbasics.R
import java.text.SimpleDateFormat
import java.util.Locale

class Validator {

    companion object {
        fun check(empty: Boolean, minLength: Int, maxLength: Int, input: String): Boolean {
            return if(empty) {
                if(input.isEmpty()) {
                    true
                } else {
                    input.length in minLength..maxLength
                }
            } else {
                input.isNotEmpty() && input.length in minLength..maxLength
            }
        }

        fun checkUrl(input: String): Boolean {
            return input.matches(Regex("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"))
        }

        fun checkDate(input: String, format: String): Boolean {
            if(input.isNotEmpty()) {
                val frm = SimpleDateFormat(format, Locale.getDefault())
                try {
                    frm.parse(input)
                    return true
                } catch (_: Exception) {
                }
                return false
            } else {
                return true
            }
        }
    }
}