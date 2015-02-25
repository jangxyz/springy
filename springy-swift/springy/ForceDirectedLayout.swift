//
//  ForceDirectedLayout.swift
//  springy
//
//  Created by 김장환 on 1/15/15.
//  Copyright (c) 2015 김장환. All rights reserved.
//

import Foundation

protocol Layout {
    func tick(#timestep:Double)
    func stop()
    func start(render:() -> (), onRenderStop:() -> (), onRenderStart:() -> ())
}

let MIN_DISTANCE_EPSILON  = 0.1
let MIN_DISTANCE_EPSILON2 = MIN_DISTANCE_EPSILON * MIN_DISTANCE_EPSILON

class ForceDirectedLayout: Layout {
    var graph:Graph
    let stiffness:Double // spring stiffness constant
    let repulsion:Double // repulsion constant
    let damping:Double // velocity damping factor
    let minEnergyThreshold:Double //threshold used to determine render stop
    let unitTime:Double
    
    var nodePoints = [String:Point]()  // keep track of points associated with nodes
    var edgeSprings = [String:Spring]() // keep track of springs associated with edges

    var _started = false    // prevents running again
    var _stop = true        // signals stop
    
    // Hooke's Law
    class func applyHookesLaw(inout springs:[Spring]) {
        var d:Vector
        var direction:Vector
        var displacement:Double
        var force:Vector

        for spring in springs {
            if spring.point1 == spring.point2 {
                continue
            }
            
            d = spring.point2.p - spring.point1.p   // direction of spring
            direction = d.normalise()
            displacement = spring.length - d.magnitude
            
            // apply force to each end point
            force = direction * (spring.k * displacement * 0.5)
            spring.point1.applyForce(force * -1)
            spring.point2.applyForce(force)
        }
    }
    // Coulomb's Law
    class func applyCoulombsLaw1(inout points:[Point], repulsion:Double) {
        var d:Vector
        var direction:Vector
        var distance:Double
        var force:Vector
        
        for (i1,point1) in enumerate(points) {
            for (i2,point2) in enumerate(points) {
                if i1 >= i2 {
                    continue
                }
                
                d = point1.p - point2.p
                direction = d.normalise()
                distance = d.magnitude + 0.1 // avoid massive forces at small distances (and divide by zero)
                
                // apply force to each end point
                force = (direction * repulsion) / (distance * distance * 0.5)
                point1.applyForce(force)
                point2.applyForce(force * -1)
            }
        }
    }
    class func applyCoulombsLaw2(inout points:[Point], repulsion:Double) {
        var d:Vector
        var direction:Vector
        var distance2:Double
        var force:Vector

        for (i1,point1) in enumerate(points) {
            for (i2,point2) in enumerate(points) {
                if i1 >= i2 {
                    continue
                }
                
                d = point1.p - point2.p
                distance2 = d.magnitude2 + 0.01
                direction = d.normalise()
                
                // apply force to each end point
                force = (direction * repulsion) / (distance2/* *  0.5*/)
                point1.applyForce(force)
                point2.applyForce(force * -1)
            }
        }
    }
    class func updateVelocity(inout points:[Point], damping:Double, timestep:Double) {
        for point in points {
            // Is this, along with updatePosition below, the only places that your
            // integration code exist?
            point.v = (point.v + (point.a * timestep)) * damping
            point.a = Vector.ZERO
        }
    }
    class func updatePosition(inout points:[Point], timestep:Double) {
        for point in points {
            // Same question as above; along with updateVelocity, is this all of
            // your integration code?
            point.p = point.p + (point.v * timestep);
        }
    }
    class func kineticEnergy(inout points:[Point]) -> Double {
        var energy = 0.0
        for point in points {
            energy += point.m * point.v.magnitude2
        }
        return energy/2
    }

    // init
    init(graph:Graph, stiffness:Double = 400.0, repulsion:Double = 400.0, damping:Double = 0.5, energyThreshold:Double = 0.001, unitTime:Double = 0.01){
        self.graph = graph
        self.stiffness = stiffness
        self.repulsion = repulsion
        self.damping = damping
        self.minEnergyThreshold = energyThreshold
        self.unitTime = unitTime
    }
    
    func point(node:Node) -> Point {
        if let p = nodePoints[node.id] {
            return p
        } else {
            // create random point
            let mass = node.data["mass"] != nil ? (node.data["mass"]! as NSString).doubleValue : 1.0
            let p = Point(p:Vector.random(), m:mass)
            nodePoints[node.id] = p
            return p
        }
    }

    func spring(edge:Edge) -> Spring {
        if let spring = edgeSprings[edge.id] {
            return spring
        }

        // find spring for any other silbing edges
        var existingSpring:Spring? = nil
        let fromEdges:[Edge] = graph.getEdges(source:edge.source, target:edge.target)
        for e:Edge in fromEdges {
            if edgeSprings[e.id] != nil {
                existingSpring = edgeSprings[e.id]!
                break
            }
        }
        // try in reverse direction
        if existingSpring == nil {
            let toEdges = graph.getEdges(source:edge.target, target:edge.source)
            for e in toEdges {
                if let _existingSpring = edgeSprings[e.id] {
                    existingSpring = _existingSpring
                    break
                }
            }
        }
        // use the silbing edge to create new spring
        if existingSpring != nil {
            return Spring(point1:existingSpring!.point1, point2:existingSpring!.point2, length:0.0, k:0.0)
        }

        // create new spring
        let length = edge.data["length"] != nil ? (edge.data["length"]! as NSString).doubleValue : 1.0
        let newSpring = Spring(
            point1:self.point(edge.source),
            point2:self.point(edge.target),
            length:length,
            k:stiffness
        )
        edgeSprings[edge.id] = newSpring
        return edgeSprings[edge.id]!
    }

    var eachNode: [(Node, Point)] {
        var nodePoints = [(Node, Point)]()
        
        for n in graph.nodes {
            nodePoints.append((n,self.point(n)))
        }
        
        return nodePoints
    }
    
    // TODO: use a generator instead of Array
    var eachSpring: [Spring] {
        var springs = [Spring]()
        
        for e in graph.edges {
            springs.append(self.spring(e))
        }

        return springs
    }

    func applyHookesLaw() {
        var springs:[Spring] = self.eachSpring
        ForceDirectedLayout.applyHookesLaw(&springs)
    }
    func applyCoulombsLaw() {
        var points = self.eachNode.map { $1 }
        ForceDirectedLayout.applyCoulombsLaw2(&points, repulsion:repulsion)
    }
    
    func attractToCenter() {
        for (_,point) in self.eachNode {
            let reverseDir:Vector = point.p * -1.0
            point.applyForce(reverseDir * (repulsion / 50.0));
        }
    }
    
    func updateVelocity(#timestep:Double) {
        var points = self.eachNode.map { $1 }
        ForceDirectedLayout.updateVelocity(&points, damping:damping, timestep:timestep)
    }

    func updatePosition(#timestep:Double) {
        var points = self.eachNode.map { $1 }
        ForceDirectedLayout.updatePosition(&points, timestep:timestep)
    }

    func totalEnergy() -> Double {
        var points = self.eachNode.map { $1 }
        return ForceDirectedLayout.kineticEnergy(&points)
    }
    
    //
    // Simulation
    //
    func tick(#timestep:Double) {
        self.applyCoulombsLaw()
        self.applyHookesLaw()
        self.attractToCenter()
        self.updateVelocity(timestep:timestep)
        self.updatePosition(timestep:timestep)
    }
    
    func stop() {
        _stop = true
    }
    
    func start(render:() -> (), onRenderStop:() -> (), onRenderStart:() -> ()) {
        if _started == true {
            return
        }
        _started = true
        _stop = false
        
        //
        onRenderStart()
        
        //
        while !_stop {
            // compute round unit time
            let roundUnitTime = unitTime
            
            // tick
            self.tick(timestep:roundUnitTime)
            
            //
            render()
            
            // check stop
            if _stop || totalEnergy() < minEnergyThreshold {
                break
            }
        }
        _started = false
        onRenderStop()
    }
    func start() { self.start({}, {}, {}) }
    func start(render:() -> ()) { self.start(render, {}, {}) }
    func start(render:() -> (), onRenderStop:() -> ()) { self.start(render, onRenderStop, {}) }
    
}
