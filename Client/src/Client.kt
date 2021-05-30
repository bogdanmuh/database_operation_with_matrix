import java.awt.TextArea
import java.awt.TextField
import java.net.Socket
import javax.swing.GroupLayout
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.system.exitProcess

class Client(val host: String, val port: Int) {
    private val socket: Socket
    private val communicator: SocketIO

    private val messageListeners = mutableListOf<(String)-> Unit>()

    init{
        socket = Socket(host, port)
        communicator = SocketIO(socket)
    }
    fun addMessageListener(l:(String)-> Unit){ messageListeners.add(l)}
    fun removeMessageListener(l:(String)-> Unit){ messageListeners.remove(l) }

    fun stop(){ communicator.stop() }

    fun start() {
        communicator.apply {
            addMessageListener { data ->
                var arras = data.split(',')
                var i = arras.get(1)
                var j = arras.get(2)
                var x = arras.get(3).split('+')
                var y = arras.get(4).split('+')
                var result = 0
                for (i in 1..x.size) {
                    result = x[i].toInt() + y[i].toInt()
                }
                send(i + j + "$result")


            }
            startDataReceiving()
        }
    }
        fun send(data: String) {
            communicator.sendData(data)
        }

        fun addSessionFinishedListener(l: () -> Unit) {
            communicator.addSocketClosedListener(l)
        }

        fun removeSessionFinishedListener(l: () -> Unit) {
            communicator.removeSocketClosedListener(l)
        }
    }

