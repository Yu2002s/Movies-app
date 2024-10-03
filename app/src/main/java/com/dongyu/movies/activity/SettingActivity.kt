package com.dongyu.movies.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dongyu.movies.base.BaseActivity
import com.dongyu.movies.databinding.ActivitySettingBinding

class SettingActivity: BaseActivity() {

    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}