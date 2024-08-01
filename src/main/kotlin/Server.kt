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
                        val indexTemplateData = File(item.readText()).readText()
                        val headerData = File("root/__assets/header.html").readText()
                        val footerData = File("root/__assets/footer.html").readText()
                        val destinationFilename = item.absolutePath.replace(".settings/index", "index.html")
                        val ingressMdFilename = item.absolutePath.replace(".settings/index", "ingress.md")
                        val ingressData = if (File(ingressMdFilename).exists()) File(ingressMdFilename).readText() else ""
                        val indexMdFilename = item.absolutePath.replace(".settings/index", "index.md")
                        val indexData = if (File(ingressMdFilename).exists()) File(indexMdFilename).readText() else ""
                        val addendumMdFilename = item.absolutePath.replace(".settings/index", "addendum.md")
                        val addendumData = if (File(addendumMdFilename).exists()) File(addendumMdFilename).readText() else ""

                        val source = indexTemplateData
                            .replace("<!-- STRMAGIX:header -->", headerData)
                            .replace("<!-- STRMAGIX:ingress -->", ingressData)
                            .replace("<!-- STRMAGIX:main -->", indexData)
                            .replace("<!-- STRMAGIX:addendum -->", addendumData)
                            .replace("<!-- STRMAGIX:footer -->", footerData)

                        File(destinationFilename).writeText(source)
                    }
                }
            }
            staticFiles("/", File("root"))
        }
    }.start(wait = true)
}
