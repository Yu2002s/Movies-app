package com.dongyu.movies.data.search

sealed class SearchSuggestItem(val name: String) {
  data class Item(val suggest: Suggest): SearchSuggestItem(suggest.name)
  data class Record(val history: History): SearchSuggestItem(history.name)
}