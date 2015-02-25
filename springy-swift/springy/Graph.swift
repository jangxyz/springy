//
//  Graph.swift
//  springy
//
//  Created by 김장환 on 1/13/15.
//  Copyright (c) 2015 김장환. All rights reserved.
//

import Foundation

protocol GraphListener {
    func graphChanged()
}

class Graph {
    var nodes:[Node] = [Node]()
    var nodeSet:[String:Node] = [String:Node]()
    var edges:[Edge] = [Edge]()
    var adjacency = [String:[String:[Edge]]]()
    //
    var nextNodeId = 0
    var nextEdgeId = 0
    var eventListeners = [GraphListener]()
    
    
    func addNode(node:Node) -> Node {
        if let existingNode = nodeSet[node.id] {
            
        } else {
            nodes.append(node)
        }
        
        nodeSet[node.id] = node
        
        //
        self.notify()
        
        return node
    }
    
    func addNodes(names:[String]) {
        for name in names {
            let newNode = Node(name, data:["label": name])
            self.addNode(newNode)
        }
    }
    
    func addEdge(edge:Edge) -> Edge {
        // append to edges
        let exists = edges.filter {$0 == edge}.count > 0
        if !exists {
            edges.append(edge)            
        }
        
        // set adjacency matrix
        self.setAdjacency(sourceId:edge.source.id, targetId:edge.target.id, edge:edge)
        
        //
        self.notify()
        
        return edge
    }
    private func setAdjacency(#sourceId:String, targetId:String, edge:Edge) {
        // create new for sourceId if none
        if let sourceAdjacency = adjacency[sourceId] {
        } else {
            adjacency[sourceId] = [String:[Edge]]()
        }
        
        // create new for targetId if none
        if let targetAdjacency = adjacency[sourceId]![targetId] {
        } else {
            adjacency[sourceId]![targetId] = [Edge]()
        }
        
        // append new edge if not exist
        let exists = adjacency[sourceId]![targetId]!.filter { $0 == edge }.count > 0
        if !exists {
            adjacency[sourceId]![targetId]!.append(edge)
        }
    }

    func addEdges(edges:[(String, String, [String:String])]) {
        for (sourceId, targetId, edgeData) in edges {
            if let source = nodeSet[sourceId] {
                if let target = nodeSet[targetId] {
                    self.newEdge(source:source, target:target, data:edgeData)
                }
            }
        }
    }
    func addEdges(edges:[(String, String)]) {
        let edgesWithData = edges.map { ($0, $1, [String:String]()) }
        return self.addEdges(edgesWithData)
    }
    
    func newNode(data:[String:String]) -> Node {
        let node = Node(self.nextNodeId++, data:data)
        self.addNode(node)
        return node;
    }
    
    func newEdge(#source:Node, target:Node, data:[String:String] = [String:String]()) -> Edge {
        let edge = self.addEdge(Edge(self.nextEdgeId++, source:source, target:target, data:data))
        return edge
    }

    func loadJSON(jsonString:String) {
        var parseError: NSError?
        let parsedObject: AnyObject? = NSJSONSerialization.JSONObjectWithData(
            jsonString.dataUsingEncoding(NSUTF8StringEncoding)!,
            options: NSJSONReadingOptions.AllowFragments,
            error: &parseError)
        let jsonData:JSON = JSON(parsedObject!)
        
        _loadJSON(jsonData)
    }
    func loadJSON(json:[String:[AnyObject]]) {
        _loadJSON(JSON(json))
    }
    private func _loadJSON(jsonData:JSON) {
        if jsonData["nodes"] != nil {
            
            let nodeNames = map(jsonData["nodes"].arrayValue) { $0.stringValue }
            self.addNodes(nodeNames)
            
            if jsonData["edges"] != nil {
                
                let edges:[(String,String)] = map(jsonData["edges"].arrayValue) {
                    ($0[0].stringValue, $0[1].stringValue)
                }
                self.addEdges(edges)
            }
        }
    }
    
    func getEdges(#source:Node, target:Node) -> [Edge] {
        if let adjacentEdges = adjacency[source.id]?[target.id] {
            return adjacentEdges
        }

        return []
    }
    
    func removeNode(node:Node) {
        // remove from nodeSet
        nodeSet.removeValueForKey(node.id)

        // remove from nodes
        if let index = find(nodes, node) {
            nodes.removeAtIndex(index)
        }
        
        // remove all in adjacency matrix
        let nodeEdges = edges.filter { $0.source.id == node.id || $0.target.id == node.id }
        for edge in nodeEdges {
            self.removeEdge(edge)
        }
        
        //
        self.notify()
    }
    
    func removeEdge(edge:Edge) {
        // remove from edges
        if let index = find(edges, edge) {
            edges.removeAtIndex(index)
        }
        
        // remove from adjacency matrix
        self.removeAdjacency(sourceId:edge.source.id, targetId:edge.target.id, edge:edge)
        
        //
        self.notify()
    }
    private func removeAdjacency(#sourceId:String, targetId:String, edge:Edge) {
        if let siblingEdges = adjacency[sourceId]?[targetId] {
            if let index = find(siblingEdges, edge) {
                adjacency[sourceId]![targetId]!.removeAtIndex(index)
                
                // clean up empty edge arrays
                if adjacency[sourceId]![targetId]!.isEmpty {
                   adjacency[sourceId]!.removeValueForKey(targetId)
                }
            }
            
            // clean up empty objects
            if adjacency[sourceId]!.isEmpty {
               adjacency.removeValueForKey(sourceId)
            }
        }
    }
    
//    func merge(data:[String:[AnyObject]]) {
////        var nodes = [Node]()
//        if let nodes = data["nodes"] {
//            for nodeInfo in nodes {
//                
//                let nodeData = nodeInfo["data"]? as? [String:String]
//                
//                var newNode:Node?
//                if let nodeId = nodeInfo["id"] as? String {
//                    newNode = Node(nodeId, data:nodeData)
//                } else if let nodeId = nodeInfo["id"] as? Int {
//                    newNode = Node(nodeId, data:nodeData)
//                }
//
//                if newNode != nil {
//                    self.addNode(newNode!)
//                }
//            }
//        }
//    }
    
    func addGraphListener(listener:GraphListener) {
        eventListeners.append(listener)
    }
    
    //
    func notify() {
        for listener in eventListeners {
            listener.graphChanged()
        }
    }
}
