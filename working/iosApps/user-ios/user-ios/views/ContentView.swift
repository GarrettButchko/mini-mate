import SwiftUI
import MapKit
import shared_user

struct ContentView: View {
    @Environment(\.scenePhase) private var scenePhase
    
    @StateObject private var viewManager = ViewManagerSwift()
    @StateObject var locationHandler = LocationHandlerSwift()
    @StateObject private var authModel = AuthViewModelSwift()
    @StateObject private var gameModel = GameViewModelSwift()
    
    @StateObject var hostVM = HostViewModelSwift()
    
    @State private var previousView: ViewType?
    @State private var showLocationExplainer = false

    private static let locationExplainerKey = "hasSeenLocationExplainer"
    
    // need this for guest host view
    @State private var showHost: Bool = true
    
    var body: some View {
        ZStack {
            viewContent
                .transition(currentTransition)
        }
        .environmentObject(gameModel)
        .environmentObject(authModel)
        .environmentObject(viewManager)
        .environmentObject(hostVM)
        .environmentObject(locationHandler)
        .animation(.easeInOut(duration: 0.4), value: viewManager.currentView)
        .onAppear {
            let status = CLLocationManager().authorizationStatus
            if status != .notDetermined || UserDefaults.standard.bool(forKey: Self.locationExplainerKey) {
                locationHandler.requestLocationAccess()
            }
        }
        .onChange(of: viewManager.currentView) { oldValue, newValue in
            previousView = oldValue
            if case .signIn = onEnum(of: newValue),
               !UserDefaults.standard.bool(forKey: Self.locationExplainerKey),
               CLLocationManager().authorizationStatus == .notDetermined {
                showLocationExplainer = true
            }
        }
        .onChange(of: scenePhase) { oldPhase, newPhase in
            switch newPhase {
            case .active:
                print("App is active")
                // Only re-request if a decision has already been made — if still
                // .notDetermined, the next cold start (onAppear) will handle it.
                if CLLocationManager().authorizationStatus != .notDetermined {
                    locationHandler.requestLocationAccess()
                }
            case .inactive:
                print("App is inactive")
            case .background:
                print("App moved to background")
            @unknown default:
                break
            }
        }
        .sheet(isPresented: $showLocationExplainer) {
            LocationPermissionView {
                UserDefaults.standard.set(true, forKey: Self.locationExplainerKey)
                showLocationExplainer = false
                locationHandler.requestLocationAccess()
            } onSkip: {
                UserDefaults.standard.set(true, forKey: Self.locationExplainerKey)
                showLocationExplainer = false
            }
            .presentationDragIndicator(.visible)
        }
        .ignoresSafeArea(.all, edges: .bottom)
    }
    
    @ViewBuilder
    private var viewContent: some View {
        switch onEnum(of: viewManager.currentView) {
        case .main(tab: let main):
            MainTabView(selectedTab: Int(main.tab))
        case .welcome:
            WelcomeView()
        case .scoreCard(let scorecard):
            ScoreCardView(isGuest: scorecard.isGuest)
        case .ad(let ad):
            InterstitialAdView(adUnitID: "ca-app-pub-8261962597301587/3394145015") {
                if ad.isGuest {
                    viewManager.kotlinVM.navigateToSignIn()
                } else {
                    viewManager.currentView = ViewType.Main(tab: 1)
                }
            }
        case .signIn:
            SignInView()
        case .host:
            HostView(showHost: $showHost, isGuest: true)
        }
    }
    
    // MARK: - Custom transition based on view switch
    private var currentTransition: AnyTransition {
        // Fallback to Welcome for the transition logic if previous is nil
        let prev = previousView ?? ViewType.Welcome.shared
        
        switch (onEnum(of: prev), onEnum(of: viewManager.currentView)) {
        case (_, .main):
            return .opacity.combined(with: .scale)
            
        case (_, .welcome):
            return .opacity
            
        default:
            return .opacity
        }
    }
}

struct MainTabView: View {
    @EnvironmentObject var viewManager: ViewManagerSwift
    @EnvironmentObject var authModel: AuthViewModelSwift
    @EnvironmentObject var gameModel: GameViewModelSwift
    @EnvironmentObject var hostVM: HostViewModelSwift
    @EnvironmentObject var locationHandler: LocationHandlerSwift
    
    @StateObject private var gameManager = GameManagerSwift()
    @StateObject var iapManager = IAPManager()
    
    var userRepo: UserRepository = KoinHelperParent.shared.getUserRepo()
    var localUserRepo: LocalUserRepository = KoinHelperParent.shared.getLocalUserRepo()
    
    @State var selectedTab: Int
    @State private var loadedTabs: Set<Int> = [1] // Home tab loaded by default
    @State private var initialLoadComplete = false
    
    init(selectedTab: Int){
        self._selectedTab = State(initialValue: selectedTab)
    }
    
    var body: some View {
        ZStack {
            Color(.red)
                .ignoresSafeArea()
            
            tabContent
            loadingOverlay
        }
        .onAppear {
            initializeAppData()
        }
        .environmentObject(iapManager)
        .environmentObject(gameManager)
    }
    
    @ViewBuilder
    private var tabContent: some View {
        TabView(selection: $selectedTab) {
            // Lazy load Stats tab
            Group {
                if loadedTabs.contains(0) {
                    StatsView()
                } else {
                    Color.clear.onAppear {
                        loadedTabs.insert(0)
                    }
                }
            }
            .tabItem { Label("Stats", systemImage: "chart.bar.xaxis") }
            .tag(0)
            
            // Home tab - always loaded
            MainView()
                .tabItem { Label("Home", systemImage: "house.fill") }
                .tag(1)
            
            // Lazy load Course tab
            if NetworkChecker.companion.shared.isConnected {
                Group {
                    if loadedTabs.contains(2) {
                        CourseView()
                    } else {
                        Color.clear.onAppear {
                            loadedTabs.insert(2)
                        }
                    }
                }
                .tabItem { Label("Courses", systemImage: "figure.golf") }
                .tag(2)
            }
        }
        .onChange(of: selectedTab) { oldValue, newValue in
            // Preload tab content when user switches
            loadedTabs.insert(newValue)
        }
    }
    
    @ViewBuilder
    private var loadingOverlay: some View {
        // Subtle loading overlay - only shows briefly during initial load
        if authModel.isLoading && !initialLoadComplete && authModel.userModel == nil {
            Color.black.opacity(0.2)
                .ignoresSafeArea()
                .overlay(
                    LogoLoadingView()
                )
                .transition(.opacity)
        }
    }
    
    private func initializeAppData() {
        // Launch background tasks that don't block UI
        Task(priority: .userInitiated) {
            await iapManager.initialize()
            
            guard let id = authModel.kotlinVM.currentUserIdentifier else { return }

            // Verify the Firebase session is still valid — catches the case where the account
            // was deleted on another device while this one still has a cached session.
            let sessionValid = try await authModel.kotlinVM.verifyAndCleanupIfDeleted()
            guard Bool(truncating: sessionValid) else { return }

            // Load user data only when nothing has fully reconciled it this session
            // (cold start with a cached session — the model may already be hydrated
            // from SQLite, but that's cache, not a sync). Right after a fresh login,
            // createOrSignInUserAndNavigateToHome just did this exact load — skip the duplicate.
            //
            // If the remote fetch fails transiently and no local user exists,
            // the KMP layer throws rather than silently creating a blank user. Retry once after a delay.
            if !authModel.kotlinVM.hasSyncedUserThisSession {
                do {
                    authModel.userModel = try await userRepo.loadOrCreateUser(id: id) { refreshedModel in
                        DispatchQueue.main.async {
                            self.authModel.userModel = refreshedModel
                        }
                    }
                } catch {
                    print("⚠️ User load failed (\(error.localizedDescription)), retrying in 3s...")
                    try await Task.sleep(nanoseconds: 3_000_000_000)
                    authModel.userModel = try await userRepo.loadOrCreateUser(id: id) { refreshedModel in
                        DispatchQueue.main.async {
                            self.authModel.userModel = refreshedModel
                        }
                    }
                }
                authModel.kotlinVM.markUserSynced()
            }
            
            print("Running after user data loaded - userModel is now: \(String(describing: authModel.userModel?.name)) with \(authModel.userModel?.gameIDs.count ?? 0) games")
            
            // Mark initial load complete
            initialLoadComplete = true
            
            // Defer non-critical operations - yield to let UI update
            await Task.yield()
            
            // Run these after initial load
            await iapManager.isPurchasedPro(authModel: authModel)
            
            if authModel.userModel != nil {
                let _ = try await KoinHelperParent.shared.getLocalGameRepo().deleteAllUnusedGames()
            }
        }
    }
}
