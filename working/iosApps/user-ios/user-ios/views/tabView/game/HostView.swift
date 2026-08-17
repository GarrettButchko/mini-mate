// HostView.swift
// MiniMate
//
// Refactored to use new SwiftData models and AuthViewModel

import SwiftUI
import MapKit
import shared_user

struct HostView: View {
    @Environment(\.scenePhase) private var scenePhase
    @Environment(\.colorScheme) private var colorScheme
    @EnvironmentObject var gameModel: GameViewModelSwift
    @EnvironmentObject var locationHandler: LocationHandlerSwift
    @EnvironmentObject var authModel: AuthViewModelSwift
    @EnvironmentObject var viewManager: ViewManagerSwift
    @EnvironmentObject var VM: HostViewModelSwift
    let localRepo: LocalGameRepository = KoinHelperParent.shared.getLocalGameRepo()
    
    @Binding var showHost: Bool

    @State private var showGameCode: Bool = false
    @State private var lobbyNotice: StatusMessage? = nil

    var isGuest: Bool
    
    // Platform-agnostic business logic isolated for KMP
    private let logic = HostViewBusinessLogic()
    
    init(
        showHost: Binding<Bool>,
        isGuest: Bool = false
    ) {
        self._showHost = showHost
        self.isGuest = isGuest
    }
    
    var body: some View {
        mainContent
    }
    
    private var mainContent: some View {
        ZStack{
            
            Form {
                playersSection
                gameInfoSection
            }
            .contentMargins(.top, 100)
            .contentMargins(.bottom, 70)
            .onTapGesture {
                VM.kotlin.resetTimer()
            }
            
            VStack{
                HStack(alignment: .center, spacing: 10){
                    
                    if isGuest {
                        Button {
                            logic.handleGuestBackAction(
                                dismissGame: { gameModel.kotlin.dismissGame() },
                                navigateToSignIn: { viewManager.kotlinVM.navigateToSignIn() }
                            )
                        } label: {
                            Image(systemName: "chevron.left")
                                .resizable()
                                .scaledToFit()
                                .foregroundStyle(.blue)
                                .frame(width: 20, height: 20)
                        }
                    } else {
                        Color.clear
                            .frame(width: 20, height: 20)
                    }
                    
                    Spacer()
                    
                    Text(logic.getHeaderTitle(isOnline: gameModel.onlineGame))
                        .font(.title)
                        .fontWeight(.bold)
                        .padding(.horizontal)
                    
                    Spacer()
                    
                    Color.clear
                        .frame(width: 20, height: 20)
                }
                .padding(.horizontal, 8)
                .padding(.top, isGuest ? 12 : 28)
                .background(
                    LinearGradient(
                        gradient: Gradient(colors: [
                            Color.sub.opacity(1),
                            Color.clear
                        ]),
                        startPoint: .top,
                        endPoint: .bottom
                    )
                    .ignoresSafeArea(edges: .top)
                )
                    
                Spacer()
                
                startGameSection
                    .padding(.top)
                    .padding(.bottom, isGuest ? 40 : 0)
                    .background(
                        LinearGradient(
                            gradient: Gradient(colors: [
                                Color.sub.opacity(1),
                                Color.clear
                            ]),
                            startPoint: .bottom,
                            endPoint: .top
                        )
                        .ignoresSafeArea(edges: .bottom)
                    )
                    
            }
        }
        .sheet(isPresented: $VM.showAddLocalPlayer) {
            AddLocalPlayerView(showColor: $VM.showAddLocalPlayer)
        }
        .onAppear {
            VM.kotlin.resetTimer()
            VM.kotlin.setUp()
        }
        .onDisappear {
            VM.kotlin.resetTimer()
            
            let shouldDismiss = logic.shouldDismissGameOnDisappear(
                isStarted: gameModel.game.started,
                isDismissed: gameModel.game.dismissed,
                showHost: showHost
            )
            
            if shouldDismiss {
                gameModel.kotlin.dismissGame()
            }
        }
        .contentShape(Rectangle())
        .alert("Delete Player?", isPresented: $VM.showDeleteAlert) {
            Button("Delete", role: .destructive) {
                logic.handleDeletePlayer(playerId: VM.playerToDelete) { id in
                    gameModel.kotlin.removePlayer(userId: id)
                }
            }
            Button("Cancel", role: .cancel) {}
        }
        .onReceive(VM.timer) { _ in
            VM.tick(showHost: $showHost)
        }
        .onChange(of: gameModel.game.players) { oldPlayers, newPlayers in
            guard gameModel.onlineGame else { return }
            let oldIds = Set(oldPlayers.map(\.userId))
            let newIds = Set(newPlayers.map(\.userId))

            // Remote players only — local "Add Player" guests (6-char ids) are
            // managed by the host directly, and the host is always present.
            let joined = newPlayers.filter {
                !oldIds.contains($0.userId) && $0.userId.count != 6 && $0.userId != gameModel.game.hostUserId
            }
            let left = oldPlayers.filter {
                !newIds.contains($0.userId) && $0.userId.count != 6 && $0.userId != gameModel.game.hostUserId
            }

            if !joined.isEmpty {
                let (title, subtitle): (String, String) = switch joined.count {
                case 1: ("\(joined[0].name) Joined", "\(joined[0].name) joined your game.")
                case 2: ("2 Joined", "\(joined[0].name) & \(joined[1].name) joined your game.")
                default: ("\(joined.count) Joined", "\(joined.count) people joined your game.")
                }
                presentTransientOverlay($lobbyNotice, StatusMessage(title: title, subtitle: subtitle, icon: "person.fill.checkmark", tint: .green))
            } else if !left.isEmpty {
                let (title, subtitle): (String, String) = switch left.count {
                case 1: ("\(left[0].name) Left", "\(left[0].name) left your game.")
                case 2: ("2 Left", "\(left[0].name) & \(left[1].name) left your game.")
                default: ("\(left.count) Left", "\(left.count) people left your game.")
                }
                presentTransientOverlay($lobbyNotice, StatusMessage(title: title, subtitle: subtitle, icon: "person.fill.xmark", tint: .secondary))
            }
        }
        .overlay {
            if showGameCode {
                GameCodeOverlay(code: gameModel.game.id, notice: lobbyNotice) {
                    VM.kotlin.resetTimer()
                    showGameCode = false
                }
            }
        }
        .animation(statusOverlaySpring, value: showGameCode)
        .overlay {
            // While the code popup is up, notices render inside it as a
            // bottom banner instead of a second full-screen overlay.
            if let lobbyNotice, !showGameCode {
                SuccessOverlay(
                    title: lobbyNotice.title,
                    subtitle: lobbyNotice.subtitle,
                    icon: lobbyNotice.icon,
                    tint: lobbyNotice.tint
                )
            }
        }
        .animation(statusOverlaySpring, value: lobbyNotice)
        .environmentObject(VM)
    }
    
    // MARK: - Sections
    private var gameInfoSection: some View {
        let course = gameModel.kotlin.course
        return Group {
            Section {
                if gameModel.onlineGame {
                    VStack{
                        HStack {
                            Text("Game Code:")
                            Spacer()
                        }

                        Image(uiImage: VM.qrCodeImage ?? UIImage(systemName: "xmark.circle") ?? UIImage())
                            .interpolation(.none)
                            .resizable()
                            .scaledToFit()
                            .colorInvertIf(colorScheme == .dark)
                            .frame(height: 200)
                            .padding()
                            .background(
                                RoundedRectangle(cornerRadius: 16)
                                    .fill(Color("mainApp"))
                            )
                            .overlay(
                                RoundedRectangle(cornerRadius: 16)
                                    .stroke(Color.primary.opacity(0.1))
                            )

                        Button {
                            VM.kotlin.resetTimer()
                            showGameCode = true
                        } label: {
                            HStack(spacing: 6) {
                                Image(systemName: "qrcode")
                                Text("Show Game Code")
                            }
                            .font(.subheadline.weight(.medium))
                            .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.borderless)
                        .padding(.top, 8)
                    }
                    
                    
                    HStack {
                        Text("Expires in:")
                        Spacer()
                        Text(VM.kotlin.timeString())
                            .monospacedDigit()
                    }
                }
                
                if locationHandler.hasLocationAccess{
                    LocationButtons(isGuest: isGuest)
                }
                
                if let course = course.value {
                    if course.customPar {
                        UserInfoRow(label: "Holes", value: String(course.numHoles))
                    } else {
                        HStack {
                            Text("Holes:")
                            NumberPickerView(
                                selectedNumber: Binding(
                                    get: { Int(gameModel.binding(for: \.numberOfHoles).wrappedValue) },
                                    set: { gameModel.binding(for: \.numberOfHoles).wrappedValue = Int32($0) }
                                ),
                                minNumber: 9, maxNumber: 21
                            )
                        }
                    }
                } else {
                    // No course → show picker
                    HStack {
                        Text("Holes:")
                        NumberPickerView(
                            selectedNumber: Binding(
                                get: { Int(gameModel.binding(for: \.numberOfHoles).wrappedValue) },
                                set: { gameModel.binding(for: \.numberOfHoles).wrappedValue = Int32($0) }
                            ),
                            minNumber: 9, maxNumber: 21
                        )
                    }
                }
            } header: {
                Text("Game Info")
            }
        }
    }
    
    private var playersSection: some View {
        Group{
            Section {
                ScrollView(.horizontal) {
                    HStack {
                        ForEach(gameModel.game.players) { player in
                            playerIconView(for: player)
                        }
                        Button(action: {
                            VM.showAddLocalPlayer = true
                            VM.kotlin.resetTimer()
                        }) {
                            VStack {
                                ZStack {
                                    Circle()
                                        .fill(.ultraThinMaterial)
                                        .frame(width: 40, height: 40)
                                    Image(systemName: "plus")
                                }
                                Text("Add Player").font(.caption)
                            }
                            .padding(.horizontal)
                        }
                        if gameModel.onlineGame {
                            VStack {
                                ProgressView()
                                    .frame(width: 40, height: 40)
                                Text("Searching...").font(.caption)
                            }.padding(.horizontal)
                        }
                    }
                    .animation(.easeInOut, value: gameModel.game.players.count)
                }
                .simultaneousGesture(
                    DragGesture().onChanged { _ in
                        VM.kotlin.resetTimer()
                    }
                )
                .frame(height: 75)
            }  header: {
                Text(logic.getPlayersHeaderText(playerCount: Int32(gameModel.game.players.count)))
            }
        }
    }
    
    @ViewBuilder
    private func playerIconView(for player: Player) -> some View {
        PlayerIconView(player: player, isRemovable: player.userId.count == 6) {
            VM.playerToDelete = player.userId
            VM.kotlin.resetTimer()
            VM.showDeleteAlert = true
        }
        .transition(.opacity)
    }
    
    private var startGameSection: some View {
        Button {
            logic.handleStartGame(
                isGuest: isGuest,
                deleteGuestGame: {
                    Task{
                        do {
                            let _ = try await localRepo.deleteGuestGame()
                        } catch {
                            print("Error deleting guest game: \(error)")
                        }
                    }
                },
                performStart: {
                    VM.startGame(showHost: $showHost, isGuest: isGuest)
                }
            )
        } label: {
            HStack{
                Image(systemName: "play.fill")
                Text("Start Game")
            }
            .font(.headline)
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .background(
                RoundedRectangle(cornerRadius: 25)
                    .fill(Color.blue)
                    .padding(.horizontal)
            )
        }
        
    }
}

struct LocationButtons: View {

    @EnvironmentObject var VM: HostViewModelSwift
    @EnvironmentObject var gameModel: GameViewModelSwift
    @EnvironmentObject var locationHandler: LocationHandlerSwift

    var isGuest: Bool = false

    var body: some View {
        let item = gameModel.kotlin.course.value?.name
        let isConnected = NetworkChecker.companion.shared.isConnected

        Group{
            if (VM.showLocationButton || isGuest) && isConnected {
                HStack{
                    VStack{
                        HStack{
                            Text("Location:")
                            Spacer()
                        }

                        if let item = item {
                            HStack{
                                Text(item)
                                    .foregroundStyle(.secondary)
                                    .truncationMode(.tail)
                                    .transition(.move(edge: .top).combined(with: .opacity).combined(with: .blurReplace))
                                Spacer()
                            }
                        } else {
                            HStack{
                                Text("No Location")
                                    .foregroundStyle(.secondary)
                                    .transition(.move(edge: .top).combined(with: .opacity).combined(with: .blurReplace))
                                Spacer()
                            }
                        }
                    }
                    .animation(.spring, value: item)

                    Spacer()

                    if item == nil {
                        searchNearbyButton
                    } else {
                        retryButton
                        exitButton
                    }
                }
                .animation(.spring, value: item == nil)

            } else if let item = item, !VM.showLocationButton && isConnected {
                HStack{
                    Text("Location:")
                    Spacer()
                    Text(item)
                }
            }
        }
        
    }
    
    var noButtons: some View{
        Group{
            if let item = gameModel.kotlin.course.value?.name {
                HStack{
                    Spacer()
                    Text(item)
                        .foregroundStyle(.secondary)
                        .truncationMode(.tail)
                        .transition(.move(edge: .top).combined(with: .opacity))
                }
            } else {
                HStack{
                    Spacer()
                    Text("No location found")
                        .foregroundStyle(.secondary)
                        .transition(.move(edge: .top).combined(with: .opacity))
                }
            }
        }
    }
    
    var searchNearbyButton: some View {
        Button {
            VM.kotlin.searchNearby()
        } label: {
            HStack(spacing: 6) {
                Image(systemName: "magnifyingglass")
                Text("Search Nearby")
            }
            .frame(width: 180, height: 50)
            .background(Color.blue)
            .foregroundColor(.white)
            .clipShape(RoundedRectangle(cornerRadius: 20))
        }
        .buttonStyle(.plain)
        .transition(.move(edge: .trailing).combined(with: .opacity).combined(with: .blurReplace))
    }

    var retryButton: some View {
        Button(action: {
            withAnimation(.spring) {
                VM.kotlin.retry()
            }
        }) {
            Image(systemName: "arrow.trianglehead.2.clockwise")
                .rotationEffect(.degrees(VM.isRotating ? 360 : 0))
                .font(.title2)
                .foregroundColor(.white)
                .frame(width: 50, height: 50)
                .background(Color.blue)
                .clipShape(Circle())
        }
        .buttonStyle(.plain)
        .transition(.move(edge: .trailing).combined(with: .opacity).combined(with: .blurReplace))
        .animation(.spring, value: VM.isRotating)
    }

    var exitButton: some View {
        Button(action: {
            withAnimation(.spring) {
                VM.kotlin.exit()
            }
        }) {
            Image(systemName: "xmark")
                .font(.title2)
                .foregroundColor(.white)
                .frame(width: 50, height: 50)
                .background(Color.red)
                .clipShape(Circle())
        }
        .buttonStyle(.plain)
        .transition(.move(edge: .trailing).combined(with: .opacity).combined(with: .blurReplace))
    }
}

private extension View {
    @ViewBuilder
    func colorInvertIf(_ condition: Bool) -> some View {
        if condition { self.colorInvert() } else { self }
    }
}

/// Full-screen popup showing the game code big enough to read across a table,
/// styled to match the app's other transient popups (StatusOverlay et al.).
/// While it's up, lobby notices ("X joined your game") render as a compact
/// banner at the bottom instead of a second full-screen overlay.
struct GameCodeOverlay: View {
    let code: String
    var notice: StatusMessage? = nil
    let onDismiss: () -> Void

    var body: some View {
        ZStack {
            Color.black.opacity(0.25)
                .ignoresSafeArea()
                .onTapGesture { onDismiss() }
            VStack{
                VStack(spacing: 16) {
                    Text("Game Code")
                        .font(.system(.title3, weight: .semibold))
                    
                    // One rounded-square tile per character, passcode style
                    HStack(spacing: 6) {
                        ForEach(Array(code.enumerated()), id: \.offset) { _, char in
                            Text(String(char))
                                .font(.system(size: 30, weight: .bold, design: .monospaced))
                                .frame(width: 38, height: 52)
                                .background(
                                    RoundedRectangle(cornerRadius: 12)
                                        .fill(.ultraThinMaterial)
                                )
                                .overlay(
                                    RoundedRectangle(cornerRadius: 12)
                                        .stroke(Color.primary.opacity(0.15))
                                )
                        }
                    }
                    
                    
                    
                    Text("Tap anywhere to close")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
                .padding(28)
                .background {
                    RoundedRectangle(cornerRadius: 38, style: .continuous)
                        .ifAGENoDefaultColor()
                        .cardShadow()
                }
                
                if let notice {
                    HStack(spacing: 8) {
                        Image(systemName: notice.icon)
                            .foregroundStyle(notice.tint)
                        Text(notice.subtitle)
                            .font(.subheadline.weight(.medium))
                    }
                    .padding(.vertical, 10)
                    .padding(.horizontal, 16)
                    .background {
                        Capsule()
                            .ifAGENoDefaultColor()
                            .cardShadow()
                    }
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                }
            }
            .padding(.horizontal, 30)
            .transition(.scale(scale: 0.9).combined(with: .opacity))
        }
        .animation(statusOverlaySpring, value: notice)
    }
}
