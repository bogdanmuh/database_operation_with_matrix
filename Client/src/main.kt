import java.awt.TextArea
import java.awt.TextField
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import javax.swing.GroupLayout
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.concurrent.thread
import kotlin.system.exitProcess

fun main() {
    val client = Client("localhost", 5904 )
    client.start()
}
