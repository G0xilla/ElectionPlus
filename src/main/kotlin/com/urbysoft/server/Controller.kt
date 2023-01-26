package com.urbysoft.server

import com.urbysoft.xmlparser.entity.Municipality
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import java.text.DecimalFormat

@Controller
class Controller {
    private var predicateCandidateList: List<PredicateCandidate>? = null

    @Autowired
    private var repository: VotesRepository? = null

    @GetMapping("/votes")
    fun homePage(model: Model): String {
        if(predicateCandidateList != null) {
            val df = DecimalFormat()
            df.maximumFractionDigits = 2
            df.minimumFractionDigits = 2


            val firstCandidate = predicateCandidateList!!.filter { it.number == 4 }[0]
            val secondCandidate = predicateCandidateList!!.filter {it.number == 7}[0]

            val votesSum = (secondCandidate.votesPredicate + firstCandidate.votesPredicate)
            val firstCandidatePrePercentage = firstCandidate.votesPredicate.toDouble() / votesSum.toDouble() * 100
            val secondCandidatePrePercentage = secondCandidate.votesPredicate.toDouble() / votesSum.toDouble() * 100

            val votesSumNow = (secondCandidate.votesNow + firstCandidate.votesNow).toDouble()
            val firstCandidateNowPercentage = firstCandidate.votesNow.toDouble() / votesSumNow.toDouble() * 100
            val secondCandidateNowPercentage = secondCandidate.votesNow.toDouble() / votesSumNow.toDouble() * 100

            model.addAttribute("pre_stats_1", "${df.format(firstCandidatePrePercentage)}%")
            model.addAttribute("pre_stats_2","${df.format(secondCandidatePrePercentage)}%")
            model.addAttribute("now_stats_1", "${df.format(firstCandidateNowPercentage)}%")
            model.addAttribute("now_stats_2","${df.format(secondCandidateNowPercentage)}%")
        } else {
            model.addAttribute("pre_stats_1", "0.00%")
            model.addAttribute("pre_stats_2","0.00%")
            model.addAttribute("now_stats_1", "0.00%")
            model.addAttribute("now_stats_2","0.00%")
        }

        return "index"
    }

    fun Float.twoDecimalNumb(): Float {
        val float = this * 10000
        val int = float.toInt()
        return int.toFloat() / 100f
    }
    @Scheduled(fixedDelay = 10000)
    fun updateData() {
        val predicateVotesCandidateMap = mutableMapOf<Int, Pair<Float, Float>>()

        try {
            val pair = repository!!.getVotes()

            pair.first.forEach { region ->
                region.districtList.forEach { district ->
                    district.municipalityList.forEach { municipality ->
                        when(municipality.type) {
                            Municipality.Type.MUNICIPALITY_WITH_DISTRICT -> {}
                            else -> {
                                val envelopGive = municipality.stats.participationStats.envelopGiveSum
                                var votesSum = 0
                                municipality.stats.candidateStats.candidates.forEach {
                                    votesSum += it.votes
                                }
                                val processPercentage = votesSum.toFloat() / envelopGive.toFloat()

                                municipality.stats.candidateStats.candidates.forEach {
                                    val predicateVotes = (it.votes) / processPercentage
                                    predicateVotesCandidateMap.merge(it.number, Pair(predicateVotes, it.votes.toFloat())) { oldValue, newValue ->
                                        return@merge Pair(oldValue.first + newValue.first, oldValue.second + newValue.second)
                                    }
                                }
                            }
                        }
                    }
                }
            }


            val envelopGive = pair.second.stats.participationStats.envelopGiveSum
            var votesSum = 0
            pair.second.stats.candidateStats.candidates.forEach {
                votesSum += it.votes
            }
            val processPercentage = votesSum.toFloat() / envelopGive.toFloat()

            pair.second.stats.candidateStats.candidates.forEach {
                val predicateVotes = (it.votes) / processPercentage
                predicateVotesCandidateMap.merge(it.number, Pair(predicateVotes, it.votes.toFloat())) { oldValue, newValue ->
                    return@merge Pair(oldValue.first + newValue.first, oldValue.second + newValue.second)
                }
            }

            val newCandidatePredicateList = mutableListOf<PredicateCandidate>()
            predicateVotesCandidateMap.forEach { (number, votes) ->
                newCandidatePredicateList.add(PredicateCandidate(number, votes.first, votes.second))
                println("Number: $number Votes: ${votes.first.toInt()}")
            }
            predicateCandidateList = newCandidatePredicateList
        } catch (e: Exception) {
            e.printStackTrace()
        }
        println("endefef")
    }
}