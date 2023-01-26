package com.urbysoft.xmlparser.entity

class CandidateStats(val candidates: List<Candidate>) {
    fun printStats() {
        println("CANDIDATE STATS")
        val sortedList = candidates.sortedBy { it.number }
        sortedList.forEach {
            println("Number: ${it.number} Votes: ${it.votes}")
        }
    }
}