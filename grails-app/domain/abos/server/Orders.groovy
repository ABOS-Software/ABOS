package abos.server

import grails.gorm.MultiTenant

class Orders implements MultiTenant<abos.server.Orders> {
    static belongsTo = [user: User, customer: Customers]
    BigDecimal cost
    int quantity
    BigDecimal amountPaid
    Boolean delivered
    Year year
    String userName

    static constraints = {
        cost min: 0.0, scale: 2
        amountPaid min: 0.0, scale: 2
        quantity min: 0
    }
    static mapping = {
        tenantId name: 'userName'
    }
}
