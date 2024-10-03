package com.dongyu.movies.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dongyu.movies.network.Repository
import com.dongyu.movies.model.user.User
import com.dongyu.movies.network.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch

class UserViewModel: ViewModel() {

  private val _userUiState = MutableStateFlow(Repository.user)

  val userUiState: StateFlow<User?> get() = _userUiState

  init {
    refreshUser()
  }

  fun logout() {
    Repository.logout()
    _userUiState.value = null
  }

  fun refreshUser() {
    val token = Repository.token
    if (token == null) {
      // 如果token为空，默认为未登录状态
      Repository.logout()
      return
    }
    viewModelScope.launch {
      // 查询云端的用户信息
      val queryUser = UserRepository.getLoggedInUser().single().getOrNull()
      if (queryUser != null) {
        // 将云端信息与本地进行同步
        Repository.saveUser(queryUser)
      } else {
        // 未查询到用户信息，则退出登录
        Repository.logout()
      }
      // 刷新显示的用户信息
      _userUiState.emit(queryUser)
    }
  }
}