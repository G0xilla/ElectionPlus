package com.urbysoft.xmlparser.entity

class ParticipationStats(
    val round: Int,
    val precinctSum: Int,
    val precinctEnd: Int,
    val precinctEndPercentage: Double,
    val electorateSum: Int,
    val envelopGiveSum: Int,
    val envelopGetSum: Int,
    val electorateParticipationPercentage: Double,
    val votesValidSum: Int,
    val votesValidPercentage: Double
) {
    fun printStats() {
        println("PARTICIPATION STATS")
        println("Round: $round")
        println("Precinct sum: $precinctSum")
        println("Precinct end: $precinctEnd")
        println("Precinct end %: $precinctEndPercentage")
        println("Electorate sum: $electorateSum")
        println("Envelop give sum: $envelopGiveSum")
        println("Envelop get sum: $envelopGetSum")
        println("Electorate participation %: $electorateParticipationPercentage")
        println("Votes valid sum: $votesValidSum")
        println("Votes valid %: $votesValidPercentage")
    }
}