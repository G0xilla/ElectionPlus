package com.urbysoft.xmlparser.parse

import com.urbysoft.xmlparser.entity.*
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.InputStream
import java.lang.Exception
import java.lang.IllegalArgumentException
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory

class XMLStatsParser {
    companion object Tags {
        const val resultRegion = "VYSLEDKY_KRAJ"
        const val regionNuts = "NUTS_KRAJ"
        const val regionName = "NAZ_KRAJ"
        const val error = "CHYBA"
        const val all = "CELKEM"
        const val candidateStats = "HODN_KAND"
        const val serialNumber = "PORADOVE_CISLO"
        const val votes = "HLASY"
        const val participation = "UCAST"
        const val round = "KOLO"
        const val precinctSum = "OKRSKY_CELKEM"
        const val precinctEnd = "OKRSKY_ZPRAC"
        const val precinctEndPercentage = "OKRSKY_ZPRAC_PROC"
        const val electorateSum = "ZAPSANI_VOLICI"
        const val envelopGiveSum = "VYDANE_OBALKY"
        const val envelopGetSum = "ODEVZDANE_OBALKY"
        const val electorateParticipationPercentage = "UCAST_PROC"
        const val validVotesSum = "PLATNE_HLASY"
        const val validVotesPercentage = "PLATNE_HLASY_PROC"
        const val district = "OKRES"
        const val municipality = "OBEC"
        const val municipalityNumber = "CIS_OBEC"
        const val municipalityName = "NAZ_OBEC"
        const val municipalityType = "TYP_OBEC"
        const val nutsDistrict = "NUTS_OKRES"
        const val nameDistrict = "NAZ_OKRES"

        const val municipalityNormal = "OBEC_BEZ_MCMO"
        const val municipalityWithDistrict = "OBEC_S_MCMO"
        const val municipalityDistrict = "MCMO"
    }

    private val documentBuilderFactory = DocumentBuilderFactory.newInstance()

    init {
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
    }

    fun parseMunicipalities(input: InputStream): MunicipalitiesResult {
        try {
            var regionCandidateStats: CandidateStats? = null
            var regionParticipationStats: ParticipationStats? = null

            val rootFirstElement: Element? = getFirstRootElement(input)

            if (rootFirstElement!!.tagName == error) {
                input.close()
                return MunicipalitiesResultError(rootFirstElement.textContent)
            }

            val regionNuts = rootFirstElement.getAttribute(regionNuts)
            val regionName = rootFirstElement.getAttribute(regionName)

            val regionChildNodeList = rootFirstElement.childNodes

            for(index in 0 until regionChildNodeList.length) {
                val node = regionChildNodeList.item(index)
                if(node.nodeType == Element.ELEMENT_NODE) {
                    val element = node as Element
                    if(element.tagName == all) {
                        regionCandidateStats = parseCandidateStats(element)
                        regionParticipationStats = parseParticipation(element)
                    }
                }
            }

            val districtElementList = mutableListOf<Element>()
            for(index in 0 until regionChildNodeList.length) {
                val node = regionChildNodeList.item(index)

                if(node.nodeType == Element.ELEMENT_NODE) {
                    val element = node as Element
                    if(element.tagName == district) {
                        districtElementList.add(element)
                    }
                }
            }

            val districtList = mutableListOf<District>()

            districtElementList.forEach {
                val districtChildList = it.childNodes

                val municipalityList = mutableListOf<Municipality>()
                var districtParticipationStats: ParticipationStats? = null
                var districtCandidateStats: CandidateStats? = null

                for(index in 0 until districtChildList.length) {
                    val node = districtChildList.item(index)

                    if(node.nodeType == Element.ELEMENT_NODE) {
                        val element = node as Element

                        if (element.tagName == municipality) {
                            val municipalityNumber = element.getAttribute(municipalityNumber).toInt()
                            val municipalityName = element.getAttribute(municipalityName)
                            val municipalityType = element.getAttribute(municipalityType).parseToType()

                            val municipalityParticipationStats = parseParticipation(element)
                            val municipalityCandidateStats = parseCandidateStats(element)
                            val municipalityStats = Stats(municipalityCandidateStats, municipalityParticipationStats)

                            val municipality = Municipality(
                                municipalityNumber,
                                municipalityName,
                                municipalityType,
                                municipalityStats
                            )

                            municipalityList.add(municipality)
                        } else if(element.tagName == all) {
                            districtCandidateStats = parseCandidateStats(element)
                            districtParticipationStats = parseParticipation(element)
                        }
                    }
                }

                val nutsDistrict = it.getAttribute(nutsDistrict)
                val nameDistrict = it.getAttribute(nameDistrict)


                val district = District(
                    nutsDistrict,
                    nameDistrict,
                    Stats(districtCandidateStats!!, districtParticipationStats!!),
                    municipalityList
                )

                districtList.add(district)
            }

            val result = Region(
                regionNuts,
                regionName,
                Stats(regionCandidateStats!!, regionParticipationStats!!),
                districtList
            )
            input.close()

            return MunicipalitiesResultOk(result)
        } catch (e: Exception) {
            throw IllegalArgumentException("Cannot parse XML file.")
        }
    }

    fun parseExternal(input: InputStream): ExternalResult {
        try {
            val rootFirstElement = getFirstRootElement(input)

            if(rootFirstElement!!.tagName == error) {
                input.close()
                return ExternalResultError(rootFirstElement.textContent)
            }

            val rootFirstElementChildNodeList = rootFirstElement.childNodes
            for(index in 0 until rootFirstElementChildNodeList.length) {
                val node = rootFirstElementChildNodeList.item(index)
                if(node.nodeType == Element.ELEMENT_NODE) {
                    val element = node as Element
                    if(element.tagName == all) {
                        val candidateStats = parseCandidateStats(element)
                        val participationStats = parseParticipation(element)
                        return ExternalResultOk(Stats(candidateStats, participationStats))
                    }
                }
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Cannot parse XML file.", e)
        }


        throw IllegalArgumentException("Cannot parse XML file.")
    }

    private fun getFirstRootElement(input: InputStream): Element? {
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()
        val document = documentBuilder.parse(input)

        val rootElement = document.firstChild as Element
        val rootChildNodeList = rootElement.childNodes
        var rootFirstElement: Element? = null
        for (index in 0 until rootChildNodeList.length) {
            val node = rootChildNodeList.item(index)
            if (node.nodeType == Element.ELEMENT_NODE) {
                return node as Element
            }
        }

        return null
    }

    private fun String.parseToType(): Municipality.Type =
        when(this) {
            municipalityNormal -> Municipality.Type.NORMAL
            municipalityWithDistrict -> Municipality.Type.MUNICIPALITY_WITH_DISTRICT
            municipalityDistrict -> Municipality.Type.DISTRICT
            else -> throw IllegalArgumentException()
        }


    private fun parseCandidateStats(root: Node): CandidateStats {
        val rootChildList = root.childNodes
        val candidateList = mutableListOf<Candidate>()
        for(index in 0 until rootChildList.length) {
            val childNode = rootChildList.item(index)
            if (childNode.nodeType == Element.ELEMENT_NODE) {
                val childElement = childNode as Element
                if(childElement.tagName == candidateStats) {
                    val number = childElement.getAttribute(serialNumber).toInt()
                    val votes = childElement.getAttribute(votes).toInt()
                    candidateList.add(Candidate(number, votes))
                }
            }
        }
        return CandidateStats(candidateList)
    }

    private fun parseParticipation(root: Node): ParticipationStats {
        val rootChildList = root.childNodes
        for(index in 0 until rootChildList.length) {
            val childNode = rootChildList.item(index)
            if (childNode.nodeType == Element.ELEMENT_NODE) {
                val childElement = childNode as Element
                if(childElement.tagName == participation) {
                    val round = childNode.getAttribute(round).toInt()
                    val precinctSum = childNode.getAttribute(precinctSum).toInt()
                    val precinctEnd = childNode.getAttribute(precinctEnd).toInt()
                    val precinctEndPercentage = childNode.getAttribute(precinctEndPercentage).toDouble()
                    val electorateSum = childNode.getAttribute(electorateSum).toInt()
                    val envelopGiveSum = childNode.getAttribute(envelopGiveSum).toInt()
                    val envelopGetSum = childNode.getAttribute(envelopGetSum).toInt()
                    val electorateParticipationPercentage = childNode.getAttribute(electorateParticipationPercentage).toDouble()
                    val votesValidSum = childNode.getAttribute(validVotesSum).toInt()
                    val votesValidPercentage = childNode.getAttribute(validVotesPercentage).toDouble()

                    return ParticipationStats(
                        round,
                        precinctSum,
                        precinctEnd,
                        precinctEndPercentage,
                        electorateSum,
                        envelopGiveSum,
                        envelopGetSum,
                        electorateParticipationPercentage,
                        votesValidSum,
                        votesValidPercentage
                    )
                }
            }
        }

        throw IllegalStateException()
    }
}