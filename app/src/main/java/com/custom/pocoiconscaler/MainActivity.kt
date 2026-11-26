package com.custom.pocoiconscaler

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    @SuppressLint("WorldReadableFiles", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = try {
            @Suppress("DEPRECATION")
            getSharedPreferences("scaler_prefs", Context.MODE_WORLD_READABLE)
        } catch (e: SecurityException) {
            Toast.makeText(this, "Активируйте модуль в LSPosed!", Toast.LENGTH_LONG).show()
            getSharedPreferences("scaler_prefs", Context.MODE_PRIVATE)
        }

        val currentScale = prefs.getInt("icon_scale", 100)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(64, 64, 64, 64)
        }

        val titleText = TextView(this).apply {
            text = "Размер иконок POCO Launcher"
            textSize = 22f
            gravity = Gravity.CENTER
        }

        val valueText = TextView(this).apply {
            text = "$currentScale%"
            textSize = 36f
            gravity = Gravity.CENTER
            setPadding(0, 48, 0, 48)
        }

        val seekBar = SeekBar(this).apply {
            max = 80
            progress = currentScale - 80
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                    val percent = progress + 80
                    valueText.text = "$percent%"
                    prefs.edit().putInt("icon_scale", percent).apply()
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }

        val restartBtn = Button(this).apply {
            text = "Применить и перезапустить POCO"
            setOnClickListener {
                try {
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "am force-stop com.mi.android.globallauncher"))
                    Toast.makeText(context, "Лаунчер перезапущен", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Остановите Рабочий стол POCO вручную", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val space = TextView(this).apply { height = 64 }

        layout.addView(titleText)
        layout.addView(valueText)
        layout.addView(seekBar)
        layout.addView(space)
        layout.addView(restartBtn)

        setContentView(layout)
    }
}
