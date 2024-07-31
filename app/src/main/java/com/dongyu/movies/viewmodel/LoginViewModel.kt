package com.dongyu.movies.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dongyu.movies.R
import com.dongyu.movies.data.user.LoginCheckState
import com.dongyu.movies.data.user.LoginFrom
import com.dongyu.movies.network.UserRepository
import com.dongyu.movies.utils.showToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _loginCheckState = MutableStateFlow(LoginCheckState(R.string.error_email))
    val loginCheckState: StateFlow<LoginCheckState> get() = _loginCheckState

    private val _codeUiState = MutableStateFlow(0)
    val codeUiState: StateFlow<Int> get() = _codeUiState

    suspend fun login(loginFrom: LoginFrom) = UserRepository.login(loginFrom)

    fun sendCode(email: String) {
        // 如果没有输入验证码
        if (!validatorEmail(email)) {
            _loginCheckState.value = LoginCheckState(validEmail = R.string.error_email)
            return
        }
        _codeUiState.value = 60
        // 次数调用接口发送验证码
        viewModelScope.launch {
            // 发送验证码
            launch {
                UserRepository.sendCode(email).onSuccess {
                    "验证码发送成功".showToast()
                }.onFailure {
                    it.message.showToast()
                }
            }
            while (codeUiState.value > 0) {
                delay(1000)
                _codeUiState.value--
            }
        }
    }

    fun validatorForm(email: String, code: String) {
        val loginCheckState = if (!validatorEmail(email)) {
            LoginCheckState(validEmail = R.string.error_email)
        } /*else if (!validatorPassword(password)) {
      LoginCheckState(validPassWord = R.string.error_password)
    } */ else if (!validatorCode(code)) {
            LoginCheckState(validCode = R.string.error_code)
        } else {
            LoginCheckState(isDataValid = true)
        }
        _loginCheckState.value = loginCheckState
    }

    private fun validatorEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    }

    private fun validatorPassword(password: String): Boolean {
        return password.trim().length in 5..16
    }

    private fun validatorCode(code: String): Boolean {
        return code.trim().length == 6
    }
}