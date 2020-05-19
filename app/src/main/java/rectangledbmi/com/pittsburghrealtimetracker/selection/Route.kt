package rectangledbmi.com.pittsburghrealtimetracker.selection

import android.graphics.Color

/**
 * This is the object container that contains all information of the buses
 *
 * Created by epicstar on 9/5/14.
 *
 * @author Jeremy Jao
 * @author Michael Antonacci
 */
class Route
/**
 * The main route constructor
 * @param route the route number
 * @param routeInfo the route info
 * @param routeColor the color of the route as an int
 */
(
        /**
         * This is the route number
         */
        var route: String?,
        /**
         * This is the route's general 3 word summary
         */
        var routeInfo: String?,
        /**
         * This is the color of the route as an int
         */
        var routeColor: Int,
        /**
         * Position of the route in the list
         *
         * @since 43
         */
        val listPosition: Int,
        /**
         * Whether or not the route is selected
         */
        var isSelected: Boolean) {

    /**
     * Gets the int color as a hex string from:
     * http://stackoverflow.com/questions/4506708/android-convert-color-int-to-hexa-string
     *
     * @return color as hex-string
     */
    val colorAsString: String
        get() = String.format("#%06X", 0xFFFFFF and routeColor)

    /**
     * The non-null constructor of the route and color as a string or hex-string
     * @param route the route number
     * @param routeInfo the route info
     * @param routeColor the color of the route as a string or string-hex
     */
    constructor(route: String, routeInfo: String, routeColor: String, listPosition: Int, isSelected: Boolean) : this(route, routeInfo, Color.parseColor(routeColor), listPosition, isSelected)

    /**
     * Copy constructor for Route objects
     * @param route
     */
    constructor(route: Route) : this(route.route, route.routeInfo, route.routeColor, route.listPosition, route.isSelected)

    /**
     * set the route color if a String is fed
     * @param routeColor the route color as a String
     */
    fun setRouteColor(routeColor: String) {
        this.routeColor = Color.parseColor(routeColor)
    }

    /**
     *
     * @return true if state is changed to selected
     * @since 58
     */
    private fun selectRoute(): Boolean {
        if (!isSelected) {
            isSelected = true
            return true
        }
        return false
    }

    /**
     *
     * @return true if state is changed to deselected
     * @since 58
     */
    private fun deselectRoute(): Boolean {
        if (isSelected) {
            isSelected = false
            return true
        }
        return false
    }

    /**
     * Toggles the selection to change the state of the route's selection.
     *
     * @return true if the route becomes selected; false if it becomes unselected
     * @since 58
     */
    fun toggleSelection(): Boolean {
        if (isSelected) {
            deselectRoute()
            return false
        } else {
            selectRoute()
            return true
        }
    }

    /**
     * Auto-Generated by Android Studio
     * @return String of Route
     */
    override fun toString(): String {
        return "$route - $routeInfo\ncolor: $routeColor - $colorAsString"
    }
}
