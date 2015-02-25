//
//  node.swift
//  springy-swift
//
//  Created by 김장환 on 1/11/15.
//  Copyright (c) 2015 김장환. All rights reserved.
//

import Foundation

struct Node: Equatable {
    let id: String
    let data: [String:String]
    
    init(id:String, data:[String:String] = [String:String]()) {
        self.id = id
        self.data = data
    }
    init(id:String, data:[String:String]?) {
        self.id = id
        self.data = data ?? [String:String]()
    }
    init(id:Int, data:[String:String] = [String:String]()) {
        self.id = String(id)
        self.data = data
    }

    init(_ id:String, data:[String:String] = [String:String]()) {
        self.id = String(id)
        self.data = data
    }
    init(_ id:Int, data:[String:String] = [String:String]()) {
        self.id = String(id)
        self.data = data
    }
    
    func description() -> String {
        return "Node: #\(self.id):\(self.data)"
    }

}

func == (left: Node, right: Node) -> Bool {
    return left.id == right.id
}
func != (left: Node, right: Node) -> Bool {
    return !(left == right)
}
