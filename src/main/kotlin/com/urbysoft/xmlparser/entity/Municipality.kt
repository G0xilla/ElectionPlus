package com.urbysoft.xmlparser.entity

class Municipality(
    val municipalityNumber: Int,
    name: String,
    val type: Type,
    stats: Stats
): StateArea(name, stats) {
    override fun printStats() {
        println("Municipality Stats")
        println("-----------------------")
        println("BASIC INFO")
        println("Number: $municipalityNumber")
        println("Name: $name")
        println("Type: $type")
        stats.printStats()
    }

    enum class Type {
        NORMAL,
        MUNICIPALITY_WITH_DISTRICT,
        DISTRICT;

        override fun toString(): String =
            when(this) {
                NORMAL -> "OBEC_BEZ_MCMO"
                MUNICIPALITY_WITH_DISTRICT -> "OBEC_S_MCMO"
                DISTRICT -> "MCMO"
            }
    }
}