//
//  point.swift
//  springy-swift
//
//  Created by 김장환 on 1/11/15.
//  Copyright (c) 2015 김장환. All rights reserved.
//

import Foundation

class Point: Equatable, Printable {
    var p: Vector
    var v: Vector
    var a: Vector
    let m: Double
    
    init(p: Vector, m: Double) {
        self.p = p
        self.m = m
        self.v = Vector(x:0, y:0)
        self.a = Vector(x:0, y:0)
    }
 
    func applyForce(force: Vector) {
        self.a += (force / self.m)
    }
    
    var description: String {
        return "Point: <p:\(p.description), v:\(v.description), a:\(a.description), m:\(m)>"
    }

}


func == (left: Point, right: Point) -> Bool {
    return left.m == right.m && left.a == right.a && left.v == right.v && left.p == right.p
}
func != (left: Point, right: Point) -> Bool {
    return !(left == right)
}
