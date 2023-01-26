package com.urbysoft.xmlparser.entity

import com.urbysoft.xmlparser.entity.CandidateStats
import com.urbysoft.xmlparser.entity.ParticipationStats

class Stats(val candidateStats: CandidateStats, val participationStats: ParticipationStats) {
    fun printStats() {
        println("-----------------------")
        candidateStats.printStats()
        println("-----------------------")
        participationStats.printStats()
    }
}