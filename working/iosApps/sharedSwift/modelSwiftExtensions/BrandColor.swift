//
//  BrandColor.swift
//

import SwiftUI

extension Color {
    /// Per-app accent. Mini Mate = green, Mini Manager = purple.
    static var brand: Color {
        #if MANAGER
        .purple
        #else
        .green
        #endif
    }

    /// Sign-in background gradient.
    static var brandGradient: [Color] { [.blue, brand] }
}

extension ShapeStyle where Self == Color {
    /// Per-app accent, usable as a `ShapeStyle` shorthand: `.foregroundStyle(.brand)`.
    static var brand: Color { Color.brand }
}
