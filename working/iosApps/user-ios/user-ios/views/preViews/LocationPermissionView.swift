import SwiftUI

struct LocationPermissionView: View {
    let onAllow: () -> Void
    let onSkip: () -> Void

    var body: some View {
        ZStack {
            Color.bg
                .ignoresSafeArea()

            VStack(spacing: 0) {
                Spacer()

                VStack(spacing: 28) {
                    // Icon
                    ZStack {
                        Circle()
                            .fill(Color.brand.opacity(0.12))
                            .frame(width: 100, height: 100)
                        Image(systemName: "location.fill")
                            .font(.system(size: 48))
                            .foregroundStyle(Color.brand)
                    }

                    // Title & subtitle
                    VStack(spacing: 10) {
                        Text("Location Access")
                            .font(.system(size: 28, weight: .bold, design: .rounded))
                            .foregroundStyle(.mainOpp)

                        Text("Mini Mate uses your location to find mini golf courses near you — it's a core feature of the app.")
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                            .multilineTextAlignment(.center)
                            .lineLimit(3)
                    }

                    // Feature rows
                    VStack(alignment: .leading, spacing: 14) {
                        featureRow(
                            icon: "map.fill",
                            color: .blue,
                            title: "Find Nearby Courses",
                            description: "Instantly discover mini golf courses around you"
                        )
                        featureRow(
                            icon: "shield.lefthalf.filled",
                            color: .green,
                            title: "Never Stored or Shared",
                            description: "Your location is used only in the moment — never saved"
                        )
                        featureRow(
                            icon: "iphone.slash",
                            color: .orange,
                            title: "No Background Tracking",
                            description: "We only check location while the app is open"
                        )
                    }
                    .padding(18)
                    .background {
                        RoundedRectangle(cornerRadius: 20, style: .continuous)
                            .fill(Color.sub)
                    }

                    // System prompt heads-up
                    
                }
                .padding(.horizontal, 28)

                Spacer()

                // Buttons
                VStack(spacing: 14) {
                    
                    HStack(spacing: 6) {
                        Image(systemName: "hand.tap.fill")
                            .font(.caption)
                        Text("iOS will show a permission popup — tap \"Allow While Using App\" to continue.")
                            .font(.caption)
                            .multilineTextAlignment(.center)
                    }
                    .foregroundStyle(.secondary)
                    .padding(.horizontal, 8)
                    
                    Button(action: onAllow) {
                        Text("Allow Location")
                            .font(.headline)
                            .foregroundStyle(.white)
                            .frame(maxWidth: .infinity)
                            .frame(height: 54)
                            .background {
                                RoundedRectangle(cornerRadius: 27)
                                    .fill(Color.blue)
                            }
                    }

                    Button(action: onSkip) {
                        Text("Not Now")
                            .font(.subheadline)
                            .foregroundStyle(.blue.opacity(0.7))
                    }
                }
                .padding(.horizontal, 28)
                .padding(.bottom, 44)
            }
        }
    }

    private func featureRow(icon: String, color: Color, title: String, description: String) -> some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 16))
                .foregroundStyle(color)
                .frame(width: 34, height: 34)
                .background(Circle().fill(color.opacity(0.15)))

            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.subheadline.weight(.semibold))
                    .foregroundStyle(.mainOpp)
                Text(description)
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }

            Spacer()
        }
    }
}
