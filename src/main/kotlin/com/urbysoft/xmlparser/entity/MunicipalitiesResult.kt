package com.urbysoft.xmlparser.entity

sealed class MunicipalitiesResult

class MunicipalitiesResultError(val message: String) : MunicipalitiesResult()
class MunicipalitiesResultOk(val region: Region): MunicipalitiesResult()