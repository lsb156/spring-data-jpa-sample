package com.ssabae.springdatajpa.sample.test

import org.springframework.test.context.TestContext
import org.springframework.test.context.support.AbstractTestExecutionListener
import org.springframework.transaction.support.TransactionTemplate
import javax.persistence.EntityManager

class DataInitializeExecutionListener : AbstractTestExecutionListener() {
    override fun afterTestMethod(testContext: TestContext) {
        val applicationContext = testContext.applicationContext
        val transactionTemplate = applicationContext.getBean(TransactionTemplate::class.java)
        transactionTemplate.execute { status ->
            val em = applicationContext.getBean(EntityManager::class.java)
            em.createQuery("SELECT i FROM Issue i").resultStream.forEach(em::remove)
//            em.createQuery("SELECT l FROM Label l").resultStream.forEach(em::remove)
            em.createQuery("SELECT r FROM Repo r").resultStream.forEach(em::remove)
            em.createQuery("SELECT c FROM Comment c").resultStream.forEach(em::remove)

            // REMOVE 는 UPDATE(SOFT DELETE) 가 되므로 직접 삭제 쿼리를 작성한다.
            // 연관관계가 추가된다면, 연관관계들도 같이 삭제 추가해야 한다.
            em.createQuery("DELETE FROM Account").executeUpdate()
            em.flush()
            null
        }
    }
}