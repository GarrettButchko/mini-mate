//
//  OperationsTab.swift
//  MiniMate
//
//  Created by Garrett Butchko on 3/9/26.
//

import SwiftUI
import shared_admin

struct OperationsTab: View {
    
    @EnvironmentObject var VM: OperationsViewModelSwift

    var body: some View {
        VStack(spacing: 16){
            
            VStack(spacing: 8){
                HStack{
                    Text("Games vs Days")
                        .font(.title2)
                        .fontWeight(.bold)
                    Spacer()
                }
                
                DataCard(data: VM.avgGamesPerDayPrime(), color: .subTwo, cornerRadius: 17)
            
                GamesPerDayChart()
            }
            .padding()
            .background {
                RoundedRectangle(cornerRadius: 25)
                    .fill(.sub)
                    .cardShadow()
            }
            
            DataCard(data: VM.avgPlayersPerGamePrime(), color: .sub, cornerRadius: 25)
                .cardShadow()
            
            VStack(spacing: 8){
                HStack{
                    Text("Busiest Times")
                        .font(.title2)
                        .fontWeight(.bold)
                    Spacer()
                }
                
                HStack{
                    DataCard(data: VM.getBusiestHour(), color: .subTwo, cornerRadius: 17)
                    DataCard(data: VM.getBusiestDay(), color: .subTwo, cornerRadius: 17)
                }
                
                
                BusiestTimesChart()
            }
            .padding()
            .background {
                RoundedRectangle(cornerRadius: 25)
                    .fill(.sub)
                    .cardShadow()
            }
            
            // SECTION 3: Game Duration & Engagement
            VStack(spacing: 8){
                HStack{
                    Text("Game Duration")
                        .font(.title2)
                        .fontWeight(.bold)
                    Spacer()
                }
                
                HStack{
                    DataCard(data: VM.getAvgGameDuration(), color: .subTwo, cornerRadius: 17)
                    DataCard(data: VM.getTotalPlayTime(), color: .subTwo, cornerRadius: 17)
                }

                HStack{
                    DataCard(data: VM.getFastestGameTime(), color: .subTwo, cornerRadius: 17)
                    DataCard(data: VM.getSlowestGameTime(), color: .subTwo, cornerRadius: 17)
                }   
                GameDurationTrendChart(VM: VM)
            }
            .padding()
            .background {
                RoundedRectangle(cornerRadius: 25)
                    .fill(.sub)
                    .cardShadow()
            }
        }
    }
}
