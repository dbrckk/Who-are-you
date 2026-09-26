package com.whoareyou.app

enum class PersonalCertainty {
    UNKNOWN,
    EXPLORING,
    LIKELY,
    ESTABLISHED
}

enum class PersonalStability {
    UNKNOWN,
    VARIABLE,
    MODERATE,
    STABLE
}

enum class ContradictionLevel {
    NONE,
    LOW,
    MODERATE,
    HIGH
}

enum class PersonalTrend {
    UNKNOWN,
    STABLE,
    RISING,
    FALLING,
    VARIABLE
}

data class PersonalTrait(
    val traitId: String,
    val score: Int,
    val confidence: Int,
    val certainty: PersonalCertainty,
    val stability: PersonalStability,
    val contradictionLevel: ContradictionLevel,
    val evidenceCount: Int,
    val sourceQuizIds: List<String>,
    val trend: PersonalTrend,
    val isDistinctive: Boolean
)

data class PersonalModel(
    val traits: List<PersonalTrait>,
    val establishedTraits: List<PersonalTrait>,
    val likelyTraits: List<PersonalTrait>,
    val exploringTraits: List<PersonalTrait>,
    val unknownTraitIds: List<String>,
    val stableTraits: List<PersonalTrait>,
    val variableTraits: List<PersonalTrait>,
    val contradictoryTraits: List<PersonalTrait>,
    val strongestKnowledgeDomains: List<TraitDomainCoverage>,
    val knowledgeGaps: List<TraitDomainCoverage>
) {
    companion object {
        val EMPTY = PersonalModel(
            traits = emptyList(),
            establishedTraits = emptyList(),
            likelyTraits = emptyList(),
            exploringTraits = emptyList(),
            unknownTraitIds = emptyList(),
            stableTraits = emptyList(),
            variableTraits = emptyList(),
            contradictoryTraits = emptyList(),
            strongestKnowledgeDomains = emptyList(),
            knowledgeGaps = emptyList()
        )
    }
}
