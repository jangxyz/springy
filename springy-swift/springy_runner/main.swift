#!/usr/bin/env xcrun swift -i

//
//  main.swift
//  springy_runner
//
//  Created by 김장환 on 1/17/15.
//  Copyright (c) 2015 김장환. All rights reserved.
//

import Foundation

func createTestGraph(#nodesNum:Int, #edgesNum:Int) -> Graph {
    var graph = Graph()
    
    // add 100 nodes
    var edgeCandidate:[String] = []
    for var i = 0; i < nodesNum; i++ {
        let node = graph.newNode(["label": String(i)])
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
//        println("node1: \(rand1) \(node1.description())")
        
        // select target
        var rand2:Int
        do {
            rand2 = random() % edgeCandidate.count
        } while (rand2 == rand1)
        
        var node2:Node = graph.nodeSet[edgeCandidate[rand2]]!
//        println("node2: \(rand2) \(node2.description())")
        
        var edge = graph.newEdge(source:node1, target:node2,
            data:["label": "\(nodeName(node1)) --> \(nodeName(node2))"]
        )
        
        // hub
        edgeCandidate.append(node1.id)
        edgeCandidate.append(node2.id)
    }
    
    return graph
}
func importGraph(filename:String) -> Graph {
    let fileContent = NSString(contentsOfFile: filename, encoding: NSUTF8StringEncoding, error: nil)

    // parse JSON
    var parseError: NSError?
    let parsedObject: AnyObject? = NSJSONSerialization.JSONObjectWithData(
        fileContent!.dataUsingEncoding(NSUTF8StringEncoding)!,
        options: NSJSONReadingOptions.AllowFragments,
        error: &parseError)
    let data:JSON = JSON(parsedObject!)
   
    //
    var graph = Graph()

    // add nodes
    if data["nodes"] != nil {
        var nodeNames = [String]()
        for nodeData in data["nodes"].arrayValue {
            var id = ""
            if let _id1 = nodeData["id"].string {
                id = _id1
            } else if let _id2 = nodeData["id"].number {
                id = "\(_id2)"
            } else {
//                let error = nodeData["id"].error
//                println("error: \(error)")
            }
            nodeNames.append(id)
        }
        graph.addNodes(nodeNames)
    }
        
    // add edges
    if data["edges"] != nil {
        let edgeTuples = map(data["edges"].arrayValue) { (e) -> (String,String,[String:String]) in
            var edgeData = [String:String]()
            for (key:String,value:JSON) in e["data"] {
                edgeData[key] = value.stringValue
            }
            
            return (e["source"]["id"].stringValue, e["target"]["id"].stringValue, edgeData)
        }
        graph.addEdges(edgeTuples)
    }

    return graph
}
func JSONStringify(value: AnyObject, prettyPrinted: Bool = false) -> String {
    var options = prettyPrinted ? NSJSONWritingOptions.PrettyPrinted : nil
    if NSJSONSerialization.isValidJSONObject(value) {
        if let data = NSJSONSerialization.dataWithJSONObject(value, options: options, error: nil) {
            if let string = NSString(data: data, encoding: NSUTF8StringEncoding) {
                return string
            }
        }
    }
    return ""
}
func exportGraph(graph:Graph,
    write:(JSON) -> () = {println($0)}) {
    func buildNode(node:Node) -> [String:AnyObject] {
        return [
            "id": node.id,
            "data": node.data
        ]
    }

    var data:[String:[AnyObject]] = [
        "nodes": graph.nodes.map { buildNode($0) },
        "edges": graph.edges.map { (edge) in
            [
                "id": edge.id,
                "source": buildNode(edge.source),
                "target": buildNode(edge.target),
                "data": edge.data
            ]
        }
    ]

    let json:JSON = JSON(data)

    write(json)
}
func importLayout(filename:String, graph:Graph) -> ForceDirectedLayout {
    let fileContent = NSString(contentsOfFile: filename, encoding: NSUTF8StringEncoding, error: nil)

    // parse JSON
    var parseError: NSError?
    let parsedObject: AnyObject? = NSJSONSerialization.JSONObjectWithData(
        fileContent!.dataUsingEncoding(NSUTF8StringEncoding)!, options: NSJSONReadingOptions.AllowFragments, error: &parseError)
    let data:JSON = JSON(parsedObject!)

    let stiffness          = data["stiffness"].doubleValue
    let repulsion          = data["repulsion"].doubleValue
    let damping            = data["damping"].doubleValue
    let unitTime           = data["unitTime"].doubleValue
    let minEnergyThreshold = 0.01

    var layout = ForceDirectedLayout(graph:graph,
        stiffness:stiffness, repulsion:repulsion, damping:damping, energyThreshold:minEnergyThreshold, unitTime:unitTime)

    func pointKey(point:Point) -> String {
        return "[\(point.p.x), \(point.p.y), \(point.v.x), \(point.v.y)]"
    }

    // set node points
    var nodePoints = [String:Point]()
    var nodePointsIndexByPM = [String:Point]()
    func buildPoint(_pointData:JSON) -> Point {
        let pointData = _pointData.dictionaryValue

        let p = pointData["p"]!.dictionaryValue
        let position = Vector(x:p["x"]!.doubleValue, y:p["y"]!.doubleValue)

        let v = pointData["v"]!.dictionaryValue
        let velocity = Vector(x:v["x"]!.doubleValue, y:v["y"]!.doubleValue)

        let mass = pointData["m"]!.doubleValue

        let point = Point(p:position, m:mass)

        point.v = velocity

        return point
    }
    for (pointId:String, _pointData:JSON) in data["nodePoints"] {
        let point = buildPoint(_pointData)

        nodePoints[pointId] = point
        nodePointsIndexByPM[pointKey(point)] = point
    }
    layout.nodePoints = nodePoints

    // set edge springs
    var edgeSprings = [String:Spring]()
    for (edgeId:String, _springData:JSON) in data["edgeSprings"] {
        let length = _springData["length"].doubleValue
        let k      = _springData["k"].doubleValue

        let point1Key = pointKey(buildPoint(_springData["point1"]))
        let point2Key = pointKey(buildPoint(_springData["point2"]))
        let point1 = nodePointsIndexByPM[point1Key]!
        let point2 = nodePointsIndexByPM[point2Key]!

        edgeSprings[edgeId] = Spring(point1:point1, point2:point2, length:length, k:k)
    }
    layout.edgeSprings = edgeSprings

    return layout
}
func exportLayout(layout:ForceDirectedLayout, write:(JSON -> ()) = {println($0)}) {
    var data:[String:AnyObject] = [
        "stiffness": layout.stiffness,
        "repulsion": layout.repulsion,
        "damping": layout.damping,
        "unitTime": layout.unitTime,
        "energy": layout.totalEnergy(),
        
        "nodePoints": [String:AnyObject](),
        "edgeSprings": [String:AnyObject]()
    ]
    
    func buildPointData(point:Point) -> [String:AnyObject] {
        return [
            "p": [ "x": point.p.x, "y": point.p.y ],
            "v": [ "x": point.v.x, "y": point.v.y ],
            "m": point.m,
            "a": [ "x": point.a.x, "y": point.a.y ]
        ]
    }
    func buildSpringData(spring:Spring) -> [String:AnyObject] {
        return [
            "point1": buildPointData(spring.point1),
            "point2": buildPointData(spring.point2),
            "length": spring.length,
            "k": spring.k
        ]
    }
    
    
    func a2d<K,V>(tuples:[(K,V)]) -> [K:V] {
        var dict = [K:V]()
        for (key,value) in tuples {
            dict[key] = value
        }
        return dict
    }
    func toDict<K,V,E>(array:[E], transform:E->(K,V)) -> [K:V] {
        var dict = [K:V]()
        for entity in array {
            let (key,value) = transform(entity)
            dict[key] = value
        }
        return dict
    }
    
    data["nodePoints"] = toDict(layout.eachNode) { ($0.id, buildPointData($1)) }
    
    var edgeSprings = [String:AnyObject]()
    for (i,spring) in enumerate(layout.eachSpring) {
        edgeSprings[String(i)] = buildSpringData(spring)
    }
    data["edgeSprings"] = edgeSprings
    
    write(JSON(data))
}


// create graph and layout
var graph:Graph
var layout:ForceDirectedLayout
// args
let args = NSProcessInfo.processInfo().arguments as NSArray
let args1 = /*"/Users/jangxyz/play/springy-swift/springy/data/20150101-123899_2_1/graph.json"*/args.count >= 2 ? args[1] as String : ""
let args2 = /*"/Users/jangxyz/play/springy-swift/springy/data/20150101-123899_2_1/layout_300.json"*/args.count >= 3 ? args[2] as String : ""

let fileManager = NSFileManager.defaultManager()
let files2 = fileManager.contentsOfDirectoryAtPath("./data/20150123-085804_10_20_400_400_0.3_0.01", error:nil)
if fileManager.fileExistsAtPath(args1) && fileManager.fileExistsAtPath(args2) {
    let graphFilename  = args1
    let layoutFilename = args2
    print("reading graph from \(graphFilename) ...")
    graph  = importGraph(graphFilename)
    println(" done: \(graph.nodes.count), \(graph.edges.count)")

    print("reading layout from \(layoutFilename) ...")
    layout = importLayout(layoutFilename, graph)
    println(" done.")
} else {
    let nodesNum = args.count >= 2 ? args1.toInt()! : 100//10
    let edgesNum = args.count >= 3 ? args2.toInt()! : 50//nodesNum * 2
    //graph = createTestGraph(nodesNum:1000, edgesNum:2000)
    graph = createTestGraph(nodesNum:nodesNum, edgesNum:edgesNum)
    layout = ForceDirectedLayout(graph:graph,
        stiffness: 400.0,
        repulsion: 400,
        damping: 0.5,
        energyThreshold: 0.01,
        unitTime: 0.01
    )
}

let nodesNum = graph.nodes.count
let edgesNum = graph.edges.count

// E1 = 154285510.127098
//


// layout summary
println("---")
println("points: \(layout.eachNode.count)")
println("springs: \(layout.eachSpring.count)")

//println("Points")
//for (i,(node,point)) in enumerate(layout.eachNode) {
//    println("- \(i+1): \(point)")
//}
//println("Springs")
//for (i,spring) in enumerate(layout.eachSpring) {
//    println("- \(i+1): \(spring)")
//}
func writeToFile(filename:String) -> (JSON -> ()) {
    return {(json:JSON) -> () in
        json.rawString()!.writeToFile(filename, atomically: false, encoding: NSUTF8StringEncoding, error: nil)
        return
    }
}

let startDate = NSDate()

let dateFormatter = NSDateFormatter()
dateFormatter.dateFormat = "YYYYMMDD-HHmmSS"
let startDateStr = dateFormatter.stringFromDate(NSDate())

let testResultPath = "data/\(startDateStr)_\(nodesNum)_\(edgesNum)_\(layout.stiffness)_\(layout.repulsion)_\(layout.damping)_\(layout.unitTime)_swift"
var isDir:ObjCBool = true
if !fileManager.fileExistsAtPath(testResultPath, isDirectory:&isDir) {
    fileManager.createDirectoryAtPath(testResultPath, withIntermediateDirectories:true, attributes: nil, error: nil)
}


// start
// onRender
var count = 0
var prevRunTime = startDate
func onRenderStart() {
    //
    exportGraph(graph, writeToFile("\(testResultPath)/graph.json"))
    exportLayout(layout, writeToFile("\(testResultPath)/layout_\(count).json"))
}
func render() {
    count += 1
    
    func checkPrint(i:Int) -> Bool {
        if (i <= 300) { return true } // DEBUG
        //return false
        
        if (i == 1)   { return true }
        if (i <= 100) { return true }
        if (i > 30  && i <=  100 && i % 10 == 0) { return true }
        if (i > 100 && i <= 1000 && i % 50 == 0) { return true }
        if (i % 100 == 0)              { return true }
        
        return false
    }
    func checkSaveAt(i:Int) -> Bool {
        if (i <= 300 && i % 10 == 0) { return true } // DEBUG
        
        if (i < 10) { return true }
        if (i == 10) { return true }
        if (i == 100) { return true }
        if (i % 1000 == 0) { return true }
        if (i % 1000 == 300) { return true }
        if (i % 1000 == 700) { return true }
        
        return false
    }

    //
    if checkPrint(count) {
        let thisTime = NSDate()
        println("- #\(count) energy: \(layout.totalEnergy()) (\(thisTime.timeIntervalSinceDate(prevRunTime)) s)")
        prevRunTime = thisTime
    }
    if (checkSaveAt(count)) {
        exportLayout(layout, writeToFile("\(testResultPath)/layout_\(count).json"))
    }

}
func onRenderStop() {
    // write layout
    //    text.writeToFile(path, atomically: false, encoding: NSUTF8StringEncoding, error: nil)
    exportLayout(layout, writeToFile("\(testResultPath)/layout_\(count).json"))
    // time
    let end = NSDate()
    println("done. took \(end.timeIntervalSinceDate(startDate)) seconds.")
}
//layout.start(render, onRenderStop:onRenderStop, onRenderStart:onRenderStart)
layout.start({}, onRenderStop:onRenderStop, onRenderStart:onRenderStart)

