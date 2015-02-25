//
//  ForceDirectedLayout.swift
//  springy
//
//  Created by 김장환 on 1/15/15.
//  Copyright (c) 2015 김장환. All rights reserved.
//

import Cocoa
import XCTest

class ForceDirectedLayoutTest: XCTestCase {

    var graph:Graph!

    override func setUp() {
        super.setUp()
        // Put setup code here. This method is called before the invocation of each test method in the class.
        graph = Graph()
    }
    
    override func tearDown() {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
        super.tearDown()
    }

    func testLayoutAcceptsArguments() {
        // layout
        let layout = ForceDirectedLayout(graph:graph, stiffness:400.0, repulsion:400, damping:0.5, energyThreshold:0.001, unitTime:0.03)
        XCTAssertNotNil(layout)
    }
    func testLayoutAcceptsArguments_with_defaults() {
        // layout
        let layout = ForceDirectedLayout(graph:graph)
        XCTAssertNotNil(layout)
    }
    
//    func testStart_

    func testPoint_creates_random_point_if_not_exist() {
        let layout = ForceDirectedLayout(graph:graph)

        let n = graph.newNode(["label": "1"])
        let point:Point = layout.point(n)
        
        XCTAssertEqual(1, layout.nodePoints.count)
    }
    func testPoint_reuses_point_for_same_node() {
        let node = graph.newNode(["label": "1"])
        let layout = ForceDirectedLayout(graph:graph)
        
        let p1:Point = layout.point(node)
        let p2:Point = layout.point(node)

        XCTAssertEqual(1, layout.nodePoints.count)
        XCTAssertEqual(p1, p2)
    }
    

    func testSpring_returns_a_spring_for_an_edge() {
        let n1 = graph.newNode(["label": "1"])
        let n2 = graph.newNode(["label": "2"])
        let e1 = graph.newEdge(source:n1, target:n2)
        let layout = ForceDirectedLayout(graph:graph)

        //
        let spring:Spring = layout.spring(e1)
        XCTAssertNotNil(spring)
    }
    
    func testSpring_creates_new_edge_if_not_existXX() {
        let n1 = graph.newNode(["label": "1"])
        let n2 = graph.newNode(["label": "2"])
        let e1 = graph.newEdge(source:n1, target:n2)
        let layout = ForceDirectedLayout(graph:graph)

        //
        let spring:Spring = layout.spring(e1)
        XCTAssertEqual(1, layout.edgeSprings.count)
        XCTAssertEqual(spring, layout.edgeSprings.values.first!)
    }
    func testSpring_reuses_spring_for_same_edge() {
        let n1 = graph.newNode(["label": "1"])
        let n2 = graph.newNode(["label": "2"])
        let e1 = graph.newEdge(source:n1, target:n2)
        let layout = ForceDirectedLayout(graph:graph)
        
        //
        let spring1 = layout.spring(e1)
        let spring2 = layout.spring(e1)
        XCTAssertEqual(1, layout.edgeSprings.count)
        XCTAssertEqual(spring1, spring2)
    }
    func testSpring_use_points_from_other_silbing_edge() {
        let n1 = graph.newNode(["label": "1"])
        let n2 = graph.newNode(["label": "2"])
        let e1 = graph.newEdge(source:n1, target:n2, data:["length":"10.0"])
        let e2 = graph.newEdge(source:n1, target:n2)
        let layout = ForceDirectedLayout(graph:graph)
        let spring1 = layout.spring(e1) // creates edge
        
        //
        let spring2 = layout.spring(e2)
        
        // has same points...
        XCTAssertEqual(spring1.point1, spring2.point1)
        XCTAssertEqual(spring1.point2, spring2.point2)
        // but not other data
        XCTAssertNotEqual(spring1.length, spring2.length)
        XCTAssertNotEqual(spring1.k, spring2.k)
        // does not contribute to another edgeSprings count
        XCTAssertEqual(1, layout.edgeSprings.count)
    }

    func testEachNode_computes_enumerable_node_and_point_collection() {
        let n1 = graph.newNode(["label": "1"])
        let n2 = graph.newNode(["label": "2"])
        let layout = ForceDirectedLayout(graph:graph)
        
        //
        var nodeCount = 0
        for (node:Node,point:Point) in layout.eachNode {
            nodeCount += 1
        }
        XCTAssertEqual(2, nodeCount)
        XCTAssertEqual(2, layout.nodePoints.count)
    }
    
    func testEachSpring_computes_enumerable_spring_collection() {
        let n1 = graph.newNode(["label": "1"])
        let n2 = graph.newNode(["label": "2"])
        let n3 = graph.newNode(["label": "3"])
        let e1 = graph.newEdge(source:n1, target:n2)
        let e2 = graph.newEdge(source:n1, target:n3)
        let e3 = graph.newEdge(source:n2, target:n3)
        let layout = ForceDirectedLayout(graph:graph)

        var springCount = 0
        //
        for spring in layout.eachSpring {
            springCount += 1
        }
        XCTAssertEqual(3, springCount)
        XCTAssertEqual(3, layout.edgeSprings.count)
    }
    
    // applyHookesLaw
    func testApplyHookesLaw_class_method_updates_springs_points_acceleration() {
        let p1 = Point(p:Vector(x:0, y:0), m:1.0)
        let p2 = Point(p:Vector(x:1, y:0), m:1.0)
        let p3 = Point(p:Vector(x:0, y:1), m:1.0)

        var springs = [
            Spring(point1:p1, point2:p2, length:0.9, k:0.5),
            Spring(point1:p2, point2:p3, length:0.9, k:0.5),
            Spring(point1:p3, point2:p1, length:0.9, k:0.5)
        ]
        
        //
        ForceDirectedLayout.applyHookesLaw(&springs)

        XCTAssertNotEqual(Vector.ZERO, springs[0].point1.a)
        XCTAssertNotEqual(Vector.ZERO, springs[0].point2.a)
        XCTAssertNotEqual(Vector.ZERO, springs[1].point1.a)
        XCTAssertNotEqual(Vector.ZERO, springs[1].point2.a)
        XCTAssertNotEqual(Vector.ZERO, springs[2].point1.a)
        XCTAssertNotEqual(Vector.ZERO, springs[2].point2.a)
    }
    func testApplyHookesLaw_updates_springs_points_acceleration() {
        let n1 = graph.newNode(["label": "1"])
        let n2 = graph.newNode(["label": "2"])
        let n3 = graph.newNode(["label": "3"])
        let e1 = graph.newEdge(source:n1, target:n2)
        let e2 = graph.newEdge(source:n1, target:n3)
        let e3 = graph.newEdge(source:n2, target:n3)
        let layout = ForceDirectedLayout(graph:graph)
        
        //
        layout.applyHookesLaw()
        
        XCTAssertNotEqual(Vector.ZERO, layout.point(n1).a)
        XCTAssertNotEqual(Vector.ZERO, layout.point(n2).a)
        XCTAssertNotEqual(Vector.ZERO, layout.point(n3).a)
    }

    // applyCoulombsLaw
    func testApplyCoulombsLaw_class_method_updates_points_acceleration() {
        var points = [
            Point(p:Vector(x:0, y:0), m:1.0),
            Point(p:Vector(x:1, y:0), m:1.0),
            Point(p:Vector(x:0, y:1), m:1.0)
        ]
        
        //
        ForceDirectedLayout.applyCoulombsLaw2(&points, repulsion:400)
        
        XCTAssertNotEqual(Vector.ZERO, points[0].a)
        XCTAssertNotEqual(Vector.ZERO, points[1].a)
        XCTAssertNotEqual(Vector.ZERO, points[2].a)
    }
    func testApplyCoulombsLaw_performance_magnitude1() {
        var points = [Point]()
        for var i = 0; i < 10; i++ {
            let point = Point(p:Vector(x:Double(i)/1000.0, y:Double(i)/1000.0), m:1.0)
            points.append(point)
        }
        
        self.measureBlock() {
            for var i = 0; i < 100; i++ {
                ForceDirectedLayout.applyCoulombsLaw1(&points, repulsion:400)
            }
        }
    }
    func testApplyCoulombsLaw_performance_magnitude2() {
        var points = [Point]()
        for var i = 0; i < 10; i++ {
            let point = Point(p:Vector(x:Double(i)/1000.0, y:Double(i)/1000.0), m:1.0)
            points.append(point)
        }
        
        self.measureBlock() {
            for var i = 0; i < 100; i++ {
                ForceDirectedLayout.applyCoulombsLaw2(&points, repulsion:400)
            }
        }
    }

    func testApplyCoulombsLaw_updates_points_acceleration() {
        let n1 = graph.newNode(["label": "1"])
        let n2 = graph.newNode(["label": "2"])
        let n3 = graph.newNode(["label": "3"])
        let layout = ForceDirectedLayout(graph:graph)
        
        //
        layout.applyCoulombsLaw()
        
        XCTAssertNotEqual(Vector.ZERO, layout.point(n1).a)
        XCTAssertNotEqual(Vector.ZERO, layout.point(n2).a)
        XCTAssertNotEqual(Vector.ZERO, layout.point(n3).a)
    }
    
    // updateVelocity
    func testUpdateVelocity_class_method_updates_points_velocity_from_acceleration() {
        
        var points = [
            Point(p:Vector(x:0, y:0), m:1.0),
            Point(p:Vector(x:0, y:1), m:1.0)
        ]
        points[0].a = Vector(x:0.3, y:0)
        points[1].a = Vector(x:0.1, y:0.2)
        
        //
        ForceDirectedLayout.updateVelocity(&points, damping:0.5, timestep:0.1)
        
        //
        XCTAssertNotEqual(Vector.ZERO, points[0].v)
        XCTAssertNotEqual(Vector.ZERO, points[1].v)
        // and resets acceleration to zero
        XCTAssertEqual(Vector.ZERO, points[0].a)
        XCTAssertEqual(Vector.ZERO, points[1].a)
    }
    func testUpdateVelocity_updates_points_velocity_from_acceleration() {
        let n1 = graph.newNode(["label": "1"])
        let n2 = graph.newNode(["label": "2"])
        let e1 = graph.newEdge(source:n1, target:n2)
        let layout = ForceDirectedLayout(graph:graph)
        layout.applyHookesLaw() // update acceleration.
        XCTAssertEqual(Vector.ZERO, layout.point(n1).v)
        XCTAssertEqual(Vector.ZERO, layout.point(n2).v)
        
        // action
        layout.updateVelocity(timestep:0.1)

        //
        XCTAssertNotEqual(Vector.ZERO, layout.point(n1).v)
        XCTAssertNotEqual(Vector.ZERO, layout.point(n2).v)
        // and resets acceleration to zero
        XCTAssertEqual(Vector.ZERO, layout.point(n1).a)
        XCTAssertEqual(Vector.ZERO, layout.point(n2).a)
    }
    
    // updatePosition
    func testUpdatePosition_class_method_updates_position_from_velocity() {
        var points = [
            Point(p:Vector(x:0, y:0), m:1.0),
            Point(p:Vector(x:0, y:1), m:1.0)
        ]
        points[0].v = Vector(x:0.3, y:0)
        points[1].v = Vector(x:-0.1, y:0.2)
        
        //
        ForceDirectedLayout.updatePosition(&points, timestep:1)
        
        //
        XCTAssertEqual(Vector(x: 0.3,y:0.0), points[0].p)
        XCTAssertEqual(Vector(x:-0.1,y:1.2), points[1].p)
    }
    func testUpdatePosition_updates_position_from_velocity() {
        let n1 = graph.newNode(["label": "1"])
        let n2 = graph.newNode(["label": "2"])
        let e1 = graph.newEdge(source:n1, target:n2)
        let layout = ForceDirectedLayout(graph:graph)

        let pos1 = layout.point(n1).p
        let pos2 = layout.point(n2).p
        
        layout.applyHookesLaw()             // update acceleration
        layout.updateVelocity(timestep:1)   // update velocity

        //
        layout.updatePosition(timestep:1)
        
        //
        XCTAssertNotEqual(pos1, layout.point(n1).p)
        XCTAssertNotEqual(pos2, layout.point(n2).p)
    }
    
    // totalEnergy
    func testTotalEnery_class_method_computes_kinetic_energy() {
        var points = [
            Point(p:Vector(x:0, y:0), m:1.0),
            Point(p:Vector(x:0, y:1), m:1.0)
        ]
        points[0].v = Vector(x:0.3, y:0.4)  // 0.25
        points[1].v = Vector(x:-0.1, y:0.2) // sqrt(0.05)
        
        //
        let energy: Double = ForceDirectedLayout.kineticEnergy(&points)
        
        //
        XCTAssertEqual(0.15, energy)
    }
    func testTotalEnergy_computes_kinetic_energy() {
        let n1 = graph.newNode(["label": "1"])
        let n2 = graph.newNode(["label": "2"])
        let e1 = graph.newEdge(source:n1, target:n2)
        let layout = ForceDirectedLayout(graph:graph)
        XCTAssertEqual(0.0, layout.totalEnergy())
        //
        layout.applyHookesLaw()             // update acceleration
        layout.updateVelocity(timestep:1)   // update velocity
        
        //
        let energy:Double = layout.totalEnergy()
        XCTAssertNotEqual(0.0, energy)
    }
    
    //
    // simulation
    //
    
    // start
    func testStart_calls_onRenderStart_before_loop() {
        let layout = ForceDirectedLayout(graph:graph)
        var onRenderStart_called = false
        func onRenderStart() {
            onRenderStart_called = true
        }
        
        //
        layout.start({}, onRenderStop: {}, onRenderStart:onRenderStart)
        XCTAssert(onRenderStart_called)
    }
    func testStart_calls_render_on_every_loop() {
        let n1 = graph.newNode(["label": "1"])
        let n2 = graph.newNode(["label": "2"])
        let e1 = graph.newEdge(source:n1, target:n2)
        let layout = ForceDirectedLayout(graph:graph)
        
        var countRender = 0
        func render() {
            countRender += 1
            //println("- energy: \(layout.totalEnergy())")
            if countRender >= 10 {
                layout.stop()
            }
        }
        
        //
        layout.start(render)
        XCTAssertEqual(10, countRender)
    }
    func testStart_calls_onRenderStop_on_loop_end() {
        let layout = ForceDirectedLayout(graph:graph)
        var onRenderStop_called = false
        func onRenderStop() {
            onRenderStop_called = true
        }
        
        //
        layout.start({}, onRenderStop: onRenderStop)
        XCTAssert(onRenderStop_called)
    }
    func XXtestStart_doesnt_run_again_while_running() {
        let n1 = graph.newNode(["label": "1"])
        let n2 = graph.newNode(["label": "2"])
        let e1 = graph.newEdge(source:n1, target:n2)
        let layout = ForceDirectedLayout(graph:graph)
        
        var countRenderStart = 0
        func onRenderStart() {
            countRenderStart += 1
        }
        
        //
        layout.start({}, onRenderStop:{}, onRenderStart:onRenderStart)
        layout.start({}, onRenderStop:{}, onRenderStart:onRenderStart)
        XCTAssertEqual(1, countRenderStart)
    }
    func testStart_loops_until_total_energy_is_lower_than_threshold() {
        let n1 = graph.newNode(["label": "1"])
        let n2 = graph.newNode(["label": "2"])
        let e1 = graph.newEdge(source:n1, target:n2)
        let layout = ForceDirectedLayout(graph:graph)

        //
        layout.start()
        XCTAssert(layout.totalEnergy() < layout.minEnergyThreshold)
    }
    func testStart_runs_tick_loop() {
    }

    // stop
    func testStop_stops_running_loop() {
        let n1 = graph.newNode(["label": "1"])
        let n2 = graph.newNode(["label": "2"])
        let e1 = graph.newEdge(source:n1, target:n2)
        let layout = ForceDirectedLayout(graph:graph)
        
        var countRender = 0
        func render() {
            countRender += 1
            //println("- energy: \(layout.totalEnergy())")
            if countRender >= 2 {
                layout.stop()
            }
        }
        
        //
        layout.start(render)
        XCTAssertEqual(2, countRender)
    }
    
    // tick
    func testTick_applies_force_and_recompute_velocity_and_position() {
    }
    
    // runner
    func XXtestRun() {
        let nodesNum = 10
        let edgesNum = nodesNum * 2
        
        func createTestGraph(nodesNum:Int, edgesNum:Int) -> Graph {
            var graph = Graph()
            
            // add 100 nodes
            var edgeCandidate:[String] = []
            for var i = 0; i < nodesNum; i++ {
                let node = graph.newNode([
                    "label": String(i),
                ])
                edgeCandidate.append(node.id);
            }
            
            func nodeName(node:Node) -> String {
                return node.data["label"] ?? node.id
            }

            // add 1000 edges
            for var i = 0; i < edgesNum; i++ {
                // select source
                
                //var randid:UInt32 = arc4random_uniform(edgeCandidate.count)
                let rand1:Int = random() % edgeCandidate.count
                var node1:Node = graph.nodeSet[edgeCandidate[rand1]]!
                
                // select target
                let rand2:Int = random() % edgeCandidate.count
                var node2:Node = graph.nodeSet[edgeCandidate[rand2]]!
                
                var edge = graph.newEdge(source:node1, target:node2, data:[
                    "label": "\(nodeName(node1)) --> \(nodeName(node2))"
                ])
                
                edgeCandidate.append(node1.id)
                edgeCandidate.append(node2.id)
            }
            
            return graph
        }
        
        var graph = createTestGraph(nodesNum, edgesNum)

        // layout
        var layout = ForceDirectedLayout(graph:graph)
        var count = 0
        func render() {
            count += 1
            println("- energy: \(layout.totalEnergy())")
        }

        layout.start(render)
        println("done.")

    }
}
