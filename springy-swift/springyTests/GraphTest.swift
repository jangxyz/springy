//
//  GraphTest.swift
//  springy
//
//  Created by 김장환 on 1/13/15.
//  Copyright (c) 2015 김장환. All rights reserved.
//

import Cocoa
import XCTest

class GraphTest: XCTestCase {
    
    var g: Graph!

    override func setUp() {
        super.setUp()
        // Put setup code here. This method is called before the invocation of each test method in the class.
        g = Graph()
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

    // addNode
    func testAddNode_adds_node() {
        g.addNode(Node(id:1))
        
        XCTAssertEqual(1, g.nodes.count)
    }
    func testAddNode_does_not_add_same_nodes() {
        let node = Node(id:1)
        g.addNode(node)
        g.addNode(node)

        XCTAssertEqual(1, g.nodes.count)
    }

    // addNodes
    func testAddNodes_add_multiple_nodes() {
        g.addNodes(["node1", "node2"])
        
        XCTAssertEqual(2, g.nodes.count)
        XCTAssertEqual("node1", g.nodes[0].id)
        let label:String! = g.nodes[0].data["label"]
        
        XCTAssertEqual("node1", label)
    }
    
    // addEdge
    func testAddEdge() {
        let e = Edge(id:1, source:Node(id:1), target:Node(id:2))
        g.addEdge(e)
            
        XCTAssertEqual(1, g.edges.count)
    }
    func testAddEdge_does_not_add_same_Edges() {
        let e = Edge(id:1, source:Node(id:1), target:Node(id:2))
        g.addEdge(e)
        g.addEdge(e)
        
        XCTAssertEqual(1, g.edges.count)
    }
    
    // addEdges
    func testAddEdges_add_multiple_edges() {
        g.addNodes(["chicken", "bird", "wing"])
        g.addEdges([
            ("chicken", "bird", ["label":"is-a"]),
            ("chicken", "wing", ["label":"has-a"])
        ])
        
        XCTAssertEqual(2, g.edges.count)
        XCTAssertEqual("chicken", g.edges[0].source.id)

        let label:String! = g.edges[0].data["label"]
        XCTAssertEqual("is-a", label)
    }
    func testAddEdges_without_label() {
        g.addNodes(["node1", "node2"])
        g.addEdges([
            ("node1", "node2"),
        ])
        
        XCTAssertEqual("node1", g.edges[0].source.id)
        XCTAssert(g.edges[0].data.isEmpty)
    }
    func XXtestAddEdges_raise_error_if_no_node() {
        g.addEdges([
            ("node1", "node2", ["label":"is-a"]),
            ("node1", "node3", ["label":"has-a"])
        ])
        XCTFail("addEdges should raise error")
    }

    // loadJSON
    func testLoadJSON_by_string() {
        g.loadJSON("{" +
            "\"nodes\": [" +
            "    \"center\"," +
            "    \"left\"," +
            "    \"right\"," +
            "    \"up\"," +
            "    \"satellite\"" +
            "]," +
            "\"edges\": [" +
            "    [\"center\", \"left\"]," +
            "    [\"center\", \"right\"]," +
            "    [\"center\", \"up\"]" +
            "]" +
        "}")

        XCTAssertEqual(5, g.nodes.count)
        XCTAssertEqual(3, g.edges.count)
        
        if g.nodes.count > 0 {
            XCTAssertEqual("center", g.nodes[0].id)
        } else {
            XCTFail("cannot find first node")
        }
        
        if g.edges.count > 0 {
            XCTAssertEqual("center", g.edges[0].source.id)
            XCTAssertEqual("left",   g.edges[0].target.id)
        } else {
            XCTFail("cannot find first edge")
        }
    }
    func testLoadJSON_by_dictionary() {
        g.loadJSON([
            "nodes": [
                "center",
                "left",
                "right",
                "up",
                "satellite"
            ],
            "edges": [
                ["center", "left"],
                ["center", "right"],
                ["center", "up"]
            ]
        ])
        
        XCTAssertEqual(5, g.nodes.count)
        XCTAssertEqual(3, g.edges.count)
        
        if g.nodes.count > 0 {
            XCTAssertEqual("center", g.nodes[0].id)
        } else {
            XCTFail("cannot find first node")
        }
        
        if g.edges.count > 0 {
            XCTAssertEqual("center", g.edges[0].source.id)
            XCTAssertEqual("left",   g.edges[0].target.id)
        } else {
            XCTFail("cannot find first edge")
        }
    }
    
    // getEdges
    func testGetEdges_between_nodes() {
        let n1 = g.addNode(Node("node1"))
        let n2 = g.addNode(Node("node2"))
        let n3 = g.addNode(Node("node3"))
        let e1 = g.addEdge(Edge("e1", source:n1, target:n2))
        let e2 = g.addEdge(Edge("e2", source:n1, target:n3))
        
        //
        let edges1 = g.getEdges(source:n1, target:n2)
        XCTAssertEqual(e1.id, edges1[0].id)

        let edges2 = g.getEdges(source:n2, target:n3)
        XCTAssertEqual(0, edges2.count)
    }
    
    // removeNode
    func testRemoveNode_deletes_node() {
        let n1 = g.addNode(Node("node1"))
        let n2 = g.addNode(Node("node2"))
        XCTAssertEqual(0, find(g.nodes, n1)!)

        //
        g.removeNode(n1)
        XCTAssertNil(find(g.nodes, n1))
    }
    func testRemoveNode_detaches_adjacent_edges() {
        let n1 = g.addNode(Node("node1"))
        let n2 = g.addNode(Node("node2"))
        let e1 = g.addEdge(Edge("e1", source:n1, target:n2))
        
        //
        g.removeNode(n1)
        let edges = g.getEdges(source:n1, target:n2)
        XCTAssertEqual(0, edges.count)
    }
    
    // removeEdge
    func testRemoveEdge_deletes_edge() {
        let n1 = g.addNode(Node("node1"))
        let n2 = g.addNode(Node("node2"))
        let e1 = g.addEdge(Edge("e1", source:n1, target:n2))

        //
        g.removeEdge(e1)
        XCTAssertEqual(0, g.edges.count)
    }
    func testRemoveEdge_detaches_associated_edges() {
        let n1 = g.addNode(Node("node1"))
        let n2 = g.addNode(Node("node2"))
        let e1 = g.addEdge(Edge("e1", source:n1, target:n2))
        
        //
        g.removeEdge(e1)
        
        let edges = g.getEdges(source:n1, target:n2)
        XCTAssertEqual(0, edges.count)
    }
    
    // merge
//    func XXtestMerge_adds_nodes() {
//        g.merge(["nodes": [
//            [
//                "id": "123",
//                "data": ["type": "user", "userid": "123", "displayname": "aaa"]
//            ],
//            [
//                "id": "234",
//                "data": ["type": "user", "userid": "234", "displayname": "bbb"]
//            ]
//        ]
//        ])
//
//        //
//        XCTAssertEqual(2, g.nodes.count)
//        XCTAssertEqual("123", g.nodes[0].id)
//        XCTAssertEqual("bbb", g.nodes[1].data["displayname"]!)
//    }
    
    // filterNodes
    
    // filterEdges
    
    // addGraphListener
    func testAddGraphListener_adds_listener() {
        class SilentListener:GraphListener {
            var callbackCalled = false
            
            func graphChanged() {
                println("graphChanged called.")
                callbackCalled = true
            }
        }

        var listener = SilentListener()
        g.addGraphListener(listener)
        
        //
        XCTAssertEqual(1, g.eventListeners.count)
    }
    
    // notify
    func testNotify_calls_graphChanged_to_event_listeners() {
        class SilentListener:GraphListener {
            var callbackCalled = false

            func graphChanged() {
                println("graphChanged called.")
                callbackCalled = true
            }
        }
        
        var obj = SilentListener()
        g.addGraphListener(obj)
        
        //
        g.notify()
        XCTAssert(obj.callbackCalled)
    }
}
