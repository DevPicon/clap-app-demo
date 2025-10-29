import SwiftUI
import shared

struct ContentView: View {
    private let viewModel: ClapViewModel
    private let audioPlayer = AudioPlayer()

    init() {
        // Get ViewModel from Koin dependency injection
        self.viewModel = KoinHelper().getClapViewModel()
    }

    var body: some View {
        VStack {
            Text("Clap App")
                .font(.largeTitle)
                .padding()

            Button(action: {
                viewModel.onClapClick()
                audioPlayer.play()
            }) {
                Image(systemName: "hand.raised.fill")
                    .font(.system(size: 100))
                    .foregroundColor(.blue)
            }
            .padding()
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
