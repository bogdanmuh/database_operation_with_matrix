import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class Server() {
    private val sSocket: ServerSocket = ServerSocket(5904)
    private val clients = mutableListOf<Client>()
    private val clientsSentData = mutableListOf<Client>()
    private var stop = false
    private var dbHelper = DBHelper("matrix")
    private val messageListeners = mutableListOf<(String)-> Unit>()
    fun addMessageListener(l:(String)-> Unit){ messageListeners.add(l) }
    fun removeMessageListener(l:(String)-> Unit){ messageListeners.remove(l) }

    inner class Client(private val socket: Socket){
        private var sio: SocketIO? = null
        private val id : Int = clients.size + 1
        fun startDialog(){
            sio = SocketIO(socket).apply{
                println("Попытка подключения startDialog")
                addSocketClosedListener { clients.remove(this@Client) }
                addMessageListener { data ->
                    //messageListeners.forEach { l -> l("[$id] $data") }
                    clients.add(this@Client)
                    var arras =data.split(',')
                    var  i = arras.get(1).toInt()
                    var  j = arras.get(2).toInt()
                    var  x = arras.get(3).toInt()
                    dbHelper.fullresultMatrix(x,i,j)
                }
                println("Попытка подключения startDialog 2")
                startDataReceiving()
            }
        }

        fun stop(){
            sio?.stop()
        }

        fun send(data: String){
            sio?.send(data)
        }
    }

    fun send(i: Int, data: String){
        clients[i].send(data)
    }

    fun stop(){
        sSocket.close()
        stop = true
    }

    fun start() {
        messageListeners.forEach { l -> l("[SERVER] Сервер запущен.") }
        stop = false
        dbHelper.run {
            createDatabase("matrix_elements.sql")
            fillMatrix()
            thread {
                try {
                    while (!stop) {
                        println("Попытка подключения")
                        clients.add(
                            Client(
                                sSocket.accept()
                            ).also { client -> client.startDialog() })
                        println("Попытка подключения 2 ")
                        for (i in 1..10){
                            for(j in 1..10){

                                clientsSentData.add(clients[1])
                                clientsSentData.forEach{client ->if (clients[1]==client) client.send("$i,"+" $j,"+"${dbHelper.getColumn(i)},"+" ${dbHelper.getRow(j)}")}
                                clients.removeAt(1)
                            }
                        }
                    }
                } catch (e: Exception){
                    messageListeners.forEach { l->
                        e.message?.let { l(it) }
                    }
                } finally {
                    stopAllClients()
                    sSocket.close()
                    messageListeners.forEach { l ->
                        l("[SERVER] Сервер остановлен.")
                    }
                }
            }
        }
    }

    private fun stopAllClients(){
        clients.forEach { client -> client.stop() }
    }
}
