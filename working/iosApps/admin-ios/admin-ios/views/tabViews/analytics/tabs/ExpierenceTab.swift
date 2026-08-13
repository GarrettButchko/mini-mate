//
//  File.swift
//  MiniMate
//
//  Created by Garrett Butchko on 3/9/26.
//
import SwiftUI
import shared_admin

struct ExpierenceTab: View {
    @EnvironmentObject var courseVM: CourseListViewModelSwift
    @EnvironmentObject var VM: ExperienceViewModelSwift
    
    var body: some View {
        VStack(spacing: 16){
            
            if let course = courseVM.selectedCourse {
                // SECTION 1: Par Performance Overview
                
                VStack(spacing: 8){
                    HStack{
                        Text("Hole Difficulty")
                            .font(.title2)
                            .fontWeight(.bold)
                        Spacer()
                        InfoButton(infoText: "Greener = Harder, Less opacity = Easier. Based on average strokes per hole. P.S. if holes are missing they are likely unplayed or have no data yet.")
                    }
                    
                    HoleDifficultyCharts()
                        .id(VM.rangeDailyDocs.count)
                    
                    HStack{
                        DataCard(data: VM.getEasiestHole(), color: .subTwo, cornerRadius: 17)
                        DataCard(data: VM.getHardestHole(), color: .subTwo, cornerRadius: 17)
                    }
                }
                .padding()
                .background {
                    RoundedRectangle(cornerRadius: 25)
                        .fill(.sub)
                        .cardShadow()
                }
                
                VStack(spacing: 8){
                    HStack{
                        Text("Par Performance")
                            .font(.title2)
                            .fontWeight(.bold)
                        Spacer()
                    }
                    
                    if !course.customPar {
                        Text("Custom Par's haven't been added to this course yet. So for reference it uses a base of par 2 on all holes 1-18 for this evaluation.")
                            .font(.footnote)
                            .foregroundStyle(.red) // Note: .tint doesn't work on Text, use .foregroundStyle
                            .padding()
                            .background{
                                RoundedRectangle(cornerRadius: 17)
                                    .fill(.subTwo)
                            }
                    }
                    
                    HStack{
                        DataCard(data: VM.getAvgRelativeToPar(), color: .subTwo, cornerRadius: 17)
                        DataCard(data: VM.getMostBeatenPar(), color: .subTwo, cornerRadius: 17)
                    }
                    HoleDifficultyParCharts(course: course)
                        .id(VM.rangeDailyDocs.count)
                }
                .padding()
                .background {
                    RoundedRectangle(cornerRadius: 25)
                        .fill(.sub)
                        .cardShadow()
                }
                
                // SECTION 2: Score Distribution
                VStack(spacing: 8){
                    HStack{
                        Text("Score Distribution")
                            .font(.title2)
                            .fontWeight(.bold)
                        Spacer()
                    }
                    
                    HStack{
                        DataCard(data: VM.getUnderParPercentage(), color: .subTwo, cornerRadius: 17)
                        DataCard(data: VM.getOverParPercentage(), color: .subTwo, cornerRadius: 17)
                    }
                    
                    DataCard(data: VM.getHoleInOneCount(), color: .subTwo, cornerRadius: 17)
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
}
