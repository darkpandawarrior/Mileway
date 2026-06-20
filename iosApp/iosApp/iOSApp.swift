import SwiftUI
import MileTracker

@main
struct iOSApp: App {
    // FCM.4: APNs registration + token/tap forwarding into the KMP layer.
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
                // DL.3: custom-scheme deep links (miletracker://…) → shared KMP DeepLinkRouter.
                .onOpenURL { url in
                    DeepLinkBridge.shared.handle(url: url.absoluteString)
                }
                // DL.3: Universal Links (https://miletracker.example.com/…) → same bridge.
                .onContinueUserActivity(NSUserActivityTypeBrowsingWeb) { activity in
                    if let url = activity.webpageURL {
                        DeepLinkBridge.shared.handle(url: url.absoluteString)
                    }
                }
        }
    }
}
