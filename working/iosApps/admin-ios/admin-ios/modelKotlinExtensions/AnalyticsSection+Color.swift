//
//  AnalyticsSection+Color.swift
//  admin-ios
//
//  Created by Garrett Butchko on 3/28/26.
//

import SwiftUI
import shared_admin

extension AnalyticsSection: @retroactive Identifiable {
    var color: Color {
        switch self {
        case .growth:
            return Color.green
        case .operations:
            return Color.purple
        case .experience:
            return Color.red
        case .retention:
            return Color.blue
        }
    }
    
    public var id: String { rawValue }
}

extension ChartTopic {
    var color: Color {
        switch self {
        case .total: return .purple
        case .first: return .blue
        case .returning: return .pink
        }
    }
}

extension Insight {
    var color: Color {
        switch insightType {
        case .good: return .green
        case .average: return .blue
        case .warning: return .orange
        case .critical: return .red
        }
    }
    
    var imageName: String {
        switch insightType {
        case .good: return "checkmark.circle.fill"
        case .average: return "exclamationmark.circle.fill"
        case .warning: return "exclamationmark.triangle.fill"
        case .critical: return "x.circle.fill"
        }
    }
}

extension HealthGrade {
    var color: Color {
        switch self {
        case .excellent, .great:
            return Color(red: 52/255, green: 199/255, blue: 89/255)   // #34C759 green
        case .good, .satisfactory:
            return Color(red: 0/255, green: 136/255, blue: 255/255)   // #0088FF blue
        case .fair:
            return Color(red: 255/255, green: 204/255, blue: 0/255)   // #FFCC00 yellow
        case .needsImprovement:
            return Color(red: 255/255, green: 149/255, blue: 0/255)   // #FF9500 orange
        case .poor:
            return Color(red: 255/255, green: 107/255, blue: 0/255)   // #FF6B00 deep orange
        case .critical:
            return Color(red: 255/255, green: 59/255, blue: 48/255)   // #FF3B30 red
        }
    }
}
    
