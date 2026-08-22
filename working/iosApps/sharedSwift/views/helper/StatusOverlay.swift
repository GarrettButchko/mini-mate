import SwiftUI
@_exported import StatusOverlayKit

// MiniMate-branded loading overlay: swaps the generic ProgressView for the
// spinning app logo. Shadows StatusOverlayKit.LoadingOverlay so every
// LoadingOverlay(message:) call site in this target gets the branded version.
struct LoadingOverlay: View {
    let message: String

    var body: some View {
        StatusOverlayKit.LoadingOverlay(message: message) { LogoLoadingView() }
    }
}
