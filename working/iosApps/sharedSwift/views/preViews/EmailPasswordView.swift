// SignUpView.swift
// MiniMate
//
// Refactored to use AuthViewModel and UserModel

import SwiftUI
import FirebaseAuth
import SwiftData

#if MINIMATE
import shared_user
#elseif MANAGER
import shared_admin
#endif

/// View for new users to sign up and create an account
struct EmailPasswordView: View {
    @EnvironmentObject var viewManager: ViewManagerSwift
    @EnvironmentObject var authModel: AuthViewModelSwift
    
    @Binding var showEmail: Bool
    @Binding var email: String
    @Binding var password: String
    @Binding var confirmPassword: String
    @Binding var showVerifyEmail: Bool
    @Binding var guestGame: Game?
    
    let keyboardHeight: CGFloat
    
    @State var showSignUp: Bool = false
    @State private var errorMessage: (message: String?, type: Bool) = (nil, false)
    @State private var isLoading: Bool = false

    typealias Field = SignInView.Field
    var isTextFieldFocused: FocusState<Field?>.Binding

    private let characterLimit = 15
    
    var disabled: Bool {
        if showSignUp {
            return email.isEmpty || password.isEmpty || confirmPassword.isEmpty || password != confirmPassword
        }
        return email.isEmpty || password.isEmpty
    }

    var body: some View {
        
            ZStack{
                ScrollViewReader { proxy in
                    ScrollView {
                        VStack {
                            // Back Button
                            Spacer(minLength: 90)
                            
                            if showVerifyEmail {
                                verifySection
                                    .transition(.move(edge: .top).combined(with: .opacity))
                            } else {
                                // Title
                                VStack(spacing: 8) {
                                    HStack {
                                        Text(showSignUp ? "Sign Up" : "Login")
                                            .font(.system(size: 40, weight: .bold))
                                            .foregroundColor(.primary)
                                        Spacer()
                                    }
                                    HStack {
                                        Text("Enter email and password")
                                            .font(.system(size: 20, weight: .light))
                                            .foregroundColor(.secondary)
                                        Spacer()
                                    }
                                }
                                Spacer(minLength: 50)
                                
                                // Form Fields
                                VStack(spacing: 20) {
                                    // Email Field
                                    
                                    emailSection.id(Field.email)
                                    passwordSection.id(Field.password)
                                    if showSignUp {
                                        confirmSection.id(Field.confirm)
                                            .transition(.move(edge: .top).combined(with: .opacity))
                                    }

                                    if let errorMessageText = errorMessage.message {
                                        Text(errorMessageText)
                                            .foregroundColor(errorMessage.type ? .green.mix(with: .subOpp, by: 0.5) : .red.mix(with: .subOpp, by: 0.5))
                                            .font(.caption)
                                            .multilineTextAlignment(.center)
                                            .padding(.vertical, 8)
                                            .padding(.horizontal, 12)
                                            .background(
                                                ZStack {
                                                    RoundedRectangle(cornerRadius: 20)
                                                        .fill(.ultraThinMaterial)
                                                    RoundedRectangle(cornerRadius: 20)
                                                        .fill(errorMessage.type ? .green.opacity(0.25) : .red.opacity(0.25))
                                                }
                                            )
                                            .transition(.move(edge: .top).combined(with: .opacity))
                                    }
                                    
                                    Button {
                                        isLoading = true
                                        if showSignUp {
                                            authModel.signUpUIManage(email: $email, password: $password, confirmPassword: $confirmPassword, isTextFieldFocused: isTextFieldFocused, errorMessage: $errorMessage, showSignUp: $showSignUp, showVerifyEmail: $showVerifyEmail, guestGame: $guestGame)
                                        } else {
                                            authModel.signInUIManage(email: $email, password: $password, confirmPassword: $confirmPassword, isTextFieldFocused: isTextFieldFocused, errorMessage: $errorMessage, showSignUp: $showSignUp, showVerifyEmail: $showVerifyEmail, guestGame: $guestGame)
                                        }
                                    } label: {
                                        
                                        ZStack {
                                            RoundedRectangle(cornerRadius: 25)
                                                .frame(width: 150, height: 50)
                                                .foregroundColor(.brand)
                                            if isLoading {
                                                ProgressView()
                                                    .tint(Color.white)
                                            } else {
                                                Text(showSignUp ? "Sign Up" : "Login")
                                                    .foregroundColor(.white)
                                            }
                                        }
                                        .opacity(disabled || isLoading ? 0.5 : 1)
                                    }
                                    .disabled(disabled || isLoading)
                                    .onChange(of: errorMessage.message) { _, _ in isLoading = false }
                                    .onChange(of: showSignUp) { _, _ in isLoading = false }

                                    // Bottom sign up / log in toggle (like the web version)
                                    Button {
                                        withAnimation(.spring(response: 0.4, dampingFraction: 0.75)) {
                                            if showSignUp {
                                                showSignUp = false
                                                confirmPassword = ""
                                                errorMessage.message = nil
                                            } else {
                                                showSignUp = true
                                                errorMessage.message = nil
                                            }
                                        }
                                    } label: {
                                        if showSignUp {
                                            (Text("Already have an account? ").foregroundColor(.secondary)
                                                + Text("Login").foregroundColor(.brand).fontWeight(.semibold))
                                        } else {
                                            (Text("Don't have an account? ").foregroundColor(.secondary)
                                                + Text("Sign Up").foregroundColor(.brand).fontWeight(.semibold))
                                        }
                                    }
                                    .font(.system(size: 12))
                                    .buttonStyle(.plain)
                                }
                                .transition(.move(edge: .top).combined(with: .opacity))
                            }
                        }
                        .padding()
                        .padding(.bottom, keyboardHeight)
                    }
                    .scrollDismissesKeyboard(.interactively)
                    .onChange(of: keyboardHeight) { _, newHeight in
                        guard newHeight > 0, let field = isTextFieldFocused.wrappedValue else { return }
                        DispatchQueue.main.async {
                            withAnimation(.easeOut(duration: 0.25)) {
                                proxy.scrollTo(field, anchor: .top)
                            }
                        }
                    }
                }
                .clipShape(RoundedRectangle(cornerRadius: 35))
            
            VStack{
                HStack {
                    Button(action: {
                        isTextFieldFocused.wrappedValue = nil
                        withAnimation {
                            showEmail = false
                            showSignUp = false
                            showVerifyEmail = false
                            email = ""
                            password = ""
                            confirmPassword = ""
                            errorMessage.message = nil
                        }
                        
                    }) {
                        
                        Circle()
                            .ifAvailableGlassEffect()
                            .frame(width: 40, height: 40)
                            .overlay(
                                
                                Image(systemName: "arrow.left")
                                    .font(.headline)
                                    .foregroundStyle(.brand)
                            )
                        
                    }
                    Spacer()
                }
                Spacer()
            }
            .padding()
        }
        .animation(.spring(response: 0.4, dampingFraction: 0.75), value: showSignUp)
        .animation(.spring(response: 0.4, dampingFraction: 0.75), value: showVerifyEmail)
        .animation(.spring(response: 0.4, dampingFraction: 0.75), value: errorMessage.message)
        .onChange(of: showVerifyEmail) { _, _ in isLoading = false }
        .task(id: showVerifyEmail) {
            guard showVerifyEmail else { return }
            while !Task.isCancelled {
                try? await Task.sleep(nanoseconds: 3_000_000_000)
                if await authModel.checkEmailVerified() {
                    finishVerification()
                    break
                }
            }
        }
    }
    
    var verifySection: some View {
        VStack(spacing: 20) {
            Image(systemName: "envelope.badge")
                .font(.system(size: 44))
                .foregroundStyle(.brand)
            
            Text("Please Verify Your Email")
                .font(.system(size: 22, weight: .semibold))
                .foregroundColor(.primary)
            
            Text("We sent a verification link to \(email.isEmpty ? "your email" : email). Verify it, then come back here to continue.")
                .font(.system(size: 14, weight: .light))
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
            
            if let errorMessageText = errorMessage.message {
                Text(errorMessageText)
                    .foregroundColor(errorMessage.type ? .green : .red)
                    .font(.system(size: 12))
                    .multilineTextAlignment(.center)
            }
            
            Button {
                doneVerifying()
            } label: {
                ZStack {
                    RoundedRectangle(cornerRadius: 25)
                        .frame(width: 150, height: 50)
                        .foregroundColor(.brand)
                    if isLoading {
                        ProgressView()
                            .tint(Color.white)
                    } else {
                        Text("Done Verifying")
                            .foregroundColor(.white)
                    }
                }
            }
            
            Button {
                resendEmail()
            } label: {
                HStack(spacing: 4) {
                    Image(systemName: "paperplane")
                        .font(.system(size: 12))
                    Text("Resend Email")
                        .font(.system(size: 12, weight: .semibold))
                }
                .foregroundColor(.brand)
            }
            .buttonStyle(.plain)
        }
        .frame(maxWidth: .infinity)
    }
    
    private func doneVerifying() {
        isLoading = true
        Task {
            let verified = await authModel.checkEmailVerified()
            if verified {
                finishVerification()
            } else {
                errorMessage = ("Not verified yet — check your inbox and try again.", false)
            }
            isLoading = false
        }
    }
    
    private func resendEmail() {
        Task {
            let sent = await authModel.resendVerificationEmail()
            errorMessage = (sent ? "Verification email sent." : "Couldn't send email. Try again.", sent)
        }
    }
    
    private func finishVerification() {
        authModel.kotlinVM.logout()
        withAnimation(.spring(response: 0.4, dampingFraction: 0.75)) {
            showVerifyEmail = false
            showSignUp = false
            email = ""
            password = ""
            confirmPassword = ""
            errorMessage.message = nil
            isLoading = false
        }
    }
    
    var emailSection: some View {
        VStack(alignment: .leading) {
            Text("Email")
                .foregroundColor(.secondary)
            HStack {
                Image(systemName: "envelope")
                    .foregroundColor(.secondary)
                TextField("example@example", text: $email)
                    .autocorrectionDisabled()
                    .textInputAutocapitalization(.never)
                    .keyboardType(.emailAddress)
                    .focused(isTextFieldFocused, equals: .email)
            }
            .padding()
            .background(RoundedRectangle(cornerRadius: 25)
                .fill(.ultraThinMaterial))
        }
    }
    
    var passwordSection: some View {
        passwordField(title: "Password", text: $password, equals: .password)
    }
    
    var confirmSection: some View {
        passwordField(title: "Confirm Password", text: $confirmPassword, equals: .confirm)
    }

    // MARK: - Helpers

    private func passwordField(title: String, text: Binding<String>, image: String = "lock", equals: Field) -> some View {
        VStack(alignment: .leading) {
            Text(title)
                .foregroundColor(.secondary)
            HStack {
                Image(systemName: image)
                    .foregroundColor(.secondary)
                SecureField("••••••", text: text)
                    .focused(isTextFieldFocused, equals: equals)
                    .onChange(of: text.wrappedValue) { _ , newValue in
                        if newValue.count > characterLimit {
                            text.wrappedValue = String(newValue.prefix(characterLimit))
                        }
                    }
            }
            .padding()
            .background(RoundedRectangle(cornerRadius: 25)
                .fill(.ultraThinMaterial))
        }
    }
}
