package dev.alpas.ozone.console.stubs

internal class FactoryStubs {
    companion object {
        fun stub(): String {
            return """
                package StubPackageName

                import dev.alpas.ozone.EntityFactory
                import dev.alpas.ozone.faker
                import StubEntitiesPackage.StubEntityClazzName
                import StubEntitiesPackage.StubTableClazzName

                internal object StubClazzName : EntityFactory<StubEntityClazzName, StubTableClazzName>() {
                    override val table = StubTableClazzName
                    
                    override fun entity(): StubEntityClazzName {
                        // https://alpas.dev/docs/ozone

                        return StubEntityClazzName {
                            // name = faker.name().name()
                            // updatedAt = Instant.now()
                            // createdAt = faker.date().past(1, TimeUnit.HOURS).toInstant()
                        }
                    }
                }
            """.trimIndent()
        }
    }
}
