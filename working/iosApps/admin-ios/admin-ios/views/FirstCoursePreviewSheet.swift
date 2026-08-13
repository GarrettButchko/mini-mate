//
//  FirstCoursePreviewSheet.swift
//  MiniMate Manager
//

import SwiftUI
import shared_admin

// MARK: - Sheet wrapper (requires Kotlin VM)

struct FirstCoursePreviewSheet: View {
    @EnvironmentObject var viewModel: CourseListViewModelSwift
    @EnvironmentObject var viewManager: ViewManagerSwift
    @Binding var isPresented: Bool

    @StateObject private var analyticsVM = AnalyticsViewModelSwift()

    private var course: Course? { viewModel.userCourses.first }

    var body: some View {
        ZStack {
            Color.bg.ignoresSafeArea()

            if let course {
                FirstCoursePreviewBody(
                    courseName: course.name,
                    courseID: course.id,
                    tier: Int(course.tier),
                    numHoles: Int(course.numHoles),
                    leaderBoardActive: course.leaderBoardActive,
                    healthReport: analyticsVM.healthReport,
                    isLoadingHealth: analyticsVM.isLoadingHealth,
                    onOpenDashboard: {
                        viewModel.kotlin.setCourse(course: course)
                        viewManager.kotlinVM.navigateToCourseTab(tab: 1)
                    },
                    onBack: { isPresented = false }
                )
                .task(id: course.id) {
                    analyticsVM.kotlin.loadHealthData(course: course)
                }
            }

            if viewModel.loadingCourse || course == nil {
                LoadingOverlay(message: "Loading course details...")
                    .transition(.opacity.combined(with: .scale(scale: 0.95)))
            }
        }
        .animation(statusOverlaySpring, value: viewModel.loadingCourse)
        .animation(statusOverlaySpring, value: course == nil)
        .animation(statusOverlaySpring, value: analyticsVM.healthReport != nil)
        .presentationDragIndicator(.visible)
    }
}

// MARK: - Layout (plain Swift values, previewable)

private struct FirstCoursePreviewBody: View {
    let courseName: String
    let courseID: String
    let tier: Int
    let numHoles: Int
    let leaderBoardActive: Bool
    let healthReport: CourseHealthReport?
    let isLoadingHealth: Bool
    let onOpenDashboard: () -> Void
    let onBack: () -> Void

    @State private var isExpanded = false

    private struct HealthSection {
        let name: String
        let color: Color
        let icon: String
        let rating: SectionHealthRating?
    }

    private var healthSections: [HealthSection] {
        [
            HealthSection(name: "Growth",     color: .green,  icon: "chart.line.uptrend.xyaxis", rating: healthReport?.growthHealth),
            HealthSection(name: "Experience", color: .red,    icon: "star",                      rating: healthReport?.experienceHealth),
            HealthSection(name: "Operations", color: .purple, icon: "clock",                     rating: healthReport?.operationsHealth),
            HealthSection(name: "Retention",  color: .blue,   icon: "arrow.clockwise",           rating: healthReport?.retentionHealth),
        ]
    }

    private struct Feature: Identifiable {
        let icon: String
        let text: String
        let tier: Int
        let isSoon: Bool

        var id: String { text }
    }

    private var allFeatures: [Feature] {
        [
            Feature(icon: "mappin.and.ellipse",        text: "Course listed on the Mini Mate map",           tier: 1, isSoon: false),
            Feature(icon: "slider.horizontal.3",       text: "Custom par info for every hole",               tier: 1, isSoon: false),
            Feature(icon: "link",                      text: "Social links on your course profile",          tier: 1, isSoon: false),
            Feature(icon: "paintpalette.fill",         text: "Custom scorecard colors & accent",             tier: 2, isSoon: false),
            Feature(icon: "megaphone.fill",            text: "Your ad in scorecards & app pages",            tier: 2, isSoon: false),
            Feature(icon: "photo.fill",                text: "Your logo in the app home & scorecard",        tier: 2, isSoon: false),
            Feature(icon: "trophy.fill",               text: "Managed course leaderboard",                   tier: 2, isSoon: false),
            Feature(icon: "bolt.fill",                 text: "Tournament mode access",                       tier: 2, isSoon: true),
            Feature(icon: "gift.fill",                 text: "Mini Mate pitch tent & marketing kit",         tier: 2, isSoon: false),
            Feature(icon: "wave.3.right",              text: "NFC-enabled tent tag (Mini Mate branded)",     tier: 2, isSoon: false),
            Feature(icon: "chart.line.uptrend.xyaxis", text: "Growth analytics dashboard",                    tier: 3, isSoon: false),
            Feature(icon: "arrow.clockwise",           text: "Player retention tracking",                    tier: 3, isSoon: false),
            Feature(icon: "star.fill",                 text: "Experience score metrics",                     tier: 3, isSoon: false),
            Feature(icon: "envelope.fill",             text: "Player email list from your course",           tier: 3, isSoon: false),
            Feature(icon: "arrow.down.doc.fill",       text: "CSV export — all data, any time",              tier: 3, isSoon: false),
            Feature(icon: "square.3.layers.3d",        text: "Beautiful analytics interface",                tier: 3, isSoon: false),
        ]
    }

    let TP: CGFloat = 30
    let BP: CGFloat = 20

    var body: some View {
        ZStack {
            ScrollView(.vertical, showsIndicators: false) {
                VStack(spacing: 20) {
                    tierCard
                    healthPreview
                    featuresPreview
                    Spacer()
                }
            }
            .contentMargins(.top, TP + 64)
            .contentMargins(.horizontal, 20)
            .contentMargins(.bottom, BP + 80)
            
            VStack(alignment: .center) {
                header
                    .padding(.horizontal, 20)
                    .padding(.top, TP)
                    .frame(maxWidth: .infinity)
                    .background(
                        LinearGradient(
                            gradient: Gradient(colors: [
                                Color.bg.opacity(1),
                                Color.clear
                            ]),
                            startPoint: .top,
                            endPoint: .bottom
                        )
                        .ignoresSafeArea(edges: .top)
                    )
                Spacer()
            }
           
            
            VStack {
                Spacer()
                ctaButtons
                    .padding([.horizontal, .bottom], BP)
                    .frame(maxWidth: .infinity)
                    .background(
                        LinearGradient(
                            gradient: Gradient(colors: [
                                Color.clear,
                                Color.bg.opacity(1)
                            ]),
                            startPoint: .top,
                            endPoint: .bottom
                        )
                        .ignoresSafeArea(edges: .bottom)
                    )
            }
            
            
        }
    }

    // MARK: - Header

    private var header: some View {
        VStack(alignment: .center, spacing: 4) {
            Text(courseName)
                .font(.title2)
                .fontWeight(.bold)
                .foregroundStyle(.mainOpp)

            Text("Course ID: \(courseID)")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
    }

    // MARK: - Tier card

    private var tierCard: some View {
        HStack {
            Image(systemName: tierIcon)
                .font(.title3)
                .foregroundStyle(.blue)

            VStack(alignment: .leading, spacing: 4) {
                Text("\(tierName) Plan")
                    .font(.headline)
                    .fontWeight(.bold)

                Text("\(numHoles) holes\(leaderBoardActive ? " · Leaderboard active" : "")")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }
            Spacer()
            Text(tierPrice)
                .font(.subheadline)
                .foregroundStyle(.secondary)
        }
        .padding()
        .background {
            RoundedRectangle(cornerRadius: 25)
                .fill(.sub)
                .cardShadow()
        }
    }

    // MARK: - Health preview

    private var healthPreview: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Already Being Tracked")
                .font(.headline)
                .fontWeight(.bold)

            let rows = stride(from: 0, to: healthSections.count, by: 2).map {
                Array(healthSections[$0..<min($0 + 2, healthSections.count)])
            }

            VStack(spacing: 8) {
                ForEach(Array(rows.enumerated()), id: \.offset) { _, row in
                    HStack(spacing: 8) {
                        ForEach(row, id: \.name) { section in
                            healthCard(section: section)
                        }
                    }
                }
            }
        }
        .padding()
        .background {
            RoundedRectangle(cornerRadius: 25)
                .fill(.sub)
                .cardShadow()
        }
    }

    @ViewBuilder
    private func healthCard(section: HealthSection) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack(spacing: 4) {
                Image(systemName: section.icon)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                Text(section.name)
                    .font(.caption)
                    .foregroundStyle(.primary)
            }

            if let rating = section.rating {
                Text(rating.grade.rawValue)
                    .font(.title)
                    .fontWeight(.bold)
                    .foregroundStyle(rating.grade.color)
                    .transition(.opacity.combined(with: .scale(scale: 0.9)))

                Text("Score: \(Int(rating.score))")
                    .font(.caption2)
                    .foregroundStyle(.secondary)

            } else {
                Text("N/A")
                    .font(.title)
                    .fontWeight(.bold)
                    .foregroundStyle(.secondary.opacity(0.3))
                    .skeleton(active: isLoadingHealth)

                Text("Score: —")
                    .font(.caption2)
                    .foregroundStyle(.secondary)
                    .skeleton(active: isLoadingHealth)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(12)
        .background {
            RoundedRectangle(cornerRadius: 13)
                .fill(.subTwo)
                .overlay {
                    RoundedRectangle(cornerRadius: 13)
                        .stroke(section.color.opacity(0.3), lineWidth: 1)
                }
        }
    }

    // MARK: - Features preview

    private var featuresPreview: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("What's Included")
                .font(.headline)
                .fontWeight(.bold)

            let displayFeatures = isExpanded ? allFeatures : Array(allFeatures.prefix(4))

            VStack(spacing: 0) {
                ForEach(displayFeatures) { feature in
                    let included = feature.tier <= tier

                    HStack(spacing: 10) {
                        Image(systemName: included ? feature.icon : "lock.fill")
                            .font(.caption)
                            .foregroundStyle(included ? .blue : .secondary.opacity(0.4))
                            .frame(width: 14)

                        Text(feature.text)
                            .font(.subheadline)
                            .foregroundStyle(included ? .primary : .secondary)

                        if feature.isSoon {
                            Text("SOON")
                                .font(.caption2)
                                .fontWeight(.bold)
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(Capsule().fill(.green.opacity(0.2)))
                                .foregroundStyle(.green)
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.vertical, 8)
                }

                Button {
                    withAnimation(.spring(response: 0.4, dampingFraction: 0.8)) {
                        isExpanded.toggle()
                    }
                } label: {
                    HStack(spacing: 6) {
                        Text(isExpanded ? "Show Less" : "View All Features")
                        Image(systemName: isExpanded ? "chevron.up" : "chevron.down")
                    }
                    .font(.system(size: 14, weight: .semibold))
                    .padding(.top, 12)
                    .frame(maxWidth: .infinity)
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background {
            RoundedRectangle(cornerRadius: 25)
                .fill(.sub)
                .cardShadow()
        }
    }

    // MARK: - CTAs

    private var ctaButtons: some View {
        VStack(spacing: 10) {
            Button(action: onOpenDashboard) {
                Text("Open Dashboard")
                    .fontWeight(.semibold)
                    .frame(maxWidth: .infinity)
                    .frame(height: 50)
                    .background {
                        RoundedRectangle(cornerRadius: 18)
                            .fill(.blue)
                    }
                    .foregroundStyle(.white)
            }

            Button(action: onBack) {
                Text("Back to Courses")
                    .font(.subheadline)
            }
        }
    }

    // MARK: - Helpers

    private var tierName: String {
        switch tier {
        case 1: return "Listed"
        case 2: return "Partner"
        case 3: return "Analytics"
        default: return "Listed"
        }
    }

    private var tierPrice: String {
        switch tier {
        case 1: return "Free"
        case 2: return "$29 / mo"
        case 3: return "$69 / mo"
        default: return "Free"
        }
    }

    private var tierIcon: String {
        switch tier {
        case 1: return "mappin.and.ellipse"
        case 2: return "building.2.fill"
        case 3: return "chart.bar.fill"
        default: return "mappin.and.ellipse"
        }
    }
}
