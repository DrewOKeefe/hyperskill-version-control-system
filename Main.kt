package svcs
import java.io.File

fun main(args: Array<String>) {
//    val args: Array<String> = readln().split(" ").map { it }.toTypedArray()
//    val file1 = File("file1.txt").createNewFile()
//    val file2 = File("file2.txt").createNewFile()

    val command = if(args.isNotEmpty()) args[0] else ""
    val input = if (args.size > 1) {
        args.copyOfRange(1, args.lastIndex + 1)
            .joinToString(" ")
            .replace("\"","")
    } else ""
    val vcs = File("./vcs").mkdir()
    val commits = File("vcs/commits").mkdir()
    val config = File("vcs/config.txt")
    val index = File("vcs/index.txt")
    val log = File("vcs/log.txt")

    fun helpMenu() {
        println("""
            These are SVCS commands:
            config      Get and set a username.
            add         Add a file to the index.
            log         Show commit logs.
            commit      Save changes.
            checkout    Restore a file.
        """.trimIndent())
    }
    fun config() {
        if (!config.exists()) config.createNewFile()
        if (args.size > 1) {
            config.writeText(input)
            println("The username is ${config.readText()}.")
        } else {
            if (config.readText().isBlank())
                println("Please, tell me who you are.")
            else println("The username is ${config.readText()}.")}
    }
    fun add() {
        if (!index.exists()) index.createNewFile()
        if (args.size > 1) {
            if (File(input).exists()) {
                index.appendText("$input\n")
                println("The file '$input' is tracked.")
            } else println("Can't find '$input'.")
        } else if (index.readText().isNotEmpty()) {
            println("Tracked files:\n${index.readText()}")
        } else println("Add a file to the index.")
    }
    fun log() {
        if (!log.exists()) log.createNewFile()
        if (log.readText().isBlank())
            println("No commits yet.")
        else {
            for (line in log.readLines()) println(line)
        }
    }

    fun commit () {
        if (args.size > 1) {
            if (!log.exists()) log.createNewFile()
            val commitCode = File(index.readLines().last()).readText().hashCode()
            if (log.readText().contains("commit $commitCode")) println("Nothing to commit.")
            else {
                File("./vcs/commits/$commitCode").mkdir()
                File("$index").forEachLine { File("./vcs/commits/$commitCode/$it").createNewFile() }
                for (file in index.readLines()) {
                    File("./vcs/commits/$commitCode/$file").writeText(File(file).readText())
                }
//                val rootDir = File(".")
//                for (file in rootDir.listFiles()) {
//                    File("./vcs/commits/$commitCode/$file").writeText("${file.readLines()}")
//                    println("passed writeText")
//                }

                val list = mutableListOf<String>(log.readLines().joinToString("\n"))
                log.writeText("""
                    commit $commitCode
                    Author: ${config.readText()}
                    $input
                """.trimIndent() + "\n\n" + list.joinToString("\n")
                )
                println("Changes are committed.")
            }
        } else println("Message was not passed.")
    }

    fun checkout() {
        if (args.size > 1) {
            if (log.readText().contains("commit $input")) {
                println("Switched to commit $input.")
                File("/vcs/commits/$input").walkTopDown().forEach { println(it) }
                    for (file in index.readLines()) {
                        File(file).writeText(
                            File("./vcs/commits/$input/$file").readText())
                    }
            } else println("Commit does not exist.")
        } else println("Commit id was not passed.")
    }
    when {
        args.isEmpty() -> helpMenu()
        command == "--help" -> helpMenu()
        command == "config" -> config()
        command == "add" -> add()
        command == "log" -> log()
        command == "commit" -> commit()
        command == "checkout" -> checkout()
        else -> println("\'$command\' is not a SVCS command.")
    }
}