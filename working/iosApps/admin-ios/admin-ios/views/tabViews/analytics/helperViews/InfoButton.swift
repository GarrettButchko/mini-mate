//
//  InfoButton.swift
//  admin-ios
//
//  Created by Garrett Butchko on 6/3/26.
//


struct InfoButton: View {
    @State var showInfo: Bool = false
    let infoText: String
    
    var body: some View {
        Button {
            showInfo = true
        } label: {
            Image(systemName: "info.circle")
                .resizable()
                .scaledToFit()
                .frame(width: 20, height: 20)
                .foregroundStyle(.blue)
        }
        .alert("Info", isPresented: $showInfo) {
            Button("OK") {}
        } message: {
            Text(infoText)
        }
    }
}