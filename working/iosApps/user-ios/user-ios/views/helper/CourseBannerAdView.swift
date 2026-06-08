import SwiftUI
import GoogleMobileAds
import shared_user

struct CourseBannerAdView: View {
    @EnvironmentObject var gameModel: GameViewModelSwift

    var showColor: Bool = false
    let adUnitID: String
    let isGuest: Bool
    let isPro: Bool
    let isConnected: Bool
    var showBackground: Bool = false

    @State private var preloadedBannerView: BannerView?

    private var shouldShowAds: Bool {
        ScoreCardBusinessLogic().shouldShowAds(
            isConnected: isConnected,
            isGuest: isGuest,
            isPro: KotlinBoolean(bool: isPro)
        )
    }

    var body: some View {
        Group {
            if let course = gameModel.course, course.customAdActive {
                let adBgColor: Color? = showColor ? course.scoreCardColor : nil

                Button {
                    if let urlString = course.adLink, let url = URL(string: urlString) {
                        UIApplication.shared.open(url)
                    }
                } label: {
                    HStack {
                        VStack(alignment: .leading) {
                            Text(course.adTitle ?? "")
                                .font(.system(size: 16, weight: .semibold))
                                .foregroundColor(.mainOpp)
                            Text(course.adDescription ?? "")
                                .font(.system(size: 12))
                                .foregroundColor(.mainOpp.opacity(0.6))
                        }
                        Spacer()
                        if let adImage = course.adImage, !adImage.isEmpty {
                            AsyncImage(url: URL(string: adImage)) { phase in
                                switch phase {
                                case .success(let image):
                                    image.resizable()
                                        .scaledToFill()
                                        .frame(width: 50, height: 50)
                                        .clipShape(RoundedRectangle(cornerRadius: 12))
                                default:
                                    Color.gray.opacity(0.2)
                                        .frame(width: 50, height: 50)
                                        .clipShape(RoundedRectangle(cornerRadius: 12))
                                }
                            }
                        }
                    }
                    .padding(16)
                }
                .background {
                    if showBackground {
                        RoundedRectangle(cornerRadius: 25)
                            .subVsColor(makeColor: adBgColor)
                    }
                }
                .cardShadow()
            } else if shouldShowAds, let preloadedBannerView {
                let adBgColor: Color? = showColor ? gameModel.course?.scoreCardColor : nil

                BannerAdView(bannerView: preloadedBannerView)
                    .background {
                        if showBackground {
                            RoundedRectangle(cornerRadius: 25)
                                .subVsColor(makeColor: adBgColor)
                        }
                    }
                    .cardShadow()
            }
        }
        .task {
            guard shouldShowAds, preloadedBannerView == nil else { return }
            let loader = BannerAdLoader(adUnitID: adUnitID)
            preloadedBannerView = await loader.load()
        }
    }
}
