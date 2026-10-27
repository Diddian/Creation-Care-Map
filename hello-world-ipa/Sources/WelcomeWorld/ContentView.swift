import SwiftUI

struct ContentView: View {
    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "hand.wave.fill")
                .font(.system(size: 46))
                .foregroundStyle(.orange)
            Text("Welcome, World!")
                .font(.largeTitle.bold())
            Text("Built headlessly on GitHub Actions")
                .font(.subheadline)
                .foregroundStyle(.secondary)
        }
        .padding(24)
    }
}
