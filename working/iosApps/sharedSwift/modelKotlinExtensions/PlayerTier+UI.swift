//
//  PlayerTier+UI.swift
//  MiniMate
//
//  Created by Garrett Butchko on 5/21/26.
//

import SwiftUI

#if MANAGER
import shared_admin

extension PlayerTier {
    
    var color: Color {
        colorKey.toColor() ?? .mainOpp
    }

    var icon: String {
        iconName
    }

    // Bridge: Kotlin renamed 'description' to avoid conflict with NSObject.description
    var description: String {
        tierDescription
    }
    
}
#endif
