import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.ncgroup.kscan.MainView

fun main() =
    application {
        Window(onCloseRequest = ::exitApplication) {
            MainView()
        }
    }
