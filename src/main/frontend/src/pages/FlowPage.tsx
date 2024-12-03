import createGraph from "../graph.ts";
import {useEffect, useRef} from "react";
import {useNavigate, useOutletContext} from "react-router-dom";
import {IResource} from "../interfaces.ts";

export function FlowPage() {
    const container = useRef<HTMLDivElement | null>(null);
    const navigate = useNavigate();

    const trail = useOutletContext<IResource>()
    if (trail == null) {
        return <></>
    }

    function onNavigate(resource: string) {
        navigate('/' + encodeURIComponent(resource));
    }

    useEffect(() => {
        createGraph(container.current!, trail, onNavigate);
    }, [trail]);

    return (
        <div className="tree-container" ref={container}>
            <svg className="tree"></svg>
            <svg className="legend"></svg>
            <div className="resource-info"></div>
        </div>
    );
}
