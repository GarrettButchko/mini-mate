//
//  OperationsViewModelSwift.swift
//  admin-ios
//

import SwiftUI
import Combine
import shared_admin

class OperationsViewModelSwift: ObservableObject {
    let kotlin: OperationsViewModel

    init(kotlin: OperationsViewModel) {
        self.kotlin = kotlin
    }

    var rangeDailyDocs: [DailyDoc] {
        get { kotlin.rangeDailyDocs }
        set { kotlin.rangeDailyDocs = newValue }
    }
    
    var deltaDailyDocs: [DailyDoc] {
        get { kotlin.deltaDailyDocs }
        set { kotlin.deltaDailyDocs = newValue }
    }
    
    var range: AnalyticsRange {
        get { kotlin.range }
        set { kotlin.range = newValue }
    }

    func avgGamesPerDay(docs: [DailyDoc]) -> Double {
        return kotlin.avgGamesPerDay(docs: docs)
    }
    
    func avgGamesPerDayPrime() -> DataPointObject {
        return kotlin.avgGamesPerDayPrime()
    }
    
    func avgPlayersPerGame(docs: [DailyDoc]) -> Double {
        return kotlin.avgPlayersPerGame(docs: docs)
    }
    
    func avgPlayersPerGamePrime() -> DataPointObject {
        return kotlin.avgPlayersPerGamePrime()
    }
    
    func getBusiestHour() -> DataPointObject {
        return kotlin.getBusiestHour()
    }

    func getBusiestDay() -> DataPointObject {
        return kotlin.getBusiestDay()
    }
    
    func prepareChartData() -> [HourData] {
        return kotlin.prepareChartData()
    }

    func getDataForGamesPerDay() -> [PlayerActivity] {
        return kotlin.getDataForGamesPerDay()
    }
    
    func getAvgGameDuration() -> DataPointObject {
        return kotlin.getAvgGameDuration()
    }
    
    func getTotalPlayTime() -> DataPointObject {
        return kotlin.getTotalPlayTime()
    }
    
    func getFastestGameTime() -> DataPointObject {
        return kotlin.getFastestGameTime()
    }
    
    func getSlowestGameTime() -> DataPointObject {
        return kotlin.getSlowestGameTime()
    }
    
    func getDataForDurationTrend() -> [GameDurationActivity] {
        return kotlin.getDataForDurationTrend()
    }
}
