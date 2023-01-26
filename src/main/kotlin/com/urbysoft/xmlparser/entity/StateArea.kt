package com.urbysoft.xmlparser.entity

abstract class StateArea(val name: String, val stats: Stats) {
    abstract fun printStats()
}