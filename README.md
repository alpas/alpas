<p align="center">
  <a href="https://alpas.dev" target="_blank">
    <img src="https://alpas.dev/images/Alpas.png">
  </a>
</p>


### Alpas - The Rapid and Delightful Kotlin Web Framework. Easy, elegant, and productive! 🚀

Alpas is a Kotlin-based web framework that gets you creating web apps and APIs simply, and quickly. 

#### Routes. Simple, and defined. 

```kotlin

import example.controllers.WelcomeController
import dev.alpas.routing.RouteGroup
import dev.alpas.routing.Router

fun Router.addRoutes() = apply {
    
    get("/", WelcomeController::welcome)

}

```

#### Controllers. Write less, do more. 

```kotlin

import dev.alpas.http.HttpCall
import dev.alpas.routing.Controller

class WelcomeController : Controller() {

    fun welcome(call: HttpCall) {

        call.render("Welcome!")

    }
}

```

## Batteries-included
Alpas strives to be simple and elegant and wants to serve you whether you have written JVM-based web
apps before or not. There is no huge learning curve to get started and Alpas comes bundled with what you'll need
to build a powerful web application or API. 

- Authentication 
- Notifications
- Security
- Emails 
- Queues
- Front-End (Pebble, Vuejs, TailwindCSS)

We have sweated picking the good parts, so you don’t have to!

Want to use a different library? That's also quick and easy to do with Alpas. 

## Get Started Quickly

The [Alpas documentation][alpas-docs] has everything you need to get started. 

- [Installation](https://alpas.dev/docs/installation) - Simple steps to get your Alpas environment ready. 
- [Quick Start Guide](https://alpas.dev/docs/quick-start-guide-todo-list) - Walks you through creating a To-Do task list. 
If you are a beginner to web development, this is for you! 
- [Starter Template](https://github.com/alpas/starter) - Starting a new project? The starter template will 
quickly get you rolling.

## Examples
Sometimes, it's easiest to see the possibilities of a new framework by checking out what modules are available in 
the ecosystem and by looking at real-life examples. [Alpas Resources][alpas-resources] has a curated list of resources 
available to you so that you can get acquainted with all things Alpas. 

## Contribution
Alpas is an open-source project and we appreciate contributions. 

If you don't want to directly contribute to the core, you can still contribute in other ways; such as: open issues for 
encountered bugs, add feature requests, submit documentation updates, and so on. 

We want you to enjoy writing web apps with Alpas. If you are not, please let us know! We'll keep improving Alpas until it 
is just right for all of us. The best way to let us help you is by joining our 
[official Slack][alpas-slack] and asking questions. [Please do!][alpas-slack]

## Sponsors

- [DigitalOcean](https://www.digitalocean.com/)


[happy-kotlin]: https://medium.com/signal-v-noise/kotlin-makes-me-a-happier-better-programmer-1fc668724563
[alpas-slack]: https://join.slack.com/t/alpasdev/shared_invite/enQtODcwMjE1MzMxODQ3LTJjZWMzOWE5MzBlYzIzMWQ2MTcxN2M2YjU3MTQ5ZDE4NjBmYjY1YTljOGIwYmJmYWFlYjc4YTcwMDFmZDIzNDE
[alpas-docs]: https://alpas.dev/docs
[alpas-resources]: https://github.com/alpas/resources
