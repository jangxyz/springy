//
//  vectorTest.swift
//  springy
//
//  Created by 김장환 on 1/13/15.
//  Copyright (c) 2015 김장환. All rights reserved.
//

import Cocoa
import XCTest

class VectorTest: XCTestCase {

    var v:Vector!
    
    override func setUp() {
        super.setUp()
        // Put setup code here. This method is called before the invocation of each test method in the class.
        v = Vector(x:1.0, y:2.0)
    }
    
    override func tearDown() {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
        super.tearDown()
    }

    func testExample() {
        // This is an example of a functional test case.
        XCTAssert(true, "Pass")
    }

    func testAdd() {
        let v2a = v.add(Vector(x:0.5, y:0.1))
        XCTAssertEqual(1.5, v2a.x)
        XCTAssertEqual(2.1, v2a.y)
        
        let v2b = v + Vector(x:0.5, y:0.1)
        XCTAssertEqual(1.5, v2b.x)
        XCTAssertEqual(2.1, v2b.y)
    }
    
    func testSubtract() {
        let v1 = v.subtract(Vector(x:0.5, y:0.1))
        XCTAssertEqual(0.5, v1.x)
        XCTAssertEqual(1.9, v1.y)
        
        let v2 = v - Vector(x:0.5, y:0.1)
        XCTAssertEqual(0.5, v2.x)
        XCTAssertEqual(1.9, v2.y)
    }
    
    func testEqual() {
        let v1 = Vector(x:1.0, y:2.0)
        let v2 = Vector(x:1.0, y:2.0)
        XCTAssertEqual(v1, v2)
    }
    
    func XXtestPerformanceExample() {
        // This is an example of a performance test case.
        self.measureBlock() {
            // Put the code you want to measure the time of here.
        }
    }

}
