import SwiftUI
import ComposeApp

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        IosBackgroundTaskManager.shared.register()
        IosBackgroundTaskManager.shared.schedule()
        return true
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    @StateObject private var toastManager = ToastManager()
    
    init() {
        // Bridge Kotlin's toast call to our Swift ToastManager
        PlatformUtils_iosKt.swiftToastHandler = { message in
            // Must dispatch to main thread (though ToastManager does it too, safer here)
            DispatchQueue.main.async {
                // Accessing the shared instance if needed, but here we can't easily access the StateObject instance directly from static context.
                // A common pattern is to use a notification or a shared singleton for the manager.
                // For simplicity, let's make ToastManager a singleton or use a notification.
                // Let's use NotificationCenter as it is robust.
                NotificationCenter.default.post(name: NSNotification.Name("ShowToast"), object: message)
            }
        }
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(toastManager)
                .onOpenURL { url in
                    // Complete the Supabase OAuth (kiito://auth-callback) redirect.
                    IosAuthDeepLink.shared.handle(urlString: url.absoluteString)
                }
                .onReceive(NotificationCenter.default.publisher(for: NSNotification.Name("ShowToast"))) { notification in
                    if let message = notification.object as? String {
                        toastManager.show(message: message)
                    }
                }
                .toast(
                    isPresented: $toastManager.isPresented,
                    message: toastManager.message,
                    tint: toastManager.tint
                )
        }
    }
}