//
//  Edge.swift
//  springy
//
//  Created by 김장환 on 1/13/15.
//  Copyright (c) 2015 김장환. All rights reserved.
//

import Foundation

struct Edge: Equatable {
    let id: String
    let source: Node
    let target: Node
    let data: [String:String]
    
    init(id:String, source:Node, target:Node, data:[String:String] = [String:String]()) {
        self.id = id
        self.source = source
        self.target = target
        self.data = data
    }

    init(id:Int, source:Node, target:Node, data:[String:String] = [String:String]()) {
        self.id = String(id)
        self.source = source
        self.target = target
        self.data = data
    }

    init(_ id:String, source:Node, target:Node, data:[String:String] = [String:String]()) {
        self.id = id
        self.source = source
        self.target = target
        self.data = data
    }

    init(_ id:Int, source:Node, target:Node, data:[String:String] = [String:String]()) {
        self.id = String(id)
        self.source = source
        self.target = target
        self.data = data
    }
    
}

func == (left: Edge, right: Edge) -> Bool {
    return left.id == right.id
}
func != (left: Edge, right: Edge) -> Bool {
    return !(left == right)
}

