package de.domjos.cloudapp.appbasics.helper

import android.content.Context
import de.domjos.cloudapp.appbasics.R

class Validator {

    companion object {
        fun validateTextNotEmpty(text: String, length: Int = -1, maxLength: Int = -1, context: Context): String {
            var validation = ""
            val param = if(length == -1 && maxLength == -1) {
                "*"
            } else if(length == -1) {
                validation = String.format(context.getString(R.string.validation_notEmpty_max), maxLength)
                "{,$maxLength}"
            } else if(maxLength == -1) {
                validation = if(length == 1) {
                    context.getString(R.string.validation_notEmpty)
                } else {
                    String.format(context.getString(R.string.validation_notEmpty_min), length)
                }
                "{$length,}"
            } else {
                validation = String.format(context.getString(R.string.validation_notEmpty_min_max), length, maxLength)
                "{$length,$maxLength}"
            }

            return if(text.matches(Regex("[a-zA-Z0-9]$param"))) {
                ""
            } else {
                validation
            }
        }
    }
}