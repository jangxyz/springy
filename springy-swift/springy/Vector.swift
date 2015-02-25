
//
//  vector.swift
//  springy-swift
//
//  Created by 김장환 on 1/11/15.
//  Copyright (c) 2015 김장환. All rights reserved.
//

import Foundation

let ARC4_RANDOM_MAX = Double(0x100000000)
func rand() -> Double
{
    var randDouble = Double(arc4random())
    return randDouble / ARC4_RANDOM_MAX
}

struct Vector: Equatable, Printable {
    let x: Double
    let y: Double
    let magnitude2: Double
    let magnitude:  Double
    
    static func random() -> Vector {
        return Vector(x:rand(), y:rand())
    }
    
    static var ZERO: Vector {
        return Vector(x:0, y:0)
    }
    
    init(x: Double, y: Double) {
        self.x = x
        self.y = y
        
        self.magnitude2 = self.x * self.x + self.y * self.y
        self.magnitude = sqrt(self.magnitude2)
    }

    var description: String {
        return "Vector:(\(x),\(y))"
    }
    
    func add(v2: Vector) -> Vector {
        return Vector(x:self.x + v2.x, y:self.y + v2.y)
    }
    
    func subtract(v2: Vector) -> Vector {
        return Vector(x:self.x - v2.x, y:self.y - v2.y)
    }
    
    func multiply(n: Double) -> Vector {
        return Vector(x:self.x * n, y:self.y * n)
    }
    
    func divide(n: Double) -> Vector {
        return Vector(x:(self.x / n), y:(self.y / n)) // Avoid divide by zero errors..
    }
    
    func normal() -> Vector {
        return Vector(x: -self.y, y: self.x)
    }

    func normalise() -> Vector {
        return self / self.magnitude
    }
    func normalize() -> Vector {
        return self.normalise()
    }
    
}

func == (left: Vector, right: Vector) -> Bool {
    return (left.x == right.x) && (left.y == right.y)
}
func != (left: Vector, right: Vector) -> Bool {
    return !(left == right)
}

func + (left: Vector, right: Vector) -> Vector {
    return left.add(right)
}
func - (left: Vector, right: Vector) -> Vector {
    return left.subtract(right)
}
func * (v: Vector, n: Double) -> Vector {
    return v.multiply(n)
}
func * (n:Double, v: Vector) -> Vector {
    return v.multiply(n)
}
func / (v: Vector, n: Double) -> Vector {
    return v.divide(n)
}

func += (inout left: Vector, right: Vector) {
    left = left + right
}
