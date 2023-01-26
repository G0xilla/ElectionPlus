package com.urbysoft.xmlparser.entity

class District(
    nuts: String,
    name: String,
    stats: Stats,
    val municipalityList: List<Municipality>
): NutsStateRegion(nuts, name, stats) {
    override fun printStats() {
        println("District Stats")
        println("-----------------------")
        println("BASIC INFO")
        println("Nuts: $nuts")
        println("Name: $name")
        stats.printStats()
    }
}