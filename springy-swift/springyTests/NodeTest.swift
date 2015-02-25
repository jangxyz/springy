//
//  NodeTest.swift
//  springy
//
//  Created by 김장환 on 1/13/15.
//  Copyright (c) 2015 김장환. All rights reserved.
//

import Cocoa
import XCTest

class NodeTest: XCTestCase {
    var n:Node!

    override func setUp() {
        super.setUp()
        // Put setup code here. This method is called before the invocation of each test method in the class.
        n = Node(id:1, data:[
            "a": "123"
        ])
    }
    
    override func tearDown() {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
        super.tearDown()
    }
    
    func testNodeId() {
        XCTAssertEqual("1", n.id)
    }
    
    func testNodeData() {
        let data:String! = n.data["a"]
        XCTAssertEqual("123", data)
    }
}
