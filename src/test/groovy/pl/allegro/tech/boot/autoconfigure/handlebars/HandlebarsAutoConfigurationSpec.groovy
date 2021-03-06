package pl.allegro.tech.boot.autoconfigure.handlebars

import static org.springframework.boot.test.EnvironmentTestUtils.addEnvironment
import static org.springframework.web.servlet.support.RequestContext.WEB_APPLICATION_CONTEXT_ATTRIBUTE

import com.github.jknack.handlebars.cache.GuavaTemplateCache
import com.github.jknack.handlebars.cache.NullTemplateCache
import com.github.jknack.handlebars.springmvc.HandlebarsViewResolver
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.mock.web.MockServletContext
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext
import org.springframework.web.servlet.ViewResolver
import spock.lang.Specification

class HandlebarsAutoConfigurationSpec extends Specification {

    def context = new AnnotationConfigWebApplicationContext()

    def setup() {
        context.servletContext = new MockServletContext()
    }

    def cleanup() {
        context?.close()
    }

    def 'should configure handlebars'() {
        given:
        'register and refresh context'()

        expect:
        context.getBeanNamesForType(ViewResolver).length == 1
        context.getBean(ViewResolver) instanceof HandlebarsViewResolver
        context.getBean(HandlebarsViewResolver).handlebars.cache instanceof GuavaTemplateCache
    }

    def 'should configure handlebars without cache'() {
        given:
        'register and refresh context'('handlebars.cache:false')

        expect:
        context.getBean(HandlebarsViewResolver).handlebars.cache instanceof NullTemplateCache
    }

    def 'should resolve view'() {
        given:
        'register and refresh context'()

        expect:
        render('hello').contentAsString == 'hello world'
    }

    def 'should resolve view from custom classpath'() {
        given:
        'register and refresh context'('handlebars.prefix:classpath:views')

        expect:
        render('prefixed').contentAsString == 'prefixed body'
    }

    def 'should resolve view with custom suffix'() {
        given:
        'register and refresh context'('handlebars.suffix:.html')

        expect:
        render('suffixed').contentAsString == 'suffixed body'
    }

    def 'register and refresh context'(String... env) {
        addEnvironment(context, env)
        context.register(HandlebarsAutoConfiguration)
        context.refresh()
    }

    def render(String viewName) throws Exception {
        def resolver = context.getBean(HandlebarsViewResolver)
        def view = resolver.resolveViewName(viewName, Locale.UK)
        assert view
        def request = new MockHttpServletRequest()
        request.setAttribute(WEB_APPLICATION_CONTEXT_ATTRIBUTE, context)
        def response = new MockHttpServletResponse()
        view.render(null, request, response)
        response
    }
}
