//
//  EdgeTest.swift
//  springy
//
//  Created by 김장환 on 1/13/15.
//  Copyright (c) 2015 김장환. All rights reserved.
//

import Cocoa
import XCTest

class EdgeTest: XCTestCase {
    var e:Edge!

    override func setUp() {
        super.setUp()
        // Put setup code here. This method is called before the invocation of each test method in the class.
        let n1 = Node(id:1)
        let n2 = Node(id:2)

        e = Edge(1, source:n1, target:n2, data:[
            "a": "123"
        ])
    }
    
    override func tearDown() {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
        super.tearDown()
    }

}
