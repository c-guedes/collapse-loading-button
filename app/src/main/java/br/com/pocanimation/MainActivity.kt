package br.com.pocanimation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.btn_morph
import kotlinx.android.synthetic.main.activity_main.revertButt


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_morph.btProgress.setOnClickListener {
            btn_morph.toggleProgress(true)
        }

        revertButt.setOnClickListener {
            btn_morph.toggleProgress(false)
        }
    }
}