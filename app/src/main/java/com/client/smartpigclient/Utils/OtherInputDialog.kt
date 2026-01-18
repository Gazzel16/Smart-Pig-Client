package com.client.smartpigclient.Utils

import android.content.Context
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.client.smartpigclient.databinding.DialogCustomInputBinding

object OtherInputDialog {

    fun show(
        context: Context,
        title: String,
        hint: String,
        targetView: AutoCompleteTextView,
        onValueSelected: ((String) -> Unit)? = null
    ) {
        // Inflate the custom layout using View Binding
        val binding = DialogCustomInputBinding.inflate(android.view.LayoutInflater.from(context))

        // Set the hint dynamically
        binding.textInputLayout.hint = hint

        AlertDialog.Builder(context)
            .setTitle(title)
            .setView(binding.root)
            .setPositiveButton("OK") { _, _ ->
                val value = binding.editTextValue.text.toString().trim()

                if (value.isNotEmpty()) {
                    targetView.setText(value, false)
                    onValueSelected?.invoke(value)
                } else {
                    binding.textInputLayout.error = "Input cannot be empty"
                    Toast.makeText(context, "Input cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                targetView.setText("", false)
            }
            .show()
    }
}
