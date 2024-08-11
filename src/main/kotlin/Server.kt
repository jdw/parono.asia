import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.routing.*
import java.io.File

val pictureFileFormats = setOf("png", "webp", "jpg", "jpeg")

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

                println("Structuring The Daily Alexis...").run {
                    val entryDirs = mutableListOf<File>()
                    File("root/daily-alexis/entry").walkBottomUp().forEach { item ->
                        if (item.isDirectory) {
                            for (idx in 1..100) {
                                val entryDir = File(item.absolutePath + "/$idx")
                                if (!entryDir.exists()) break

                                entryDirs.add(entryDir)
                                println(entryDir.absolutePath)
                            }
                        }
                    }

                    val headerData = File("root/__assets/header.html").readText()
                    val footerData = File("root/__assets/footer.html").readText()
                    val ingressData = File("root/daily-alexis/ingress.md").readText()
                    val addendumData = File("root/daily-alexis/addendum.md").readText()
                    val showPictureTemplateData = File("root/daily-alexis/__assets/show_picture.md").readText()
                    val magix = "<!-- STRMAGIX:main -->"
                    val indexData = File("root/daily-alexis/__assets/standard-entry.html").readText()
                        .replace("<!-- STRMAGIX:header -->", headerData)
                        .replace("<!-- STRMAGIX:ingress -->", ingressData)
                        .replace("<!-- STRMAGIX:addendum -->", addendumData)
                        .replace("<!-- STRMAGIX:footer -->", footerData)

                    val rssItems = mutableListOf<Map<String, String>>()
                    entryDirs.forEachIndexed { idx, dir ->
                        createIndexFiles(entryDirs.getOrNull(idx - 1),
                            dir,
                            entryDirs.getOrNull(idx + 1),
                            indexData.replace(magix, showPictureTemplateData))

                        rssItems += createRssFeedItem(dir)
                    }

                    var itemsXml = ""
                    rssItems.sortedBy { it["title"] }.forEach { item ->
                        itemsXml += "<item>\n"
                        listOf("title", "description", "pubDate", "link").forEach {  itemsXml += "\t<$it>${item[it]}</$it>\r" }

                        itemsXml += "</item>\n"
                    }

                    val feed = File("root/daily-alexis/__assets/rss-base.xml").readText().replace("<!-- ITEMS STRMAGIX -->", itemsXml)
                    File("root/daily-alexis/rss.xml").writeText(feed)
                }

            }
            staticFiles("/", File("root"))
        }
    }.start(wait = true)
}


fun createRssFeedItem(currentDir: File): Map<String, String> {
    val ret = mutableMapOf<String, String>()

    ret["pubDate"] = File(currentDir.absolutePath + "/pubDate.txt").readText()
    val (date, entry) = currentDir
        .absolutePath
        .split("entry/")
        .last()
        .split("/")
    ret["title"] = "$date, #: $entry"
    ret["description"] = File(currentDir.absolutePath + "/description.txt").readText()
    ret["link"] = "https://parono.asia" + currentDir.absolutePath.split("root").last()

    return ret
}

fun createIndexFiles(prevDir: File?, currentDir: File, nextDir: File?, templateData: String) {
    var newTemplateData = templateData
    val pics = findPictures(currentDir)
    prevDir?.let { newTemplateData = newTemplateData.replace("<!-- arrow entry prev -->", "[⏪](${prevDir.absolutePath.split("root").last()})") }
    nextDir?.let { newTemplateData = newTemplateData.replace("<!-- arrow entry next -->", "[⏩](${nextDir.absolutePath.split("root").last()})") }

    pics.forEachIndexed { idx, _ ->
        createPictureHtmlFile(pics, idx, currentDir, newTemplateData)
    }

    File(currentDir.absolutePath + "/1.html").readText().let {
        File(currentDir.absolutePath + "/index.html").writeText(it)
        File("root/daily-alexis/index.html").writeText(it)
    }
}


fun findPictures(dir: File): List<File> {
    val ret = mutableListOf<File>()
    pictureFileFormats.forEach { fileFormat ->
        for (idx in 1..1000) {
            val file = File("${dir.absolutePath}/$idx.$fileFormat")
            if (file.exists()) ret.add(file)
        }
    }

    ret.forEach {
        println("Found: ${it.absolutePath}")
    }

    return ret
}


fun createPictureHtmlFile(pics: List<File>, idx: Int, currentDir: File, templateData: String) {
    val fileNumber = idx + 1
    val currentPic = pics[idx]

    var data = templateData
        .replace("<!-- main picture -->", currentPic.absolutePath.split("root").last())
    File(currentDir.absolutePath + "/$fileNumber.txt").let {
        if (it.exists()) data = data.replace("<!-- main picture caption -->", it.readText())
    }

    if (fileNumber != 1) {
        val path = "${currentDir.absolutePath.split("root").last()}/${fileNumber - 1}.html"
        data = data.replace("<!-- arrow pic prev -->", "[⬅\uFE0F]($path)")
    }

    pics.getOrNull(idx + 1)?.let {
        val path = "${currentDir.absolutePath.split("root").last()}/${fileNumber + 1}.html"
        data = data.replace("<!-- arrow pic next -->", "[➡\uFE0F]($path)")
    }

    //
    val (date, entry) = currentDir
        .absolutePath
        .split("entry/")
        .last()
        .split("/")

    data = data.replace("<!-- date of entry -->", date)
    data = data.replace("<!-- number of entry -->", "#$entry")

    File(currentDir.absolutePath + "/description.txt").let {
        if (it.exists()) data = data.replace("<!-- description -->", it.readText())
    }

    File(currentDir.absolutePath + "/$fileNumber.html").writeText(data)
}