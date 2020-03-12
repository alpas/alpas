package dev.alpas.auth

interface AuthChannel {
    /**
     * The user provider to be used for fetching a user.
     */
    val userProvider: UserProvider? get() = null

    /**
     * The actual user that was authenticated.
     */
    var user: Authenticatable?

    /**
     * Attempt to login using the [id] and [password] as credentials and return true if login was successful.
     *
     * @param id The id to use for logging in.
     * @param password The password to use for logging in.
     *
     * @return True if login was successful.
     */
    fun attempt(id: String, password: String): Boolean {
        return attempt(id, password, false)
    }

    /**
     * Attempt to login using the [id] and [password] as credentials and whether to
     * remember the user as well or not. Returns true if login was successful.
     *
     * @param id The id to use for logging in.
     * @param password The password to use for logging in.
     * @param remember Whether to remember the user or not.
     *
     * @return True if login was successful.
     */
    fun attempt(id: String, password: String, remember: Boolean): Boolean {
        return false
    }

    /**
     * Check if the auth channel is authenticated. If not it tries to login the user.
     *
     * @return True if login was successful.
     */
    fun check(): Boolean {
        if (!isLoggedIn()) {
            tryLogin()
        }
        return isLoggedIn()
    }

    /**
     * Check if the channel is authenticated or not.
     */
    fun isLoggedIn(): Boolean {
        return user != null
    }

    /**
     * Try to login and return true if successful.
     */
    fun tryLogin(): Boolean {
        return false
    }

    /**
     * Login a [user] with and whether to remember the user or not.
     */
    fun login(user: Authenticatable, remember: Boolean = false) {
        TODO("not implemented")
    }

    /**
     * Try to login a user using the given [id].
     *
     * Return the authenticatable user if login was successful otherwise return null.
     */
    fun loginUsingId(id: Any): Authenticatable? {
        return null
    }

    /**
     * Log the user out.
     */
    fun logout() {
        TODO("not implemented")
    }
}
