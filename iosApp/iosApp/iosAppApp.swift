import SwiftUI
import shared

@main
struct iosAppApp: App {
    init() {
        // Initialize Koin dependency injection
        KoinHelper.Companion.shared.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
