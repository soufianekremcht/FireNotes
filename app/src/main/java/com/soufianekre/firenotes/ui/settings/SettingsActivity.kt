package com.soufianekre.firenotes.ui.settings

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.soufianekre.firenotes.MyViewModelFactory
import com.soufianekre.firenotes.R
import com.soufianekre.firenotes.databinding.ActivitySettingsBinding
import com.soufianekre.firenotes.ui.base.BaseActivity


public class SettingsActivity : BaseActivity() {

    lateinit var mViewDataBinding: ActivitySettingsBinding
    private lateinit var viewBinding: ActivitySettingsBinding
    private var factory: MyViewModelFactory? = null
    private lateinit var mSettingsViewModel :SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_settings)
        mViewDataBinding.executePendingBindings()
        viewBinding = mViewDataBinding

        viewBinding.apply {
            vm = getViewModel()
        }
        setSupportActionBar(viewBinding.settingsToolbar)


        viewBinding.settingsToolbar.setTitle(R.string.settings)
        viewBinding.settingsToolbar.setNavigationOnClickListener { v -> onBackPressed() }

        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        loadFragment(SettingsFragment())
    }

    private fun getViewModel(): SettingsViewModel? {
        mSettingsViewModel =
            ViewModelProvider(this, MyViewModelFactory()).get(SettingsViewModel::class.java)
        return mSettingsViewModel
    }

    private fun loadFragment(fragment :Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings_container,fragment)
            .commit();
    }


    override fun onDestroy() {
        super.onDestroy()
    }




}