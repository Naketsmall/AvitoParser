fun main(){
    var checked: ArrayDeque<String> = ArrayDeque(listOf("", "", "", "", ""))
    for (i in 0..4){
        checked.removeFirst()
        checked.add("dwd$i")
        println(checked)
    }
    println("dwd3" in checked)
    println(checked[0])
    println(checked.first())
}