//
//  RendererTest.swift
//  springy
//
//  Created by 김장환 on 1/16/15.
//  Copyright (c) 2015 김장환. All rights reserved.
//

import Cocoa
import XCTest

class RendererTest: XCTestCase {

    var graph:Graph!
    var layout:ForceDirectedLayout!
    
    override func setUp() {
        super.setUp()
        // Put setup code here. This method is called before the invocation of each test method in the class.
        graph = Graph()
        layout = ForceDirectedLayout(graph:graph, stiffness:400.0, repulsion:400, damping:0.5, energyThreshold:0.001, unitTime:0.03)
    }
    
    override func tearDown() {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
        super.tearDown()
    }

    func XXtestPerformanceExample() {
        // This is an example of a performance test case.
        self.measureBlock() {
            // Put the code you want to measure the time of here.
        }
    }
    
    func testRendererAcceptsArguments() {
        let renderer = Renderer(layout:layout,
            clear: { () in return },
            drawEdge: {(edge:Edge, p1:Point, p2:Point) in return},
            drawNode: {(node:Node, p:Point) in return},
            onRenderStart: { () in return },
            onRenderStop: { () in return }
        )
        XCTAssertNotNil(renderer)
    }
    func testRendererAcceptsDefaultArguments() {
        let renderer = Renderer(layout:layout)
        XCTAssertNotNil(renderer)
    }

    func testConformsToGraphListener() {
        let renderer:GraphListener = Renderer(layout:layout)
    }
    
//    func testStart_calls_layout_start() {
//        let renderer = Renderer(layout:layout)
//        
//    }
}
