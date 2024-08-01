import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.routing.*
import java.io.File

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(CallLogging)

        routing {
            get("/scripts/copy-index-files") {
                println("Scripts: Copy index files")

                File("root").walk().forEach { item ->
                    if (item.endsWith(".settings/index")) {
                        val indexFile = item.readText()
                        val headerData = File("root/__assets/header.html").readText()
                        val footerData = File("root/__assets/footer.html").readText()
                        val sourceData = File(indexFile)
                            .readText()
                            .replace("<!-- header magix -->", headerData)
                            .replace("<!-- footer magix -->", footerData)
                        val destFilename = item.absolutePath.replace(".settings/index", "index.html")


                        if (destFilename.contains("propelling.agency/")) {
                            println("Copying $indexFile to ${destFilename.split("propelling.agency/").last()}")
                        }
                        else {
                            println("Copying $indexFile to $destFilename")
                        }

                        File(destFilename).writeText(sourceData)
                    }
                }
            }
            staticFiles("/", File("root"))
        }
    }.start(wait = true)
}
