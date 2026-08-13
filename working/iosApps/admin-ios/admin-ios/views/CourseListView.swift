//
//  CourseListView.swift
//  MiniMate Manager
//
//  Created by Garrett Butchko on 12/18/25.
//

import SwiftUI
import UIKit
import FirebaseAuth
import shared_admin

struct CourseListView: View {
    @EnvironmentObject var authModel: AuthViewModelSwift
    @EnvironmentObject var viewManager: ViewManagerSwift
    @EnvironmentObject var viewModel: CourseListViewModelSwift
    
    private var userRepo: UserRepository = KoinHelperParent.shared.getUserRepo()
    
    @State private var isSheetPresented: Bool = false
    @State private var showUnsuccessfulAlert: Bool = false
    @State private var isRotating: Bool = false
    @State private var showSuccessOverlay: Bool = false
    @State private var showLeftCourseOverlay: Bool = false
    @State private var showFirstCourseSheet: Bool = false

    @State private var viewHeight: CGFloat = 0 // State to track the height of the view
    
    var body: some View {
        GeometryReader { geometry in
            Group{
                if viewModel.hasCourse {
                    multiCourse
                        .contentMargins(.horizontal, 16)
                        .transition(.opacity)
                } else {
                    firstCourse
                        .padding()
                        .transition(.opacity)
                }
            }
            .animation(.bouncy, value: viewModel.hasCourse)
            .onReceive(viewModel.timer) { _ in
                viewModel.kotlin.tick()
            }
            .alert("Add Course", isPresented: $viewModel.showAddCourseAlert) {
                TextField("Password", text: $viewModel.password)
                
                Button("Add", role: .none) {
                    let wasFirstCourse = !viewModel.hasCourse
                    viewModel.kotlin.tryPassword { success in
                        Task { @MainActor in
                            viewModel.password = ""
                            viewModel.showAddCourseAlert = false

                            if Bool(truncating: success) {
                                if wasFirstCourse {
                                    showFirstCourseSheet = true
                                } else {
                                    presentTransientOverlay($showSuccessOverlay)
                                }
                            }
                        }
                    }
                }
                .disabled(viewModel.password.isEmpty)
                
                Button("Cancel", role: .cancel) {
                    viewModel.password = ""
                    viewModel.showAddCourseAlert = false
                }
            } message: {
                Text("Enter course password to begin.")
            }
            .onAppear {
                Task {
                    // 1. Ensure you have the user ID (likely from your auth state)
                    guard let id = authModel.kotlinVM.currentUserIdentifier else { return }

                    // Load user data only when nothing has fully reconciled it this
                    // session (cold start with a cached session — the model may already
                    // be hydrated from SQLite, but that's cache, not a sync). Right
                    // after a fresh login, createOrSignInUserAndNavigateToHome just did
                    // this exact load — skip the duplicate round-trip.
                    if !authModel.kotlinVM.hasSyncedUserThisSession {
                        // Load user data and keep BOTH Swift and Kotlin models updated
                        authModel.userModel = try await userRepo.loadOrCreateUser(id: id) { refreshedModel in
                            DispatchQueue.main.async {
                                self.authModel.userModel = refreshedModel
                            }
                        }
                        authModel.kotlinVM.markUserSynced()
                    }
                    
                    if viewModel.userCourses.isEmpty {
                        if let adminCount = authModel.userModel?.adminCourses.count, adminCount > 1 {
                            viewModel.kotlin.getCourses()
                        } else if let adminCount = authModel.userModel?.adminCourses.count, adminCount == 1 {
                            viewModel.kotlin.getCourse {
                                Task { @MainActor in
                                    viewManager.kotlinVM.navigateToCourseTab(tab: 1)
                                }
                            }
                        } else {
                            viewModel.loadingCourse = false
                        }
                    }
                }

                if viewModel.didLeaveCourse {
                    presentTransientOverlay($showLeftCourseOverlay)
                    viewModel.kotlin.clearDidLeaveCourse()
                }
            }
            .onChange(of: viewModel.didLeaveCourse) { _, didLeave in
                if didLeave {
                    presentTransientOverlay($showLeftCourseOverlay)
                    viewModel.kotlin.clearDidLeaveCourse()
                }
            }
        }
        .ignoresSafeArea(.keyboard, edges: .bottom)
        .overlay {
            if showSuccessOverlay {
                SuccessOverlay(title: "Course Added!", subtitle: "You can now manage this course.")
            } else if showLeftCourseOverlay {
                SuccessOverlay(
                    title: "Left Course",
                    subtitle: "You're no longer an admin of this course.",
                    icon: "person.fill.xmark",
                    tint: .secondary
                )
            } else if viewModel.loadingCourse {
                LoadingOverlay(message: "Loading courses...")
            }
        }
        .animation(statusOverlaySpring, value: showSuccessOverlay)
        .animation(statusOverlaySpring, value: showLeftCourseOverlay)
        .animation(statusOverlaySpring, value: viewModel.loadingCourse)
        .sheet(isPresented: $showFirstCourseSheet) {
            FirstCoursePreviewSheet(isPresented: $showFirstCourseSheet)
                .environmentObject(viewModel)
                .environmentObject(viewManager)
        }
    }

    var multiCourse: some View{
        VStack{
            HStack{
                Text("Courses")
                    .font(.title).fontWeight(.bold)
                Spacer()
                
                Button(action: {
                    withAnimation(){
                        isRotating = true
                        viewModel.kotlin.getCourses()
                        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
                            isRotating = false
                        }
                    }
                }) {
                    Image(systemName: "arrow.trianglehead.2.clockwise")
                        .rotationEffect(.degrees(isRotating ? 360 : 0))
                        .font(.title2)
                        .foregroundColor(.blue)
                        .frame(width: 30, height: 30)
                }
                
                Button(action: {
                    isSheetPresented = true
                }) {
                    if let photoURL = authModel.firebaseUser?.photoURL {
                        AsyncImage(url: URL(string: photoURL)) { image in
                            image
                                .resizable()
                                .scaledToFill()
                        } placeholder: {
                            Image("logoOpp")
                                .resizable()
                                .scaledToFill()
                        }
                        .frame(width: 40, height: 40)
                        .clipShape(Circle())
                    } else {
                        Image("logoOpp")
                            .resizable()
                            .scaledToFill()
                            .frame(width: 40, height: 40)
                    }
                }
                .sheet(isPresented: $isSheetPresented) {
                    ProfileView(isSheetPresent: $isSheetPresented)
                }
            }
            .padding([.horizontal, .top], 16)
            
            ScrollView{
                    VStack(spacing: 16){
                        if let message = viewModel.message {
                            MessageBanner(message: message, timeRemaining: viewModel.timeRemaining)
                                .transition(.opacity)
                        }

                        ForEach(viewModel.userCourses) { course in
                            CourseButtonView(viewModel: viewModel, course: course)
                                .transition(.opacity)
                        }

                        Button {
                            viewModel.showAddCourseAlert = true
                        } label: {
                            ZStack{
                                RoundedRectangle(cornerRadius: 25)
                                    .foregroundStyle(.blue)
                                    .frame(height: 60)
                                HStack(alignment: .center){
                                    Image(systemName: "plus")
                                        .foregroundStyle(.white)
                                    Text("Add a new course")
                                        .font(.subheadline)
                                        .fontWeight(.medium)
                                        .foregroundStyle(.white)
                                }
                            }
                            .opacity(viewModel.timeRemaining > 0 ? 0.5 : 1)
                        }
                        .disabled(viewModel.timeRemaining > 0)
                        Spacer()
                    }
                    .animation(.easeInOut, value: viewModel.message)
                }
                .contentMargins(.top, 16)
            
            Button {
                UIApplication.shared.open(URL(string: AppLinks.shared.coursePasswordUrl)!)
            } label: {
                HStack {
                    Image(systemName: "safari.fill")
                    Text("Get another course password")
                    Spacer()
                    Image(systemName: "chevron.right")
                }
                .font(.subheadline.weight(.semibold))
                .padding()
                .frame(maxWidth: .infinity)
                .background{
                    RoundedRectangle(cornerRadius: 18).fill(.sub)
                        .cardShadow()
                }
                .foregroundStyle(.mainOpp)
            }
            .padding([.horizontal, .bottom])
        }
    }
    
    var firstCourse: some View {
        VStack{
            HStack{
                Spacer()
                
                Button(action: {
                    isSheetPresented = true
                }) {
                    if let photoURL = authModel.firebaseUser?.photoURL {
                        AsyncImage(url: URL(string: photoURL)) { image in
                            image
                                .resizable()
                                .scaledToFill()
                        } placeholder: {
                            Image("logoOpp")
                                .resizable()
                                .scaledToFill()
                        }
                        .frame(width: 40, height: 40)
                        .clipShape(Circle())
                    } else {
                        Image("logoOpp")
                            .resizable()
                            .scaledToFill()
                            .frame(width: 40, height: 40)
                    }
                }
                .sheet(isPresented: $isSheetPresented) {
                    ProfileView(isSheetPresent: $isSheetPresented)
                }
            }
            
            
            VStack(spacing: 18) {
                
                VStack(alignment: .leading, spacing: 8) {
                    Text("Add your first course")
                        .font(.largeTitle.bold())
                    
                    Text("Mini Manager lets you customize your scorecard, run leaderboards/tournaments, and view course analytics.")
                        .foregroundStyle(.secondary)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                
                Spacer()
                
                VStack(alignment: .leading, spacing: 30) {
                    Label("Customize your scorecard look", systemImage: "paintpalette.fill")
                        .foregroundStyle(.red)
                    Label("Collect emails + see analytics", systemImage: "chart.bar.fill")
                        .foregroundStyle(.green)
                    Label("Leaderboards and tournaments", systemImage: "trophy.fill")
                        .foregroundStyle(.blue)
                    Label("Run promos with your own in-app ad", systemImage: "megaphone.fill")
                        .foregroundStyle(.orange)
                }
                .font(.headline)
                .padding()
                .frame(maxWidth: .infinity, alignment: .leading)
                .background{
                    RoundedRectangle(cornerRadius: 25).fill(.subTwo)
                }
                
                
                Label("And More!", systemImage: "plus")
                    .font(.headline)
                    .padding()
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background{
                        RoundedRectangle(cornerRadius: 25).fill(.subTwo)
                    }
                
                
                Spacer()
                
                VStack(alignment: .leading, spacing: 10) {
                    Text("Need your course password?")
                        .font(.headline)
                    
                    Text("Visit our website to request it and we’ll email your course password to the address on file.")
                        .foregroundStyle(.secondary)
                    
                    // Placeholder until you have the site
                    Button {
                        UIApplication.shared.open(URL(string: AppLinks.shared.coursePasswordUrl)!)
                    } label: {
                        HStack {
                            Image(systemName: "safari.fill")
                            Text("Get my course password")
                            Spacer()
                            Image(systemName: "chevron.right")
                        }
                        .font(.subheadline.weight(.semibold))
                        .padding()
                        .frame(maxWidth: .infinity)
                        .background(RoundedRectangle(cornerRadius: 18).fill(.sub))
                        .foregroundStyle(.mainOpp)
                        .cardShadow()
                    }
                }
                .padding(.top, 6)
                
                Button {
                    viewModel.showAddCourseAlert = true // ✅ same alert as the list button
                } label: {
                    ZStack {
                        RoundedRectangle(cornerRadius: 25)
                            .foregroundStyle(.blue)
                            .frame(height: 60)
                        HStack {
                            Image(systemName: "plus")
                                .foregroundStyle(.white)
                            Text("Add a course")
                                .font(.headline)
                                .foregroundStyle(.white)
                        }
                    }
                }
                .opacity(viewModel.timeRemaining > 0 ? 0.5 : 1)
                .disabled(viewModel.timeRemaining > 0)
                
                if let message = viewModel.message {
                    MessageBanner(message: message, timeRemaining: viewModel.timeRemaining)
                        .transition(.opacity)
                }
            }
            .animation(.easeInOut, value: viewModel.message)
        }
    }
}

private struct MessageBanner: View {
    let message: String
    let timeRemaining: Double

    var body: some View {
        VStack(spacing: 6) {
            if timeRemaining > 0 {
                Text(message)
                    .font(.headline)
                    .foregroundStyle(.blue)

                Text("Try again in \(max(0, Int(ceil(timeRemaining)))) seconds")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            } else {
                Text(message)
                    .font(.headline)
                    .foregroundStyle(.red)
            }
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 25)
                .fill(timeRemaining > 0 ? .blue.opacity(0.2) : .red.opacity(0.2))
                .cardShadow()
        )
    }
}

