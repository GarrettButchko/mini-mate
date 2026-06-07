//
//  SectionStatsView.swift
//  MiniMate
//
//  Created by Garrett Butchko on 5/13/25.
//
import SwiftUI
import SwiftData
import shared_user

struct SectionStatsView<Content: View>: View {
    @Environment(\.colorScheme) private var colorScheme
    var title: String
    var subtitle: String? = nil
    var spacing: CGFloat

    var makeColor: Color? = nil
    @ViewBuilder var content: () -> Content
    var body: some View {
        VStack(alignment: .leading, spacing: spacing) {
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(.title3).fontWeight(.bold)
                        .foregroundStyle(.mainOpp)
                    if let subtitle {
                        Text(subtitle)
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
                Spacer()
            }
            content()
        }
        .padding()
        .background{
            RoundedRectangle(cornerRadius: 25)
                .subVsColor(makeColor: makeColor)
                .cardShadow()
        }
    }
}
