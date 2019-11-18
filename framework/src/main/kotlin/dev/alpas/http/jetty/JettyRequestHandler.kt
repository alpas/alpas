package dev.alpas.http.jetty

import org.eclipse.jetty.server.Request
import org.eclipse.jetty.servlet.ServletContextHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

internal class JettyRequestHandler : ServletContextHandler(null, "/", SESSIONS) {
    override fun doHandle(target: String, jettyReq: Request, req: HttpServletRequest, res: HttpServletResponse) {
        // todo: check for websocket servletRequest
        jettyReq.setAttribute("jetty-target", target)
        jettyReq.setAttribute("jetty-request", jettyReq)
        nextHandle(target, jettyReq, req, res)
    }
}
