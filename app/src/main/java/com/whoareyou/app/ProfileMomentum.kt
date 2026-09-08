package com.whoareyou.app

data class ProfileMomentum(
    val changingCount: Int,
    val stableCount: Int,
    val strongestChange: ProfileDimension?
)

object ProfileMomentumEngine {
    fun derive(dimensions: List<ProfileDimension>): ProfileMomentum {
        val withChange = dimensions.filter { it.change != null }
        val changing = withChange.filter { dimension ->
            val delta = dimension.change?.delta ?: 0
            kotlin.math.abs(delta) >= 5
        }
        val stable = withChange.size - changing.size
        val strongest = changing.maxByOrNull { dimension ->
            kotlin.math.abs(dimension.change?.delta ?: 0)
        }

        return ProfileMomentum(
            changingCount = changing.size,
            stableCount = stable,
            strongestChange = strongest
        )
    }
}
