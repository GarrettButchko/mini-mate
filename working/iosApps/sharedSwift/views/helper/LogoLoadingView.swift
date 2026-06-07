import SwiftUI

struct LogoLoadingView: View {
    @State private var isAnimating = false
    var size: CGFloat = 50

    var body: some View {
        Image("logoOpp")
            .resizable()
            .aspectRatio(contentMode: .fit)
            .frame(width: size, height: size)
            .grayscale(1.0)
            .opacity(0.5)
            .rotationEffect(.degrees(isAnimating ? 360 : 0))
            .animation(
                .linear(duration: 1.2).repeatForever(autoreverses: false),
                value: isAnimating
            )
            .onAppear { isAnimating = true }
    }
}
