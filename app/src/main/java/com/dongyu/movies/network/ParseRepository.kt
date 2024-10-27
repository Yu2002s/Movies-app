package com.dongyu.movies.network


object ParseRepository {

    private val parseService = Repository.parseService

    fun getParseSourceList() = requestSimpleCallResult {
        parseService.getParseSourceList()
    }

}