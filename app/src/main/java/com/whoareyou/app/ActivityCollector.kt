package com.whoareyou.app

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

interface ActivityDataSource {
    suspend fun state(): BehaviorSourceState
    suspend fun readSteps(start: Instant, end: Instant): Long?
}

class ActivityCollector(
    private val dataSource: ActivityDataSource,
    private val zone: ZoneId = ZoneId.systemDefault()
) {
    suspend fun collectDay(date: LocalDate): BehaviorCollectionResult<ActivityDay> {
        val state = runCatching { dataSource.state() }
            .getOrElse { return BehaviorCollectionResult.Unavailable(BehaviorSourceState.ERROR) }

        if (state != BehaviorSourceState.AVAILABLE) {
            return BehaviorCollectionResult.Unavailable(state)
        }

        val start = date.atStartOfDay(zone).toInstant()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant()
        val steps = runCatching { dataSource.readSteps(start, end) }
            .getOrElse { return BehaviorCollectionResult.Unavailable(BehaviorSourceState.ERROR) }
            ?: return BehaviorCollectionResult.NoData

        if (steps < 0L) {
            return BehaviorCollectionResult.Unavailable(BehaviorSourceState.ERROR)
        }

        return BehaviorCollectionResult.Data(
            ActivityDay(
                epochDay = date.toEpochDay(),
                steps = steps
            )
        )
    }
}

class HealthConnectActivityDataSource(context: Context) : ActivityDataSource {
    private val appContext = context.applicationContext

    override suspend fun state(): BehaviorSourceState {
        if (HealthConnectClient.getSdkStatus(appContext) != HealthConnectClient.SDK_AVAILABLE) {
            return BehaviorSourceState.UNSUPPORTED
        }
        val granted = client().permissionController.getGrantedPermissions()
        return if (READ_STEPS_PERMISSION in granted) {
            BehaviorSourceState.AVAILABLE
        } else {
            BehaviorSourceState.PERMISSION_REQUIRED
        }
    }

    override suspend fun readSteps(start: Instant, end: Instant): Long? {
        val response = client().aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        )
        return response[StepsRecord.COUNT_TOTAL]
    }

    private fun client(): HealthConnectClient = HealthConnectClient.getOrCreate(appContext)

    companion object {
        val READ_STEPS_PERMISSION: String =
            HealthPermission.getReadPermission(StepsRecord::class)
    }
}
