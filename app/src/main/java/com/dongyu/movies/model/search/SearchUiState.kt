package com.dongyu.movies.model.search

sealed class SearchUiState(val name: String)

class SearchUiSuggest(name: String = "") : SearchUiState(name)

class SearchUiResult(name: String) : SearchUiState(name)

