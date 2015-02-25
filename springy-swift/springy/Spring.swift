//
//  Spring.swift
//  springy
//
//  Created by 김장환 on 1/13/15.
//  Copyright (c) 2015 김장환. All rights reserved.
//

import Foundation

class Spring: Equatable, Printable {
    var point1: Point
    var point2: Point
    let length: Double
    let k: Double
    
    init(point1: Point, point2: Point, length: Double, k: Double) {
        self.point1 = point1;
        self.point2 = point2;
        self.length = length; // spring length at rest
        self.k = k; // spring constant (See Hooke's law) .. how stiff the spring is
    }
    
    var description: String {
        return "Spring: <point1:\(point1.description), point2:\(point2.description), length:\(length), k:\(k)>"
    }
}

func == (l:Spring, r:Spring) -> Bool {
    return l.point1 == r.point1 && l.point2 == r.point2 && l.length == r.length && l.k == r.k
}
func != (l:Spring, r:Spring) -> Bool {
    return !(l == r)
}