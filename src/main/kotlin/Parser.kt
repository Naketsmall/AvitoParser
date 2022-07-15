import org.jsoup.Connection
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.nio.charset.StandardCharsets

class Parser(private var link: String, private var hook: String) {
    private var wall: Document
    private var running: Boolean = false
    private var checked: ArrayDeque<String> = ArrayDeque(listOf("", "", "", "", "", "", "", "", "", ""))
    private val SLEEP_CONST: Long = 20000

    init {
        wall = Jsoup.connect(link).userAgent("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36").get()
        fillChecked()
        println("initialized")
    }

    private fun fillChecked(){
        val cur = wall.getElementsByClass("items-items-kAJAg")[0].children()
        for (el in 0 until checked.size){
            if ("ads" !in cur[el].className() && "witcher" !in cur[el].className()){
                checked.addLast(cur[el].child(1).child(0).child(0).attr("href"))
                checked.removeFirst()
                println(cur[el].child(1).child(0).child(0).attr("href"))
            }
        }
        Thread.sleep(SLEEP_CONST)
    }

    fun start() {
        running = true
        while (running) {
            try {
                wall = Jsoup.connect(link)
                    .userAgent("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36")
                    .get()
            } catch (e: HttpStatusException) {
                e.printStackTrace()
                continue
            }
            val cur = wall.getElementsByClass("items-items-kAJAg")[0].children()
            for (el in 0 until checked.size){
                if ("ads" !in cur[el].className() && "witcher" !in cur[el].className()){
                    val path: String = cur[el].child(1).child(0).child(0).attr("href")
                    if (path !in checked) {
                        println("\n"+path)
                        sendMessage(cur[el])
                        checked.removeLast()
                        checked.addFirst(path)
                    }
                }
            }
            Thread.sleep(SLEEP_CONST)
            print(".")
        }

    }

    private fun sendMessage(el: Element) {
        val title = try {el.child(1).child(1).child(1).child(0).attr("title") }
        catch (e: java.lang.IndexOutOfBoundsException) {""}

        val url = try {"https://m.avito.ru${el.child(1).child(0).child(0).attr("href")}"}
        catch (e: java.lang.IndexOutOfBoundsException) {""}

        val price = try {el.child(1).child(1).child(2).child(0).child(0).child(3).text()}
        catch (e: java.lang.IndexOutOfBoundsException) {""}

        var descr = try {el.child(0).attr("content").toString().replace(""""""", "").replace("\n", " ")}
        catch (e: java.lang.IndexOutOfBoundsException) {""}


        if (descr.length > 800)
            descr = descr.substring(0, 800) + "..."
        val imUrl = try {el.child(1).child(0).child(0).child(0).child(0).child(0)
            .child(0).child(0).child(0).attr("srcset").split(',').last().split(' ').first()}
            catch (e: java.lang.IndexOutOfBoundsException) {""}



        val req = Message(title, price, descr, url, imUrl).build()
        println(req)
        try {
            println(
                Jsoup.connect(hook).header("Content-Type", "application/json").requestBody(req)
                    .method(Connection.Method.POST).execute().statusCode()
            )
        } catch (e: HttpStatusException) {
            e.printStackTrace()
        }

    }

    private class Message(
        var title: String,
        var price: String,
        var description: String,
        var url: String,
        var imUrl: String,
        var color: Int = 10828031
    ) {

        fun build(): String {
            return String("""
                {
                    "content": "Нашел новое предложение:",
                    "embeds": [{
                            "title": "$title \nЦена: $price",
                            "color": $color,
                            "description": "$description",
                            "url": "$url",
                            "image": {"url": "$imUrl"}}]
                }""".toByteArray(), StandardCharsets.UTF_8)
        }
    }
}

