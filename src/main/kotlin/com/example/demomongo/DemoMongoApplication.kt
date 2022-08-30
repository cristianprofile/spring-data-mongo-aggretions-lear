package com.example.demomongo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.ConvertOperators
import org.springframework.data.mongodb.core.aggregation.LookupOperation
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime


@Document
data class Order (
    @Id
    val id: String? = null,
    val item: String,
    val prices: MutableList<Price> = mutableListOf(),
    val comments: MutableList<Comment> = mutableListOf(),
)

data class Price (
    val date: LocalDateTime = LocalDateTime.now(),
    val value: Int = 8
)

data class Comment (
    val date: LocalDateTime = LocalDateTime.now(),
    val value: Int = 8
)

@Document
data class Delivery (
    @Id
    val id: String? = null,
    val orderId: String,
    val code: String,
)


data class OrderUnwind (

    val id: String? = null,
    val item: String,
    val price: Price,
    val comment: Comment
)


data class DeliveryProjection (
    val id: String? = null,
    val orderId: String,
    val code: String,
    val orders: MutableList<Order>
)


@Repository
interface OrderRepository : CrudRepository<Order, String> , OrderCustomRepository

@Repository
interface DeliveryRepository : CrudRepository<Delivery, String>, DeliveryCustomRepository

interface DeliveryCustomRepository {

    fun findByDeliveryJoinOrder(): MutableList<DeliveryProjection>
}


interface OrderCustomRepository {

    fun findByOrderListUnwind(): MutableList<OrderUnwind>
}

class DeliveryCustomRepositoryImpl(private val mongoTemplate: MongoTemplate) : DeliveryCustomRepository {
    override fun findByDeliveryJoinOrder(): MutableList<DeliveryProjection> {
        val projectionOperation = Aggregation.project()
            .andInclude("orderId", "code")
            .and(ConvertOperators.ToObjectId.toObjectId("\$orderId")).`as`("orderIdDoc")



        val lookupOperation = LookupOperation.newLookup()
            .from("order")
            .localField("orderIdDoc")
            .foreignField("_id")
            .`as`("orders")


        val agg2= Aggregation.newAggregation(projectionOperation, lookupOperation)

        return mongoTemplate.aggregate(agg2, "delivery", DeliveryProjection::class.java).mappedResults
    }
}

class OrderCustomRepositoryImpl(private val mongoTemplate: MongoTemplate) : OrderCustomRepository {
    override fun findByOrderListUnwind(): MutableList<OrderUnwind> {
        val unwindPricesStage = Aggregation.unwind("prices")
        val sortPricesStage = Aggregation.sort(ASC, "date")
        val unwindCommentsStage = Aggregation.unwind("comments")
        val sortCommentsStage = Aggregation.sort(ASC, "date")

        val groupStage = Aggregation.group("_id")
            .last("item").`as`("item")
            .first("prices").`as`("price")
            .first("comments").`as`("comment")

        val agg = Aggregation.newAggregation(unwindPricesStage,sortPricesStage, unwindCommentsStage, sortCommentsStage, groupStage)
        return mongoTemplate.aggregate(agg, "order", OrderUnwind::class.java).mappedResults
    }
}

@SpringBootApplication
class DemoMongoApplication

fun main(args: Array<String>) {
    runApplication<DemoMongoApplication>(*args)
}
