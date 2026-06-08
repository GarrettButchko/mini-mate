import SwiftUI
import GoogleMobileAds

struct BannerAdView: View {
    let adUnitID: String
    @Binding var isAdLoaded: Bool

    init(adUnitID: String, isAdLoaded: Binding<Bool> = .constant(false)) {
        self.adUnitID = adUnitID
        self._isAdLoaded = isAdLoaded
    }

    var body: some View {
        _BannerAdRepresentable(adUnitID: adUnitID, isAdLoaded: $isAdLoaded)
            .frame(height: isAdLoaded ? 50 : 0)
            .padding(isAdLoaded ? 16 : 0)
            .animation(.easeInOut(duration: 0.25), value: isAdLoaded)
    }
}

private struct _BannerAdRepresentable: UIViewRepresentable {
    let adUnitID: String
    @Binding var isAdLoaded: Bool

    func makeUIView(context: Context) -> BannerView {
        let bannerView = BannerView(adSize: AdSizeBanner)
        bannerView.adUnitID = adUnitID
        bannerView.delegate = context.coordinator
        bannerView.load(Request())
        return bannerView
    }

    func updateUIView(_ uiView: BannerView, context: Context) {
        if uiView.rootViewController == nil {
            uiView.rootViewController = UIApplication.shared.getRootViewController()
        }
    }

    func makeCoordinator() -> Coordinator { Coordinator(self) }

    class Coordinator: NSObject, BannerViewDelegate {
        var parent: _BannerAdRepresentable
        init(_ parent: _BannerAdRepresentable) { self.parent = parent }

        func bannerViewDidReceiveAd(_ bannerView: BannerView) {
            DispatchQueue.main.async { self.parent.isAdLoaded = true }
        }

        func bannerView(_ bannerView: BannerView, didFailToReceiveAdWithError error: Error) {
            DispatchQueue.main.async { self.parent.isAdLoaded = false }
        }
    }
}

extension UIApplication {
    func getRootViewController() -> UIViewController? {
        guard let scene = connectedScenes.first as? UIWindowScene,
              let root = scene.windows.first(where: \.isKeyWindow)?.rootViewController else {
            return nil
        }
        return root
    }
}
