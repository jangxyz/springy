//
//  PointTest.swift
//  springy
//
//  Created by 김장환 on 1/13/15.
//  Copyright (c) 2015 김장환. All rights reserved.
//

import Cocoa
import XCTest

class PointTest: XCTestCase {
    var p:Point!
    
    override func setUp() {
        super.setUp()
        // Put setup code here. This method is called before the invocation of each test method in the class.
        let v = Vector(x:1.0, y:2.0)
        p = Point(p:v, m:1.0)
    }
    
    override func tearDown() {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
        super.tearDown()
    }

    func testAccelVector_defaults_to_zero() {
        XCTAssertEqual(Vector(x:0.0, y:0.0), p.a)
    }

    func testApplyForce_updates_accel() {
        p.applyForce(Vector(x:1.0, y:1.0))
        XCTAssertEqual(Vector(x:1.0, y:1.0), p.a)
    }
    
    func testApplyForce_updates_accel_divided_by_mass() {
        let p2 = Point(p:Vector(x:0,y:0), m:1.5)
        p2.applyForce(Vector(x:1.0, y:1.0))

        XCTAssertEqual(Vector(x:1.0/1.5, y:1.0/1.5), p2.a)
    }
    
    
    func XXtestPerformanceExample() {
        // This is an example of a performance test case.
        self.measureBlock() {
            // Put the code you want to measure the time of here.
        }
    }

}
