package com.dongyu.movies.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dongyu.movies.base.BaseRepository
import com.dongyu.movies.data.user.User
import com.dongyu.movies.network.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch

class UserViewModel: ViewModel() {

  private val _userUiState = MutableStateFlow(BaseRepository.user)

  val userUiState: StateFlow<User?> get() = _userUiState

  init {
    refreshUser()
  }

  fun logout() {
    BaseRepository.logout()
    _userUiState.value = null
  }

  fun refreshUser() {
    val token = BaseRepository.token
    if (token == null) {
      // 如果token为空，默认为未登录状态
      BaseRepository.logout()
      return
    }
    viewModelScope.launch {
      // 查询云端的用户信息
      val queryUser = UserRepository.getLoggedInUser().single().getOrNull()
      if (queryUser != null) {
        // 将云端信息与本地进行同步
        BaseRepository.saveUser(queryUser)
      } else {
        // 未查询到用户信息，则退出登录
        BaseRepository.logout()
      }
      // 刷新显示的用户信息
      _userUiState.emit(queryUser)
    }
  }
}