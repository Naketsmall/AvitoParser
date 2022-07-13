import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.awt.Color
import java.nio.charset.StandardCharsets

class Parser(private var link: String, private var hook: String) {
    private var wall: Document
    private var running: Boolean = false
    private var checked: ArrayDeque<String> = ArrayDeque(listOf("", "", "", "", "", "", "", "", "", ""))

    init {
        wall = Jsoup.connect(link).get()
        fillChecked()
    }

    fun start() {
        running = true
        sendMessage("/moskva/kommercheskaya_nedvizhimost/svobodnogo_naznacheniya_42.5_m_2331269806")
        /*while (running) {

            wall = Jsoup.connect(link).get()
            val cur = wall.getElementsByClass("items-items-kAJAg")[0].children()
            for (el in 0 until checked.size){
                if ("ads" !in cur[el].className() && "witcher" !in cur[el].className()){
                    val path: String = cur[el].child(1).child(0).child(0).attr("href")
                    if (path !in checked) {
                        println("\n"+path)
                        sendMessage(path)
                        checked.removeLast()
                        checked.addFirst(path)
                    }
                }
            }
            Thread.sleep(20000)
            print(".")
        }
*/
    }

    private fun fillChecked(){
        val cur = wall.getElementsByClass("items-items-kAJAg")[0].children()
        for (el in 0 until checked.size - 5){
            if ("ads" !in cur[el].className() && "witcher" !in cur[el].className()){
                checked.addLast(cur[el].child(1).child(0).child(0).attr("href"))
                checked.removeFirst()
                println(cur[el].child(1).child(0).child(0).attr("href"))
                sendMessage(cur[el].child(1).child(0).child(0).attr("href"))
                break
            }
        }
    }

    private fun sendMessage(path: String) {
        val itemPage = Jsoup.connect("https://m.avito.ru$path").get()

        var descr = try {
            println("1st block: " + itemPage.body().getElementsByClass("style-item-description-text-SzN56").size)
            itemPage.body().getElementsByClass("style-item-description-text-SzN56")[0].text()
        } catch (e: java.lang.IndexOutOfBoundsException) {
            try {
                println("2nd block: " + itemPage.body().getElementsByClass("style-item-description-1e2Yo").size)
                itemPage.body().getElementsByClass("style-item-description-1e2Yo")[0].text()
            } catch (e: java.lang.IndexOutOfBoundsException) {
                itemPage.body().getElementsByClass("style-item-description-html-1_RNo")[0].text()
            }
        }

        if (descr.length > 800)
            descr = descr.substring(800) + "..."

        descr = descr.replace(""""""", "")
        val title = itemPage.body().getElementsByClass("title-info-title-text")[0].text()

//        Jsoup.connect(hook).data("content", "https://m.avito.ru$path\nОписание:\n$descr").post()
        val req = Message(title, descr, "https://m.avito.ru$path", "").build()
        println(req)
        println(Jsoup.connect(hook).header("Content-Type", "application/json").requestBody(req).method(Connection.Method.POST).execute().statusCode())
        //Jsoup.connect(hook)

    }

    private class Message(
        var title: String,
        var description: String,
        var url: String,
        var imUrl: String,
        var color: Int = 9694793
    ) {


        fun build(): String {
            return String("""
                {
                    "content": "Нашел новое предложение:",
                    "embeds": [{
                            "title": "$title",
                            "color": $color,
                            "description": "$description",
                            "url": "$url",
                            "image": {"url": "$imUrl"}}]
                }""".toByteArray(), StandardCharsets.UTF_8)
        }
    }
}

