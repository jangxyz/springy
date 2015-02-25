//
//  Renderer.swift
//  springy
//
//  Created by 김장환 on 1/16/15.
//  Copyright (c) 2015 김장환. All rights reserved.
//

import Foundation

class Renderer:GraphListener {
    var layout:ForceDirectedLayout
    let clear: () -> ()
    let drawEdge: (Edge, Point, Point) -> ()
    let drawNode: (Node, Point) -> ()
    let onRenderStop: () -> ()
    let onRenderStart: () -> ()

    init(layout:ForceDirectedLayout,
            clear: () -> () = {},
            drawEdge: (Edge, Point, Point) -> () = { (_,_,_) in return },
            drawNode: (Node, Point) -> () = { (_,_) in return },
            onRenderStart: () -> () = {},
            onRenderStop: () -> () = {}
    ) {
        self.layout = layout
        self.clear = clear
        self.drawEdge = drawEdge
        self.drawNode = drawNode
        self.onRenderStop = onRenderStop
        self.onRenderStart = onRenderStart
    }
    
    func graphChanged() {
//        self.start()
    }

}