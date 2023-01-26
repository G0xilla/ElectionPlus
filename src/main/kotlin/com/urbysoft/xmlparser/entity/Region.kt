package com.urbysoft.xmlparser.entity

class Region(
    nuts: String,
    name: String,
    stats: Stats,
    val districtList: List<District>
): NutsStateRegion(nuts, name, stats) {
    override fun printStats() {
        println("Region Stats")
        println("-----------------------")
        println("BASIC INFO")
        println("Region: $nuts")
        println("Name: $name")
        stats.printStats()
    }
}