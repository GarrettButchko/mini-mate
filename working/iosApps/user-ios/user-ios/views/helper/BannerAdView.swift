import SwiftUI
import GoogleMobileAds

// Google's intended preloading pattern: instantiate a BannerView early, kick off
// load(), and hand the same instance into the view hierarchy when ready.
// `BannerAdLoader` owns the BannerView + delegate together so they stay alive
// for the duration of the async load.
@MainActor
final class BannerAdLoader: NSObject, BannerViewDelegate {
    private let bannerView: BannerView
    private var continuation: CheckedContinuation<BannerView?, Never>?

    init(adUnitID: String, adSize: AdSize = AdSizeBanner) {
        self.bannerView = BannerView(adSize: adSize)
        super.init()
        bannerView.adUnitID = adUnitID
        bannerView.rootViewController = UIApplication.shared.getRootViewController()
        bannerView.delegate = self
    }

    func load() async -> BannerView? {
        await withCheckedContinuation { continuation in
            self.continuation = continuation
            bannerView.load(Request())
        }
    }

    nonisolated func bannerViewDidReceiveAd(_ bannerView: BannerView) {
        Task { @MainActor in
            continuation?.resume(returning: bannerView)
            continuation = nil
        }
    }

    nonisolated func bannerView(_ bannerView: BannerView, didFailToReceiveAdWithError error: Error) {
        print("Banner ad failed to load: \(error.localizedDescription)")
        Task { @MainActor in
            continuation?.resume(returning: nil)
            continuation = nil
        }
    }
}

struct BannerAdView: View {
    let bannerView: BannerView
    let adSize: AdSize

    @State private var isAdLoaded = false

    init(bannerView: BannerView, adSize: AdSize = AdSizeBanner) {
        self.bannerView = bannerView
        self.adSize = adSize
    }

    var body: some View {
        BannerViewContainer(bannerView: bannerView)
            .frame(height: adSize.size.height)
            .padding(16)
            .animation(.easeInOut(duration: 0.25), value: isAdLoaded)
    }
}

private struct BannerViewContainer: UIViewRepresentable {
    typealias UIViewType = BannerView

    let bannerView: BannerView

    func makeUIView(context: Context) -> BannerView {
        bannerView
    }

    func updateUIView(_ uiView: BannerView, context: Context) {
        if uiView.rootViewController == nil {
            uiView.rootViewController = UIApplication.shared.getRootViewController()
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

// Demo view showing the full preload-then-display flow.
// Uses Google's public test ad unit so it's safe to run.
struct BannerAdDemoView: View {
    private static let testAdUnitID = "ca-app-pub-3940256099942544/2435281174"

    @State private var preloadedBanner: BannerView?
    @State private var didFail = false

    var body: some View {
        VStack(spacing: 24) {
            Text("Banner Ad Demo")
                .font(.title2.weight(.semibold))

            if let preloadedBanner {
                BannerAdView(bannerView: preloadedBanner)
            } else if didFail {
                Text("Ad failed to load")
                    .foregroundStyle(.secondary)
            } else {
                ProgressView("Preloading ad…")
            }

            Spacer()
        }
        .padding()
        .task {
            let loader = BannerAdLoader(adUnitID: BannerAdDemoView.testAdUnitID)
            if let banner = await loader.load() {
                preloadedBanner = banner
            } else {
                didFail = true
            }
        }
    }
}

#Preview {
    BannerAdDemoView()
}
