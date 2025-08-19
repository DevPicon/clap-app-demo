import SwiftUI
import shared

struct ContentView: View {
    @State private var clapCount = 0
    @State private var isPlaying = false
    private let viewModel = ClapViewModel()
    
    var body: some View {
        VStack {
            Text("Clap App")
                .font(.largeTitle)
                .padding()
            
            Button(action: {
                viewModel.onClapClick()
                clapCount += 1
            }) {
                Image(systemName: "hand.raised.fill")
                    .font(.system(size: 100))
                    .foregroundColor(.blue)
            }
            .padding()
            
            Text("Claps: \(clapCount)")
                .font(.title2)
                .padding()
            
            if isPlaying {
                Text("🔊 Playing...")
                    .foregroundColor(.green)
                    .padding()
            }
        }
        .onAppear {
            viewModel.initialize()
        }
        .onDisappear {
            viewModel.release()
        }
    }
}

#Preview {
    ContentView()
}
