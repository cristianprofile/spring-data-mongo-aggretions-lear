package com.example.demomongo

import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters
import org.jeasy.random.FieldPredicates
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.Month


@DataMongoTest
@TestInstance(PER_CLASS)
class DemoMongoApplicationTests {

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var deliveryRepository: DeliveryRepository

    private lateinit var easyRandom: EasyRandom
    

    @BeforeAll
    fun setUp() {
        val parameters = EasyRandomParameters()
            .seed(100000L)
            .objectPoolSize(100)
            .excludeField(FieldPredicates.named("globalRole"))
            .excludeField(FieldPredicates.named("planDefinition"))
            .excludeField(FieldPredicates.named("plan"))
            .randomizationDepth(3)
            .charset(Charset.forName("UTF-8"))
            .stringLengthRange(20, 30)
            .overrideDefaultInitialization(true)
            .collectionSizeRange(1, 3)
            .scanClasspathForConcreteTypes(true)
        easyRandom = EasyRandom(parameters)
    }
    
    @Test
    fun testUnwindAndJoinMongoQuery() {
        // ************* create example data  // 
        val price1 = easyRandom.nextObject(Price::class.java).copy(
            value= 4, 
            date = LocalDateTime.of(2016, Month.APRIL, 15, 3, 15)
        )
        val price2 = easyRandom.nextObject(Price::class.java).copy(
            value= 5, 
            date = LocalDateTime.of(2016, Month.MAY, 15, 3, 15))
     
        val comment1 = easyRandom.nextObject(Comment::class.java).copy(
            value= 6, 
            date = LocalDateTime.of(2018, Month.APRIL, 15, 3, 15)
        )
        val comment2 = easyRandom.nextObject(Comment::class.java).copy(
            value= 7, 
            date = LocalDateTime.of(2018, Month.MAY, 15, 3, 15))



        val order = easyRandom.nextObject(Order::class.java).copy(id = null,item= "itema",  prices = mutableListOf(price1,price2), comments = mutableListOf(comment1, comment2))
        val order2 = easyRandom.nextObject(Order::class.java).copy(id = null,item= "itemb",  prices = mutableListOf(price1,price2), comments = mutableListOf(comment1, comment2))
        
        val savedOrder1 = orderRepository.save(order)
            orderRepository.save(order2)


        val delivery = easyRandom.nextObject(Delivery::class.java).copy(
            orderId = savedOrder1.id!!,
            code = "pepito")

        deliveryRepository.save(delivery)

        // ************* end  create example data  // 
        
        // test unwind
        val findByOrderListUnwind = orderRepository.findByOrderListUnwind()

        findByOrderListUnwind.forEach {
            assert(it.comment == comment1 && it.price == price1)
        }

        // end test unwind


        // test join using lookup

        val findByDeliveryJoinOrder = deliveryRepository.findByDeliveryJoinOrder()
        findByDeliveryJoinOrder.forEach {
            assert(it.orders.size==1 && it.orders.contains(savedOrder1))
        }

    }

}

