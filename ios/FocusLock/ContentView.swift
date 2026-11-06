import SwiftUI
import FamilyControls
import DeviceActivity

struct ContentView: View {
    @EnvironmentObject var sessionManager: StudySessionManager
    @State private var showingAppPicker = false
    @State private var showingGiveUpAlert = false
    
    // Preset timer options: 15, 25, 30, 45, 60, 90 mins
    let presetDurations = [15, 25, 30, 45, 60, 90]

    var body: some View {
        ZStack {
            // Obsidian & dark green gradient
            LinearGradient(
                colors: [Color(red: 0.02, green: 0.05, blue: 0.03), Color(red: 0.04, green: 0.09, blue: 0.06), Color.black],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack(spacing: 18) {
                // Top Header Bar
                HStack {
                    HStack(spacing: 8) {
                        Image(systemName: "lock.shield.fill")
                            .foregroundColor(Color(red: 0.0, green: 0.9, blue: 0.46))
                            .font(.title2)
                        Text("FocusLock")
                            .font(.title3.bold())
                            .foregroundColor(.white)
                    }
                    Spacer()
                    
                    // FamilyActivityPicker Selection Trigger
                    Button {
                        showingAppPicker = true
                    } label: {
                        HStack(spacing: 6) {
                            Image(systemName: "apps.iphone")
                            Text("\(sessionManager.selectedApps.applicationTokens.count) Shielded")
                        }
                        .font(.caption.bold())
                        .padding(.horizontal, 12)
                        .padding(.vertical, 7)
                        .background(Color(red: 0.08, green: 0.17, blue: 0.12))
                        .foregroundColor(Color(red: 0.0, green: 0.9, blue: 0.46))
                        .clipShape(Capsule())
                    }
                }
                .padding(.horizontal)

                Spacer()

                // Large Circular Countdown Timer
                ZStack {
                    // Outer subtle track
                    Circle()
                        .stroke(Color(red: 0.06, green: 0.15, blue: 0.10), lineWidth: 16)
                        .frame(width: 270, height: 270)

                    // Glowing Progress Arc
                    Circle()
                        .trim(from: 0.0, to: sessionManager.progress)
                        .stroke(
                            AngularGradient(
                                colors: [
                                    Color(red: 0.0, green: 0.9, blue: 0.46),
                                    Color(red: 0.11, green: 0.91, blue: 0.71),
                                    Color(red: 0.0, green: 0.9, blue: 0.46)
                                ],
                                center: .center
                            ),
                            style: StrokeStyle(lineWidth: 16, lineCap: .round)
                        )
                        .rotationEffect(.degrees(-90))
                        .frame(width: 270, height: 270)
                        .animation(.easeInOut(duration: 0.5), value: sessionManager.progress)

                    // Center Countdown Digits
                    VStack(spacing: 8) {
                        Text(sessionManager.sessionState == .running ? "FOCUS LOCKED" : "STUDY SESSION")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(Color(red: 0.0, green: 0.9, blue: 0.46))
                            .padding(.horizontal, 10)
                            .padding(.vertical, 4)
                            .background(Color(red: 0.04, green: 0.22, blue: 0.13))
                            .clipShape(Capsule())

                        Text(sessionManager.timeRemainingString)
                            .font(.system(size: 54, weight: .heavy, design: .monospaced))
                            .foregroundColor(.white)

                        Text(sessionManager.studyGoal.isEmpty ? "Deep Work Session" : sessionManager.studyGoal)
                            .font(.subheadline)
                            .foregroundColor(Color(red: 0.62, green: 0.73, blue: 0.67))
                            .lineLimit(1)
                    }
                }

                Spacer()

                if sessionManager.sessionState == .idle {
                    // Study Goal / Subject Input
                    VStack(alignment: .leading, spacing: 6) {
                        Text("STUDY GOAL / SUBJECT")
                            .font(.caption2.bold())
                            .foregroundColor(Color(red: 0.42, green: 0.53, blue: 0.47))

                        TextField("e.g. Master Organic Chem Chapter 4", text: ${'$'}sessionManager.studyGoal)
                            .padding()
                            .background(Color(red: 0.08, green: 0.17, blue: 0.12))
                            .cornerRadius(14)
                            .foregroundColor(.white)
                    }
                    .padding(.horizontal)

                    // Preset Duration Pills (15, 25, 30, 45, 60, 90)
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 10) {
                            ForEach(presetDurations, id: \.self) { mins in
                                Button {
                                    sessionManager.setDuration(minutes: mins)
                                } label: {
                                    VStack(spacing: 2) {
                                        Text("\(mins)")
                                            .font(.headline.bold())
                                        Text("min")
                                            .font(.caption2)
                                    }
                                    .frame(width: 58, height: 58)
                                    .background(sessionManager.selectedDurationMinutes == mins ? Color(red: 0.0, green: 0.9, blue: 0.46) : Color(red: 0.08, green: 0.17, blue: 0.12))
                                    .foregroundColor(sessionManager.selectedDurationMinutes == mins ? .black : .white)
                                    .cornerRadius(14)
                                }
                            }
                        }
                        .padding(.horizontal)
                    }

                    // START STUDY Button
                    Button {
                        sessionManager.startStudySession()
                    } label: {
                        HStack {
                            Image(systemName: "play.fill")
                            Text("START STUDY • \(sessionManager.selectedDurationMinutes) MIN")
                                .fontWeight(.bold)
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(Color(red: 0.0, green: 0.9, blue: 0.46))
                        .foregroundColor(.black)
                        .cornerRadius(16)
                    }
                    .padding(.horizontal)
                } else {
                    // Active Session Controls
                    HStack(spacing: 14) {
                        Button {
                            if sessionManager.sessionState == .running {
                                sessionManager.pauseSession()
                            } else {
                                sessionManager.resumeSession()
                            }
                        } label: {
                            Text(sessionManager.sessionState == .running ? "Pause" : "Resume")
                                .frame(maxWidth: .infinity)
                                .frame(height: 54)
                                .background(Color(red: 0.11, green: 0.23, blue: 0.16))
                                .foregroundColor(.white)
                                .cornerRadius(14)
                        }

                        Button {
                            showingGiveUpAlert = true
                        } label: {
                            Text("End Early")
                                .frame(maxWidth: .infinity)
                                .frame(height: 54)
                                .background(Color.red.opacity(0.85))
                                .foregroundColor(.white)
                                .cornerRadius(14)
                        }
                    }
                    .padding(.horizontal)
                }
            }
            .padding(.vertical)
        }
        .sheet(isPresented: ${'$'}showingAppPicker) {
            FamilyActivityPicker(selection: ${'$'}sessionManager.selectedApps)
        }
        .alert("End Study Session Early?", isPresented: ${'$'}showingGiveUpAlert) {
            Button("Keep Focusing", role: .cancel) {}
            Button("End Early", role: .destructive) {
                sessionManager.stopStudySession(completed: false)
            }
        } message: {
            Text("Ending the session early will immediately unshield your blocked apps.")
        }
    }
}
