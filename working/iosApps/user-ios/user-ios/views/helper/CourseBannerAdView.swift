import SwiftUI
import shared

struct CourseBannerAdView: View {
    let adUnitID: String
    let course: Course?
    let isGuest: Bool
    let isPro: Bool
    let isConnected: Bool
    var showBackground: Bool = false

    @State private var adLoaded = false

    var body: some View {
        if let course = course, course.customAdActive {
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
                            .foregroundColor(.subTwoOpp)
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
                .padding()
            }
            .frame(height: 50)
            .background(Color.clear)
        } else if shouldShowAds {
            BannerAdView(adUnitID: adUnitID, isAdLoaded: $adLoaded)
                .background(backgroundView)
        }
    }

    private var shouldShowAds: Bool {
        return isConnected && (isGuest || !isPro)
    }

    @ViewBuilder
    private var backgroundView: some View {
        if adLoaded && showBackground {
            if let course = course, let scoreCardColor = course.scoreCardColor {
                Color.subVsColor(makeColor: scoreCardColor)
                    .clipShape(RoundedRectangle(cornerRadius: 25))
            } else {
                Color.sub
                    .cardShadow()
                    .clipShape(RoundedRectangle(cornerRadius: 25))
            }
        }
    }
}
