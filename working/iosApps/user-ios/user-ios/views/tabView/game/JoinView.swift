// JoinView.swift
// MiniMate
//
// Refactored to use SwiftData models and AuthViewModel

import SwiftUI
import shared_user

struct JoinView: View {
    @StateObject private var viewModel = JoinViewModelSwift()
    @EnvironmentObject var viewManager: ViewManagerSwift
    @EnvironmentObject var gameModel: GameViewModelSwift
    @EnvironmentObject var authModel: AuthViewModelSwift
    
    @Binding var showJoin: Bool
    @State private var showScanner = false
    @FocusState private var codeFieldFocused: Bool
    // Which tile the next keystroke overwrites; nil = append at the end
    @State private var selectedIndex: Int? = nil
    // Raw keystroke buffer behind the tiles (see handleCodeInput)
    @State private var hiddenCodeInput: String = ""
    @State private var lastCommittedCode: String = ""
    @State private var lobbyNotice: StatusMessage? = nil
    @State private var viewContentHeight: CGFloat = 0
    @State private var contentMargins: CGFloat = 70
    
    // Platform-agnostic business logic isolated for KMP
    private let logic = JoinViewBusinessLogic()
    
    var body: some View {
        ZStack{
            mainContent
                .contentMargins(.top, contentMargins)
                .contentMargins(.bottom, contentMargins)
            
            headerOverlay
            bottomButtons
        }
        .onChange(of: showJoin) { oldValue, newValue in
            viewModel.kotlin.joinDidDismiss(showJoin: showJoin)
        }
        .onChange(of: gameModel.game) { oldGame, newGame in
            // Trigger logic based on the specific property of the new game object
            if newGame.started != oldGame.started {
                viewModel.kotlin.gameDidStart(started: newGame.started) {
                    viewManager.kotlinVM.navigateToScoreCard(isGuest: false)
                }
            }
                
            if newGame.dismissed != oldGame.dismissed {
                viewModel.kotlin.gameDidDismiss(dismissed: newGame.dismissed)
            }
        }
        .onChange(of: viewModel.inGame) { wasInGame, isInGame in
            if !wasInGame && isInGame {
                let host = gameModel.game.players.first { $0.userId == gameModel.game.hostUserId }
                presentTransientOverlay($lobbyNotice, StatusMessage(
                    title: "Joined!",
                    subtitle: host.map { "You joined \($0.name)'s game." } ?? "You joined the game.",
                    icon: "person.fill.checkmark",
                    tint: .green
                ))
            }
        }
        .onChange(of: gameModel.game.players) { oldPlayers, newPlayers in
            guard viewModel.inGame else { return }
            let oldIds = Set(oldPlayers.map(\.userId))
            let newIds = Set(newPlayers.map(\.userId))

            // Only report churn once we're an established member of the lobby —
            // the initial load right after joining floods the list with everyone.
            guard let selfId = authModel.userModel?.googleId, oldIds.contains(selfId) else { return }

            let joined = newPlayers.filter { !oldIds.contains($0.userId) && $0.userId != selfId }
            let left = oldPlayers.filter { !newIds.contains($0.userId) && $0.userId != selfId }

            if !joined.isEmpty {
                let (title, subtitle): (String, String) = switch joined.count {
                case 1: ("\(joined[0].name) Joined", "\(joined[0].name) joined the game.")
                case 2: ("2 Joined", "\(joined[0].name) & \(joined[1].name) joined the game.")
                default: ("\(joined.count) Joined", "\(joined.count) people joined the game.")
                }
                presentTransientOverlay($lobbyNotice, StatusMessage(title: title, subtitle: subtitle, icon: "person.fill.checkmark", tint: .green))
            } else if !left.isEmpty {
                let (title, subtitle): (String, String) = switch left.count {
                case 1: ("\(left[0].name) Left", "\(left[0].name) left the game.")
                case 2: ("2 Left", "\(left[0].name) & \(left[1].name) left the game.")
                default: ("\(left.count) Left", "\(left.count) people left the game.")
                }
                presentTransientOverlay($lobbyNotice, StatusMessage(title: title, subtitle: subtitle, icon: "person.fill.xmark", tint: .secondary))
            }
        }
        .overlay {
            if let lobbyNotice {
                SuccessOverlay(
                    title: lobbyNotice.title,
                    subtitle: lobbyNotice.subtitle,
                    icon: lobbyNotice.icon,
                    tint: lobbyNotice.tint
                )
            }
        }
        .animation(statusOverlaySpring, value: lobbyNotice)
        .background { backgroundLayer }
    }
    
    // MARK: - Extracted Layers
    
    private var headerOverlay: some View {
        VStack {
            HStack {
                Spacer()
                Text("Join Game")
                    .font(.title)
                    .fontWeight(.bold)
                    .padding(.horizontal)
                    .padding(.vertical, 8)
                    .padding(.top, 20)
                Spacer()
            }
            .padding(.bottom)
            .background(
                LinearGradient(
                    gradient: Gradient(colors: [
                        (viewModel.inGame ? Color.sub.opacity(1) : Color.bg.opacity(1)),
                        Color.clear
                    ]),
                    startPoint: .top,
                    endPoint: .bottom
                )
                .ignoresSafeArea(edges: .top)
            )
            Spacer()
        }
    }
    
    private var bottomButtons: some View {
        VStack {
            Spacer()
            Group {
                if viewModel.inGame {
                    exitButton
                } else {
                    joinButton
                }
            }
            .padding(.top)
            .background(
                LinearGradient(
                    gradient: Gradient(colors: [
                        (viewModel.inGame ? Color.sub.opacity(1) : Color.bg.opacity(1)),
                        Color.clear
                    ]),
                    startPoint: .bottom,
                    endPoint: .top
                )
                .ignoresSafeArea(edges: .bottom)
            )
        }
    }
    
    private var exitButton: some View {
        Button {
            viewModel.showExitAlert = true
        } label: {
            Text("Exit Game")
                .font(.headline)
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
                .background(
                    RoundedRectangle(cornerRadius: 25)
                        .fill(Color.red)
                        .padding(.horizontal)
                )
        }
        .foregroundColor(.red)
        .alert("Exit Game?", isPresented: $viewModel.showExitAlert) {
            Button("Leave", role: .destructive) {
                viewModel.kotlin.leaveGame()
            }
            Button("Cancel", role: .cancel) {}
        }
    }
    
    private var joinButton: some View {
        Button {
            viewModel.kotlin.joinGame()
        } label: {
            HStack(alignment: .center){
                Image(systemName: "person.2.badge.plus.fill")
                Text("Join Game")
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
            .opacity(logic.getJoinButtonOpacity(gameCode: viewModel.gameCode))
            .safeAreaPadding(.bottom, 10)
        }
        .disabled(logic.isJoinButtonDisabled(gameCode: viewModel.gameCode))
    }
    
    private var backgroundLayer: some View {
        GeometryReader { proxy in
            Color.bg.ignoresSafeArea()
                .task(id: proxy.size) {
                    withAnimation {
                        viewContentHeight = proxy.size.height - contentMargins - contentMargins
                    }
                }
        }
    }
    
    // MARK: - Content Sections
    
    private var mainContent: some View {
        Group {
            if !viewModel.inGame {
                joinGameCard
            } else {
                activeGameLobby
            }
        }
    }
    
    // MARK: - Join Game View
    @ViewBuilder
    private var joinGameCard: some View {
        ScrollView{
            VStack{
                VStack(spacing: 24) {
                    // Header
                    VStack(spacing: 8) {
                        Text("Enter Game Code")
                            .font(.system(.title2, design: .rounded))
                            .fontWeight(.bold)
                        
                        Text("Ask the host for the 6-digit code")
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                    }
                    
                    // Input & Scanner
                    VStack(spacing: 20) {
                        gameCodeTextField
                        actionDivider
                        scanButton
                    }
                }
                .padding(.vertical, 32)
                .padding(.horizontal, 20)
                .background {
                    RoundedRectangle(cornerRadius: 25, style: .continuous)
                        .fill(.sub)
                        .cardShadow()
                }
                
                if !viewModel.message.isEmpty {
                    Text(viewModel.message)
                        .font(.footnote)
                        .foregroundStyle(.red.opacity(0.8))
                        .transition(.blurReplace)
                        .multilineTextAlignment(.center)
                        .padding(.vertical, 8)
                        .padding(.horizontal, 16)
                        .background(
                            RoundedRectangle(cornerRadius: 12)
                                .fill(.red.opacity(0.2))
                        )
                }
            }
            .frame(minHeight: viewContentHeight, alignment: .center)
        }
    }
    
    // MARK: - Sub-Components
    /// Passcode-style entry matching GameCodeOverlay in HostView: one rounded
    /// square per character. A hidden TextField drives the keyboard; the tiles
    /// are purely visual. Tap a filled tile to overwrite that position, tap an
    /// empty one to append, long-press for Paste.
    private var gameCodeTextField: some View {
        HStack(spacing: 6) {
            let chars = Array(viewModel.gameCode)
            ForEach(0..<6, id: \.self) { index in
                let cursorIndex = selectedIndex ?? chars.count
                let isActive = codeFieldFocused && index == cursorIndex
                Text(index < chars.count ? String(chars[index]) : "0")
                    .font(.system(size: 30, weight: .bold, design: .monospaced))
                    .foregroundStyle(index < chars.count ? Color.primary : Color.secondary.opacity(0.4))
                    .frame(width: 38, height: 52)
                    .background(
                        RoundedRectangle(cornerRadius: 12)
                            .fill(.ultraThinMaterial)
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(
                                isActive ? Color.blue : Color.primary.opacity(0.15),
                                lineWidth: isActive ? 2 : 1
                            )
                    )
                    .onTapGesture {
                        selectedIndex = index < chars.count ? index : nil
                        codeFieldFocused = true
                    }
            }
        }
        .background(
            TextField("", text: $hiddenCodeInput)
                .keyboardType(.asciiCapable)
                .textInputAutocapitalization(.characters)
                .disableAutocorrection(true)
                .focused($codeFieldFocused)
                .opacity(0)
                .allowsHitTesting(false)
        )
        .contentShape(Rectangle())
        .onTapGesture {
            selectedIndex = nil
            codeFieldFocused = true
        }
        .animation(.easeOut(duration: 0.15), value: viewModel.gameCode)
        .onAppear {
            hiddenCodeInput = viewModel.gameCode
            lastCommittedCode = viewModel.gameCode
        }
        .onChange(of: hiddenCodeInput) { oldValue, newValue in
            handleCodeInput(oldValue: oldValue, newValue: newValue)
        }
        .onChange(of: viewModel.gameCode) { _, newValue in
            // External updates (QR scan, Kotlin-side formatting) → resync tiles/buffer
            if newValue != hiddenCodeInput {
                applyExternalCode(newValue)
            }
        }
    }

    /// Interprets raw keystrokes from the hidden field. The field's caret is
    /// always at the end, so a keystroke shows up as an append/removal there —
    /// this remaps it onto the selected tile when one is targeted, and treats
    /// multi-character jumps as a paste.
    private func handleCodeInput(oldValue: String, newValue: String) {
        // Ignore the onChange fired by our own programmatic resync below
        if newValue == lastCommittedCode { return }

        var result = newValue
        var newSelection = selectedIndex

        if let sel = selectedIndex, sel < oldValue.count,
           newValue.count == oldValue.count + 1, let typed = newValue.last {
            // Typed one character with a tile selected → overwrite that position
            var chars = Array(oldValue)
            chars[sel] = typed
            result = String(chars)
            newSelection = sel + 1 < chars.count ? sel + 1 : nil
        } else if let sel = selectedIndex, sel < oldValue.count,
                  newValue.count == oldValue.count - 1 {
            // Backspace with a tile selected → delete that position
            var chars = Array(oldValue)
            chars.remove(at: sel)
            result = String(chars)
            newSelection = sel < chars.count ? sel : nil
        } else if newValue.count > oldValue.count + 1 {
            // Multi-character jump = paste → the pasted text becomes the code
            result = newValue.hasPrefix(oldValue) ? String(newValue.dropFirst(oldValue.count)) : newValue
            newSelection = nil
        }

        let formatted = logic.formatEnteredCode(code: result)
        selectedIndex = newSelection
        lastCommittedCode = formatted
        viewModel.gameCode = formatted
        hiddenCodeInput = formatted
    }

    /// Applies a code that didn't come from keystrokes (QR scan, Paste, Kotlin).
    private func applyExternalCode(_ code: String) {
        selectedIndex = nil
        lastCommittedCode = code
        hiddenCodeInput = code
        if viewModel.gameCode != code {
            viewModel.gameCode = code
        }
    }

    private var actionDivider: some View {
        HStack {
            Rectangle().fill(.separator).frame(height: 1)
            Text("OR").font(.caption2).fontWeight(.black).foregroundStyle(.tertiary)
            Rectangle().fill(.separator).frame(height: 1)
        }
        .padding(.horizontal, 40)
    }

    private var scanButton: some View {
        Button {
            showScanner = true
        } label: {
            Label("Scan QR Code", systemImage: "qrcode.viewfinder")
                .fontWeight(.medium)
                .foregroundStyle(.primary)
                .padding(.vertical, 10)
                .padding(.horizontal, 16)
                .background(Capsule().fill(.ultraThinMaterial))
        }
        .sheet(isPresented: $showScanner) {
            camView
                .presentationDragIndicator(.visible)
        }
    }

    // MARK: - Active Game Lobby
    @ViewBuilder
    private var activeGameLobby: some View {
        Form {
            playersSection
            
            Section(header: Text("Game Info")) {
                infoRow(title: "Code:", value: gameModel.game.id)
                infoRow(title: "Date:", value: gameModel.game.date.formatted())
                infoRow(title: "Holes:", value: "\(gameModel.game.numberOfHoles)")
                infoRow(title: "Location:", value: logic.getLocationName(name: gameModel.game.locationName))
            }
        }
    }

    private func infoRow(title: String, value: String) -> some View {
        HStack {
            Text(title)
            Spacer()
            Text(value)
                .foregroundStyle(.secondary)
        }
    }
    
    private var camView: some View {
        ZStack {
            QRScannerView { scannedCode in
                let formattedCode = logic.formatScannedCode(code: scannedCode)
                
                DispatchQueue.main.async {
                    viewModel.gameCode = formattedCode
                    withAnimation {
                        showScanner = false
                    }
                }
            }
            .ignoresSafeArea()
            
            Color.black.opacity(0.6)
                .mask {
                    Rectangle()
                        .overlay(
                            RoundedRectangle(cornerRadius: 30)
                                .frame(width: 260, height: 260)
                                .blendMode(.destinationOut)
                        )
                }
                .overlay{
                    ScannerCornerShape()
                        .stroke(.white, style: StrokeStyle(lineWidth: 8, lineCap: .round, lineJoin: .round))
                        .frame(width: 260, height: 260)
                }
                .ignoresSafeArea()
            
            VStack {
                VStack(spacing: 8) {
                    Text("Scan QR Code")
                        .font(.system(.title2, design: .rounded))
                        .fontWeight(.bold)
                    
                    Text("Center the code inside the frame")
                        .font(.subheadline)
                }
                .padding(.top, 60)
                .foregroundStyle(.white)
                
                Spacer()
                
                Button {
                    showScanner = false
                } label: {
                    Image(systemName: "xmark.circle.fill")
                        .font(.system(size: 44))
                        .foregroundStyle(.white.opacity(0.8))
                }
                .padding(.bottom, 40)
            }
        }
    }
    
    private var playersSection: some View {
        Section(header: Text(logic.getPlayersHeaderText(playerCount: Int32(gameModel.game.players.count)))) {
            ScrollView(.horizontal) {
                HStack {
                    ForEach(gameModel.game.players) { player in
                        PlayerIconView(player: player, isRemovable: false) {}
                            .transition(.opacity)
                    }
                    VStack {
                        ProgressView()
                            .frame(width: 40, height: 40)
                        Text("Waiting...").font(.caption)
                    }.padding(.horizontal)
                }
                .animation(.easeInOut, value: gameModel.game.players.count)
            }
            .frame(height: 75)
        }
    }
}
