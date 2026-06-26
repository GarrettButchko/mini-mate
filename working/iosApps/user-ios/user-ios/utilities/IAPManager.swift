//
//  IAPManager.swift
//  MiniMate
//
//  Created by Garrett Butchko on 6/2/25.
//

import StoreKit
import SwiftUI
import Combine
import shared_user

@MainActor
final class IAPManager: ObservableObject {
    static let productID = "com.minimate.user.pro"

    @Published var products: [Product] = []
    @Published var isPurchasing = false
    @Published var lastError: String?

    private var isInitialized = false
    private var transactionListener: Task<Void, Never>?

    init() {
        transactionListener = listenForTransactions()
    }

    deinit {
        transactionListener?.cancel()
    }

    /// Loads products from StoreKit. Safe to call multiple times.
    func initialize() async {
        guard !isInitialized else {
            print("[IAP] initialize() already ran, skipping")
            return
        }
        isInitialized = true
        await retrieveProducts()
    }

    func retrieveProducts() async {
        print("[IAP] Fetching products for ID: \(Self.productID)")
        do {
            let fetched = try await Product.products(for: [Self.productID])
            self.products = fetched
            if fetched.isEmpty {
                print("⚠️ [IAP] No products returned. Check the .storekit config or App Store Connect listing.")
            } else {
                print("[IAP] Loaded \(fetched.count) product(s): \(fetched.map { "\($0.id) @ \($0.displayPrice)" })")
            }
        } catch {
            print("❌ [IAP] Product fetch failed: \(error)")
            lastError = error.localizedDescription
        }
    }

    /// Presents the system purchase sheet and handles the result.
    /// Cross-platform rule: only writes `isPro = true` to Firebase if it was previously `false`.
    func purchase(_ product: Product, authModel: AuthViewModelSwift, showSheet: Binding<Bool>) async -> Bool {
        print("🛒 [IAP] Purchase started for: \(product.id) (\(product.displayPrice))")
        isPurchasing = true
        defer { isPurchasing = false }

        do {
            print("🛒 [IAP] Calling product.purchase()…")
            let result = try await product.purchase()
            print("🛒 [IAP] product.purchase() returned a result")

            switch result {
            case .success(let verification):
                print("✅ [IAP] Purchase result: .success — verifying…")
                let transaction: StoreKit.Transaction
                do {
                    transaction = try checkVerified(verification)
                    print("🛡️ [IAP] Verified transaction \(transaction.id)")
                } catch {
                    print("❌ [IAP] Verification failed: \(error)")
                    lastError = "Purchase verification failed."
                    return false
                }

                await transaction.finish()
                print("🏁 [IAP] Transaction finished with Apple")

                await applyProUnlock(authModel: authModel)

                showSheet.wrappedValue = false
                return true

            case .userCancelled:
                print("🛑 [IAP] Purchase result: .userCancelled")
                return false

            case .pending:
                print("⏳ [IAP] Purchase result: .pending (Ask to Buy)")
                lastError = "Purchase is pending approval."
                return false

            @unknown default:
                print("❓ [IAP] Purchase result: unknown")
                return false
            }

        } catch {
            print("🚨 [IAP] Purchase threw: \(error.localizedDescription)")
            lastError = error.localizedDescription
            return false
        }
    }

    /// Checks Apple's currentEntitlements and only upgrades local + Firebase Pro state.
    /// Never downgrades — protects users who purchased on Play Store.
    func isPurchasedPro(authModel: AuthViewModelSwift) async {
        var hasPro = false
        for await result in Transaction.currentEntitlements {
            if case .verified(let transaction) = result,
               transaction.productID == Self.productID,
               transaction.revocationDate == nil {
                hasPro = true
                break
            }
        }
        guard hasPro else {
            print("[IAP] Entitlement check: no active Apple Pro entitlement.")
            return
        }
        print("[IAP] Entitlement check: Apple Pro entitlement found.")
        await applyProUnlock(authModel: authModel)
    }

    /// Sets local `isPro = true` and conditionally writes to Firebase.
    /// Only writes to Firebase if the user wasn't already Pro (cross-platform safe).
    private func applyProUnlock(authModel: AuthViewModelSwift) async {
        guard let userModel = authModel.userModel else {
            print("⚠️ [IAP] No userModel — can't sync Pro state.")
            return
        }

        let wasAlreadyPro = userModel.isPro

        withAnimation {
            authModel.userModel?.isPro = true
        }

        guard !wasAlreadyPro else {
            print("ℹ️ [IAP] User was already Pro — skipping remote save.")
            return
        }

        do {
            _ = try await KoinHelperParent.shared.getRemoteUserRepo().save(userModel: userModel, updateLastUpdated: true)
            print("✅ [IAP] Pro status synced to Firebase.")
        } catch {
            print("⚠️ [IAP] Failed to sync Pro status to Firebase: \(error)")
        }
    }

    /// Handles external transaction updates (purchases on other devices, refunds, family sharing).
    /// Entitlement state is fully re-checked on next app launch via `isPurchasedPro`.
    private func listenForTransactions() -> Task<Void, Never> {
        Task.detached(priority: .background) {
            for await update in Transaction.updates {
                do {
                    let transaction = try Self.staticCheckVerified(update)
                    await transaction.finish()
                    print("[IAP] External transaction processed: \(transaction.productID)")
                } catch {
                    print("⚠️ [IAP] External transaction failed verification: \(error)")
                }
            }
        }
    }

    private func checkVerified<T>(_ verification: VerificationResult<T>) throws -> T {
        try Self.staticCheckVerified(verification)
    }

    private nonisolated static func staticCheckVerified<T>(_ verification: VerificationResult<T>) throws -> T {
        switch verification {
        case .unverified(_, let error):
            throw error
        case .verified(let value):
            return value
        }
    }
}
