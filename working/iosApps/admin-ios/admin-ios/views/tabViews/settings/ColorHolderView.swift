//
//  ColorHolderView.swift
//  MiniMate Manager
//
//  Created by Garrett Butchko on 12/21/25.
//

import SwiftUI
import shared_admin

struct ColorHolderView: View {
    
    var color: Color? = nil
    
    let showFunction: () -> Void
    let deleteFunction: () -> Void
    
    var body: some View {
        Button {
            if color == nil {
                withAnimation() {
                    showFunction()
                }
            } else {
                deleteFunction()
            }
        } label: {
            ZStack {
                Circle()
                    .fill(.ultraThinMaterial)
                    .frame(width: 40, height: 40)
                Circle()
                    .fill(color ?? .clear)
                    .frame(width: 30, height: 30)
                    .scaleEffect(color == nil ? 0 : 1)
                    .opacity(color == nil ? 0 : 1)
                Image(systemName: "plus")
                    .resizable()
                    .frame(width: 20, height: 20)
                    .scaleEffect(color == nil ? 1 : 0)
                    .opacity(color == nil ? 1 : 0)
            }
        }
        .animation(.easeInOut, value: color == nil)
    }
}


