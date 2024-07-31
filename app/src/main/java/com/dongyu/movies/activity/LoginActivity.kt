package com.dongyu.movies.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dongyu.movies.R
import com.dongyu.movies.base.BaseActivity
import com.dongyu.movies.base.BaseRepository
import com.dongyu.movies.data.user.LoginFrom
import com.dongyu.movies.databinding.ActivityLoginBinding
import com.dongyu.movies.utils.showToast
import com.dongyu.movies.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity() {

  companion object {
    private val TAG = LoginActivity::class.java.simpleName
  }

  private val binding by lazy {
    ActivityLoginBinding.inflate(layoutInflater)
  }

  private val loginViewModel by viewModels<LoginViewModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(binding.root)
    val toolBar = binding.header.toolBar
    setSupportActionBar(toolBar)
    supportActionBar?.apply {
      title = getString(R.string.login)
      setDisplayHomeAsUpEnabled(true)
    }

    val emailInput = binding.inputEmail.editText!!
    val codeInput = binding.inputCode.editText!!
    val loginBtn = binding.btnLogin
    val codeBtn = binding.btnGetCode

    emailInput.requestFocus()

    lifecycleScope.launch {
      loginViewModel.loginCheckState.collect { loginCheckState ->
        codeBtn.isEnabled = validCode()
        emailInput.error = loginCheckState.validEmail?.let { getString(it) }
        codeInput.error = loginCheckState.validCode?.let { getString(it) }
        loginBtn.isEnabled = loginCheckState.isDataValid
      }
    }

    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.CREATED) {
        loginViewModel.codeUiState.collect {
          codeBtn.isEnabled = validCode()
          codeBtn.text = if (it == 0) getString(R.string.get_code)
          else getString(R.string.sent_code).format(it)
        }
      }
    }

    codeBtn.setOnClickListener {
      loginViewModel.sendCode(emailInput.text.toString())
    }

    loginBtn.setOnClickListener { v ->
      v.isEnabled = false
      lifecycleScope.launch {
        loginViewModel.login(
          LoginFrom(
            emailInput.text.toString().trim(),
            codeInput.text.toString().trim()
          )
        ).collect { result ->
          v.isEnabled = true
          result.onSuccess { token ->
            // 保存token
            BaseRepository.saveToken(token)
            "登录成功".showToast()
            Handler(Looper.getMainLooper()).postDelayed({
              setResult(RESULT_OK)
              finish()
            }, 500)
          }.onFailure {
            it.message.showToast()
          }
        }
      }
    }

    val changedListener: (text: Editable?) -> Unit = {
      loginViewModel.validatorForm(
        emailInput.text.toString(),
        codeInput.text.toString()
      )
    }

    emailInput.doAfterTextChanged(changedListener)
    codeInput.doAfterTextChanged(changedListener)

    codeInput.setOnEditorActionListener { _, actionId, _ ->
      if (actionId == EditorInfo.IME_ACTION_DONE) {
        if (loginBtn.isEnabled)
          loginBtn.callOnClick()
      }
      false
    }
  }

  private fun validCode() = loginViewModel.loginCheckState.value.validEmail == null
      && loginViewModel.codeUiState.value == 0
}