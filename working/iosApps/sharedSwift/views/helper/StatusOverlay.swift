//
//  StatusOverlay.swift
//  MiniMate
//
//  Shared success/loading overlay used across admin-ios and user-ios.
//  Mirrors ColorPickerView's glass-card look (ifAGENoDefaultColor + cardShadow)
//  so transient status moments read as one consistent design language.
//

import SwiftUI
import Combine

/// The spring MiniMate's transient popups (ColorPickerView, SuccessOverlay,
/// LoadingOverlay) show and dismiss with. Reuse this instead of inventing a
/// new curve so every popup enters and exits with the same feel.
let statusOverlaySpring: Animation = .spring(response: 0.35, dampingFraction: 0.8)

/// Flips `flag` on with `statusOverlaySpring`, holds for `duration`, then flips
/// it off with the same spring — the show/auto-dismiss pattern shared by every
/// transient success overlay in the app.
func presentTransientOverlay(_ flag: Binding<Bool>, duration: Double = 1.5) {
    withAnimation(statusOverlaySpring) {
        flag.wrappedValue = true
    }
    DispatchQueue.main.asyncAfter(deadline: .now() + duration) {
        withAnimation(statusOverlaySpring) {
            flag.wrappedValue = false
        }
    }
}

/// Message-carrying variant of the above: shows `message`, holds for
/// `duration`, then clears it — unless a newer message replaced it meanwhile.
func presentTransientOverlay(
    _ current: Binding<StatusMessage?>,
    _ message: StatusMessage,
    duration: Double = 1.5
) {
    withAnimation(statusOverlaySpring) {
        current.wrappedValue = message
    }
    DispatchQueue.main.asyncAfter(deadline: .now() + duration) {
        withAnimation(statusOverlaySpring) {
            if current.wrappedValue == message {
                current.wrappedValue = nil
            }
        }
    }
}

struct SuccessOverlay: View {
    let title: String
    let subtitle: String
    // Defaults keep the classic green-checkmark success look; override for
    // actions that aren't celebratory (logout, deletion) so the icon matches
    // the tone without resorting to alarming warning colors.
    var icon: String = "checkmark.circle.fill"
    var tint: Color = .green

    var body: some View {
        ZStack {
            Color.black.opacity(0.25)
                .ignoresSafeArea()

            VStack(spacing: 24) {
                Image(systemName: icon)
                    .font(.system(size: 56, weight: .medium))
                    .foregroundStyle(tint)

                VStack(spacing: 6) {
                    Text(title)
                        .font(.system(.title3, weight: .semibold))

                    Text(subtitle)
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                        .multilineTextAlignment(.center)
                }
            }
            .padding(24)
            .background {
                RoundedRectangle(cornerRadius: 38, style: .continuous)
                    .ifAGENoDefaultColor()
                    .cardShadow()
            }
            .padding(.horizontal, 30)
            .transition(.scale(scale: 0.9).combined(with: .opacity))
        }
    }
}

struct LoadingOverlay: View {
    let message: String

    var body: some View {
        ZStack {
            Color.black.opacity(0.25)
                .ignoresSafeArea()

            VStack(spacing: 20) {
                LogoLoadingView()

                Text(message)
                    .font(.system(.subheadline, weight: .medium))
                    .foregroundStyle(.secondary)
            }
            .padding(28)
            .background {
                RoundedRectangle(cornerRadius: 38, style: .continuous)
                    .ifAGENoDefaultColor()
                    .cardShadow()
            }
            .padding(.horizontal, 30)
            .transition(.scale(scale: 0.9).combined(with: .opacity))
        }
    }
}

/// A one-shot success message posted by a screen right before it navigates
/// away, and consumed by whichever screen it lands on next (e.g. logging out
/// or deleting an account both land on `SignInView`, which has no other way
/// to know why it's being shown).
struct StatusMessage: Equatable {
    let title: String
    let subtitle: String
    var icon: String = "checkmark.circle.fill"
    var tint: Color = .green
}

final class TransientStatusSignal: ObservableObject {
    static let shared = TransientStatusSignal()
    private init() {}

    @Published var pending: StatusMessage? = nil
}

/// Attach to any screen that can be the landing point of a
/// `TransientStatusSignal` (e.g. SignInView after logout / account deletion).
/// Consumes the pending message once on appear and shows it as a transient
/// `SuccessOverlay` with the standard show/auto-dismiss timing.
private struct ConsumesTransientStatus: ViewModifier {
    @State private var statusMessage: StatusMessage? = nil

    func body(content: Content) -> some View {
        content
            .overlay {
                if let statusMessage {
                    SuccessOverlay(
                        title: statusMessage.title,
                        subtitle: statusMessage.subtitle,
                        icon: statusMessage.icon,
                        tint: statusMessage.tint
                    )
                }
            }
            .animation(statusOverlaySpring, value: statusMessage)
            .onAppear {
                guard let pending = TransientStatusSignal.shared.pending else { return }
                TransientStatusSignal.shared.pending = nil

                withAnimation(statusOverlaySpring) {
                    statusMessage = pending
                }
                DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
                    withAnimation(statusOverlaySpring) {
                        statusMessage = nil
                    }
                }
            }
    }
}

extension View {
    func consumesTransientStatus() -> some View {
        modifier(ConsumesTransientStatus())
    }
}
