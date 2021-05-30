import java.io.File
import java.sql.*
import kotlin.random.Random

/**
 * Класс помощник для базы данных
 * Будет создавать,связывать и пополнять бд
 * @param dbName - имя базы данных
 * @param address - адрес на котором работает бд "localhost" по умолчанию
 * @param port - порт на котором работает бд "3306" по умолчанию
 * @param user - логин для доступа к бд "root" по умолчанию
 * @param password - пароль для доступа к бд "root" по умолчанию
 */
class DBHelper(
    val dbName : String,
    val address : String = "localhost",
    val port : Int = 3306,
    val user : String = "root",
    val password : String = ""
) {
    private var connection: Connection? = null
    private var statement: Statement? = null

    /**
     * Метод создания базы данных с указанием файла, где есть sql-запросы
     * @param userdata - имя файла с запросами бд
     */
    fun createDatabase(userdata: String) {
        connect()
        dropAllTables()
        createTablesFromDump(userdata)
    }
    /**
     * Метод подключения к бд  $dbName
     * Обращение к субд через statement (sql запросы)
     */
    private fun connect(){
        //Проверка на закрытое подключение.утверждение
        statement?.run{ if (!isClosed) close() }
        var rep = 0
        //Попытка подключения к бд
        do {
            try {
                connection =
                    DriverManager.getConnection("jdbc:mysql://$address:$port/$dbName?serverTimezone=UTC",
                        user,password
                    )
                statement =
                    DriverManager.getConnection("jdbc:mysql://$address:$port/$dbName?serverTimezone=UTC",
                        user, password
                    ).createStatement()
            } catch (e: SQLSyntaxErrorException) {
                println("Ошибка подключения к бд ${dbName} : \n${e.toString()}")
                println("Попытка создания бд ${dbName}")
                val tstmt =
                    DriverManager.getConnection("jdbc:mysql://$address:$port/?serverTimezone=UTC", user, password)
                        .createStatement()
                tstmt.execute("CREATE SCHEMA `$dbName`")
                tstmt.closeOnCompletion()
                rep++
            }
        } while (statement == null && rep < 2)
    }
    private fun dropAllTables(){
        println("Удаление всех таблиц в базе данных...")
        statement?.execute("DROP TABLE if exists `matrix elements`")


        println("Все таблицы удалены.")
    }

    /**
     * Создание таблиц через готовые sql запросы в файле
     * @param userdata - название файла
     */
    private fun createTablesFromDump(userdata : String){
        println("Создание структуры базы данных из дампа...")
        try {
            var query = ""
            File(userdata).forEachLine {
                if(!it.startsWith("--") && it.isNotEmpty()){
                    query += it
                    if (it.endsWith(';')) {
                        statement?.addBatch(query)
                        query = ""
                    }
                }
            }
            statement?.executeBatch()
            statement?.execute("INSERT INTO `$dbName`.`matrix elements` (`id matrix`, `row`, `column`, `value`) VALUES ('1', '12', '32', '4');")
            println("Структура базы данных успешно создана.")
        }
        catch (e: SQLException){
            println(e.message)
        }
        catch (e: Exception){
            println(e.message)
        }
    }
    fun fillMatrix(){
        println("Заполнения Матрицы случайными числами  ...")
        try{
            statement?.addBatch("Use $dbName")
            val requestTemplate = "INSERT INTO `matrix elements`" +
                    " (`id matrix`, `row`, `column`, `value`) VALUES "
            for (i in 1..10 ){
                for (j in 1..10 ){
                    var x=Random.nextInt(1,20)
                    var request = "$requestTemplate"+"(1,"+"$i"+",$j"+",$x"+");"
                    statement?.addBatch(request)
                }
            }
            for (i in 1..10 ){
                for (j in 1..10 ){
                    var x=Random.nextInt(1,20)
                    var request = "$requestTemplate"+"(2,"+"$i"+",$j"+",$x"+");"
                    statement?.addBatch(request)
                }
            }
            statement?.executeBatch()
            statement?.clearBatch()
            println("Таблица успешно заполнена случайными элементами матрицы!")
        } catch(e: Exception){
            println(e.toString())
        }
    }
    fun fullresultMatrix(row:Int,column:Int,data:Int){
        try{
            val requestTemplate = "INSERT INTO `matrix elements`" +
                    "(`id matrix`, `row`, `column`, `value`) VALUES "
            var request = "$requestTemplate"+"(3,"+"$row"+"$,column"+"$data"+");"
            statement?.addBatch(request)
            statement?.executeBatch()
            statement?.clearBatch()
            println("элемент с индексами  $row $column  лежит в матрице")
        } catch(e: Exception){
            println(e.toString())
        }

    }
    fun getColumn( i:Int  ): String {
        val sql ="SELECT 'value' FROM `matrix elements` WHERE `id matrix`='1' AND `column`='$i'"
        val rs =statement?.executeQuery(sql)
        var result=""
        while(rs?.next()==true){
            var result =result +rs.getString(1)+","
        }
        return result
    }
    fun getRow( i:Int  ): String {
        val sql ="SELECT 'value' FROM `matrix elements` WHERE `id matrix`='1' AND `row`='$i'"
        val rs =statement?.executeQuery(sql)
        var result=""
        while(rs?.next()==true){
            var result =result +rs.getString(1)
        }
        return result
    }


}