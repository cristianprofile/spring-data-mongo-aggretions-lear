## Spring data mongo example using unwind and lookup aggregations
This projects was created to be able to learn how to use unwind and lookup aggregations using Spring data mongo. I use Kotlin for the faster prototype.  Data Model classes in mongo:

    @Document
    data class Order (
    @Id
    val id: String? = null,
    val item: String,
    val prices: MutableList<Price> = mutableListOf(),
    val comments: MutableList<Comment> = mutableListOf())

    data class Price (
    val date: LocalDateTime = LocalDateTime.now(),
    val value: Int = 8)

    data class Comment (
    val date: LocalDateTime = LocalDateTime.now(),
    val value: Int = 8)

    @Document
    data class Delivery (
    @Id
    val id: String? = null,
    val orderId: String,
    val code: String)

Application use mongo database in memory (flapdoodle) to be able to test 2 functions:

- findByOrderListUnwind get all orders only adding the first comment a and prices ordered by date desc

- findByDeliveryJoinOrder get all deliveries creating a join sql like operation using lookup
