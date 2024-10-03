package com.dongyu.movies.viewmodel

import androidx.lifecycle.ViewModel
import com.dongyu.movies.model.home.NavItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel: ViewModel() {

    private val _navStateFlow = MutableStateFlow(listOf(NavItem(title = "首页")))

    val navStateFlow = _navStateFlow.asStateFlow()

    fun updateNav(navItems: List<NavItem>) {
        _navStateFlow.value = navItems
    }
}