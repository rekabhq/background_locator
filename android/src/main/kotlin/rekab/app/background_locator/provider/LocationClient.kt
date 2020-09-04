package rekab.app.background_locator.provider

enum class LocationClient(val value: Int) {
    Google(0), Android(1);

    companion object {
        fun fromInt(value: Int) = values().firstOrNull { it.value == value }
    }
}