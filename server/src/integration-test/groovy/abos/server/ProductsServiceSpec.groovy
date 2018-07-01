package abos.server

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.hibernate.SessionFactory
import spock.lang.Specification

@Integration
@Rollback
class ProductsServiceSpec extends Specification {

    ProductsService productsService
    SessionFactory sessionFactory

    private Long setupData() {
        // TODO: Populate valid domain instances and return a valid ID
        //new Products(...).save(flush: true, failOnError: true)
        //new Products(...).save(flush: true, failOnError: true)
        //Products products = new Products(...).save(flush: true, failOnError: true)
        //new Products(...).save(flush: true, failOnError: true)
        //new Products(...).save(flush: true, failOnError: true)
        assert false, "TODO: Provide a setupData() implementation for this generated test suite"
        //products.id
    }

    void "test get"() {
        setupData()

        expect:
        productsService.get(1) != null
    }

    void "test list"() {
        setupData()

        when:
        List<Products> productsList = productsService.list(max: 2, offset: 2)

        then:
        productsList.size() == 2
        assert false, "TODO: Verify the correct instances are returned"
    }

    void "test count"() {
        setupData()

        expect:
        productsService.count() == 5
    }

    void "test delete"() {
        Long productsId = setupData()

        expect:
        productsService.count() == 5

        when:
        productsService.delete(productsId)
        sessionFactory.currentSession.flush()

        then:
        productsService.count() == 4
    }

    void "test save"() {
        when:
        assert false, "TODO: Provide a valid instance to save"
        Products products = new Products()
        productsService.save(products)

        then:
        products.id != null
    }
}
