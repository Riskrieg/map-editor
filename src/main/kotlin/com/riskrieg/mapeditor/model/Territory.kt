package com.riskrieg.mapeditor.model

import java.awt.Point
import java.util.*

class Territory(val name: String, val seedPoints: Set<Point> = HashSet()) {

    constructor(name: String, seedPoint: Point) : this(name, setOf<Point>(seedPoint))

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val territory = o as Territory
        return name == territory.name && seedPoints == territory.seedPoints
    }

    override fun hashCode(): Int {
        return Objects.hash(name, seedPoints)
    }

    init {
        require(name.isNotBlank()) { "String 'name' cannot be blank" }
        check(seedPoints.isNotEmpty()) { "Set<Point> 'seedPoints' must not be empty" }
    }
}