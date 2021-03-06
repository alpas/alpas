package dev.alpas.http.jetty

import dev.alpas.http.SessionListener
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.session.SessionHandler
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

internal class JettyRequestHandler(val session: SessionHandler?, servlet: AlpasServlet) :
    ServletContextHandler(null, "/", SESSIONS) {

    init {
        if (session != null) {
            sessionHandler = session
            sessionHandler.addEventListener(SessionListener())
        }
        addServlet(ServletHolder(servlet), "/")
    }

    override fun doHandle(target: String, jettyReq: Request, req: HttpServletRequest, res: HttpServletResponse) {
        // todo: check for websocket servletRequest
        jettyReq.setAttribute("jetty-target", target)
        jettyReq.setAttribute("jetty-request", jettyReq)
        nextHandle(target, jettyReq, req, res)
    }
}
