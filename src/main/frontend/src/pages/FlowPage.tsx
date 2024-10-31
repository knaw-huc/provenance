import createGraph from "../graph.ts";
import {useEffect, useRef} from "react";
import {INode} from "../interfaces.ts";

export function FlowPage({trail}: { trail: INode }) {
    const container = useRef<HTMLDivElement | null>(null);

    useEffect(() => {
        createGraph(container.current!, trail);
    }, [trail]);

    return (
        <div className="tree-container" ref={container}>
            <svg className="tree"></svg>
            <svg className="legend"></svg>
            <div className="resource-info"></div>
        </div>
    );
}
