package com.urbysoft.xmlparser.entity

sealed class ExternalResult

class ExternalResultError(val message: String) : ExternalResult()
class ExternalResultOk(val stats: Stats) : ExternalResult()