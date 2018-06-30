package abos.server

import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.*
import grails.plugin.springsecurity.annotation.Secured

@Secured('ROLE_USER')
class UserRoleController {

    static scaffold = UserRole
}
